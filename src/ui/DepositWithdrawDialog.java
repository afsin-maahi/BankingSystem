import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;

public class DepositWithdrawDialog extends JDialog {

    public DepositWithdrawDialog(JFrame parent, Connection connection, AppUser user, boolean isDeposit) {
        super(parent, isDeposit ? "Deposit Funds" : "Withdraw Funds", true);
        setSize(320, 200);
        setLocationRelativeTo(parent);

        boolean isPrivileged = "ADMIN".equals(user.getRole()) || "TELLER".equals(user.getRole());

        JTextField accountIdField = new JTextField(String.valueOf(user.getAccountId() != null ? user.getAccountId() : ""), 12);
        accountIdField.setEditable(isPrivileged); // customers can only act on their own account
        JTextField amountField = new JTextField(12);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Account ID:"), gbc);
        gbc.gridx = 1;
        panel.add(accountIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1;
        panel.add(amountField, gbc);

        JButton actionButton = new JButton(isDeposit ? "Deposit" : "Withdraw");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(actionButton, gbc);

        actionButton.addActionListener(e -> {
            int accountId;
            BigDecimal amount;
            try {
                accountId = Integer.parseInt(accountIdField.getText().trim());
                amount = new BigDecimal(amountField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid account ID and amount.", "Invalid input", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Non-privileged users may only touch their own account, even if they
            // somehow edit the field — this is the real enforcement point.
            if (!isPrivileged && (user.getAccountId() == null || accountId != user.getAccountId())) {
                JOptionPane.showMessageDialog(this, "You can only act on your own account.", "Access denied", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                AccountService service = new AccountService(connection);
                if (isDeposit) service.deposit(accountId, amount);
                else service.withdraw(accountId, amount);
                JOptionPane.showMessageDialog(this, (isDeposit ? "Deposit" : "Withdrawal") + " successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(panel);
    }
}
