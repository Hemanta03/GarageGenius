package com.garagegenius.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.garagegenius.model.PartOrder;
import com.garagegenius.model.OrderItem;
import com.garagegenius.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class OrderDAO {
    private static final Logger logger = LoggerFactory.getLogger(OrderDAO.class);


    public boolean placeOrder(PartOrder order) {
        Connection conn = null;
        PreparedStatement orderStmt = null;
        PreparedStatement itemStmt = null;
        PreparedStatement stockStmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Transactional

            // 1. Insert Order
            String orderSql = "INSERT INTO orders (customer_id, total_amount, status) VALUES (?, ?, ?)";
            orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setInt(1, order.getCustomerId());
            orderStmt.setDouble(2, order.getTotalAmount());
            orderStmt.setString(3, order.getStatus());

            if (orderStmt.executeUpdate() == 0) {
                conn.rollback();
                return false;
            }

            rs = orderStmt.getGeneratedKeys();
            int orderId = -1;
            if (rs.next()) {
                orderId = rs.getInt(1);
                order.setOrderId(orderId); // FIX: set the generated ID back onto the order object
            } else {
                conn.rollback();
                return false;
            }

            // 2. Insert Order Items & Update Stock
            String itemSql = "INSERT INTO order_items (order_id, part_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";
            String stockSql = "UPDATE spare_parts SET quantity_in_stock = quantity_in_stock - ? WHERE part_id = ? AND quantity_in_stock >= ?";
            
            itemStmt = conn.prepareStatement(itemSql);
            stockStmt = conn.prepareStatement(stockSql);

            for (OrderItem item : order.getItems()) {
                // Insert Item
                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, item.getPartId());
                itemStmt.setInt(3, item.getQuantity());
                itemStmt.setDouble(4, item.getUnitPrice());
                itemStmt.setDouble(5, item.getSubtotal());
                itemStmt.addBatch();

                // Update Stock
                stockStmt.setInt(1, item.getQuantity());
                stockStmt.setInt(2, item.getPartId());
                stockStmt.setInt(3, item.getQuantity());
                
                int affectedRows = stockStmt.executeUpdate();
                if (affectedRows == 0) {
                    // Out of stock or part not found
                    conn.rollback();
                    return false;
                }
            }

            itemStmt.executeBatch();
            conn.commit();
            return true;

        } catch (SQLException e) {
            logger.error("Database error occurred", e);
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        } finally {
            closeResources(conn, orderStmt, rs);
            try { if (itemStmt != null) itemStmt.close(); } catch (SQLException e) {}
            try { if (stockStmt != null) stockStmt.close(); } catch (SQLException e) {}
        }
        return false;
    }

    public List<PartOrder> getOrdersByCustomer(int customerId) {
        List<PartOrder> orders = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM orders WHERE customer_id = ? ORDER BY created_at DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, customerId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                PartOrder order = extractOrderFromResultSet(rs);
                orders.add(order);
            }
            populateOrderItems(orders);
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return orders;
    }

    public List<PartOrder> getAllOrders() {
        List<PartOrder> orders = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM orders ORDER BY created_at DESC";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                PartOrder order = extractOrderFromResultSet(rs);
                orders.add(order);
            }
            populateOrderItems(orders);
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return orders;
    }

    public PartOrder getOrderById(int orderId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM orders WHERE order_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, orderId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                PartOrder order = extractOrderFromResultSet(rs);
                order.setItems(getOrderItems(order.getOrderId()));
                return order;
            }
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return null;
    }

    public boolean updateOrderStatus(int orderId, String status) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            stmt.setInt(2, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }

    private List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT oi.*, p.part_name FROM order_items oi " +
                         "JOIN spare_parts p ON oi.part_id = p.part_id " +
                         "WHERE oi.order_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, orderId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setOrderItemId(rs.getInt("order_item_id"));
                item.setOrderId(rs.getInt("order_id"));
                item.setPartId(rs.getInt("part_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setUnitPrice(rs.getDouble("unit_price"));
                item.setSubtotal(rs.getDouble("subtotal"));
                item.setPartName(rs.getString("part_name"));
                items.add(item);
            }
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return items;
    }

    private void populateOrderItems(List<PartOrder> orders) {
        if (orders.isEmpty()) return;
        Map<Integer, PartOrder> orderMap = new HashMap<>();
        StringBuilder ids = new StringBuilder();
        for (PartOrder o : orders) {
            orderMap.put(o.getOrderId(), o);
            o.setItems(new ArrayList<>());
            ids.append(o.getOrderId()).append(",");
        }
        ids.deleteCharAt(ids.length() - 1);

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT oi.*, p.part_name FROM order_items oi JOIN spare_parts p ON oi.part_id = p.part_id WHERE oi.order_id IN (" + ids.toString() + ")";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                OrderItem item = new OrderItem();
                item.setOrderItemId(rs.getInt("order_item_id"));
                item.setOrderId(rs.getInt("order_id"));
                item.setPartId(rs.getInt("part_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setUnitPrice(rs.getDouble("unit_price"));
                item.setSubtotal(rs.getDouble("subtotal"));
                item.setPartName(rs.getString("part_name"));

                PartOrder order = orderMap.get(item.getOrderId());
                if (order != null) {
                    order.getItems().add(item);
                }
            }
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    private PartOrder extractOrderFromResultSet(ResultSet rs) throws SQLException {
        PartOrder order = new PartOrder();
        order.setOrderId(rs.getInt("order_id"));
        order.setCustomerId(rs.getInt("customer_id"));
        order.setTotalAmount(rs.getDouble("total_amount"));
        order.setStatus(rs.getString("status"));
        order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return order;
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
