package com.garagegenius.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Job card/work order model.
 *
 * <p>A job card links a customer and vehicle to assigned staff, status, notes, mileage, and
 * total billing amount. Some fields (customer/staff names, vehicle details) are populated via
 * joined queries for display.</p>
 */
public class JobCard {

    private int jobId;
    private int vehicleId;
    private int customerId;
    private int assignedStaffId;
    private LocalDateTime createdDate;
    private LocalDate estimatedCompletion;
    private LocalDate actualCompletion;
    private String status;
    private double totalAmount;
    private String notes;
    private int mileageAtService;

    private String customerName;
    private String staffName;
    private String licensePlate;
    private String vehicleMake;
    private String vehicleModel;

    public JobCard() {}

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getAssignedStaffId() {
        return assignedStaffId;
    }

    public void setAssignedStaffId(int assignedStaffId) {
        this.assignedStaffId = assignedStaffId;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDate getEstimatedCompletion() {
        return estimatedCompletion;
    }

    public void setEstimatedCompletion(LocalDate estimatedCompletion) {
        this.estimatedCompletion = estimatedCompletion;
    }

    public LocalDate getActualCompletion() {
        return actualCompletion;
    }

    public void setActualCompletion(LocalDate actualCompletion) {
        this.actualCompletion = actualCompletion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getMileageAtService() {
        return mileageAtService;
    }

    public void setMileageAtService(int mileageAtService) {
        this.mileageAtService = mileageAtService;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getVehicleMake() {
        return vehicleMake;
    }

    public void setVehicleMake(String vehicleMake) {
        this.vehicleMake = vehicleMake;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }
}