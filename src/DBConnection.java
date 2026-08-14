import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central place to configure your MySQL connection.
 * Edit URL / USER / PASSWORD to match your local setup.
 */
public class DBConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/banking_db";
    private static final String USER     = "root";
    private static final String PASSWORD = "yourRealPassword";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
