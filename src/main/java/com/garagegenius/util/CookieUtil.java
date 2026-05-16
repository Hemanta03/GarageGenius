package com.garagegenius.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
/**
 * Convenience methods for reading/writing HTTP cookies.
 *
 * <p>Cookies are scoped to the application's context path and set as HttpOnly. Secure cookies
 * are enabled automatically when the current request is HTTPS.</p>
 */
public class CookieUtil {

    private CookieUtil() {
    }
    /**
     * Returns the value of the first cookie matching {@code name}.
     *
     * @param request current request
     * @param name cookie name
     * @return cookie value, or {@code null} if not present
     */
    public static String getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
    /**
     * Adds/overwrites a cookie value.
     *
     * @param request current request (used for path + secure flag)
     * @param response current response
     * @param name cookie name
     * @param value cookie value (null will be stored as empty string)
     * @param maxAgeSeconds max age in seconds
     */
    public static void setCookie(HttpServletRequest request,
                                 HttpServletResponse response,
                                 String name,
                                 String value,
                                 int maxAgeSeconds) {

        Cookie cookie = new Cookie(name, value == null ? "" : value);
        cookie.setMaxAge(maxAgeSeconds);

        String contextPath = request.getContextPath();
        cookie.setPath((contextPath == null || contextPath.isBlank()) ? "/" : (contextPath + "/"));

        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());

        response.addCookie(cookie);
    }
    /**
     * Deletes a cookie by setting max-age to 0 with the same scope/path.
     * @param request current request
     * @param response current response
     * @param name cookie name
     */
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        setCookie(request, response, name, "", 0);
    }
}
