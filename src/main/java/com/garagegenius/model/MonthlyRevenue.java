package com.garagegenius.model;

/**
 * Simple DTO for monthly revenue reporting.
 *
 * <p>Populated by aggregate invoice queries (month, invoice count, total revenue).</p>
 */
public class MonthlyRevenue {
    private String month;
    private int invoiceCount;
    private double totalRevenue;

    public MonthlyRevenue() {}

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public int getInvoiceCount() {
        return invoiceCount;
    }

    public void setInvoiceCount(int invoiceCount) {
        this.invoiceCount = invoiceCount;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
