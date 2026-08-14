import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Salted SHA-256 password hashing. Enough to demonstrate "why plaintext
 * passwords are unsafe" in an interview. Production systems would use
 * BCrypt/Argon2 instead for built-in work-factor tuning — worth
 * mentioning that tradeoff if asked.
 */
public class PasswordUtil {

    private static final SecureRandom RNG = new SecureRandom();

    public static String generateSalt() {
        byte[] saltBytes = new byte[16];
        RNG.nextBytes(saltBytes);
        return toHex(saltBytes);
    }

    public static String hash(String plainPassword, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes());
            byte[] hashedBytes = digest.digest(plainPassword.getBytes());
            return toHex(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public static boolean verify(String plainPassword, String salt, String storedHash) {
        return hash(plainPassword, salt).equals(storedHash);
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
