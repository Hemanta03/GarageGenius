package com.garagegenius.model;

/**
 * Service catalog item model.
 *
 * <p>Represents a billable service (repair/maintenance/etc.) that can be added to a job card.</p>
 */
public class Service {

    private int serviceId;
    private String serviceName;
    private String description;
    private double basePrice;
    private double estimatedDurationHrs;
    private String category;

    public Service() {}

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public double getEstimatedDurationHrs() {
        return estimatedDurationHrs;
    }

    public void setEstimatedDurationHrs(double estimatedDurationHrs) {
        this.estimatedDurationHrs = estimatedDurationHrs;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}