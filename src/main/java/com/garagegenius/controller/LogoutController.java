package com.garagegenius.controller;

import com.garagegenius.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Logs out the current user by invalidating the HTTP session.
 *
 * <p>Mapped to {@code /logout}. After logout, users are redirected to {@code /login}.</p>
 */
public class LogoutController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        SessionUtil.destroySession(request);
        response.sendRedirect(request.getContextPath() + "/login");
    }
}