package com.garagegenius.service;

import com.garagegenius.dao.CustomerDAO;
import com.garagegenius.dao.UserDAO;
import com.garagegenius.model.Customer;
import com.garagegenius.model.User;
import com.garagegenius.util.PasswordUtil;
import com.garagegenius.util.ValidationUtil;

import java.time.LocalDate;

/**
 * Authentication and registration business logic.
 *
 * <p>Login is handled against {@code users} table records and uses BCrypt password verification.
 * Registration creates a {@code customer} user in {@code pending} status, plus a corresponding
 * {@code customers} row for customer-specific profile fields.</p>
 */
public class AuthService {

    private UserDAO userDAO = new UserDAO();
    private CustomerDAO customerDAO = new CustomerDAO();

    /**
     * Attempts to authenticate a user.
     *
     * <p>Only users with {@code status=active} are allowed to log in.</p>
     *
     * @param email email address
     * @param password plaintext password
     * @return the authenticated {@link User} or {@code null} if invalid/inactive
     */
    public User login(String email, String password) {
        User user = userDAO.getUserByEmail(email);
        if (user != null && user.getStatus() != null && "active".equalsIgnoreCase(user.getStatus())) {
            if (PasswordUtil.verifyPassword(password, user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    /**
     * Registers a new customer account.
     *
     * <p>Creates a {@link User} with role {@code customer} and status {@code pending},
     * then creates a linked {@link Customer} profile row.</p>
     *
     * @param fullName customer full name
     * @param email email address (must be unique)
     * @param phone phone (must be unique)
     * @param password plaintext password
     * @param address address (optional)
     * @param city city (optional)
     * @return {@code true} if both user + customer profile were created
     */
    public boolean register(String fullName, String email, String phone, String password, String address, String city) {
        if (validateRegistration(fullName, email, phone, password) != null) return false;
        if (userDAO.emailExists(email) || userDAO.phoneExists(phone)) return false;

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(PasswordUtil.hashPassword(password));
        user.setRole("customer");
        user.setPhone(phone);
        user.setStatus("pending");

        int userId = userDAO.createUser(user);
        if (userId > 0) {
            Customer customer = new Customer();
            customer.setUserId(userId);
            customer.setAddress(address);
            customer.setCity(city);
            customer.setLoyaltyPoints(0);
            customer.setRegisteredDate(LocalDate.now());
            return customerDAO.createCustomer(customer) > 0;
        }
        return false;
    }

    /**
     * Validates registration inputs and returns a user-friendly error message.
     *
     * @param fullName full name
     * @param email email address
     * @param phone phone number
     * @param password plaintext password
     * @return {@code null} when valid; otherwise an error message for display
     */
    public String validateRegistration(String fullName, String email, String phone, String password) {
        if (!ValidationUtil.isValidName(fullName)) return "Invalid full name. Use letters and spaces only.";
        if (!ValidationUtil.isValidEmail(email)) return "Invalid email format.";
        if (!ValidationUtil.isValidPhone(phone)) return "Invalid phone number. Use 10–15 digits.";
        if (!ValidationUtil.isValidPassword(password)) return "Password must be at least 8 characters with at least 1 digit and 1 symbol.";
        return null;
    }
}
