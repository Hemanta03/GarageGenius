package com.garagegenius.controller.admin;

import com.garagegenius.dao.InvoiceDAO;
import com.garagegenius.dao.JobCardDAO;
import com.garagegenius.service.BillingService;
import com.garagegenius.model.Invoice;
import com.garagegenius.model.JobCard;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Admin controller for invoice operations.
 *
 * <p>Allows admins to generate invoices from completed jobs, view invoice details, and record
 * payments (paid/partial/unpaid).</p>
 *
 * <p>Mapped to {@code /admin/invoices}.</p>
 */
public class InvoiceController extends HttpServlet {

    private InvoiceDAO invoiceDAO;
    private JobCardDAO jobCardDAO;
    private com.garagegenius.dao.OrderDAO orderDAO;
    private BillingService billingService;

    @Override
    public void init() throws ServletException {
        invoiceDAO = new InvoiceDAO();
        jobCardDAO = new JobCardDAO();
        orderDAO = new com.garagegenius.dao.OrderDAO();
        billingService = new BillingService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "generate":
                int jobId = Integer.parseInt(request.getParameter("jobId"));
                JobCard jc = jobCardDAO.getJobCardById(jobId);
                request.setAttribute("jobCard", jc);
                request.getRequestDispatcher("/views/admin/invoices/generate.jsp").forward(request, response);
                break;
            case "view":
                int id = Integer.parseInt(request.getParameter("id"));
                Invoice inv = invoiceDAO.getInvoiceById(id);
                request.setAttribute("invoice", inv);
                if (inv != null) {
                    if (inv.getJobId() != null && inv.getJobId() != 0) {
                        request.setAttribute("jobServices", jobCardDAO.getJobServices(inv.getJobId()));
                        request.setAttribute("jobParts", jobCardDAO.getJobParts(inv.getJobId()));
                    }
                    if (inv.getOrderId() != null && inv.getOrderId() != 0) {
                        com.garagegenius.model.PartOrder order = orderDAO.getOrderById(inv.getOrderId());
                        if (order != null) {
                            request.setAttribute("orderParts", order.getItems());
                        }
                    }
                }
                request.getRequestDispatcher("/views/admin/invoices/view.jsp").forward(request, response);
                break;
            case "pay":
                int payId = Integer.parseInt(request.getParameter("id"));
                Invoice payInv = invoiceDAO.getInvoiceById(payId);
                request.setAttribute("invoice", payInv);
                request.getRequestDispatcher("/views/admin/invoices/pay.jsp").forward(request, response);
                break;
            case "list":
            default:
                listInvoices(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("create".equals(action)) {
            generateInvoice(request, response);
        } else if ("updatePayment".equals(action)) {
            updatePayment(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/invoices");
        }
    }

    private void generateInvoice(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int jobId = Integer.parseInt(request.getParameter("jobId"));
        double taxRate = Double.parseDouble(request.getParameter("taxRate"));
        double discount = Double.parseDouble(request.getParameter("discount"));

        Invoice inv = billingService.generateInvoice(jobId, taxRate, discount);
        if (inv != null) {
            response.sendRedirect(request.getContextPath() + "/admin/invoices?success=generated");
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/invoices?error=generation_failed");
        }
    }

    private void updatePayment(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        String status = request.getParameter("status");
        String method = request.getParameter("method");
        
        Invoice inv = invoiceDAO.getInvoiceById(id);
        double amountPaid = 0.0;
        
        if ("paid".equals(status) && inv != null) {
            amountPaid = inv.getTotalAmount();
        } else if ("partial".equals(status)) {
            String amtStr = request.getParameter("amountPaid");
            if (amtStr != null && !amtStr.trim().isEmpty()) {
                amountPaid = Double.parseDouble(amtStr);
            }
        }
        
        if (invoiceDAO.updatePaymentStatus(id, status, amountPaid, method)) {
            response.sendRedirect(request.getContextPath() + "/admin/invoices?success=paid");
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/invoices?error=failed");
        }
    }

    private void listInvoices(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Invoice> list = invoiceDAO.getAllInvoices();
        request.setAttribute("invoices", list);
        request.setAttribute("revenue", invoiceDAO.getTotalRevenue());
        request.setAttribute("pageTitle", "Manage Invoices");
        request.getRequestDispatcher("/views/admin/invoices/list.jsp").forward(request, response);
    }
}
