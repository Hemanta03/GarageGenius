package com.garagegenius.controller.customer;

import com.garagegenius.dao.CustomerDAO;
import com.garagegenius.dao.UserDAO;
import com.garagegenius.model.Customer;
import com.garagegenius.util.SessionUtil;
import com.garagegenius.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Customer profile controller.
 *
 * <p>Shows and updates the logged-in customer's profile information. Updates are applied across
 * both {@code users} (name/phone) and {@code customers} (address/city) records.</p>
 *
 * <p>Mapped to {@code /customer/profile}.</p>
 */
public class CustomerProfileController extends HttpServlet {

    private CustomerDAO customerDAO;
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        customerDAO = new CustomerDAO();
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Integer userId = SessionUtil.getLoggedInUserId(request);
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Customer c = customerDAO.getCustomerByUserId(userId);
        if (c == null) {
            response.sendRedirect(request.getContextPath() + "/customer/dashboard");
            return;
        }

        request.setAttribute("customer", c);
        request.setAttribute("pageTitle", "My Profile");
        request.getRequestDispatcher("/views/customer/profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Integer userId = SessionUtil.getLoggedInUserId(request);
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Customer c = customerDAO.getCustomerByUserId(userId);
        if (c == null) {
            response.sendRedirect(request.getContextPath() + "/customer/dashboard");
            return;
        }

        String fullName = ValidationUtil.sanitize(request.getParameter("fullName"));
        String phone = ValidationUtil.sanitize(request.getParameter("phone"));
        String address = ValidationUtil.sanitize(request.getParameter("address"));
        String city = ValidationUtil.sanitize(request.getParameter("city"));

        if (!ValidationUtil.isValidName(fullName)) {
            request.setAttribute("errorMessage", "Invalid name. Use letters and spaces only.");
            request.setAttribute("customer", c);
            request.getRequestDispatcher("/views/customer/profile.jsp").forward(request, response);
            return;
        }

        if (!ValidationUtil.isValidPhone(phone)) {
            request.setAttribute("errorMessage", "Invalid phone number. Use 10–15 digits.");
            request.setAttribute("customer", c);
            request.getRequestDispatcher("/views/customer/profile.jsp").forward(request, response);
            return;
        }

        c.setAddress(address);
        c.setCity(city);
        boolean okUser = userDAO.updateBasicInfo(userId, fullName, phone);
        boolean okCustomer = customerDAO.updateCustomer(c);

        if (okUser && okCustomer) {
            response.sendRedirect(request.getContextPath() + "/customer/profile?success=updated");
        } else {
            request.setAttribute("errorMessage", "Profile update failed.");
            request.setAttribute("customer", customerDAO.getCustomerByUserId(userId));
            request.getRequestDispatcher("/views/customer/profile.jsp").forward(request, response);
        }
    }
}

