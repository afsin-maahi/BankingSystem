import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.util.List;

public class TransactionHistoryDialog extends JDialog {

    public TransactionHistoryDialog(JFrame parent, Connection connection, AppUser user) {
        super(parent, "Transaction History", true);
        setSize(480, 320);
        setLocationRelativeTo(parent);

        boolean isPrivileged = "ADMIN".equals(user.getRole()) || "TELLER".equals(user.getRole());
        Integer accountId = user.getAccountId();

        if (!isPrivileged && accountId == null) {
            add(new JLabel("No account linked to this user.", SwingConstants.CENTER));
            return;
        }

        JPanel topPanel = new JPanel();
        JTextField accountIdField = new JTextField(
            accountId != null ? String.valueOf(accountId) : "", 10);
        accountIdField.setEditable(isPrivileged);
        JButton loadButton = new JButton("Load");
        topPanel.add(new JLabel("Account ID:"));
        topPanel.add(accountIdField);
        topPanel.add(loadButton);

        String[] columns = {"Date", "Type", "Amount", "Related Account"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);

        loadButton.addActionListener(e -> {
            model.setRowCount(0);
            int id;
            try {
                id = Integer.parseInt(accountIdField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid account ID.", "Invalid input", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!isPrivileged && (accountId == null || id != accountId)) {
                JOptionPane.showMessageDialog(this, "You can only view your own transaction history.", "Access denied", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                List<Transaction> history = new AccountService(connection).getTransactionHistory(id);
                for (Transaction t : history) {
                    model.addRow(new Object[]{t.getCreatedAt(), t.getType(), t.getAmount(),
                        t.getRelatedAccountId() != null ? t.getRelatedAccountId() : "-"});
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        setLayout(new java.awt.BorderLayout());
        add(topPanel, java.awt.BorderLayout.NORTH);
        add(new JScrollPane(table), java.awt.BorderLayout.CENTER);

        if (accountId != null) loadButton.doClick(); // auto-load for customers
    }
}
