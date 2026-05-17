package com.garagegenius.controller;

import com.garagegenius.model.User;
import com.garagegenius.service.AuthService;
import com.garagegenius.util.CookieUtil;
import com.garagegenius.util.SessionUtil;
import com.garagegenius.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Handles authentication (login) requests.
 *
 * <p>GET renders the login form and pre-fills the last used email (cookie). POST verifies
 * credentials via {@link AuthService}, establishes a session via {@link SessionUtil}, then
 * redirects to the appropriate role dashboard.</p>
 *
 * <p>Mapped to {@code /login}.</p>
 */
public class LoginController extends HttpServlet {

    private static final String LAST_EMAIL_COOKIE = "gg_last_email";
    private AuthService authService;

    @Override
    public void init() {
        this.authService = new AuthService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (SessionUtil.isLoggedIn(request)) {
            redirectBasedOnRole(request, response, SessionUtil.getLoggedInRole(request));
            return;
        }

        String lastEmail = CookieUtil.getCookie(request, LAST_EMAIL_COOKIE);
        if (!ValidationUtil.isEmpty(lastEmail)) {
            request.setAttribute("emailPrefill", lastEmail);
        }

        request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (ValidationUtil.isEmpty(email) || ValidationUtil.isEmpty(password)) {
            request.setAttribute("errorMessage", "Email and password are required");
            request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
            return;
        }

        User user = authService.login(email, password);

        if (user != null) {
            SessionUtil.setUserSession(
                    request,
                    user.getUserId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole()
            );

            CookieUtil.setCookie(request, response, LAST_EMAIL_COOKIE, user.getEmail(), 60 * 60 * 24 * 30);
            redirectBasedOnRole(request, response, user.getRole());
        } else {
            request.setAttribute("errorMessage", "Invalid email or password");
            request.getRequestDispatcher("/views/auth/login.jsp").forward(request, response);
        }
    }

    private void redirectBasedOnRole(HttpServletRequest request, HttpServletResponse response, String role)
            throws IOException {

        String contextPath = request.getContextPath();

        switch (role != null ? role : "") {
            case "admin":
                response.sendRedirect(contextPath + "/admin/dashboard");
                break;
            case "staff":
                response.sendRedirect(contextPath + "/staff/dashboard");
                break;
            case "customer":
                response.sendRedirect(contextPath + "/customer/dashboard");
                break;
            default:
                response.sendRedirect(contextPath + "/login");
        }
    }
}
