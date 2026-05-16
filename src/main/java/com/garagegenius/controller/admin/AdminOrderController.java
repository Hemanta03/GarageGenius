package com.garagegenius.controller.admin;

import com.garagegenius.dao.OrderDAO;
import com.garagegenius.dao.InvoiceDAO;
import com.garagegenius.model.PartOrder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller for admins to manage customer spare part orders.
 */
public class AdminOrderController extends HttpServlet {

    private OrderDAO orderDAO;
    private InvoiceDAO invoiceDAO;

    @Override
    public void init() throws ServletException {
        orderDAO = new OrderDAO();
        invoiceDAO = new InvoiceDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<PartOrder> allOrders = orderDAO.getAllOrders();
        request.setAttribute("orders", allOrders);
        request.setAttribute("pageTitle", "Manage Customer Orders");
        request.getRequestDispatcher("/views/admin/orders/list.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int orderId = Integer.parseInt(request.getParameter("orderId"));
        String newStatus = request.getParameter("status");

        boolean success = orderDAO.updateOrderStatus(orderId, newStatus);
        if (success) {
            // Auto-refund: if the order is cancelled, refund its linked invoice
            if ("cancelled".equals(newStatus)) {
                invoiceDAO.refundByOrderId(orderId);
            }
            request.getSession().setAttribute("successMessage", "Order #" + orderId + " updated to " + newStatus);
        } else {
            request.getSession().setAttribute("errorMessage", "Failed to update order #" + orderId);
        }
        response.sendRedirect(request.getContextPath() + "/admin/orders");
    }
}
