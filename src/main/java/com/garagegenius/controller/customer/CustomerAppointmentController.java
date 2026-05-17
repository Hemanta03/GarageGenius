package com.garagegenius.controller.customer;

import com.garagegenius.dao.AppointmentDAO;
import com.garagegenius.dao.CustomerDAO;
import com.garagegenius.dao.ServiceDAO;
import com.garagegenius.dao.VehicleDAO;
import com.garagegenius.model.Appointment;
import com.garagegenius.model.Customer;
import com.garagegenius.model.Vehicle;
import com.garagegenius.util.SessionUtil;
import com.garagegenius.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Customer appointments controller.
 *
 * <p>Allows customers to request new appointments for their own vehicles/services and to view/cancel
 * previously requested appointments. If a customer has no vehicles, the request form also allows
 * them to register a vehicle inline in the same step.</p>
 *
 * <p>Mapped to {@code /customer/appointments}.</p>
 */
public class CustomerAppointmentController extends HttpServlet {

    private static final DateTimeFormatter DB_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private AppointmentDAO appointmentDAO;
    private CustomerDAO customerDAO;
    private VehicleDAO vehicleDAO;
    private ServiceDAO serviceDAO;

    @Override
    public void init() throws ServletException {
        appointmentDAO = new AppointmentDAO();
        customerDAO = new CustomerDAO();
        vehicleDAO = new VehicleDAO();
        serviceDAO = new ServiceDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Customer customer = getLoggedInCustomer(request);
        if (customer == null) {
            response.sendRedirect(request.getContextPath() + "/customer/dashboard");
            return;
        }

        String action = request.getParameter("action");
        if ("new".equals(action)) {
            List<Vehicle> vehicles = vehicleDAO.getVehiclesByCustomerId(customer.getCustomerId());
            request.setAttribute("vehicles", vehicles);
            request.setAttribute("services", serviceDAO.getAllServices());
            request.setAttribute("noVehicles", vehicles.isEmpty());
            request.setAttribute("pageTitle", "Request Appointment");
            request.getRequestDispatcher("/views/customer/appointments/request.jsp").forward(request, response);
            return;
        }

        request.setAttribute("appointments", appointmentDAO.getAppointmentsByCustomerId(customer.getCustomerId()));
        request.setAttribute("pageTitle", "My Appointments");
        request.getRequestDispatcher("/views/customer/appointments/list.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Customer customer = getLoggedInCustomer(request);
        if (customer == null) {
            response.sendRedirect(request.getContextPath() + "/customer/dashboard");
            return;
        }

        String action = request.getParameter("action");
        if ("create".equals(action)) {
            createAppointment(request, response, customer);
        } else if ("addVehicleOnly".equals(action)) {
            addVehicleOnly(request, response, customer);
        } else if ("cancel".equals(action)) {
            cancelAppointment(request, response, customer);
        } else {
            response.sendRedirect(request.getContextPath() + "/customer/appointments");
        }
    }

    /** Standard flow: customer already has vehicles, picks from dropdown. */
    private void createAppointment(HttpServletRequest request, HttpServletResponse response, Customer customer)
            throws IOException, ServletException {

        String dateValue = request.getParameter("requestedDate");
        String preferredTime = request.getParameter("preferredTime");
        String notes = request.getParameter("notes");

        if (ValidationUtil.isEmpty(dateValue) || ValidationUtil.isEmpty(preferredTime)) {
            forwardRequestFormWithError(request, response, customer, "Please choose a date and preferred time.");
            return;
        }

        LocalDate requestedDate = LocalDate.parse(dateValue);
        if (requestedDate.isBefore(LocalDate.now())) {
            forwardRequestFormWithError(request, response, customer, "Appointment date cannot be in the past.");
            return;
        }

        String normalizedTime;
        try {
            LocalTime time = LocalTime.parse(preferredTime);
            normalizedTime = time.format(DB_TIME_FORMAT);
        } catch (DateTimeParseException e) {
            forwardRequestFormWithError(request, response, customer, "Invalid preferred time.");
            return;
        }

        int vehicleId = Integer.parseInt(request.getParameter("vehicleId"));
        Vehicle vehicle = vehicleDAO.getVehicleById(vehicleId);
        if (vehicle == null || vehicle.getCustomerId() != customer.getCustomerId()) {
            forwardRequestFormWithError(request, response, customer, "Please choose one of your registered vehicles.");
            return;
        }

        bookAppointment(request, response, customer, vehicleId, normalizedTime, requestedDate, notes);
    }

    /** New flow: customer has no vehicles — register vehicle only, then redirect to appointment form. */
    private void addVehicleOnly(HttpServletRequest request, HttpServletResponse response, Customer customer)
            throws IOException, ServletException {

        // --- Vehicle fields ---
        String make = ValidationUtil.sanitize(request.getParameter("make"));
        String model = ValidationUtil.sanitize(request.getParameter("vmodel"));
        String yearStr = request.getParameter("year");
        String licensePlate = ValidationUtil.sanitize(request.getParameter("licensePlate"));
        String color = ValidationUtil.sanitize(request.getParameter("color"));
        String fuelType = request.getParameter("fuelType");
        String vinNumber = ValidationUtil.sanitize(request.getParameter("vinNumber"));

        if (ValidationUtil.isEmpty(make) || ValidationUtil.isEmpty(model)
                || ValidationUtil.isEmpty(yearStr) || ValidationUtil.isEmpty(licensePlate)
                || ValidationUtil.isEmpty(fuelType)) {
            forwardRequestFormWithError(request, response, customer,
                    "Please fill in all required vehicle fields (Make, Model, Year, License Plate, Fuel Type).");
            return;
        }

        if (vehicleDAO.licensePlateExists(licensePlate)) {
            forwardRequestFormWithError(request, response, customer,
                    "That license plate is already registered. Please check and try again.");
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException e) {
            forwardRequestFormWithError(request, response, customer, "Invalid vehicle year.");
            return;
        }

        // --- Save vehicle ---
        Vehicle v = new Vehicle();
        v.setCustomerId(customer.getCustomerId());
        v.setMake(make);
        v.setModel(model);
        v.setYear(year);
        v.setLicensePlate(licensePlate);
        v.setColor(color != null ? color : "");
        v.setVinNumber(vinNumber != null ? vinNumber : "");
        v.setMileage(0);
        v.setFuelType(fuelType);

        int newVehicleId = vehicleDAO.addVehicle(v);
        if (newVehicleId < 0) {
            forwardRequestFormWithError(request, response, customer, "Could not save vehicle. Please try again.");
            return;
        }

        // Redirect back to the appointment page, now they will have a vehicle
        response.sendRedirect(request.getContextPath() + "/customer/appointments?action=new&success=vehicle_added");
    }

    /** Shared helper: saves the appointment record and redirects or returns error. */
    private void bookAppointment(HttpServletRequest request, HttpServletResponse response,
                                 Customer customer, int vehicleId, String normalizedTime,
                                 LocalDate requestedDate, String notes) throws IOException, ServletException {
        Appointment appointment = new Appointment();
        appointment.setCustomerId(customer.getCustomerId());
        appointment.setVehicleId(vehicleId);
        appointment.setServiceId(Integer.parseInt(request.getParameter("serviceId")));
        appointment.setRequestedDate(requestedDate);
        appointment.setPreferredTime(normalizedTime);
        appointment.setNotes(ValidationUtil.sanitize(notes));
        appointment.setStatus("pending");

        if (appointmentDAO.createAppointment(appointment) > 0) {
            response.sendRedirect(request.getContextPath() + "/customer/appointments?success=requested");
        } else {
            forwardRequestFormWithError(request, response, customer, "Appointment request could not be saved.");
        }
    }

    private void cancelAppointment(HttpServletRequest request, HttpServletResponse response, Customer customer)
            throws IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        appointmentDAO.cancelCustomerAppointment(id, customer.getCustomerId());
        response.sendRedirect(request.getContextPath() + "/customer/appointments?success=cancelled");
    }

    private void forwardRequestFormWithError(HttpServletRequest request, HttpServletResponse response,
                                              Customer customer, String message)
            throws ServletException, IOException {
        List<Vehicle> vehicles = vehicleDAO.getVehiclesByCustomerId(customer.getCustomerId());
        request.setAttribute("errorMessage", message);
        request.setAttribute("vehicles", vehicles);
        request.setAttribute("services", serviceDAO.getAllServices());
        request.setAttribute("noVehicles", vehicles.isEmpty());
        request.setAttribute("pageTitle", "Request Appointment");
        request.getRequestDispatcher("/views/customer/appointments/request.jsp").forward(request, response);
    }

    private Customer getLoggedInCustomer(HttpServletRequest request) {
        Integer userId = SessionUtil.getLoggedInUserId(request);
        return userId == null ? null : customerDAO.getCustomerByUserId(userId);
    }
}
