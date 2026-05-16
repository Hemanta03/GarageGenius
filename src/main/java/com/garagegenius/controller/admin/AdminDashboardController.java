package com.garagegenius.controller.admin;

import com.garagegenius.dao.CustomerDAO;
import com.garagegenius.dao.JobCardDAO;
import com.garagegenius.dao.UserDAO;
import com.garagegenius.dao.InvoiceDAO;
import com.garagegenius.dao.InventoryDAO;
import com.garagegenius.dao.AppointmentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Admin dashboard controller.
 *
 * <p>Aggregates high-level KPIs (customers, jobs, staff count, revenue, low stock, pending
 * appointments) for display on the admin dashboard page.</p>
 *
 * <p>Mapped to {@code /admin/dashboard}.</p>
 */
public class AdminDashboardController extends HttpServlet {

    private CustomerDAO customerDAO;
    private JobCardDAO jobCardDAO;
    private UserDAO userDAO;
    private InvoiceDAO invoiceDAO;
    private InventoryDAO inventoryDAO;
    private AppointmentDAO appointmentDAO;

    @Override
    public void init() throws ServletException {
        customerDAO = new CustomerDAO();
        jobCardDAO = new JobCardDAO();
        userDAO = new UserDAO();
        invoiceDAO = new InvoiceDAO();
        inventoryDAO = new InventoryDAO();
        appointmentDAO = new AppointmentDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("totalCustomers", customerDAO.getTotalCustomers());
        request.setAttribute("activeJobs", jobCardDAO.getActiveJobsCount());
        request.setAttribute("totalStaff", userDAO.getUsersByRole("staff").size());
        request.setAttribute("todayRevenue", invoiceDAO.getTodayRevenue());
        request.setAttribute("lowStockCount", inventoryDAO.getLowStockParts().size());
        request.setAttribute("pendingAppointments", appointmentDAO.getPendingAppointmentCount());
        
        request.setAttribute("pageTitle", "Admin Dashboard");
        request.getRequestDispatcher("/views/admin/dashboard.jsp").forward(request, response);
    }
}
