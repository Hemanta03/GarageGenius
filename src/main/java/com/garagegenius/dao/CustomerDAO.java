package com.garagegenius.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.garagegenius.model.Customer;
import com.garagegenius.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for customer profile data ({@code customers} table).
 *
 * <p>Customer records are linked to {@code users} via {@code user_id}. Queries often join
 * {@code customers} and {@code users} to return combined profile details for views.</p>
 */
public class CustomerDAO {
    private static final Logger logger = LoggerFactory.getLogger(CustomerDAO.class);


    /**
     * Inserts a customer profile row.
     *
     * @param customer customer profile to create
     * @return generated customer id, or -1 on failure
     */
    public int createCustomer(Customer customer) {
        int generatedId = -1;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO customers (user_id, address, city, loyalty_points, registered_date) VALUES (?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, customer.getUserId());
            stmt.setString(2, customer.getAddress());
            stmt.setString(3, customer.getCity());
            stmt.setInt(4, customer.getLoyaltyPoints());
            stmt.setDate(5, customer.getRegisteredDate() != null ? Date.valueOf(customer.getRegisteredDate()) : new Date(System.currentTimeMillis()));

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    generatedId = rs.getInt(1);
                    customer.setCustomerId(generatedId);
                }
            }
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return generatedId;
    }

    /**
     * Loads a customer profile by user id.
     *
     * @param userId linked user id
     * @return customer profile or {@code null}
     */
    public Customer getCustomerByUserId(int userId) {
        return getCustomerByQuery("SELECT c.*, u.full_name, u.email, u.phone, u.status FROM customers c JOIN users u ON c.user_id = u.user_id WHERE c.user_id = ?", userId);
    }

    /**
     * Loads a customer profile by customer id.
     *
     * @param customerId customer id
     * @return customer profile or {@code null}
     */
    public Customer getCustomerById(int customerId) {
        return getCustomerByQuery("SELECT c.*, u.full_name, u.email, u.phone, u.status FROM customers c JOIN users u ON c.user_id = u.user_id WHERE c.customer_id = ?", customerId);
    }

    /**
     * Returns all customer profiles (joined with basic user details).
     *
     * @return list of customers
     */
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT c.*, u.full_name, u.email, u.phone, u.status FROM customers c JOIN users u ON c.user_id = u.user_id ORDER BY c.customer_id DESC";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                customers.add(extractCustomerFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return customers;
    }

    /**
     * Updates mutable customer profile fields.
     *
     * @param customer updated customer profile
     * @return {@code true} if updated
     */
    public boolean updateCustomer(Customer customer) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE customers SET address = ?, city = ?, loyalty_points = ? WHERE customer_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, customer.getAddress());
            stmt.setString(2, customer.getCity());
            stmt.setInt(3, customer.getLoyaltyPoints());
            stmt.setInt(4, customer.getCustomerId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }

    /**
     * Deletes a customer profile row.
     *
     * @param customerId customer id
     * @return {@code true} if deleted
     */
    public boolean deleteCustomer(int customerId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "DELETE FROM customers WHERE customer_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, customerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }

    /**
     * @return total number of customer profile rows
     */
    public int getTotalCustomers() {
        int count = 0;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT COUNT(*) FROM customers";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            if (rs.next()) count = rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return count;
    }

    private Customer getCustomerByQuery(String sql, int id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            if (rs.next()) return extractCustomerFromResultSet(rs);
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return null;
    }

    private Customer extractCustomerFromResultSet(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("customer_id"));
        c.setUserId(rs.getInt("user_id"));
        c.setAddress(rs.getString("address"));
        c.setCity(rs.getString("city"));
        c.setLoyaltyPoints(rs.getInt("loyalty_points"));
        Date regDate = rs.getDate("registered_date");
        if (regDate != null) c.setRegisteredDate(regDate.toLocalDate());
        c.setFullName(rs.getString("full_name"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setStatus(rs.getString("status"));
        return c;
    }

    private void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) DBConnection.closeConnection(conn);
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        }
    }
}
