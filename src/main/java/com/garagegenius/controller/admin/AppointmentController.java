package com.garagegenius.controller.admin;

import com.garagegenius.dao.AppointmentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Admin controller for appointment requests.
 *
 * <p>Displays all appointment requests and allows admins to update request status
 * (approve/reject/complete/cancel) with optional admin notes.</p>
 *
 * <p>Mapped to {@code /admin/appointments}.</p>
 */
public class AppointmentController extends HttpServlet {

    private static final List<String> VALID_STATUSES = Arrays.asList("pending", "approved", "rejected", "completed", "cancelled");

    private AppointmentDAO appointmentDAO;

    @Override
    public void init() throws ServletException {
        appointmentDAO = new AppointmentDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("appointments", appointmentDAO.getAllAppointments());
        request.setAttribute("pageTitle", "Appointment Requests");
        request.getRequestDispatcher("/views/admin/appointments/list.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        if ("status".equals(action)) {
            int id = Integer.parseInt(request.getParameter("id"));
            String status = request.getParameter("status");
            String adminNotes = request.getParameter("adminNotes");

            if (VALID_STATUSES.contains(status)) {
                appointmentDAO.updateAppointmentStatus(id, status, adminNotes);
            }
        }
        response.sendRedirect(request.getContextPath() + "/admin/appointments");
    }
}
