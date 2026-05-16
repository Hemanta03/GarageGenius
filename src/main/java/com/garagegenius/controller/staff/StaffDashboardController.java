package com.garagegenius.controller.staff;

import com.garagegenius.dao.JobCardDAO;
import com.garagegenius.model.JobCard;
import com.garagegenius.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Staff landing page controller.
 *
 * <p>Shows the logged-in staff member only the job cards assigned to them.</p>
 *
 * <p>Mapped to {@code /staff/dashboard}.</p>
 */
public class StaffDashboardController extends HttpServlet {

    private JobCardDAO jobCardDAO;

    @Override
    public void init() throws ServletException {
        jobCardDAO = new JobCardDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int staffId = SessionUtil.getLoggedInUserId(request);
        List<JobCard> myJobs = jobCardDAO.getJobCardsByStaffId(staffId);
        
        request.setAttribute("jobs", myJobs);
        request.setAttribute("pageTitle", "Staff Dashboard");
        request.getRequestDispatcher("/views/staff/dashboard.jsp").forward(request, response);
    }
}