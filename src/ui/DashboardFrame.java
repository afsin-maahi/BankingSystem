import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

/**
 * Shows only the actions the logged-in user's role has permission for.
 * Every button click still goes through AccessControl again inside the
 * relevant dialog/service call — hiding a button is a UX nicety, not
 * the actual security boundary.
 */
public class DashboardFrame extends JFrame {

    private final Connection connection;
    private final AppUser user;
    private final AccessControl accessControl;

    public DashboardFrame(Connection connection, AppUser user) {
        super("Banking Management System — " + user);
        this.connection = connection;
        this.user = user;
        this.accessControl = new AccessControl(connection);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 420);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel welcome = new JLabel("Logged in as: " + user);
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(welcome);
        panel.add(Box.createVerticalStrut(12));

        addIfPermitted(panel, "VIEW_ALL_ACCOUNTS", "View All Accounts",
            e -> new AccountListDialog(this, connection).setVisible(true));

        addIfPermitted(panel, "VIEW_OWN_ACCOUNT", "View My Account",
            e -> viewOwnAccount());

        addIfPermitted(panel, "CREATE_ACCOUNT", "Create New Account",
            e -> new CreateAccountDialog(this, connection).setVisible(true));

        addIfPermitted(panel, "DEPOSIT", "Deposit Funds",
            e -> new DepositWithdrawDialog(this, connection, user, true).setVisible(true));

        addIfPermitted(panel, "WITHDRAW", "Withdraw Funds",
            e -> new DepositWithdrawDialog(this, connection, user, false).setVisible(true));

        addIfPermitted(panel, "TRANSFER_FUNDS", "Transfer Funds",
            e -> new TransferFundsDialog(this, connection, user).setVisible(true));

        addIfPermitted(panel, "VIEW_TRANSACTION_HISTORY", "Transaction History",
            e -> new TransactionHistoryDialog(this, connection, user).setVisible(true));

        panel.add(Box.createVerticalStrut(16));
        JButton logoutButton = new JButton("Log Out");
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutButton.addActionListener(e -> {
            new LoginFrame(connection).setVisible(true);
            dispose();
        });
        panel.add(logoutButton);

        add(panel);
    }

    private void addIfPermitted(JPanel panel, String permission, String label, java.awt.event.ActionListener action) {
        if (accessControl.hasPermission(user, permission)) {
            JButton button = new JButton(label);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setMaximumSize(new Dimension(250, 30));
            button.addActionListener(action);
            panel.add(Box.createVerticalStrut(6));
            panel.add(button);
        }
    }

    private void viewOwnAccount() {
        if (user.getAccountId() == null) {
            JOptionPane.showMessageDialog(this, "No account linked to this user.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            Account acct = new AccountService(connection).getAccount(user.getAccountId());
            if (acct == null) {
                JOptionPane.showMessageDialog(this, "Account not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this,
                "Account #" + acct.getAccountId() + "\nHolder: " + acct.getHolderName() +
                "\nBalance: Rs. " + acct.getBalance(),
                "My Account", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
