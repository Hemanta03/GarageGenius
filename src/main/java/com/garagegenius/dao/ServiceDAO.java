package com.garagegenius.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.garagegenius.model.Service;
import com.garagegenius.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for the service catalog ({@code services} table).
 *
 * <p>Services represent billable work items that can be added to job cards.</p>
 */
public class ServiceDAO {
    private static final Logger logger = LoggerFactory.getLogger(ServiceDAO.class);


    /**
     * Inserts a new service.
     *
     * @param service service record
     * @return generated service id, or -1 on failure
     */
    public int addService(Service service) {
        int generatedId = -1;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO services (service_name, description, base_price, estimated_duration_hrs, category) VALUES (?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, service.getServiceName());
            stmt.setString(2, service.getDescription());
            stmt.setDouble(3, service.getBasePrice());
            stmt.setDouble(4, service.getEstimatedDurationHrs());
            stmt.setString(5, service.getCategory());

            if (stmt.executeUpdate() > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    generatedId = rs.getInt(1);
                    service.setServiceId(generatedId);
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
     * @return all services ordered by name
     */
    public List<Service> getAllServices() {
        List<Service> services = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM services ORDER BY service_name ASC";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                services.add(extractServiceFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return services;
    }

    /**
     * Loads a service by id.
     *
     * @param serviceId service id
     * @return service or {@code null}
     */
    public Service getServiceById(int serviceId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM services WHERE service_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, serviceId);
            rs = stmt.executeQuery();
            if (rs.next()) return extractServiceFromResultSet(rs);
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return null;
    }

    /**
     * Updates a service record.
     *
     * @param service updated service
     * @return {@code true} if updated
     */
    public boolean updateService(Service service) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE services SET service_name=?, description=?, base_price=?, estimated_duration_hrs=?, category=? WHERE service_id=?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, service.getServiceName());
            stmt.setString(2, service.getDescription());
            stmt.setDouble(3, service.getBasePrice());
            stmt.setDouble(4, service.getEstimatedDurationHrs());
            stmt.setString(5, service.getCategory());
            stmt.setInt(6, service.getServiceId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }

    /**
     * Deletes a service record.
     *
     * @param serviceId service id
     * @return {@code true} if deleted
     */
    public boolean deleteService(int serviceId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "DELETE FROM services WHERE service_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, serviceId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }

    private Service extractServiceFromResultSet(ResultSet rs) throws SQLException {
        Service s = new Service();
        s.setServiceId(rs.getInt("service_id"));
        s.setServiceName(rs.getString("service_name"));
        s.setDescription(rs.getString("description"));
        s.setBasePrice(rs.getDouble("base_price"));
        s.setEstimatedDurationHrs(rs.getDouble("estimated_duration_hrs"));
        s.setCategory(rs.getString("category"));
        return s;
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
