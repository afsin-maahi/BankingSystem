import java.sql.*;

/** Handles user registration and login against the users/roles tables. */
public class AuthService {

    private final Connection connection;

    public AuthService(Connection connection) {
        this.connection = connection;
    }

    public boolean registerUser(String username, String plainPassword, String roleName, Integer accountId) {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash(plainPassword, salt);

        String sql = "INSERT INTO users (username, password_hash, salt, role_id, account_id) " +
                     "SELECT ?, ?, ?, role_id, ? FROM roles WHERE role_name = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hash);
            ps.setString(3, salt);
            if (accountId != null) ps.setInt(4, accountId); else ps.setNull(4, Types.INTEGER);
            ps.setString(5, roleName);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("Registration failed: " + e.getMessage());
            return false;
        }
    }

    public AppUser login(String username, String plainPassword) {
        String sql = "SELECT u.user_id, u.password_hash, u.salt, u.account_id, r.role_name " +
                     "FROM users u JOIN roles r ON u.role_id = r.role_id " +
                     "WHERE u.username = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                String storedHash = rs.getString("password_hash");
                String salt = rs.getString("salt");
                if (!PasswordUtil.verify(plainPassword, salt, storedHash)) return null;

                int userId = rs.getInt("user_id");
                String role = rs.getString("role_name");
                int accountIdRaw = rs.getInt("account_id");
                Integer accountId = rs.wasNull() ? null : accountIdRaw;

                return new AppUser(userId, username, role, accountId);
            }
        } catch (SQLException e) {
            System.err.println("Login failed: " + e.getMessage());
            return null;
        }
    }
}
