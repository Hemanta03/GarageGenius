package com.garagegenius.util;
import org.mindrot.jbcrypt.BCrypt;
/**
 * Password hashing and verification helpers.
 *
 * <p>Uses BCrypt to store password hashes (never stores plaintext passwords).</p>
 */
public class PasswordUtil {
    /**
     * Hashes a plaintext password using BCrypt with a per-password salt.
     *
     * @param plainPassword plaintext password
     * @return BCrypt hash string suitable for storage
     */
    public static String hashPassword(String plainPassword) {

        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }
    /**
     * Verifies a plaintext password against a stored BCrypt hash.
     *
     * @param plainPassword plaintext password
     * @param hashedPassword stored BCrypt hash
     * @return {@code true} if the password matches
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}