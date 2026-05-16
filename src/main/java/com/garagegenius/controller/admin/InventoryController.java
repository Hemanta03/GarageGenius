package com.garagegenius.controller.admin;

import com.garagegenius.dao.InventoryDAO;
import com.garagegenius.service.InventoryService;
import com.garagegenius.model.SparePart;
import com.garagegenius.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Admin controller for spare parts inventory management.
 *
 * <p>Supports CRUD for spare parts and stock restocking. Restock operations are routed through
 * {@link InventoryService} to ensure audit logging occurs consistently.</p>
 *
 * <p>Mapped to {@code /admin/inventory}.</p>
 */
public class InventoryController extends HttpServlet {

    private InventoryDAO inventoryDAO;
    private InventoryService inventoryService;

    @Override
    public void init() throws ServletException {
        inventoryDAO = new InventoryDAO();
        inventoryService = new InventoryService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "add":
                request.getRequestDispatcher("/views/admin/inventory/add.jsp").forward(request, response);
                break;
            case "edit":
                int id = Integer.parseInt(request.getParameter("id"));
                request.setAttribute("part", inventoryDAO.getSparePartById(id));
                request.getRequestDispatcher("/views/admin/inventory/edit.jsp").forward(request, response);
                break;
            case "restock":
                int restockId = Integer.parseInt(request.getParameter("id"));
                request.setAttribute("part", inventoryDAO.getSparePartById(restockId));
                request.getRequestDispatcher("/views/admin/inventory/restock.jsp").forward(request, response);
                break;
            case "delete":
                response.sendRedirect(request.getContextPath() + "/admin/inventory?error=delete_requires_post");
                break;
            case "list":
            default:
                listInventory(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("create".equals(action)) {
            createPart(request, response);
        } else if ("update".equals(action)) {
            updatePart(request, response);
        } else if ("restock".equals(action)) {
            restockPart(request, response);
        } else if ("delete".equals(action)) {
            deletePart(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/inventory");
        }
    }

    private void createPart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String num = request.getParameter("partNumber");
        if (inventoryDAO.partNumberExists(num)) {
            response.sendRedirect(request.getContextPath() + "/admin/inventory?action=add&error=num_exists");
            return;
        }

        try {
            SparePart p = new SparePart();
            p.setPartName(request.getParameter("partName"));
            p.setPartNumber(num);
            p.setCategory(request.getParameter("category"));
            p.setQuantityInStock(Integer.parseInt(request.getParameter("qty")));
            p.setUnitPrice(Double.parseDouble(request.getParameter("unitPrice")));
            p.setSupplierName(request.getParameter("supplierName"));
            p.setReorderLevel(Integer.parseInt(request.getParameter("reorderLevel")));

            if (inventoryDAO.addSparePart(p) > 0) {
                response.sendRedirect(request.getContextPath() + "/admin/inventory?success=created");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/inventory?error=failed");
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/admin/inventory?error=invalid_input");
        }
    }

    private void updatePart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            SparePart p = inventoryDAO.getSparePartById(id);
            if (p != null) {
                p.setPartName(request.getParameter("partName"));
                p.setPartNumber(request.getParameter("partNumber"));
                p.setCategory(request.getParameter("category"));
                p.setUnitPrice(Double.parseDouble(request.getParameter("unitPrice")));
                p.setSupplierName(request.getParameter("supplierName"));
                p.setReorderLevel(Integer.parseInt(request.getParameter("reorderLevel")));
                p.setQuantityInStock(Integer.parseInt(request.getParameter("qty")));
                
                if (inventoryDAO.updateSparePart(p)) {
                    response.sendRedirect(request.getContextPath() + "/admin/inventory?success=updated");
                } else {
                    response.sendRedirect(request.getContextPath() + "/admin/inventory?error=failed");
                }
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/inventory?error=not_found");
            }
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/admin/inventory?error=invalid_input");
        }
    }

    private void restockPart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        int qty = Integer.parseInt(request.getParameter("quantity"));
        int userId = SessionUtil.getLoggedInUserId(request);
        
        if (inventoryService.restock(id, qty, userId)) {
            response.sendRedirect(request.getContextPath() + "/admin/inventory?success=restocked");
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/inventory?error=failed");
        }
    }

    private void listInventory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<SparePart> list = inventoryDAO.getAllSpareParts();
        request.setAttribute("parts", list);
        request.setAttribute("lowStock", inventoryService.getLowStockAlerts());
        request.setAttribute("pageTitle", "Manage Inventory");
        request.getRequestDispatcher("/views/admin/inventory/list.jsp").forward(request, response);
    }

    private void deletePart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        inventoryDAO.deleteSparePart(Integer.parseInt(request.getParameter("id")));
        response.sendRedirect(request.getContextPath() + "/admin/inventory?success=deleted");
    }
}
