package com.garagegenius.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.garagegenius.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO for complex/aggregate reporting queries.
 *
 * <p>Returns report rows as generic {@link Map} values to keep reporting flexible without
 * introducing many additional DTO classes.</p>
 */
public class ReportDAO {
    private static final Logger logger = LoggerFactory.getLogger(ReportDAO.class);


    public List<Map<String, Object>> getServiceSummaryReport() {
        String sql =
                "SELECT s.service_name, SUM(js.quantity) AS total_quantity, SUM(js.subtotal) AS total_revenue "
                        + "FROM job_services js "
                        + "JOIN services s ON js.service_id = s.service_id "
                        + "JOIN job_cards j ON js.job_id = j.job_id "
                        + "WHERE j.status = 'completed' "
                        + "GROUP BY s.service_id, s.service_name "
                        + "ORDER BY total_revenue DESC";
        return queryToMapList(sql);
    }

    public List<Map<String, Object>> getStaffPerformanceReport() {
        String sql =
                "SELECT u.user_id, u.full_name, "
                        + "COUNT(j.job_id) AS jobs_completed, "
                        + "AVG(TIMESTAMPDIFF(HOUR, j.created_date, j.actual_completion)) AS avg_completion_hours "
                        + "FROM job_cards j "
                        + "JOIN users u ON j.assigned_staff_id = u.user_id "
                        + "WHERE j.status = 'completed' AND j.actual_completion IS NOT NULL "
                        + "GROUP BY u.user_id, u.full_name "
                        + "ORDER BY jobs_completed DESC";
        return queryToMapList(sql);
    }

    private List<Map<String, Object>> queryToMapList(String sql, Object... params) {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            rs = stmt.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                list.add(row);
            }
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) DBConnection.closeConnection(conn);
            } catch (SQLException e) {
                logger.error("Database error occurred", e);
            }
        }

        return list;
    }
}

