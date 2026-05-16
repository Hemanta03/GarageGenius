package com.garagegenius.controller.admin;

import com.garagegenius.dao.UserDAO;
import com.garagegenius.model.User;
import com.garagegenius.util.PasswordUtil;
import com.garagegenius.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;

/**
 * Admin controller for managing application users (admin/staff) and customer account status.
 *
 * <p>Allows creating staff/admin users, updating user status (active/inactive/pending),
 * and resetting passwords to a temporary value.</p>
 *
 * <p>Mapped to {@code /admin/users}.</p>
 */
public class AdminUserController extends HttpServlet {

    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "add":
                request.setAttribute("pageTitle", "Create User");
                request.getRequestDispatcher("/views/admin/users/add.jsp").forward(request, response);
                break;
            case "list":
            default:
                List<User> users = userDAO.getAllUsers();
                request.setAttribute("users", users);
                request.setAttribute("pageTitle", "Manage Users");
                request.getRequestDispatcher("/views/admin/users/list.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String action = request.getParameter("action");
        if ("create".equals(action)) {
            createUser(request, response);
        } else if ("status".equals(action)) {
            updateStatus(request, response);
        } else if ("resetPassword".equals(action)) {
            resetPassword(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/users");
        }
    }

    private void createUser(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String fullName = ValidationUtil.sanitize(request.getParameter("fullName"));
        String email = ValidationUtil.sanitize(request.getParameter("email"));
        String phone = ValidationUtil.sanitize(request.getParameter("phone"));
        String role = ValidationUtil.sanitize(request.getParameter("role")).toLowerCase();

        if (!ValidationUtil.isValidName(fullName)) {
            request.setAttribute("errorMessage", "Invalid name. Use letters and spaces only.");
            request.getRequestDispatcher("/views/admin/users/add.jsp").forward(request, response);
            return;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            request.setAttribute("errorMessage", "Invalid email format.");
            request.getRequestDispatcher("/views/admin/users/add.jsp").forward(request, response);
            return;
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            request.setAttribute("errorMessage", "Invalid phone number. Use 10–15 digits.");
            request.getRequestDispatcher("/views/admin/users/add.jsp").forward(request, response);
            return;
        }
        if (!("admin".equals(role) || "staff".equals(role))) {
            request.setAttribute("errorMessage", "Role must be admin or staff.");
            request.getRequestDispatcher("/views/admin/users/add.jsp").forward(request, response);
            return;
        }
        if (userDAO.emailExists(email)) {
            request.setAttribute("errorMessage", "Email is already in use.");
            request.getRequestDispatcher("/views/admin/users/add.jsp").forward(request, response);
            return;
        }

        String tempPassword = generateTempPassword();

        User u = new User();
        u.setFullName(fullName);
        u.setEmail(email);
        u.setPhone(phone);
        u.setRole(role);
        u.setStatus("active");
        u.setPassword(PasswordUtil.hashPassword(tempPassword));

        int id = userDAO.createUser(u);
        if (id > 0) {
            response.sendRedirect(request.getContextPath() + "/admin/users?success=created&tempPass=" + tempPassword);
        } else {
            request.setAttribute("errorMessage", "User creation failed.");
            request.getRequestDispatcher("/views/admin/users/add.jsp").forward(request, response);
        }
    }

    private void updateStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String status = ValidationUtil.sanitize(request.getParameter("status")).toLowerCase();
            if (!("active".equals(status) || "inactive".equals(status) || "pending".equals(status))) {
                response.sendRedirect(request.getContextPath() + "/admin/users?error=bad_status");
                return;
            }
            userDAO.updateUserStatus(id, status);
            response.sendRedirect(request.getContextPath() + "/admin/users?success=status_updated");
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/users?error=invalid_id");
        }
    }

    private void resetPassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String tempPassword = generateTempPassword();
            userDAO.updatePassword(id, PasswordUtil.hashPassword(tempPassword));
            response.sendRedirect(request.getContextPath() + "/admin/users?success=password_reset&tempPass=" + tempPassword);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/users?error=invalid_id");
        }
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789@#!";
        SecureRandom rng = new SecureRandom();
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) sb.append(chars.charAt(rng.nextInt(chars.length())));
        return sb.toString();
    }
}

