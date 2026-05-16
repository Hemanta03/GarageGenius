package com.garagegenius.controller.staff;

import com.garagegenius.dao.InventoryDAO;
import com.garagegenius.model.SparePart;
import com.garagegenius.service.InventoryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Staff read-only inventory view controller.
 *
 * <p>Displays all spare parts and highlights low-stock alerts for operational awareness.</p>
 *
 * <p>Mapped to {@code /staff/inventory}.</p>
 */
public class StaffInventoryController extends HttpServlet {

    private InventoryDAO inventoryDAO;
    private InventoryService inventoryService;

    @Override
    public void init() throws ServletException {
        inventoryDAO = new InventoryDAO();
        inventoryService = new InventoryService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<SparePart> parts = inventoryDAO.getAllSpareParts();
        request.setAttribute("parts", parts);
        request.setAttribute("lowStock", inventoryService.getLowStockAlerts());
        request.setAttribute("pageTitle", "Inventory");
        request.getRequestDispatcher("/views/staff/inventory.jsp").forward(request, response);
    }
}

