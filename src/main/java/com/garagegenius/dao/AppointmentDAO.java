package com.garagegenius.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.garagegenius.model.Appointment;
import com.garagegenius.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for appointment requests ({@code appointments} table).
 *
 * <p>Provides CRUD-style operations for creating appointment requests, listing appointments
 * (admin/customer), and updating status/admin notes.</p>
 */
public class AppointmentDAO {
    private static final Logger logger = LoggerFactory.getLogger(AppointmentDAO.class);


    /**
     * Inserts a new appointment request.
     *
     * @param appointment appointment to persist
     * @return generated appointment id, or -1 on failure
     */
    public int createAppointment(Appointment appointment) {
        int generatedId = -1;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO appointments (customer_id, vehicle_id, service_id, requested_date, preferred_time, status, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, appointment.getCustomerId());
            stmt.setInt(2, appointment.getVehicleId());
            stmt.setInt(3, appointment.getServiceId());
            stmt.setDate(4, Date.valueOf(appointment.getRequestedDate()));
            stmt.setTime(5, java.sql.Time.valueOf(appointment.getPreferredTime()));
            stmt.setString(6, appointment.getStatus() != null ? appointment.getStatus() : "pending");
            stmt.setString(7, appointment.getNotes());

            if (stmt.executeUpdate() > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    generatedId = rs.getInt(1);
                    appointment.setAppointmentId(generatedId);
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
     * @return all appointments ordered by requested/created date
     */
    public List<Appointment> getAllAppointments() {
        return getAppointmentsByQuery(baseQuery() + " ORDER BY a.requested_date DESC, a.created_at DESC", -1);
    }

    /**
     * Returns appointments for a specific customer.
     *
     * @param customerId customer id
     * @return list of appointments
     */
    public List<Appointment> getAppointmentsByCustomerId(int customerId) {
        return getAppointmentsByQuery(baseQuery() + " WHERE a.customer_id = ? ORDER BY a.requested_date DESC, a.created_at DESC", customerId);
    }

    /**
     * Loads a single appointment by id.
     *
     * @param appointmentId appointment id
     * @return appointment or {@code null} if not found
     */
    public Appointment getAppointmentById(int appointmentId) {
        List<Appointment> results = getAppointmentsByQuery(baseQuery() + " WHERE a.appointment_id = ?", appointmentId);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Updates appointment status and stores admin notes.
     *
     * @param appointmentId appointment id
     * @param status new status
     * @param adminNotes optional notes visible to admin
     * @return {@code true} if a row was updated
     */
    public boolean updateAppointmentStatus(int appointmentId, String status, String adminNotes) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE appointments SET status = ?, admin_notes = ? WHERE appointment_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setString(2, adminNotes);
            stmt.setInt(3, appointmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }

    /**
     * Cancels a pending appointment owned by the given customer.
     *
     * @param appointmentId appointment id
     * @param customerId customer id
     * @return {@code true} if the appointment was cancelled
     */
    public boolean cancelCustomerAppointment(int appointmentId, int customerId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE appointments SET status = 'cancelled' WHERE appointment_id = ? AND customer_id = ? AND status = 'pending'";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, appointmentId);
            stmt.setInt(2, customerId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }

    /**
     * Returns count of appointments in {@code pending} status.
     *
     * @return pending appointment count
     */
    public int getPendingAppointmentCount() {
        int count = 0;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM appointments WHERE status = 'pending'");
            rs = stmt.executeQuery();
            if (rs.next()) count = rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return count;
    }

    private String baseQuery() {
        return "SELECT a.*, u.full_name AS customer_name, u.email AS customer_email, " +
                "v.license_plate, v.make, v.model, s.service_name " +
                "FROM appointments a " +
                "JOIN customers c ON a.customer_id = c.customer_id " +
                "JOIN users u ON c.user_id = u.user_id " +
                "JOIN vehicles v ON a.vehicle_id = v.vehicle_id " +
                "JOIN services s ON a.service_id = s.service_id";
    }

    private List<Appointment> getAppointmentsByQuery(String sql, int id) {
        List<Appointment> appointments = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            if (id != -1) stmt.setInt(1, id);
            rs = stmt.executeQuery();
            while (rs.next()) {
                appointments.add(extractAppointmentFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return appointments;
    }

    private Appointment extractAppointmentFromResultSet(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setAppointmentId(rs.getInt("appointment_id"));
        a.setCustomerId(rs.getInt("customer_id"));
        a.setVehicleId(rs.getInt("vehicle_id"));
        a.setServiceId(rs.getInt("service_id"));
        Date requestedDate = rs.getDate("requested_date");
        if (requestedDate != null) a.setRequestedDate(requestedDate.toLocalDate());
        a.setPreferredTime(rs.getString("preferred_time"));
        a.setStatus(rs.getString("status"));
        a.setNotes(rs.getString("notes"));
        a.setAdminNotes(rs.getString("admin_notes"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) a.setCreatedAt(createdAt.toLocalDateTime());
        a.setCustomerName(rs.getString("customer_name"));
        a.setCustomerEmail(rs.getString("customer_email"));
        a.setLicensePlate(rs.getString("license_plate"));
        a.setVehicleMake(rs.getString("make"));
        a.setVehicleModel(rs.getString("model"));
        a.setServiceName(rs.getString("service_name"));
        return a;
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
