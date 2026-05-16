package com.garagegenius.service;

import com.garagegenius.dao.InvoiceDAO;
import com.garagegenius.dao.JobCardDAO;
import com.garagegenius.model.Invoice;
import com.garagegenius.model.JobCard;

import java.time.LocalDate;

/**
 * Billing/invoicing domain logic.
 *
 * <p>Responsible for generating invoices from completed job cards and for basic
 * calculations like tax/total. Persistence is delegated to DAO classes.</p>
 */
public class BillingService {

    private InvoiceDAO invoiceDAO = new InvoiceDAO();
    private JobCardDAO jobCardDAO = new JobCardDAO();

    /**
     * Generates an invoice for a completed job card.
     *
     * @param jobId job card id
     * @param taxRate tax rate percentage (e.g. 13.0 for 13%)
     * @param discount discount amount to apply
     * @return created {@link Invoice}, or {@code null} if job does not exist or is not completed
     */
    public Invoice generateInvoice(int jobId, double taxRate, double discount) {
        JobCard jc = jobCardDAO.getJobCardById(jobId);
        if (jc == null || !"completed".equalsIgnoreCase(jc.getStatus())) return null;

        double subtotal = jc.getTotalAmount();
        double taxAmount = calculateTax(subtotal, taxRate);
        // BUG-06 FIX: cap discount so total cannot be negative
        double maxDiscount = subtotal + taxAmount;
        if (discount > maxDiscount) discount = maxDiscount;
        double total = calculateTotal(subtotal, taxAmount, discount);

        Invoice inv = new Invoice();
        inv.setJobId(jobId);
        inv.setCustomerId(jc.getCustomerId());
        inv.setInvoiceDate(LocalDate.now());
        inv.setDueDate(LocalDate.now().plusDays(7));
        inv.setSubtotal(subtotal);
        inv.setTaxRate(taxRate);
        inv.setTaxAmount(taxAmount);
        inv.setDiscount(discount);
        inv.setTotalAmount(total);
        inv.setPaymentStatus("unpaid");

        int invId = invoiceDAO.createInvoice(inv);
        if (invId > 0) {
            inv.setInvoiceId(invId);
            return inv;
        }
        return null;
    }

    /**
     * Calculates tax amount for a subtotal.
     *
     * @param subtotal subtotal amount
     * @param taxRate tax rate percentage (e.g. 13.0)
     * @return tax amount
     */
    public double calculateTax(double subtotal, double taxRate) {
        return subtotal * (taxRate / 100);
    }

    /**
     * Calculates final total.
     *
     * @param subtotal subtotal
     * @param taxAmount tax amount
     * @param discount discount amount
     * @return total amount
     */
    public double calculateTotal(double subtotal, double taxAmount, double discount) {
        return subtotal + taxAmount - discount;
    }
}
