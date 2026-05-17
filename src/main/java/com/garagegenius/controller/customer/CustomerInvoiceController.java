package com.garagegenius.controller.customer;

import com.garagegenius.dao.CustomerDAO;
import com.garagegenius.dao.InvoiceDAO;
import com.garagegenius.dao.JobCardDAO;
import com.garagegenius.model.Customer;
import com.garagegenius.model.Invoice;
import com.garagegenius.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Customer invoices controller.
 *
 * <p>Lists invoices belonging to the logged-in customer and allows viewing invoice details
 * (services/parts line items) for a single invoice.</p>
 *
 * <p>Mapped to {@code /customer/invoices}.</p>
 */
public class CustomerInvoiceController extends HttpServlet {

    private CustomerDAO customerDAO;
    private InvoiceDAO invoiceDAO;
    private JobCardDAO jobCardDAO;

    @Override
    public void init() throws ServletException {
        customerDAO = new CustomerDAO();
        invoiceDAO = new InvoiceDAO();
        jobCardDAO = new JobCardDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int userId = SessionUtil.getLoggedInUserId(request);
        Customer c = customerDAO.getCustomerByUserId(userId);
        
        if (c != null) {
            String action = request.getParameter("action");
            if ("view".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id"));
                Invoice inv = invoiceDAO.getInvoiceById(id);
                if (inv == null || inv.getCustomerId() != c.getCustomerId()) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
                    return;
                }
                request.setAttribute("invoice", inv);
                if (inv.getJobId() != null && inv.getJobId() != 0) {
                    request.setAttribute("jobServices", jobCardDAO.getJobServices(inv.getJobId()));
                    request.setAttribute("jobParts", jobCardDAO.getJobParts(inv.getJobId()));
                }
                if (inv.getOrderId() != null && inv.getOrderId() != 0) {
                    com.garagegenius.dao.OrderDAO orderDAO = new com.garagegenius.dao.OrderDAO();
                    com.garagegenius.model.PartOrder order = orderDAO.getOrderById(inv.getOrderId());
                    if (order != null) {
                        request.setAttribute("orderParts", order.getItems());
                    }
                }
                request.getRequestDispatcher("/views/customer/view-invoice.jsp").forward(request, response);
            } else {
                request.setAttribute("invoices", invoiceDAO.getInvoicesByCustomerId(c.getCustomerId()));
                request.setAttribute("pageTitle", "My Invoices");
                request.getRequestDispatcher("/views/customer/invoices.jsp").forward(request, response);
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/customer/dashboard");
        }
    }
}
