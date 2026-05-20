package com.garagegenius.filter;

import com.garagegenius.util.CsrfUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * CSRF protection filter for POST requests.
 *
 * <p>Ensures each session has a CSRF token and validates it for all POST requests except
 * authentication endpoints ({@code /login} and {@code /register}). For protected POST requests,
 * forms must include {@code csrfToken} matching the session token.</p>
 */
public class CsrfFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(true);
        String token = CsrfUtil.ensureToken(session);

        if ("POST".equalsIgnoreCase(req.getMethod())) {
            String path = req.getRequestURI();
            // Exclude auth endpoints — CSRF token may not exist yet on fresh session
            boolean isAuthEndpoint = path.endsWith("/login") || path.endsWith("/register");
            if (!isAuthEndpoint) {
                String provided = req.getParameter("csrfToken");
                if (provided == null || !provided.equals(token)) {
                    res.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token.");
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}

