package com.garagegenius.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.garagegenius.model.SparePart;
import com.garagegenius.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for spare parts inventory ({@code spare_parts} and {@code inventory_log} tables).
 *
 * <p>Handles spare part CRUD, stock adjustments, low-stock queries, and writing audit log entries.</p>
 */
public class InventoryDAO {
    private static final Logger logger = LoggerFactory.getLogger(InventoryDAO.class);


    public int addSparePart(SparePart part) {
        int generatedId = -1;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO spare_parts (part_name, part_number, category, quantity_in_stock, unit_price, supplier_name, reorder_level) VALUES (?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, part.getPartName());
            stmt.setString(2, part.getPartNumber());
            stmt.setString(3, part.getCategory());
            stmt.setInt(4, part.getQuantityInStock());
            stmt.setDouble(5, part.getUnitPrice());
            stmt.setString(6, part.getSupplierName());
            stmt.setInt(7, part.getReorderLevel());

            if (stmt.executeUpdate() > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    generatedId = rs.getInt(1);
                    part.setPartId(generatedId);
                }
            }
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return generatedId;
    }

    public List<SparePart> getAllSpareParts() {
        List<SparePart> parts = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM spare_parts ORDER BY part_name ASC";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                parts.add(extractPartFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return parts;
    }

    public List<SparePart> getAvailableParts() {
        List<SparePart> parts = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM spare_parts WHERE quantity_in_stock > 0 ORDER BY part_name ASC";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                parts.add(extractPartFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return parts;
    }

    public SparePart getSparePartById(int partId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM spare_parts WHERE part_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, partId);
            rs = stmt.executeQuery();
            if (rs.next()) return extractPartFromResultSet(rs);
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return null;
    }

    public boolean updateSparePart(SparePart part) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE spare_parts SET part_name=?, part_number=?, category=?, quantity_in_stock=?, unit_price=?, supplier_name=?, reorder_level=? WHERE part_id=?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, part.getPartName());
            stmt.setString(2, part.getPartNumber());
            stmt.setString(3, part.getCategory());
            stmt.setInt(4, part.getQuantityInStock());
            stmt.setDouble(5, part.getUnitPrice());
            stmt.setString(6, part.getSupplierName());
            stmt.setInt(7, part.getReorderLevel());
            stmt.setInt(8, part.getPartId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }

    public boolean deleteSparePart(int partId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "DELETE FROM spare_parts WHERE part_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, partId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }

    public boolean partNumberExists(String partNumber) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT COUNT(*) FROM spare_parts WHERE part_number = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, partNumber);
            rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return false;
    }

    public List<SparePart> getLowStockParts() {
        List<SparePart> parts = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM spare_parts WHERE quantity_in_stock <= reorder_level";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                parts.add(extractPartFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return parts;
    }

    public boolean restockPart(int partId, int quantity, int performedBy) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement("UPDATE spare_parts SET quantity_in_stock = quantity_in_stock + ?, last_restocked = CURRENT_TIMESTAMP WHERE part_id = ?");
            stmt.setInt(1, quantity);
            stmt.setInt(2, partId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }

    public void logInventoryAction(int partId, String action, int change, int prevStock, int newStock, int performedBy) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO inventory_log (part_id, action, quantity_change, previous_stock, new_stock, performed_by) VALUES (?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, partId);
            stmt.setString(2, action);
            stmt.setInt(3, change);
            stmt.setInt(4, prevStock);
            stmt.setInt(5, newStock);
            stmt.setInt(6, performedBy);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, null);
        }
    }

    private SparePart extractPartFromResultSet(ResultSet rs) throws SQLException {
        SparePart part = new SparePart();
        part.setPartId(rs.getInt("part_id"));
        part.setPartName(rs.getString("part_name"));
        part.setPartNumber(rs.getString("part_number"));
        part.setCategory(rs.getString("category"));
        part.setQuantityInStock(rs.getInt("quantity_in_stock"));
        part.setUnitPrice(rs.getDouble("unit_price"));
        part.setSupplierName(rs.getString("supplier_name"));
        part.setReorderLevel(rs.getInt("reorder_level"));
        Timestamp lr = rs.getTimestamp("last_restocked");
        if (lr != null) {
            part.setLastRestocked(lr.toLocalDateTime().toLocalDate());
        }
        return part;
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
