package com.garagegenius.service;

import com.garagegenius.dao.JobCardDAO;
import com.garagegenius.dao.ServiceDAO;
import com.garagegenius.dao.InventoryDAO;
import com.garagegenius.dao.ServiceHistoryDAO;
import com.garagegenius.dao.InvoiceDAO;
import com.garagegenius.model.JobCard;
import com.garagegenius.model.Service;
import com.garagegenius.model.SparePart;

import java.util.Map;

/**
 * Job workflow/business logic for creating and updating job cards.
 *
 * <p>Encapsulates multi-step operations such as creating a job with services, attaching
 * parts to a job while adjusting inventory, recalculating totals, and creating service
 * history when jobs are completed.</p>
 */
public class JobService {

    private JobCardDAO jobCardDAO = new JobCardDAO();
    private ServiceDAO serviceDAO = new ServiceDAO();
    private InventoryDAO inventoryDAO = new InventoryDAO();
    private ServiceHistoryDAO serviceHistoryDAO = new ServiceHistoryDAO();
    private InvoiceDAO invoiceDAO = new InvoiceDAO();

    /**
     * Creates a job card and adds selected services as line items.
     *
     * @param jobCard job card to create
     * @param serviceIds selected service ids (may be null)
     * @param quantities quantities aligned with {@code serviceIds} (may be null → defaults to 1)
     * @return newly created job id, or {@code -1/0} depending on DAO failure
     */
    public int createJobWithServices(JobCard jobCard, int[] serviceIds, int[] quantities) {
        int jobId = jobCardDAO.createJobCard(jobCard);
        if (jobId > 0 && serviceIds != null) {
            double subtotal = 0;
            for (int i = 0; i < serviceIds.length; i++) {
                Service s = serviceDAO.getServiceById(serviceIds[i]);
                if (s != null) {
                    int qty = (quantities != null && i < quantities.length) ? quantities[i] : 1;
                    jobCardDAO.addJobService(jobId, s.getServiceId(), qty, s.getBasePrice());
                    subtotal += s.getBasePrice() * qty;
                }
            }
            jobCardDAO.updateJobTotal(jobId, subtotal);
        }
        return jobId;
    }

    /**
     * Adds parts to an existing job and decreases inventory stock accordingly.
     *
     * @param jobId job card id
     * @param partIds selected part ids
     * @param quantities quantities aligned with {@code partIds} (may be null → defaults to 1)
     * @param performedBy user id performing the operation (for inventory auditing)
     * @return {@code true} if the operation executed (individual missing parts are skipped)
     */
    public boolean createJobWithParts(int jobId, int[] partIds, int[] quantities, int performedBy) {
        if (jobId <= 0 || partIds == null) return false;
        for (int i = 0; i < partIds.length; i++) {
            SparePart part = inventoryDAO.getSparePartById(partIds[i]);
            if (part != null) {
                int qty = (quantities != null && i < quantities.length) ? quantities[i] : 1;
                if (part.getQuantityInStock() >= qty) {
                    jobCardDAO.addJobPart(jobId, part.getPartId(), qty, part.getUnitPrice());
                    inventoryDAO.restockPart(part.getPartId(), -qty, performedBy);
                }
            }
        }
        double currentTotal = calculateJobTotal(jobId);
        jobCardDAO.updateJobTotal(jobId, currentTotal);
        return true;
    }

    /**
     * Calculates the current total for a job by summing service and parts subtotals.
     *
     * @param jobId job card id
     * @return total amount
     */
    public double calculateJobTotal(int jobId) {
        double total = 0;
        for (Map<String, Object> s : jobCardDAO.getJobServices(jobId)) {
            Number val = (Number) s.get("subtotal");
            if (val != null) total += val.doubleValue();
        }
        for (Map<String, Object> p : jobCardDAO.getJobParts(jobId)) {
            Number val = (Number) p.get("subtotal");
            if (val != null) total += val.doubleValue();
        }
        return total;
    }

    /**
     * Updates job status and triggers service history creation when completed.
     *
     * @param jobId job card id
     * @param status new status
     * @return {@code true} if the status was updated
     */
    public boolean updateJobStatus(int jobId, String status) {
        boolean updated = jobCardDAO.updateJobStatus(jobId, status);
        if (updated) {
            if ("completed".equalsIgnoreCase(status)) {
                serviceHistoryDAO.createIfMissing(jobId);
            } else if ("cancelled".equalsIgnoreCase(status)) {
                // Auto-refund any paid invoice linked to this job
                invoiceDAO.refundByJobId(jobId);
            }
        }
        return updated;
    }
}
