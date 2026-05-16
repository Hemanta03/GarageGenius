package com.garagegenius.controller.admin;

import com.garagegenius.dao.VehicleDAO;
import com.garagegenius.dao.CustomerDAO;
import com.garagegenius.model.Vehicle;
import com.garagegenius.model.Customer;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Admin controller for vehicle records.
 *
 * <p>Provides CRUD operations for vehicles and links vehicles to customers. Enforces unique
 * license plate numbers during creation.</p>
 *
 * <p>Mapped to {@code /admin/vehicles}.</p>
 */
public class VehicleController extends HttpServlet {

    private VehicleDAO vehicleDAO;
    private CustomerDAO customerDAO;

    @Override
    public void init() throws ServletException {
        vehicleDAO = new VehicleDAO();
        customerDAO = new CustomerDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "add":
                request.setAttribute("customers", customerDAO.getAllCustomers());
                request.getRequestDispatcher("/views/admin/vehicles/add.jsp").forward(request, response);
                break;
            case "edit":
                int id = Integer.parseInt(request.getParameter("id"));
                request.setAttribute("vehicle", vehicleDAO.getVehicleById(id));
                request.setAttribute("customers", customerDAO.getAllCustomers());
                request.getRequestDispatcher("/views/admin/vehicles/edit.jsp").forward(request, response);
                break;
            case "delete":
                response.sendRedirect(request.getContextPath() + "/admin/vehicles?error=delete_requires_post");
                break;
            case "list":
            default:
                listVehicles(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("create".equals(action)) {
            createVehicle(request, response);
        } else if ("update".equals(action)) {
            updateVehicle(request, response);
        } else if ("delete".equals(action)) {
            deleteVehicle(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/vehicles");
        }
    }

    private void createVehicle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String plate = request.getParameter("licensePlate");
        if (vehicleDAO.licensePlateExists(plate)) {
            response.sendRedirect(request.getContextPath() + "/admin/vehicles?action=add&error=plate_exists");
            return;
        }

        Vehicle v = new Vehicle();
        v.setCustomerId(Integer.parseInt(request.getParameter("customerId")));
        v.setLicensePlate(plate);
        v.setMake(request.getParameter("make"));
        v.setModel(request.getParameter("model"));
        v.setYear(Integer.parseInt(request.getParameter("year")));
        v.setColor(request.getParameter("color"));
        v.setFuelType(request.getParameter("fuelType"));
        
        String vin = request.getParameter("vinNumber");
        v.setVinNumber(vin != null && !vin.trim().isEmpty() ? vin : "");
        
        String mileageStr = request.getParameter("mileage");
        v.setMileage(mileageStr != null && !mileageStr.trim().isEmpty() ? Integer.parseInt(mileageStr) : 0);

        if (vehicleDAO.addVehicle(v) > 0) {
            response.sendRedirect(request.getContextPath() + "/admin/vehicles?success=created");
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/vehicles?error=failed");
        }
    }

    private void updateVehicle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        Vehicle v = vehicleDAO.getVehicleById(id);
        if (v != null) {
            v.setLicensePlate(request.getParameter("licensePlate"));
            v.setMake(request.getParameter("make"));
            v.setModel(request.getParameter("model"));
            v.setYear(Integer.parseInt(request.getParameter("year")));
            v.setColor(request.getParameter("color"));
            v.setFuelType(request.getParameter("fuelType"));
            
            String vin = request.getParameter("vinNumber");
            v.setVinNumber(vin != null && !vin.trim().isEmpty() ? vin : "");
            
            String mileageStr = request.getParameter("mileage");
            v.setMileage(mileageStr != null && !mileageStr.trim().isEmpty() ? Integer.parseInt(mileageStr) : 0);
            if (vehicleDAO.updateVehicle(v)) {
                response.sendRedirect(request.getContextPath() + "/admin/vehicles?success=updated");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/vehicles?error=failed");
            }
        }
    }

    private void listVehicles(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Vehicle> list = vehicleDAO.getAllVehicles();
        request.setAttribute("vehicles", list);
        request.setAttribute("pageTitle", "Manage Vehicles");
        request.getRequestDispatcher("/views/admin/vehicles/list.jsp").forward(request, response);
    }

    private void deleteVehicle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        vehicleDAO.deleteVehicle(Integer.parseInt(request.getParameter("id")));
        response.sendRedirect(request.getContextPath() + "/admin/vehicles?success=deleted");
    }
}
