/** Represents the currently logged-in user. */
public class AppUser {
    private final int userId;
    private final String username;
    private final String role;       // ADMIN, TELLER, CUSTOMER
    private final Integer accountId; // null for ADMIN/TELLER

    public AppUser(int userId, String username, String role, Integer accountId) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.accountId = accountId;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public Integer getAccountId() { return accountId; }

    @Override
    public String toString() {
        return username + " [" + role + "]";
    }
}
