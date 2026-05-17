package com.garagegenius.controller.customer;

import com.garagegenius.dao.CustomerDAO;
import com.garagegenius.dao.ServiceHistoryDAO;
import com.garagegenius.model.Customer;
import com.garagegenius.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Customer service history controller.
 *
 * <p>Displays service history entries associated with the logged-in customer.</p>
 *
 * <p>Mapped to {@code /customer/history}.</p>
 */
public class CustomerHistoryController extends HttpServlet {

    private CustomerDAO customerDAO;
    private ServiceHistoryDAO serviceHistoryDAO;

    @Override
    public void init() throws ServletException {
        customerDAO = new CustomerDAO();
        serviceHistoryDAO = new ServiceHistoryDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Integer userId = SessionUtil.getLoggedInUserId(request);
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Customer c = customerDAO.getCustomerByUserId(userId);
        if (c == null) {
            response.sendRedirect(request.getContextPath() + "/customer/dashboard");
            return;
        }

        request.setAttribute("history", serviceHistoryDAO.getByCustomerId(c.getCustomerId()));
        request.setAttribute("pageTitle", "Service History");
        request.getRequestDispatcher("/views/customer/history.jsp").forward(request, response);
    }
}

