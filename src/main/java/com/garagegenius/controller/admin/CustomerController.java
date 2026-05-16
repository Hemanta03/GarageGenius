package com.garagegenius.controller.admin;

import com.garagegenius.dao.CustomerDAO;
import com.garagegenius.dao.UserDAO;
import com.garagegenius.model.Customer;
import com.garagegenius.model.User;
import com.garagegenius.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;

/**
 * Admin controller for managing customers.
 *
 * <p>Supports listing, viewing, creating walk-in customers, editing customer profile fields,
 * approving pending registrations (activates linked user), and deactivating accounts.</p>
 *
 * <p>Mapped to {@code /admin/customers}.</p>
 */
public class CustomerController extends HttpServlet {

    private CustomerDAO customerDAO;
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        customerDAO = new CustomerDAO();
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "add":
                request.getRequestDispatcher("/views/admin/customers/add.jsp").forward(request, response);
                break;
            case "edit":
                try {
                    int id = Integer.parseInt(request.getParameter("id"));
                    Customer customer = customerDAO.getCustomerById(id);
                    request.setAttribute("customer", customer);
                    request.getRequestDispatcher("/views/admin/customers/edit.jsp").forward(request, response);
                } catch (NumberFormatException e) {
                    response.sendRedirect(request.getContextPath() + "/admin/customers?error=invalid_id");
                }
                break;
            case "view":
                viewCustomer(request, response);
                break;
            case "approve":
                approveCustomer(request, response);
                break;
            case "delete":
                response.sendRedirect(request.getContextPath() + "/admin/customers?error=delete_requires_post");
                break;
            case "list":
            default:
                listCustomers(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("create".equals(action)) {
            createCustomer(request, response);
        } else if ("update".equals(action)) {
            updateCustomer(request, response);
        } else if ("delete".equals(action)) {
            deleteCustomer(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/customers");
        }
    }

    private void createCustomer(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String city = request.getParameter("city");

        // Handle walk-in customers who don't provide an email by setting it to NULL
        if (email == null || email.trim().isEmpty()) {
            email = null;
        }
        if (address == null || address.trim().isEmpty()) {
            address = "N/A";
        }
        if (city == null || city.trim().isEmpty()) {
            city = "N/A";
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole("customer");
        user.setStatus("active");
        // BUG-07 FIX: generate a random temporary password instead of hardcoding "Customer@123"
        String tempPassword = generateTempPassword();
        user.setPassword(PasswordUtil.hashPassword(tempPassword));

        int userId = userDAO.createUser(user);
        if (userId > 0) {
            Customer customer = new Customer();
            customer.setUserId(userId);
            customer.setAddress(address);
            customer.setCity(city);
            customer.setRegisteredDate(LocalDate.now());
            customer.setLoyaltyPoints(0);

            if (customerDAO.createCustomer(customer) > 0) {
                response.sendRedirect(request.getContextPath() + "/admin/customers?success=created&tempPass=" + tempPassword);
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/customers?error=creation_failed");
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/customers?error=user_exists");
        }
    }

    /** BUG-07 FIX: Generates a random 10-char password with letters, digits, and a symbol */
    private String generateTempPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789@#!";
        SecureRandom rng = new SecureRandom();
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) sb.append(chars.charAt(rng.nextInt(chars.length())));
        return sb.toString();
    }

    private void updateCustomer(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Customer customer = customerDAO.getCustomerById(id);
            if (customer != null) {
                customer.setAddress(request.getParameter("address"));
                customer.setCity(request.getParameter("city"));
                customer.setLoyaltyPoints(Integer.parseInt(request.getParameter("loyaltyPoints")));
                if (customerDAO.updateCustomer(customer)) {
                    response.sendRedirect(request.getContextPath() + "/admin/customers?success=updated");
                } else {
                    response.sendRedirect(request.getContextPath() + "/admin/customers?error=update_failed");
                }
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/customers?error=invalid_input");
        }
    }

    private void approveCustomer(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Customer c = customerDAO.getCustomerById(id);
            if (c != null) userDAO.updateUserStatus(c.getUserId(), "active");
            response.sendRedirect(request.getContextPath() + "/admin/customers?status=approved");
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/customers?error=invalid_id");
        }
    }

    private void deleteCustomer(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Customer c = customerDAO.getCustomerById(id);
            if (c != null) userDAO.updateUserStatus(c.getUserId(), "inactive");
            response.sendRedirect(request.getContextPath() + "/admin/customers?status=deleted");
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/customers?error=invalid_id");
        }
    }

    private void listCustomers(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Customer> customerList = customerDAO.getAllCustomers();
        request.setAttribute("customers", customerList);
        request.setAttribute("pageTitle", "Manage Customers");
        request.getRequestDispatcher("/views/admin/customers/list.jsp").forward(request, response);
    }

    private void viewCustomer(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Customer customer = customerDAO.getCustomerById(id);
            request.setAttribute("customer", customer);
            request.getRequestDispatcher("/views/admin/customers/view.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/customers?error=invalid_id");
        }
    }
}
