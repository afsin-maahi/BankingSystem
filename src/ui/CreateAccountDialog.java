import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Connection;

public class CreateAccountDialog extends JDialog {

    public CreateAccountDialog(JFrame parent, Connection connection) {
        super(parent, "Create New Customer Account", true);
        setSize(390, 300);
        setLocationRelativeTo(parent);
        setResizable(false);

        JTextField nameField = new JTextField(16);
        JTextField balanceField = new JTextField(16);
        JTextField usernameField = new JTextField(16);
        JPasswordField passwordField = new JPasswordField(16);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Holder Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Initial Balance:"), gbc);
        gbc.gridx = 1;
        panel.add(balanceField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        JButton createButton = new JButton("Create Account & Login");
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(createButton, gbc);

        createButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Enter holder name, username, and password.",
                    "Missing info", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (password.length() < 4) {
                JOptionPane.showMessageDialog(this,
                    "Password must contain at least 4 characters.",
                    "Invalid password", JOptionPane.WARNING_MESSAGE);
                return;
            }

            BigDecimal balance;
            try {
                balance = new BigDecimal(balanceField.getText().trim());
                if (balance.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                    "Enter a valid non-negative balance.",
                    "Invalid input", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                AccountService service = new AccountService(connection);
                int accountId = service.createCustomerAccount(name, balance, username, password);

                JOptionPane.showMessageDialog(this,
                    "Customer account created successfully!\n\n" +
                    "Account ID: " + accountId + "\n" +
                    "Username: " + username + "\n" +
                    "Role: CUSTOMER\n\n" +
                    "The customer can now log in using these credentials.",
                    "Account Created", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Cannot create account", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(panel);
    }
}
