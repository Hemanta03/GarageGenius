package com.garagegenius.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Small helper for reading/writing authentication state in the HTTP session.
 *
 * <p>The application uses session-based authentication. After a successful login,
 * controllers call {@link #setUserSession(HttpServletRequest, int, String, String, String)}
 * to store the authenticated user identity and role. Filters and controllers then use
 * the getters in this class to enforce access control.</p>
 */
public class SessionUtil {
    /**
     * @param request current request
     * @return logged-in user id, or {@code null} if no session/auth exists
     */
    public static Integer getLoggedInUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        return (Integer) session.getAttribute("userId");
    }
    /**
     * @param request current request
     * @return role string (e.g. {@code admin}, {@code staff}, {@code customer}), or {@code null}
     */
    public static String getLoggedInRole(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        return (String) session.getAttribute("role");
    }
    /**
     * @param request current request
     * @return user's full name, or {@code null} if not authenticated
     */
    public static String getLoggedInName(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        return (String) session.getAttribute("fullName");
    }
    /**
     * @param request current request
     * @return {@code true} if a logged-in user id exists in session
     */
    public static boolean isLoggedIn(HttpServletRequest request) {
        return getLoggedInUserId(request) != null;
    }
    /**
     * @param request current request
     * @param role role to check
     * @return {@code true} if current session role matches exactly
     */
    public static boolean hasRole(HttpServletRequest request, String role) {
        String userRole = getLoggedInRole(request);
        return role.equals(userRole);
    }
    /**
     * Stores the authenticated user identity into a fresh session.
     *
     * <p>Invalidates any existing session before creating a new one to reduce
     * the risk of session fixation.</p>
     *
     * @param request current request
     * @param userId authenticated user id
     * @param fullName user's display name
     * @param email user's email
     * @param role application role (stored lower-cased)
     */
    public static void setUserSession(HttpServletRequest request,
                                      int userId,
                                      String fullName,
                                      String email,
                                      String role) {
        // BUG-09 FIX: Invalidate old session before creating a new one (prevents session fixation)
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute("userId", userId);
        session.setAttribute("fullName", fullName);
        session.setAttribute("email", email);
        session.setAttribute("role", role == null ? null : role.toLowerCase());
        session.setMaxInactiveInterval(30 * 60);
    }
    /**
     * Invalidates the current session if it exists.
     *
     * @param request current request
     */
    public static void destroySession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}