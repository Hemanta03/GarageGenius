package com.garagegenius.controller.customer;

import com.garagegenius.dao.InventoryDAO;
import com.garagegenius.model.OrderItem;
import com.garagegenius.model.SparePart;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for managing the session-based shopping cart.
 */
public class CartController extends HttpServlet {

    private InventoryDAO inventoryDAO;

    @Override
    public void init() throws ServletException {
        inventoryDAO = new InventoryDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        List<OrderItem> cart = (List<OrderItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }

        if ("add".equals(action)) {
            int partId = Integer.parseInt(request.getParameter("partId"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));

            // Check if part already in cart
            boolean exists = false;
            for (OrderItem item : cart) {
                if (item.getPartId() == partId) {
                    item.setQuantity(item.getQuantity() + quantity);
                    item.setSubtotal(item.getQuantity() * item.getUnitPrice());
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                SparePart part = inventoryDAO.getSparePartById(partId);
                if (part != null) {
                    OrderItem item = new OrderItem();
                    item.setPartId(partId);
                    item.setPartName(part.getPartName());
                    item.setQuantity(quantity);
                    item.setUnitPrice(part.getUnitPrice());
                    item.setSubtotal(quantity * part.getUnitPrice());
                    cart.add(item);
                }
            }
            response.sendRedirect(request.getContextPath() + "/customer/inventory");
        } else if ("remove".equals(action)) {
            int partId = Integer.parseInt(request.getParameter("partId"));
            cart.removeIf(item -> item.getPartId() == partId);
            response.sendRedirect(request.getContextPath() + "/customer/cart");
        } else if ("clear".equals(action)) {
            cart.clear();
            response.sendRedirect(request.getContextPath() + "/customer/cart");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("pageTitle", "My Shopping Cart");
        request.getRequestDispatcher("/views/customer/cart.jsp").forward(request, response);
    }
}
