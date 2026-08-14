import java.math.BigDecimal;
import java.sql.Connection;

/**
 * Run this ONCE after loading schema.sql to create test logins for all
 * three roles. Uses the app's own PasswordUtil so hashes are consistent
 * with what login() expects (no manually-typed hashes to get wrong).
 *
 * Usage: javac *.java && java SeedUsers
 */
public class SeedUsers {

    public static void main(String[] args) throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            AuthService authService = new AuthService(conn);
            AccountService accountService = new AccountService(conn);

            // Admin — no linked account
            authService.registerUser("admin", "password123", "ADMIN", null);
            System.out.println("Created ADMIN user: admin / password123");

            // Teller — no linked account
            authService.registerUser("teller1", "password123", "TELLER", null);
            System.out.println("Created TELLER user: teller1 / password123");

            // Customer — needs an account first
            int accountId = accountService.createAccount("Ravi Kumar", new BigDecimal("5000.00"));
            authService.registerUser("customer1", "password123", "CUSTOMER", accountId);
            System.out.println("Created CUSTOMER user: customer1 / password123 (account #" + accountId + ")");

            System.out.println("\nSeeding complete. You can now run Main.java and log in with any of the above.");
        }
    }
}
