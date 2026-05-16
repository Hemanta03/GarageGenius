package com.garagegenius.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.garagegenius.model.JobCard;
import com.garagegenius.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO for job cards and job line items ({@code job_cards}, {@code job_services}, {@code job_parts}).
 *
 * <p>Handles job card CRUD, status updates, totals, and reading/writing service/parts line items.</p>
 */
public class JobCardDAO {
    private static final Logger logger = LoggerFactory.getLogger(JobCardDAO.class);


    public int createJobCard(JobCard jobCard) {
        int generatedId = -1;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO job_cards (vehicle_id, customer_id, assigned_staff_id, created_date, status, notes, mileage_at_service, total_amount) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, jobCard.getVehicleId());
            stmt.setInt(2, jobCard.getCustomerId());
            stmt.setInt(3, jobCard.getAssignedStaffId());
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            stmt.setString(5, jobCard.getStatus() != null ? jobCard.getStatus() : "pending");
            stmt.setString(6, jobCard.getNotes());
            stmt.setInt(7, jobCard.getMileageAtService());
            stmt.setDouble(8, jobCard.getTotalAmount());

            if (stmt.executeUpdate() > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    generatedId = rs.getInt(1);
                    jobCard.setJobId(generatedId);
                }
            }
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return generatedId;
    }

    public List<JobCard> getAllJobCards() {
        return getJobCardsByQuery("SELECT j.*, u.full_name as customer_name, s.full_name as staff_name, v.license_plate, v.make, v.model " +
                "FROM job_cards j " +
                "JOIN customers c ON j.customer_id = c.customer_id " +
                "JOIN users u ON c.user_id = u.user_id " +
                "JOIN users s ON j.assigned_staff_id = s.user_id " +
                "JOIN vehicles v ON j.vehicle_id = v.vehicle_id " +
                "ORDER BY j.created_date DESC", -1);
    }

    public List<JobCard> getJobCardsByCustomerId(int customerId) {
        return getJobCardsByQuery("SELECT j.*, u.full_name as customer_name, s.full_name as staff_name, v.license_plate, v.make, v.model " +
                "FROM job_cards j " +
                "JOIN customers c ON j.customer_id = c.customer_id " +
                "JOIN users u ON c.user_id = u.user_id " +
                "JOIN users s ON j.assigned_staff_id = s.user_id " +
                "JOIN vehicles v ON j.vehicle_id = v.vehicle_id " +
                "WHERE j.customer_id = ? " +
                "ORDER BY j.created_date DESC", customerId);
    }

    public List<JobCard> getJobCardsByStaffId(int staffId) {
        return getJobCardsByQuery("SELECT j.*, u.full_name as customer_name, s.full_name as staff_name, v.license_plate, v.make, v.model " +
                "FROM job_cards j " +
                "JOIN customers c ON j.customer_id = c.customer_id " +
                "JOIN users u ON c.user_id = u.user_id " +
                "JOIN users s ON j.assigned_staff_id = s.user_id " +
                "JOIN vehicles v ON j.vehicle_id = v.vehicle_id " +
                "WHERE j.assigned_staff_id = ? " +
                "ORDER BY j.created_date DESC", staffId);
    }

    public JobCard getJobCardById(int jobId) {
        List<JobCard> results = getJobCardsByQuery("SELECT j.*, u.full_name as customer_name, s.full_name as staff_name, v.license_plate, v.make, v.model " +
                "FROM job_cards j " +
                "JOIN customers c ON j.customer_id = c.customer_id " +
                "JOIN users u ON c.user_id = u.user_id " +
                "JOIN users s ON j.assigned_staff_id = s.user_id " +
                "JOIN vehicles v ON j.vehicle_id = v.vehicle_id " +
                "WHERE j.job_id = ?", jobId);
        return results.isEmpty() ? null : results.get(0);
    }

    public boolean updateJobStatus(int jobId, String status) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE job_cards SET status = ?, actual_completion = CASE WHEN ? = 'completed' THEN COALESCE(actual_completion, CURRENT_TIMESTAMP) ELSE actual_completion END WHERE job_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setString(2, status);
            stmt.setInt(3, jobId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }

    public boolean updateJobCard(JobCard jc) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE job_cards SET vehicle_id=?, assigned_staff_id=?, status=?, notes=?, mileage_at_service=?, total_amount=?, actual_completion = CASE WHEN ? = 'completed' THEN COALESCE(actual_completion, CURRENT_TIMESTAMP) ELSE actual_completion END WHERE job_id=?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, jc.getVehicleId());
            stmt.setInt(2, jc.getAssignedStaffId());
            stmt.setString(3, jc.getStatus());
            stmt.setString(4, jc.getNotes());
            stmt.setInt(5, jc.getMileageAtService());
            stmt.setDouble(6, jc.getTotalAmount());
            stmt.setString(7, jc.getStatus());
            stmt.setInt(8, jc.getJobId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }

    public int getActiveJobsCount() {
        int count = 0;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT COUNT(*) FROM job_cards WHERE status IN ('pending', 'in_progress')";
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

    public void addJobService(int jobId, int serviceId, int qty, double unitPrice) {
        String sql = "INSERT INTO job_services (job_id, service_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";
        executeMappingInsert(sql, jobId, serviceId, qty, unitPrice, qty * unitPrice);
    }

    public void addJobPart(int jobId, int partId, int qty, double unitPrice) {
        String sql = "INSERT INTO job_parts (job_id, part_id, quantity_used, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";
        executeMappingInsert(sql, jobId, partId, qty, unitPrice, qty * unitPrice);
    }

    public List<Map<String, Object>> getJobServices(int jobId) {
        String sql = "SELECT js.*, s.service_name FROM job_services js JOIN services s ON js.service_id = s.service_id WHERE js.job_id = ?";
        return getMappingList(sql, jobId);
    }

    public List<Map<String, Object>> getJobParts(int jobId) {
        String sql = "SELECT jp.*, p.part_name FROM job_parts jp JOIN spare_parts p ON jp.part_id = p.part_id WHERE jp.job_id = ?";
        return getMappingList(sql, jobId);
    }

    public void updateJobTotal(int jobId, double total) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement("UPDATE job_cards SET total_amount = ? WHERE job_id = ?");
            stmt.setDouble(1, total);
            stmt.setInt(2, jobId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    private List<JobCard> getJobCardsByQuery(String sql, int id) {
        List<JobCard> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            if (id != -1) stmt.setInt(1, id);
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(extractJobCardFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return list;
    }

    private JobCard extractJobCardFromResultSet(ResultSet rs) throws SQLException {
        JobCard jc = new JobCard();
        jc.setJobId(rs.getInt("job_id"));
        jc.setVehicleId(rs.getInt("vehicle_id"));
        jc.setCustomerId(rs.getInt("customer_id"));
        jc.setAssignedStaffId(rs.getInt("assigned_staff_id"));
        jc.setStatus(rs.getString("status"));
        jc.setNotes(rs.getString("notes"));
        jc.setMileageAtService(rs.getInt("mileage_at_service"));
        jc.setTotalAmount(rs.getDouble("total_amount"));
        Timestamp created = rs.getTimestamp("created_date");
        if(created != null) jc.setCreatedDate(created.toLocalDateTime());
        Timestamp estimated = rs.getTimestamp("estimated_completion");
        if (estimated != null) jc.setEstimatedCompletion(estimated.toLocalDateTime().toLocalDate());
        Timestamp actual = rs.getTimestamp("actual_completion");
        if (actual != null) jc.setActualCompletion(actual.toLocalDateTime().toLocalDate());
        jc.setCustomerName(rs.getString("customer_name"));
        jc.setStaffName(rs.getString("staff_name"));
        jc.setLicensePlate(rs.getString("license_plate"));
        jc.setVehicleMake(rs.getString("make"));
        jc.setVehicleModel(rs.getString("model"));
        return jc;
    }

    private void executeMappingInsert(String sql, Object... params) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    private List<Map<String, Object>> getMappingList(String sql, int jobId) {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, jobId);
            rs = stmt.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    map.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                list.add(map);
            }
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return list;
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
