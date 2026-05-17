package com.garagegenius.controller.customer;

import com.garagegenius.dao.CustomerDAO;
import com.garagegenius.dao.JobCardDAO;
import com.garagegenius.dao.VehicleDAO;
import com.garagegenius.model.Customer;
import com.garagegenius.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Customer dashboard controller.
 *
 * <p>Loads the logged-in customer's profile and related data (vehicles and job cards) for the
 * customer portal landing page.</p>
 *
 * <p>Mapped to {@code /customer/dashboard}.</p>
 */
public class CustomerPortalController extends HttpServlet {

    private CustomerDAO customerDAO;
    private JobCardDAO jobCardDAO;
    private VehicleDAO vehicleDAO;

    @Override
    public void init() throws ServletException {
        customerDAO = new CustomerDAO();
        jobCardDAO = new JobCardDAO();
        vehicleDAO = new VehicleDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int userId = SessionUtil.getLoggedInUserId(request);
        Customer c = customerDAO.getCustomerByUserId(userId);
        
        if (c != null) {
            request.setAttribute("customer", c);
            request.setAttribute("vehicles", vehicleDAO.getVehiclesByCustomerId(c.getCustomerId()));
            request.setAttribute("jobs", jobCardDAO.getJobCardsByCustomerId(c.getCustomerId()));
        }
        
        request.setAttribute("pageTitle", "My Dashboard");
        request.getRequestDispatcher("/views/customer/dashboard.jsp").forward(request, response);
    }
}
