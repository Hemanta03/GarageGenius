package com.garagegenius.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.garagegenius.model.Invoice;
import com.garagegenius.model.MonthlyRevenue;
import com.garagegenius.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for invoices and revenue reporting ({@code invoices} table).
 *
 * <p>Provides invoice CRUD, payment status updates, and aggregate revenue queries used by dashboards/reports.</p>
 */
public class InvoiceDAO {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceDAO.class);


    public int createInvoice(Invoice invoice) {
        int generatedId = -1;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO invoices (job_id, order_id, customer_id, invoice_date, due_date, subtotal, tax_rate, tax_amount, discount, total_amount, payment_status, amount_paid, payment_method) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            if (invoice.getJobId() != null) {
                stmt.setInt(1, invoice.getJobId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            if (invoice.getOrderId() != null) {
                stmt.setInt(2, invoice.getOrderId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setInt(3, invoice.getCustomerId());
            stmt.setDate(4, invoice.getInvoiceDate() != null ? Date.valueOf(invoice.getInvoiceDate()) : new Date(System.currentTimeMillis()));
            stmt.setDate(5, invoice.getDueDate() != null ? Date.valueOf(invoice.getDueDate()) : new Date(System.currentTimeMillis() + 86400000L * 7));
            stmt.setDouble(6, invoice.getSubtotal());
            stmt.setDouble(7, invoice.getTaxRate());
            stmt.setDouble(8, invoice.getTaxAmount());
            stmt.setDouble(9, invoice.getDiscount());
            stmt.setDouble(10, invoice.getTotalAmount());
            stmt.setString(11, invoice.getPaymentStatus() != null ? invoice.getPaymentStatus() : "unpaid");
            stmt.setDouble(12, invoice.getAmountPaid());
            stmt.setString(13, invoice.getPaymentMethod());

            if (stmt.executeUpdate() > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    generatedId = rs.getInt(1);
                    invoice.setInvoiceId(generatedId);
                }
            }
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return generatedId;
    }

    public List<Invoice> getAllInvoices() {
        return getInvoicesByQuery("SELECT i.*, u.full_name as customer_name, v.license_plate, v.make, v.model " +
                "FROM invoices i " +
                "JOIN customers c ON i.customer_id = c.customer_id " +
                "JOIN users u ON c.user_id = u.user_id " +
                "LEFT JOIN job_cards j ON i.job_id = j.job_id " +
                "LEFT JOIN vehicles v ON j.vehicle_id = v.vehicle_id " +
                "ORDER BY i.invoice_date DESC", -1);
    }

    public List<Invoice> getInvoicesByCustomerId(int customerId) {
        return getInvoicesByQuery("SELECT i.*, u.full_name as customer_name, v.license_plate, v.make, v.model " +
                "FROM invoices i " +
                "JOIN customers c ON i.customer_id = c.customer_id " +
                "JOIN users u ON c.user_id = u.user_id " +
                "LEFT JOIN job_cards j ON i.job_id = j.job_id " +
                "LEFT JOIN vehicles v ON j.vehicle_id = v.vehicle_id " +
                "WHERE i.customer_id = ? " +
                "ORDER BY i.invoice_date DESC", customerId);
    }

    public Invoice getInvoiceById(int invoiceId) {
        List<Invoice> results = getInvoicesByQuery("SELECT i.*, u.full_name as customer_name, v.license_plate, v.make, v.model " +
                "FROM invoices i " +
                "JOIN customers c ON i.customer_id = c.customer_id " +
                "JOIN users u ON c.user_id = u.user_id " +
                "LEFT JOIN job_cards j ON i.job_id = j.job_id " +
                "LEFT JOIN vehicles v ON j.vehicle_id = v.vehicle_id " +
                "WHERE i.invoice_id = ?", invoiceId);
        return results.isEmpty() ? null : results.get(0);
    }

    public Invoice getInvoiceByJobId(int jobId) {
        List<Invoice> results = getInvoicesByQuery("SELECT i.*, u.full_name as customer_name, v.license_plate, v.make, v.model " +
                "FROM invoices i " +
                "JOIN customers c ON i.customer_id = c.customer_id " +
                "JOIN users u ON c.user_id = u.user_id " +
                "LEFT JOIN job_cards j ON i.job_id = j.job_id " +
                "LEFT JOIN vehicles v ON j.vehicle_id = v.vehicle_id " +
                "WHERE i.job_id = ?", jobId);
        return results.isEmpty() ? null : results.get(0);
    }

    public boolean updatePaymentStatus(int invoiceId, String status, double amountPaid, String method) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE invoices SET payment_status = ?, amount_paid = ?, payment_method = ?, payment_date = ? WHERE invoice_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setDouble(2, amountPaid);
            stmt.setString(3, method);
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(5, invoiceId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }

    /**
     * Marks the invoice linked to a Job Card as 'refunded'.
     * Called automatically when a job is cancelled.
     *
     * @param jobId the job card id
     * @return true if an invoice was found and refunded
     */
    public boolean refundByJobId(int jobId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE invoices SET payment_status = 'refunded', amount_paid = 0.00 "
                       + "WHERE job_id = ? AND payment_status IN ('paid', 'partial')";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, jobId);
            int rows = stmt.executeUpdate();
            if (rows > 0) System.out.println("[INFO] Invoice refunded for job_id=" + jobId);
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }

    /**
     * Marks the invoice linked to a Shop Order as 'refunded'.
     * Called automatically when an order is cancelled.
     *
     * @param orderId the order id
     * @return true if an invoice was found and refunded
     */
    public boolean refundByOrderId(int orderId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE invoices SET payment_status = 'refunded', amount_paid = 0.00 "
                       + "WHERE order_id = ? AND payment_status IN ('paid', 'partial')";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, orderId);
            int rows = stmt.executeUpdate();
            if (rows > 0) System.out.println("[INFO] Invoice refunded for order_id=" + orderId);
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }

    public double getTodayRevenue() {
        return getRevenueByQuery("SELECT SUM(amount_paid) FROM invoices WHERE DATE(payment_date) = CURDATE() AND payment_status NOT IN ('unpaid', 'refunded')");
    }

    public double getTotalRevenue() {
        return getRevenueByQuery("SELECT SUM(amount_paid) FROM invoices WHERE payment_status NOT IN ('unpaid', 'refunded')");
    }

    public List<MonthlyRevenue> getMonthlyRevenueReport() {
        List<MonthlyRevenue> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT DATE_FORMAT(payment_date, '%Y-%m') AS month, " +
                         "COUNT(invoice_id) AS invoiceCount, " +
                         "SUM(amount_paid) AS totalRevenue " +
                         "FROM invoices " +
                         "WHERE payment_status NOT IN ('unpaid', 'refunded') AND payment_date IS NOT NULL " +
                         "GROUP BY DATE_FORMAT(payment_date, '%Y-%m') " +
                         "ORDER BY month DESC";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while (rs.next()) {
                MonthlyRevenue mr = new MonthlyRevenue();
                mr.setMonth(rs.getString("month"));
                mr.setInvoiceCount(rs.getInt("invoiceCount"));
                mr.setTotalRevenue(rs.getDouble("totalRevenue"));
                list.add(mr);
            }
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return list;
    }

    private List<Invoice> getInvoicesByQuery(String sql, int id) {
        List<Invoice> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            if (id != -1) stmt.setInt(1, id);
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(extractInvoiceFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return list;
    }

    private double getRevenueByQuery(String sql) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return 0.0;
    }

    private Invoice extractInvoiceFromResultSet(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setInvoiceId(rs.getInt("invoice_id"));
        
        Object jobId = rs.getObject("job_id");
        if (jobId != null) inv.setJobId(((Number) jobId).intValue());
        
        Object orderId = rs.getObject("order_id");
        if (orderId != null) inv.setOrderId(((Number) orderId).intValue());
        
        inv.setCustomerId(rs.getInt("customer_id"));
        Date invDate = rs.getDate("invoice_date");
        if(invDate != null) inv.setInvoiceDate(invDate.toLocalDate());
        Date dueDate = rs.getDate("due_date");
        if(dueDate != null) inv.setDueDate(dueDate.toLocalDate());
        inv.setSubtotal(rs.getDouble("subtotal"));
        inv.setTaxRate(rs.getDouble("tax_rate"));
        inv.setTaxAmount(rs.getDouble("tax_amount"));
        inv.setDiscount(rs.getDouble("discount"));
        inv.setTotalAmount(rs.getDouble("total_amount"));
        inv.setPaymentStatus(rs.getString("payment_status"));
        inv.setAmountPaid(rs.getDouble("amount_paid"));
        inv.setPaymentMethod(rs.getString("payment_method"));
        Date payDate = rs.getDate("payment_date");
        if(payDate != null) inv.setPaymentDate(payDate.toLocalDate());
        inv.setCustomerName(rs.getString("customer_name"));
        inv.setLicensePlate(rs.getString("license_plate"));
        inv.setVehicleMake(rs.getString("make"));
        inv.setVehicleModel(rs.getString("model"));
        return inv;
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
