import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class LoginFrame extends JFrame {

    private final Connection connection;
    private final JTextField usernameField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);

    public LoginFrame(Connection connection) {
        super("Banking Management System — Login");
        this.connection = connection;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(360, 220);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        JButton loginButton = new JButton("Log In");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(loginButton, gbc);

        loginButton.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin()); // Enter key submits

        add(panel);
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter both username and password.", "Missing info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AuthService authService = new AuthService(connection);
        AppUser user = authService.login(username, password);

        if (user == null) {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login failed", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            return;
        }

        new DashboardFrame(connection, user).setVisible(true);
        dispose();
    }
}
