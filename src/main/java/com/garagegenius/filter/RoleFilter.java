package com.garagegenius.filter;

import com.garagegenius.util.SessionUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Role-based access control (RBAC) for URL namespaces.
 *
 * <p>Uses the request path prefix to enforce role boundaries:</p>
 * <ul>
 *   <li>{@code /admin/*} requires role {@code admin}</li>
 *   <li>{@code /staff/*} requires role {@code staff} (or {@code admin})</li>
 *   <li>{@code /customer/*} requires role {@code customer}</li>
 * </ul>
 *
 * <p>When access is denied, a 403 is returned (handled by the configured error page).</p>
 */
public class RoleFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        String role = SessionUtil.getLoggedInRole(httpRequest);

        if (path.startsWith("/admin") && !"admin".equals(role)) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Admin privileges required.");
            return;
        } else if (path.startsWith("/staff") && !"staff".equals(role) && !"admin".equals(role)) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Staff privileges required.");
            return;
        } else if (path.startsWith("/customer") && !"customer".equals(role)) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Customer privileges required.");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
