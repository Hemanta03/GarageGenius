package com.garagegenius.controller.customer;

import com.garagegenius.dao.InventoryDAO;
import com.garagegenius.model.SparePart;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller for customers to view available spare parts.
 */
public class CustomerInventoryController extends HttpServlet {

    private InventoryDAO inventoryDAO;

    @Override
    public void init() throws ServletException {
        inventoryDAO = new InventoryDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<SparePart> availableParts = inventoryDAO.getAvailableParts();
        request.setAttribute("parts", availableParts);
        request.setAttribute("pageTitle", "Spare Parts Shop");
        request.getRequestDispatcher("/views/customer/inventory.jsp").forward(request, response);
    }
}
