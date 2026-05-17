package com.garagegenius.controller.customer;

import com.garagegenius.dao.CustomerDAO;
import com.garagegenius.dao.OrderDAO;
import com.garagegenius.model.Customer;
import com.garagegenius.model.OrderItem;
import com.garagegenius.model.PartOrder;
import com.garagegenius.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Controller for placing orders from the cart.
 */
public class CheckoutController extends HttpServlet {

    private OrderDAO orderDAO;
    private CustomerDAO customerDAO;
    private com.garagegenius.dao.InvoiceDAO invoiceDAO;

    @Override
    public void init() throws ServletException {
        orderDAO = new OrderDAO();
        customerDAO = new CustomerDAO();
        invoiceDAO = new com.garagegenius.dao.InvoiceDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        List<OrderItem> cart = (List<OrderItem>) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/customer/inventory");
            return;
        }

        Integer userId = SessionUtil.getLoggedInUserId(request);
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        Customer customer = customerDAO.getCustomerByUserId(userId);

        if (customer == null) {
            request.setAttribute("errorMessage", "Customer profile not found. Please contact support.");
            request.getRequestDispatcher("/views/customer/cart.jsp").forward(request, response);
            return;
        }

        // Create Order object
        PartOrder order = new PartOrder();
        order.setCustomerId(customer.getCustomerId());
        order.setItems(cart);
        
        double total = 0;
        for (OrderItem item : cart) {
            total += item.getSubtotal();
        }
        order.setTotalAmount(total);

        boolean success = orderDAO.placeOrder(order);

        if (success) {
            // Generate Invoice for the shop order
            com.garagegenius.model.Invoice inv = new com.garagegenius.model.Invoice();
            inv.setOrderId(order.getOrderId());
            inv.setCustomerId(customer.getCustomerId());
            inv.setSubtotal(total);
            double taxRate = 13.00; // Default tax rate
            double taxAmount = (total * taxRate) / 100.0;
            inv.setTaxRate(taxRate);
            inv.setTaxAmount(taxAmount);
            inv.setDiscount(0.0);
            inv.setTotalAmount(total + taxAmount);
            inv.setPaymentStatus("unpaid");
            inv.setInvoiceDate(java.time.LocalDate.now());
            inv.setDueDate(java.time.LocalDate.now().plusDays(7));
            invoiceDAO.createInvoice(inv);

            session.removeAttribute("cart");
            session.setAttribute("successMessage", "Order placed successfully! Your order ID is #" + order.getOrderId() + ". Invoice has been generated.");
            response.sendRedirect(request.getContextPath() + "/customer/orders");
        } else {
            request.setAttribute("errorMessage", "Failed to place order. One or more items might be out of stock.");
            request.getRequestDispatcher("/views/customer/cart.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Display customer order history
        Integer userId = SessionUtil.getLoggedInUserId(request);
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        Customer customer = customerDAO.getCustomerByUserId(userId);
        
        if (customer != null) {
            List<PartOrder> orders = orderDAO.getOrdersByCustomer(customer.getCustomerId());
            request.setAttribute("orders", orders);
        }
        
        request.setAttribute("pageTitle", "My Orders");
        request.getRequestDispatcher("/views/customer/orders.jsp").forward(request, response);
    }
}
