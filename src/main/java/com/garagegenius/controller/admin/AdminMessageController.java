package com.garagegenius.controller.admin;

import java.io.IOException;
import java.util.List;

import com.garagegenius.dao.MessageDAO;
import com.garagegenius.dao.UserDAO;
import com.garagegenius.model.Message;
import com.garagegenius.model.User;
import com.garagegenius.util.SessionUtil;
import com.garagegenius.util.ValidationUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Admin internal messaging controller (admin ↔ staff).
 *
 * <p>Supports inbox/sent views, composing messages to staff, viewing messages, marking as read,
 * and viewing job-specific message threads.</p>
 *
 * <p>Mapped to {@code /admin/messages}.</p>
 */
public class AdminMessageController extends HttpServlet {

    private MessageDAO messageDAO;
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        messageDAO = new MessageDAO();
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Integer userId = SessionUtil.getLoggedInUserId(request);
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "inbox";

        switch (action) {
            case "sent":
                request.setAttribute("messages", messageDAO.getSent(userId));
                request.setAttribute("box", "sent");
                request.setAttribute("pageTitle", "Sent Messages");
                request.getRequestDispatcher("/views/admin/messages/inbox.jsp").forward(request, response);
                break;
            case "view":
                viewMessage(request, response, userId);
                break;
            case "compose":
                showCompose(request, response);
                break;
            case "thread":
                showThread(request, response);
                break;
            case "inbox":
            default:
                request.setAttribute("messages", messageDAO.getInbox(userId));
                request.setAttribute("box", "inbox");
                request.setAttribute("pageTitle", "Inbox");
                request.getRequestDispatcher("/views/admin/messages/inbox.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Integer userId = SessionUtil.getLoggedInUserId(request);
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        if ("send".equals(action)) {
            int receiverId = Integer.parseInt(request.getParameter("receiverId"));
            String subject = ValidationUtil.sanitize(request.getParameter("subject"));
            String body = ValidationUtil.sanitize(request.getParameter("body"));

            Integer jobId = null;
            String jobIdStr = request.getParameter("jobId");
            if (!ValidationUtil.isEmpty(jobIdStr)) {
                jobId = Integer.parseInt(jobIdStr);
            }

            if (ValidationUtil.isEmpty(subject) || ValidationUtil.isEmpty(body)) {
                request.setAttribute("errorMessage", "Subject and message body are required.");
                showCompose(request, response);
                return;
            }

            Message m = new Message();
            m.setSenderId(userId);
            m.setReceiverId(receiverId);
            m.setJobId(jobId);
            m.setSubject(subject);
            m.setBody(body);
            m.setStatus("unread");
            messageDAO.sendMessage(m);

            response.sendRedirect(request.getContextPath() + "/admin/messages?success=sent");
        } else if ("markRead".equals(action)) {
            int id = Integer.parseInt(request.getParameter("id"));
            messageDAO.markRead(id, userId);
            response.sendRedirect(request.getContextPath() + "/admin/messages");
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/messages");
        }
    }

    private void viewMessage(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException, ServletException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Message m = messageDAO.getMessageById(id);
            if (m == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            boolean canView = (m.getSenderId() == userId) || (m.getReceiverId() == userId);
            if (!canView) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            if (m.getReceiverId() == userId && "unread".equalsIgnoreCase(m.getStatus())) {
                messageDAO.markRead(m.getMessageId(), userId);
                m = messageDAO.getMessageById(id);
            }

            request.setAttribute("message", m);
            if (m.getJobId() != null) {
                request.setAttribute("thread", messageDAO.getJobThreadForAdmin(m.getJobId()));
            }
            request.setAttribute("pageTitle", "View Message");
            request.getRequestDispatcher("/views/admin/messages/view.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/messages");
        }
    }

    private void showCompose(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<User> staff = userDAO.getUsersByRole("staff");
        request.setAttribute("staff", staff);

        String receiverId = request.getParameter("receiverId");
        if (!ValidationUtil.isEmpty(receiverId)) request.setAttribute("receiverId", receiverId);

        String jobId = request.getParameter("jobId");
        if (!ValidationUtil.isEmpty(jobId)) request.setAttribute("jobId", jobId);

        request.setAttribute("pageTitle", "Compose Message");
        request.getRequestDispatcher("/views/admin/messages/compose.jsp").forward(request, response);
    }

    private void showThread(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String jobIdStr = request.getParameter("jobId");
        if (ValidationUtil.isEmpty(jobIdStr)) {
            response.sendRedirect(request.getContextPath() + "/admin/messages");
            return;
        }

        int jobId;
        try {
            jobId = Integer.parseInt(jobIdStr);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/messages");
            return;
        }

        request.setAttribute("thread", messageDAO.getJobThreadForAdmin(jobId));
        request.setAttribute("jobId", jobId);
        request.setAttribute("pageTitle", "Job Messages");
        request.getRequestDispatcher("/views/admin/messages/thread.jsp").forward(request, response);
    }
}