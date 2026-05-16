package com.garagegenius.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.garagegenius.model.Contact;
import com.garagegenius.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for public contact messages ({@code contacts} table).
 *
 * <p>Stores contact submissions and allows admins to view and update message status.</p>
 */
public class ContactDAO {
    private static final Logger logger = LoggerFactory.getLogger(ContactDAO.class);


    public boolean saveContact(Contact contact) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO contacts (name, email, phone, subject, message) VALUES (?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, contact.getName());
            stmt.setString(2, contact.getEmail());
            stmt.setString(3, contact.getPhone());
            stmt.setString(4, contact.getSubject());
            stmt.setString(5, contact.getMessage());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }

    public List<Contact> getAllContacts() {
        List<Contact> contacts = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM contacts ORDER BY submitted_at DESC";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                contacts.add(extractContactFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return contacts;
    }

    public boolean updateContactStatus(int contactId, String status) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE contacts SET status = ? WHERE contact_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setInt(2, contactId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }

    private Contact extractContactFromResultSet(ResultSet rs) throws SQLException {
        Contact c = new Contact();
        c.setContactId(rs.getInt("contact_id"));
        c.setName(rs.getString("name"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setSubject(rs.getString("subject"));
        c.setMessage(rs.getString("message"));
        Timestamp ts = rs.getTimestamp("submitted_at");
        if(ts != null) c.setSubmittedAt(ts.toLocalDateTime());
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
