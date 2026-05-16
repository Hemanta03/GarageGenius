package com.garagegenius.service;

import com.garagegenius.dao.InventoryDAO;
import com.garagegenius.model.SparePart;

import java.util.List;

/**
 * Inventory business logic for spare parts.
 *
 * <p>Wraps DAO operations to apply stock arithmetic and to ensure inventory actions
 * are logged consistently.</p>
 */
public class InventoryService {

    private InventoryDAO inventoryDAO = new InventoryDAO();

    /**
     * Increases stock for a part and logs the restock action.
     *
     * @param partId spare part id
     * @param quantity quantity to add (must be positive)
     * @param performedBy user id performing the action (for audit log)
     * @return {@code true} if restock succeeded
     */
    public boolean restock(int partId, int quantity, int performedBy) {
        SparePart part = inventoryDAO.getSparePartById(partId);
        if (part == null) return false;

        int prevStock = part.getQuantityInStock();
        int newStock = prevStock + quantity;

        if (inventoryDAO.restockPart(partId, quantity, performedBy)) {
            inventoryDAO.logInventoryAction(partId, "restock", quantity, prevStock, newStock, performedBy);
            return true;
        }
        return false;
    }

    /**
     * Decreases stock for a part (when used in a job) and logs the usage action.
     *
     * @param partId spare part id
     * @param quantity quantity to subtract (must be positive)
     * @param performedBy user id performing the action (for audit log)
     * @return {@code true} if enough stock exists and the update succeeded
     */
    public boolean usePart(int partId, int quantity, int performedBy) {
        SparePart part = inventoryDAO.getSparePartById(partId);
        if (part == null || part.getQuantityInStock() < quantity) return false;

        int prevStock = part.getQuantityInStock();
        int newStock = prevStock - quantity;

        if (inventoryDAO.restockPart(partId, -quantity, performedBy)) {
            inventoryDAO.logInventoryAction(partId, "used", -quantity, prevStock, newStock, performedBy);
            return true;
        }
        return false;
    }

    /**
     * Returns all parts currently at/below their reorder level.
     *
     * @return list of low-stock spare parts
     */
    public List<SparePart> getLowStockAlerts() {
        return inventoryDAO.getLowStockParts();
    }
}
