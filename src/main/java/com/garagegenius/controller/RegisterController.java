package com.garagegenius.controller;

import com.garagegenius.service.AuthService;
import com.garagegenius.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Handles customer self-registration.
 *
 * <p>GET renders the registration form. POST validates inputs and creates a new user with
 * role {@code customer} and status {@code pending}. An admin must approve the account before
 * login succeeds.</p>
 *
 * <p>Mapped to {@code /register}.</p>
 */
public class RegisterController extends HttpServlet {

    private AuthService authService;

    @Override
    public void init() {
        this.authService = new AuthService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String city = request.getParameter("city");

        String validationError = authService.validateRegistration(fullName, email, phone, password);
        if (validationError != null) {
            request.setAttribute("errorMessage", validationError);
            request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
            return;
        }

        boolean success = authService.register(fullName, email, phone, password, address, city);

        if (success) {
            request.getSession().setAttribute("successMessage", "Registration submitted! Your account is pending admin approval. You will be able to log in once approved.");
            response.sendRedirect(request.getContextPath() + "/login");
        } else {
            request.setAttribute("errorMessage", "Registration failed. Email/Phone may be in use.");
            request.getRequestDispatcher("/views/auth/register.jsp").forward(request, response);
        }
    }
}
