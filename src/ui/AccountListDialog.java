import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.util.List;

public class AccountListDialog extends JDialog {

    public AccountListDialog(JFrame parent, Connection connection) {
        super(parent, "All Accounts", true);
        setSize(480, 320);
        setLocationRelativeTo(parent);

        String[] columns = {"Account ID", "Holder Name", "Balance", "Created At"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        try {
            List<Account> accounts = new AccountService(connection).getAllAccounts();
            for (Account a : accounts) {
                model.addRow(new Object[]{a.getAccountId(), a.getHolderName(), a.getBalance(), a.getCreatedAt()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading accounts: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        JTable table = new JTable(model);
        add(new JScrollPane(table));
    }
}
