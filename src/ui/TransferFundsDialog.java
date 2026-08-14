import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;

public class TransferFundsDialog extends JDialog {

    public TransferFundsDialog(JFrame parent, Connection connection, AppUser user) {
        super(parent, "Transfer Funds", true);
        setSize(320, 240);
        setLocationRelativeTo(parent);

        boolean isPrivileged = "ADMIN".equals(user.getRole()) || "TELLER".equals(user.getRole());

        JTextField fromField = new JTextField(String.valueOf(user.getAccountId() != null ? user.getAccountId() : ""), 12);
        fromField.setEditable(isPrivileged);
        JTextField toField = new JTextField(12);
        JTextField amountField = new JTextField(12);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("From Account ID:"), gbc);
        gbc.gridx = 1;
        panel.add(fromField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("To Account ID:"), gbc);
        gbc.gridx = 1;
        panel.add(toField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1;
        panel.add(amountField, gbc);

        JButton transferButton = new JButton("Transfer");
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(transferButton, gbc);

        transferButton.addActionListener(e -> {
            int fromId, toId;
            BigDecimal amount;
            try {
                fromId = Integer.parseInt(fromField.getText().trim());
                toId = Integer.parseInt(toField.getText().trim());
                amount = new BigDecimal(amountField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter valid account IDs and amount.", "Invalid input", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!isPrivileged && (user.getAccountId() == null || fromId != user.getAccountId())) {
                JOptionPane.showMessageDialog(this, "You can only transfer from your own account.", "Access denied", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                new AccountService(connection).transfer(fromId, toId, amount);
                JOptionPane.showMessageDialog(this, "Transfer successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(panel);
    }
}
