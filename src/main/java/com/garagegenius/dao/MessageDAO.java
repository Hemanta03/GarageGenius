package com.garagegenius.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.garagegenius.model.Message;
import com.garagegenius.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for internal messages ({@code messages} table).
 *
 * <p>Supports sending messages, inbox/sent queries, per-job threads, and marking messages as read.</p>
 */
public class MessageDAO {
    private static final Logger logger = LoggerFactory.getLogger(MessageDAO.class);


    public int sendMessage(Message message) {
        int generatedId = -1;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO messages (sender_id, receiver_id, job_id, subject, body, status) VALUES (?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, message.getSenderId());
            stmt.setInt(2, message.getReceiverId());
            if (message.getJobId() == null) {
                stmt.setObject(3, null);
            } else {
                stmt.setInt(3, message.getJobId());
            }
            stmt.setString(4, message.getSubject());
            stmt.setString(5, message.getBody());
            stmt.setString(6, message.getStatus() == null ? "unread" : message.getStatus());

            if (stmt.executeUpdate() > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    generatedId = rs.getInt(1);
                    message.setMessageId(generatedId);
                }
            }
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }

        return generatedId;
    }

    public List<Message> getInbox(int userId) {
        return getMessagesByQuery(baseQuery() + " WHERE m.receiver_id = ? ORDER BY m.sent_at DESC", userId);
    }

    public List<Message> getSent(int userId) {
        return getMessagesByQuery(baseQuery() + " WHERE m.sender_id = ? ORDER BY m.sent_at DESC", userId);
    }

    public List<Message> getJobThreadForUser(int jobId, int userId) {
        return getMessagesByQuery(
                baseQuery()
                        + " WHERE m.job_id = ? AND (m.sender_id = ? OR m.receiver_id = ?) ORDER BY m.sent_at ASC",
                jobId, userId, userId
        );
    }

    public List<Message> getJobThreadForAdmin(int jobId) {
        return getMessagesByQuery(baseQuery() + " WHERE m.job_id = ? ORDER BY m.sent_at ASC", jobId);
    }

    public Message getMessageById(int messageId) {
        List<Message> results = getMessagesByQuery(baseQuery() + " WHERE m.message_id = ?", messageId);
        return results.isEmpty() ? null : results.get(0);
    }

    public boolean markRead(int messageId, int receiverId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE messages SET status = 'read', read_at = CURRENT_TIMESTAMP WHERE message_id = ? AND receiver_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, messageId);
            stmt.setInt(2, receiverId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, null);
        }
        return false;
    }

    private String baseQuery() {
        return "SELECT m.*, su.full_name AS sender_name, ru.full_name AS receiver_name "
                + "FROM messages m "
                + "JOIN users su ON m.sender_id = su.user_id "
                + "JOIN users ru ON m.receiver_id = ru.user_id";
    }

    private List<Message> getMessagesByQuery(String sql, Object... params) {
        List<Message> messages = new ArrayList<>();
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
            while (rs.next()) {
                messages.add(extractMessage(rs));
            }
        } catch (SQLException e) {
            logger.error("Database error occurred", e);
        } finally {
            closeResources(conn, stmt, rs);
        }
        return messages;
    }

    private Message extractMessage(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setMessageId(rs.getInt("message_id"));
        m.setSenderId(rs.getInt("sender_id"));
        m.setReceiverId(rs.getInt("receiver_id"));
        Object jobId = rs.getObject("job_id");
        if (jobId instanceof Number) {
            m.setJobId(((Number) jobId).intValue());
        } else {
            m.setJobId(null);
        }
        m.setSubject(rs.getString("subject"));
        m.setBody(rs.getString("body"));
        m.setStatus(rs.getString("status"));
        Timestamp sentAt = rs.getTimestamp("sent_at");
        if (sentAt != null) m.setSentAt(sentAt.toLocalDateTime());
        Timestamp readAt = rs.getTimestamp("read_at");
        if (readAt != null) m.setReadAt(readAt.toLocalDateTime());
        m.setSenderName(rs.getString("sender_name"));
        m.setReceiverName(rs.getString("receiver_name"));
        return m;
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

