package com.garagegenius.controller.admin;

import com.garagegenius.dao.ContactDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import jakarta.servlet.annotation.WebServlet;

/**
 * Admin controller for the public contact inbox.
 *
 * <p>Lists contact form submissions and allows updating message status (new/read/replied).</p>
 *
 * <p>Mapped to {@code /admin/contacts}.</p>
 */
public class ContactAdminController extends HttpServlet {

    private ContactDAO contactDAO;

    @Override
    public void init() throws ServletException {
        contactDAO = new ContactDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("contacts", contactDAO.getAllContacts());
        request.setAttribute("pageTitle", "Contact Inbox");
        request.getRequestDispatcher("/views/admin/contacts/list.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        if ("status".equals(action)) {
            int id = Integer.parseInt(request.getParameter("id"));
            String status = request.getParameter("status");
            contactDAO.updateContactStatus(id, status);
        }
        response.sendRedirect(request.getContextPath() + "/admin/contacts");
    }
}

