import javax.swing.SwingUtilities;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) throws Exception {
        Connection connection = DBConnection.getConnection();
        SwingUtilities.invokeLater(() -> new LoginFrame(connection).setVisible(true));
    }
}
