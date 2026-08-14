import java.sql.*;

/**
 * Data-driven RBAC: checks role_permissions rather than hardcoding
 * if/else on role name. Adding/removing a permission is a data change,
 * not a redeploy — that's the actual point of RBAC worth explaining
 * in an interview.
 */
public class AccessControl {

    private final Connection connection;

    public AccessControl(Connection connection) {
        this.connection = connection;
    }

    public boolean hasPermission(AppUser user, String permissionName) {
        String sql = "SELECT COUNT(*) FROM role_permissions rp " +
                     "JOIN roles r ON rp.role_id = r.role_id " +
                     "JOIN permissions p ON rp.permission_id = p.permission_id " +
                     "WHERE r.role_name = ? AND p.permission_name = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getRole());
            ps.setString(2, permissionName);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Permission check failed: " + e.getMessage());
            return false; // fail closed
        }
    }

    public void requirePermission(AppUser user, String permissionName) {
        if (!hasPermission(user, permissionName)) {
            throw new SecurityException(
                user.getUsername() + " (" + user.getRole() + ") lacks permission: " + permissionName
            );
        }
    }
}
