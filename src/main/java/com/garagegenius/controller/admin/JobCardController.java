package com.garagegenius.controller.admin;

import com.garagegenius.dao.JobCardDAO;
import com.garagegenius.dao.CustomerDAO;
import com.garagegenius.dao.VehicleDAO;
import com.garagegenius.dao.UserDAO;
import com.garagegenius.dao.ServiceDAO;
import com.garagegenius.dao.InventoryDAO;
import com.garagegenius.dao.ServiceHistoryDAO;
import com.garagegenius.service.JobService;
import com.garagegenius.model.JobCard;
import com.garagegenius.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Admin controller for job cards.
 *
 * <p>Creates jobs (assigning staff, selecting services), edits jobs (status/notes/mileage),
 * attaches parts used, and displays job card details including service/parts line items.</p>
 *
 * <p>Mapped to {@code /admin/jobcards}.</p>
 */
public class JobCardController extends HttpServlet {

    private JobCardDAO jobCardDAO;
    private CustomerDAO customerDAO;
    private VehicleDAO vehicleDAO;
    private UserDAO userDAO;
    private ServiceDAO serviceDAO;
    private InventoryDAO inventoryDAO;
    private JobService jobService;
    private ServiceHistoryDAO serviceHistoryDAO;

    @Override
    public void init() throws ServletException {
        jobCardDAO = new JobCardDAO();
        customerDAO = new CustomerDAO();
        vehicleDAO = new VehicleDAO();
        userDAO = new UserDAO();
        serviceDAO = new ServiceDAO();
        inventoryDAO = new InventoryDAO();
        jobService = new JobService();
        serviceHistoryDAO = new ServiceHistoryDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "add":
                request.setAttribute("customers", customerDAO.getAllCustomers());
                request.setAttribute("vehicles", vehicleDAO.getAllVehicles());
                request.setAttribute("staff", userDAO.getUsersByRole("staff"));
                request.setAttribute("services", serviceDAO.getAllServices());
                request.getRequestDispatcher("/views/admin/jobcards/add.jsp").forward(request, response);
                break;
            case "view":
                viewJobCard(request, response);
                break;
            case "edit":
                try {
                    int id = Integer.parseInt(request.getParameter("id"));
                    request.setAttribute("jobCard", jobCardDAO.getJobCardById(id));
                    request.setAttribute("staff", userDAO.getUsersByRole("staff"));
                    request.setAttribute("services", serviceDAO.getAllServices());
                    request.setAttribute("parts", inventoryDAO.getAllSpareParts());
                    request.getRequestDispatcher("/views/admin/jobcards/edit.jsp").forward(request, response);
                } catch (NumberFormatException e) {
                    response.sendRedirect(request.getContextPath() + "/admin/jobcards?error=invalid_id");
                }
                break;
            case "status":
                response.sendRedirect(request.getContextPath() + "/admin/jobcards?error=status_requires_post");
                break;
            case "list":
            default:
                listJobCards(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("create".equals(action)) {
            createJobCard(request, response);
        } else if ("update".equals(action)) {
            updateJobCard(request, response);
        } else if ("addParts".equals(action)) {
            addPartsToJob(request, response);
        } else if ("status".equals(action)) {
            updateStatus(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/jobcards");
        }
    }

    private void createJobCard(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int customerId = Integer.parseInt(request.getParameter("customerId"));
            int vehicleId = Integer.parseInt(request.getParameter("vehicleId"));
            
            // Validate that the vehicle belongs to the selected customer
            com.garagegenius.model.Vehicle v = vehicleDAO.getVehicleById(vehicleId);
            if (v == null || v.getCustomerId() != customerId) {
                response.sendRedirect(request.getContextPath() + "/admin/jobcards?action=add&error=vehicle_mismatch");
                return;
            }

            JobCard jc = new JobCard();
            jc.setCustomerId(customerId);
            jc.setVehicleId(vehicleId);
            jc.setAssignedStaffId(Integer.parseInt(request.getParameter("staffId")));
            jc.setMileageAtService(Integer.parseInt(request.getParameter("mileage")));
            jc.setNotes(request.getParameter("notes"));
            jc.setStatus("pending");

            String[] serviceIdsStr = request.getParameterValues("services");
            int[] serviceIds = null;
            if (serviceIdsStr != null) {
                serviceIds = new int[serviceIdsStr.length];
                for (int i = 0; i < serviceIdsStr.length; i++) {
                    serviceIds[i] = Integer.parseInt(serviceIdsStr[i]);
                }
            }

            int jobId = jobService.createJobWithServices(jc, serviceIds, null);
            if (jobId > 0) {
                response.sendRedirect(request.getContextPath() + "/admin/jobcards?success=created");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/jobcards?error=failed");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/jobcards?error=invalid_input");
        }
    }

    private void updateJobCard(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            JobCard jc = jobCardDAO.getJobCardById(id);
            if (jc != null) {
                jc.setAssignedStaffId(Integer.parseInt(request.getParameter("staffId")));
                jc.setStatus(request.getParameter("status"));
                jc.setNotes(request.getParameter("notes"));
                jc.setMileageAtService(Integer.parseInt(request.getParameter("mileage")));
                String totalStr = request.getParameter("total");
                if (totalStr != null && !totalStr.trim().isEmpty()) {
                    jc.setTotalAmount(Double.parseDouble(totalStr));
                }
                
                if (jobCardDAO.updateJobCard(jc)) {
                    if ("completed".equalsIgnoreCase(jc.getStatus())) {
                        serviceHistoryDAO.createIfMissing(id);
                    }
                    response.sendRedirect(request.getContextPath() + "/admin/jobcards?success=updated");
                } else {
                    response.sendRedirect(request.getContextPath() + "/admin/jobcards?error=failed");
                }
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/jobcards?error=not_found");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/jobcards?error=invalid_input");
        }
    }

    private void addPartsToJob(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int jobId = Integer.parseInt(request.getParameter("id"));
            // BUG-01 FIX: pass actual logged-in userId so inventory_log FK is satisfied
            Integer performedBy = SessionUtil.getLoggedInUserId(request);
            if (performedBy == null) performedBy = 0;

            String[] partIdsStr = request.getParameterValues("parts");
            if (partIdsStr != null) {
                int[] partIds = new int[partIdsStr.length];
                for (int i = 0; i < partIdsStr.length; i++) {
                    partIds[i] = Integer.parseInt(partIdsStr[i]);
                }
                jobService.createJobWithParts(jobId, partIds, null, performedBy);
            }
            response.sendRedirect(request.getContextPath() + "/admin/jobcards?action=edit&id=" + jobId + "&success=parts_added");
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/jobcards?error=invalid_input");
        }
    }

    private void listJobCards(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<JobCard> list = jobCardDAO.getAllJobCards();
        request.setAttribute("jobCards", list);
        request.setAttribute("pageTitle", "Manage Job Cards");
        request.getRequestDispatcher("/views/admin/jobcards/list.jsp").forward(request, response);
    }

    private void viewJobCard(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            request.setAttribute("jobCard", jobCardDAO.getJobCardById(id));
            request.setAttribute("services", jobCardDAO.getJobServices(id));
            request.setAttribute("parts", jobCardDAO.getJobParts(id));
            request.getRequestDispatcher("/views/admin/jobcards/view.jsp").forward(request, response);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/jobcards?error=invalid_id");
        }
    }

    private void updateStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String status = request.getParameter("newStatus");
            jobService.updateJobStatus(id, status);
            response.sendRedirect(request.getContextPath() + "/admin/jobcards");
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/jobcards?error=invalid_id");
        }
    }
}
