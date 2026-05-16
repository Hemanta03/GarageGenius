package com.garagegenius.controller.staff;

import com.garagegenius.dao.InventoryDAO;
import com.garagegenius.dao.JobCardDAO;
import com.garagegenius.model.JobCard;
import com.garagegenius.model.SparePart;
import com.garagegenius.service.JobService;
import com.garagegenius.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Staff controller for viewing and updating assigned job cards.
 *
 * <p>Allows staff to view a job card they are assigned to, update its status, and record parts
 * used (which adjusts inventory and updates the job total).</p>
 *
 * <p>Mapped to {@code /staff/jobcards}.</p>
 */
public class StaffJobCardController extends HttpServlet {

    private JobCardDAO jobCardDAO;
    private InventoryDAO inventoryDAO;
    private JobService jobService;

    @Override
    public void init() throws ServletException {
        jobCardDAO = new JobCardDAO();
        inventoryDAO = new InventoryDAO();
        jobService = new JobService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("view".equals(action)) {
            int id = Integer.parseInt(request.getParameter("id"));
            JobCard job = jobCardDAO.getJobCardById(id);
            Integer staffId = SessionUtil.getLoggedInUserId(request);
            if (job == null || staffId == null || job.getAssignedStaffId() != staffId) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied.");
                return;
            }
            request.setAttribute("job", job);
            request.setAttribute("services", jobCardDAO.getJobServices(id));
            request.setAttribute("parts", jobCardDAO.getJobParts(id));
            request.setAttribute("allParts", inventoryDAO.getAllSpareParts());
            request.getRequestDispatcher("/views/staff/view-job.jsp").forward(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/staff/dashboard");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("updateStatus".equals(action)) {
            int jobId = Integer.parseInt(request.getParameter("id"));
            JobCard job = jobCardDAO.getJobCardById(jobId);
            Integer staffId = SessionUtil.getLoggedInUserId(request);
            if (job == null || staffId == null || job.getAssignedStaffId() != staffId) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied.");
                return;
            }
            String status = request.getParameter("status");
            jobService.updateJobStatus(jobId, status);
            response.sendRedirect(request.getContextPath() + "/staff/dashboard?success=status_updated");
        } else if ("useParts".equals(action)) {
            int jobId = Integer.parseInt(request.getParameter("id"));
            JobCard job = jobCardDAO.getJobCardById(jobId);
            Integer staffId = SessionUtil.getLoggedInUserId(request);
            if (job == null || staffId == null || job.getAssignedStaffId() != staffId) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied.");
                return;
            }

            String[] selected = request.getParameterValues("partId");
            if (selected != null && selected.length > 0) {
                List<Integer> partIdsList = new ArrayList<>();
                List<Integer> quantitiesList = new ArrayList<>();

                for (String partIdStr : selected) {
                    int partId = Integer.parseInt(partIdStr);
                    String qtyParam = request.getParameter("qty_" + partId);
                    int qty = 0;
                    try {
                        qty = Integer.parseInt(qtyParam);
                    } catch (NumberFormatException ignored) {
                    }

                    if (qty > 0) {
                        partIdsList.add(partId);
                        quantitiesList.add(qty);
                    }
                }

                int[] partIds = partIdsList.stream().mapToInt(Integer::intValue).toArray();
                int[] quantities = quantitiesList.stream().mapToInt(Integer::intValue).toArray();
                if (partIds.length > 0) {
                    jobService.createJobWithParts(jobId, partIds, quantities, staffId);
                }
            }

            response.sendRedirect(request.getContextPath() + "/staff/jobcards?action=view&id=" + jobId + "&success=parts_used");
        }
    }
}
