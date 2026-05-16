package com.garagegenius.util;

import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;
/**
 * CSRF token generation/management utilities.
 *
 * <p>The token is stored in the user's {@link HttpSession} under attribute name
 * {@code csrfToken}. Tokens are URL-safe Base64 strings.</p>
 */
public class CsrfUtil {
    private static final SecureRandom RNG = new SecureRandom();
    /**
     * Ensures a CSRF token exists in the session.
     *
     * @param session current session (must not be {@code null})
     * @return the existing token if present; otherwise a newly generated token
     */
    public static String ensureToken(HttpSession session) {
        Object existing = session.getAttribute("csrfToken");
        if (existing instanceof String && !((String) existing).isEmpty()) {
            return (String) existing;
        }
        byte[] bytes = new byte[32];
        RNG.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute("csrfToken", token);
        return token;
    }
}

