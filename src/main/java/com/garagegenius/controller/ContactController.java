package com.garagegenius.controller;

import com.garagegenius.dao.ContactDAO;
import com.garagegenius.model.Contact;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;

/**
 * Public contact form controller.
 *
 * <p>GET renders the contact page and displays any flash-style success/error message stored
 * in session. POST creates a {@link Contact} record via {@link ContactDAO} and redirects back
 * to the contact page.</p>
 *
 * <p>Mapped to {@code /contact}.</p>
 */
public class ContactController extends HttpServlet {

    private ContactDAO contactDAO;

    @Override
    public void init() throws ServletException {
        contactDAO = new ContactDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Object success = request.getSession().getAttribute("successMessage");
        if (success != null) {
            request.setAttribute("successMessage", success);
            request.getSession().removeAttribute("successMessage");
        }
        Object error = request.getSession().getAttribute("errorMessage");
        if (error != null) {
            request.setAttribute("errorMessage", error);
            request.getSession().removeAttribute("errorMessage");
        }
        request.getRequestDispatcher("/views/public/contact.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Contact c = new Contact();
        c.setName(request.getParameter("name"));
        c.setEmail(request.getParameter("email"));
        c.setPhone(request.getParameter("phone"));
        c.setSubject(request.getParameter("subject"));
        c.setMessage(request.getParameter("message"));

        if (contactDAO.saveContact(c)) {
            request.getSession().setAttribute("successMessage", "Thank you for contacting us! We will get back to you soon.");
        } else {
            request.getSession().setAttribute("errorMessage", "Oops! Something went wrong. Please try again.");
        }
        response.sendRedirect(request.getContextPath() + "/contact");
    }
}
