package com.garagegenius.controller.admin;

import com.garagegenius.dao.ServiceDAO;
import com.garagegenius.model.Service;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Admin controller for service catalog management.
 *
 * <p>
 * Handles CRUD for services offered by the garage (name/description/base
 * price/duration/category).
 * </p>
 *
 * <p>
 * Mapped to {@code /admin/services}.
 * </p>
 */
public class ServiceController extends HttpServlet {

    private ServiceDAO serviceDAO;

    @Override
    public void init() throws ServletException {
        serviceDAO = new ServiceDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null)
            action = "list";

        switch (action) {
            case "add":
                request.getRequestDispatcher("/views/admin/services/add.jsp").forward(request, response);
                break;
            case "edit":
                int id = Integer.parseInt(request.getParameter("id"));
                request.setAttribute("service", serviceDAO.getServiceById(id));
                request.getRequestDispatcher("/views/admin/services/edit.jsp").forward(request, response);
                break;
            case "delete":
                response.sendRedirect(request.getContextPath() + "/admin/services?error=delete_requires_post");
                break;
            case "list":
            default:
                listServices(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("create".equals(action)) {
            createService(request, response);
        } else if ("update".equals(action)) {
            updateService(request, response);
        } else if ("delete".equals(action)) {
            deleteService(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/services");
        }
    }

    private void createService(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Service s = new Service();
            s.setServiceName(request.getParameter("serviceName"));
            s.setDescription(request.getParameter("description"));
            s.setBasePrice(Double.parseDouble(request.getParameter("basePrice")));
            s.setEstimatedDurationHrs(Double.parseDouble(request.getParameter("duration")));
            s.setCategory(request.getParameter("category"));

            if (serviceDAO.addService(s) > 0) {
                response.sendRedirect(request.getContextPath() + "/admin/services?success=created");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/services?error=failed");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/services?error=invalid_input");
        }
    }

    private void updateService(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Service s = serviceDAO.getServiceById(id);
            if (s != null) {
                s.setServiceName(request.getParameter("serviceName"));
                s.setDescription(request.getParameter("description"));
                s.setBasePrice(Double.parseDouble(request.getParameter("basePrice")));
                s.setEstimatedDurationHrs(Double.parseDouble(request.getParameter("duration")));
                s.setCategory(request.getParameter("category"));

                if (serviceDAO.updateService(s)) {
                    response.sendRedirect(request.getContextPath() + "/admin/services?success=updated");
                } else {
                    response.sendRedirect(request.getContextPath() + "/admin/services?error=failed");
                }
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/services?error=not_found");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/services?error=invalid_input");
        }
    }

    private void listServices(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Service> services = serviceDAO.getAllServices();
        request.setAttribute("services", services);
        request.setAttribute("pageTitle", "Manage Services");
        request.getRequestDispatcher("/views/admin/services/list.jsp").forward(request, response);
    }

    private void deleteService(HttpServletRequest request, HttpServletResponse response) throws IOException {
        serviceDAO.deleteService(Integer.parseInt(request.getParameter("id")));
        response.sendRedirect(request.getContextPath() + "/admin/services?success=deleted");
    }
}