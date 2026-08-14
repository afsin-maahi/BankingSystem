import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * All core banking operations. Deposit/withdraw/transfer each write a
 * matching row to `transactions` so there's always an audit trail —
 * worth mentioning as a "security auditing" talking point.
 */
public class AccountService {

    private final Connection connection;

    public AccountService(Connection connection) {
        this.connection = connection;
    }

    // ---------- Account management ----------

    public int createAccount(String holderName, BigDecimal initialBalance) throws SQLException {
        String sql = "INSERT INTO accounts (holder_name, balance) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, holderName);
            ps.setBigDecimal(2, initialBalance);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    /**
     * Creates a bank account and its CUSTOMER login together as one transaction.
     * If either insert fails, neither record is kept.
     */
    public int createCustomerAccount(String holderName, BigDecimal initialBalance,
                                     String username, String plainPassword) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        connection.setAutoCommit(false);
        try {
            int accountId;

            String accountSql = "INSERT INTO accounts (holder_name, balance) VALUES (?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(accountSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, holderName);
                ps.setBigDecimal(2, initialBalance);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Could not create account.");
                    accountId = keys.getInt(1);
                }
            }

            String salt = PasswordUtil.generateSalt();
            String hash = PasswordUtil.hash(plainPassword, salt);

            String userSql = "INSERT INTO users (username, password_hash, salt, role_id, account_id) " +
                             "SELECT ?, ?, ?, role_id, ? FROM roles WHERE role_name = 'CUSTOMER'";
            try (PreparedStatement ps = connection.prepareStatement(userSql)) {
                ps.setString(1, username.trim());
                ps.setString(2, hash);
                ps.setString(3, salt);
                ps.setInt(4, accountId);
                if (ps.executeUpdate() != 1) {
                    throw new SQLException("CUSTOMER role was not found.");
                }
            } catch (SQLIntegrityConstraintViolationException ex) {
                throw new IllegalStateException("Username '" + username + "' already exists. Choose another username.");
            }

            connection.commit();
            return accountId;
        } catch (RuntimeException | SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                ex.addSuppressed(rollbackEx);
            }
            throw ex;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public Account getAccount(int accountId) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapAccount(rs);
            }
        }
    }

    public List<Account> getAllAccounts() throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts ORDER BY account_id";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) accounts.add(mapAccount(rs));
        }
        return accounts;
    }

    private Account mapAccount(ResultSet rs) throws SQLException {
        return new Account(
            rs.getInt("account_id"),
            rs.getString("holder_name"),
            rs.getBigDecimal("balance"),
            rs.getTimestamp("created_at")
        );
    }

    // ---------- Transactions ----------

    public void deposit(int accountId, BigDecimal amount) throws SQLException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive");

        connection.setAutoCommit(false);
        try {
            updateBalance(accountId, amount);
            logTransaction(accountId, "DEPOSIT", amount, null);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void withdraw(int accountId, BigDecimal amount) throws SQLException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive");

        connection.setAutoCommit(false);
        try {
            Account acct = getAccount(accountId);
            if (acct == null) {
                throw new IllegalStateException("Account not found: " + accountId);
            }
            if (acct.getBalance().compareTo(amount) < 0) {
                throw new IllegalStateException("Insufficient balance");
            }
            updateBalance(accountId, amount.negate());
            logTransaction(accountId, "WITHDRAWAL", amount, null);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /** Atomic transfer: both legs succeed together or neither does. */
    public void transfer(int fromAccountId, int toAccountId, BigDecimal amount) throws SQLException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (fromAccountId == toAccountId) throw new IllegalArgumentException("Cannot transfer to the same account");

        connection.setAutoCommit(false);
        try {
            Account from = getAccount(fromAccountId);
            if (from == null) throw new IllegalStateException("Source account not found");
            if (from.getBalance().compareTo(amount) < 0) throw new IllegalStateException("Insufficient balance");

            Account to = getAccount(toAccountId);
            if (to == null) throw new IllegalStateException("Destination account not found");

            updateBalance(fromAccountId, amount.negate());
            updateBalance(toAccountId, amount);

            logTransaction(fromAccountId, "TRANSFER_OUT", amount, toAccountId);
            logTransaction(toAccountId, "TRANSFER_IN", amount, fromAccountId);

            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public List<Transaction> getTransactionHistory(int accountId) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int relRaw = rs.getInt("related_account_id");
                    Integer related = rs.wasNull() ? null : relRaw;
                    list.add(new Transaction(
                        rs.getInt("transaction_id"),
                        rs.getInt("account_id"),
                        rs.getString("type"),
                        rs.getBigDecimal("amount"),
                        related,
                        rs.getTimestamp("created_at")
                    ));
                }
            }
        }
        return list;
    }

    // ---------- internal helpers ----------

    private void updateBalance(int accountId, BigDecimal delta) throws SQLException {
        String sql = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBigDecimal(1, delta);
            ps.setInt(2, accountId);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("Account not found: " + accountId);
        }
    }

    private void logTransaction(int accountId, String type, BigDecimal amount, Integer relatedAccountId) throws SQLException {
        String sql = "INSERT INTO transactions (account_id, type, amount, related_account_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setString(2, type);
            ps.setBigDecimal(3, amount);
            if (relatedAccountId != null) ps.setInt(4, relatedAccountId); else ps.setNull(4, Types.INTEGER);
            ps.executeUpdate();
        }
    }
}
