package com.garagegenius.controller.admin;

import com.garagegenius.dao.InvoiceDAO;
import com.garagegenius.dao.JobCardDAO;
import com.garagegenius.dao.InventoryDAO;
import com.garagegenius.dao.ReportDAO;
import com.garagegenius.model.SparePart;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Admin reporting controller.
 *
 * <p>Renders different reports based on the {@code type} query parameter (revenue, inventory,
 * services summary, and staff performance).</p>
 *
 * <p>Mapped to {@code /admin/reports}.</p>
 */
public class ReportController extends HttpServlet {

    private InvoiceDAO invoiceDAO;
    private JobCardDAO jobCardDAO;
    private InventoryDAO inventoryDAO;
    private ReportDAO reportDAO;

    @Override
    public void init() throws ServletException {
        invoiceDAO = new InvoiceDAO();
        jobCardDAO = new JobCardDAO();
        inventoryDAO = new InventoryDAO();
        reportDAO = new ReportDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String reportType = request.getParameter("type");
        if (reportType == null) reportType = "revenue";

        switch (reportType) {
            case "inventory":
                List<SparePart> allParts = inventoryDAO.getAllSpareParts();
                double totalValue = 0;
                for (SparePart p : allParts) {
                    totalValue += p.getQuantityInStock() * p.getUnitPrice();
                }
                request.setAttribute("report", allParts);
                request.setAttribute("lowStock", inventoryDAO.getLowStockParts());
                request.setAttribute("totalInventoryValue", totalValue);
                request.getRequestDispatcher("/views/admin/reports/inventory.jsp").forward(request, response);
                break;
            case "services":
                request.setAttribute("report", reportDAO.getServiceSummaryReport());
                request.getRequestDispatcher("/views/admin/reports/services.jsp").forward(request, response);
                break;
            case "performance":
                request.setAttribute("report", reportDAO.getStaffPerformanceReport());
                request.getRequestDispatcher("/views/admin/reports/performance.jsp").forward(request, response);
                break;
            case "revenue":
            default:
                request.setAttribute("totalRevenue", invoiceDAO.getTotalRevenue());
                request.setAttribute("todayRevenue", invoiceDAO.getTodayRevenue());
                request.setAttribute("report", invoiceDAO.getMonthlyRevenueReport());
                request.setAttribute("invoices", invoiceDAO.getAllInvoices());
                request.getRequestDispatcher("/views/admin/reports/revenue.jsp").forward(request, response);
                break;
        }
    }
}
