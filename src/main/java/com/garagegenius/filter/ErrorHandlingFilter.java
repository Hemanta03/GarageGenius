package com.garagegenius.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Defensive error handling wrapper for the entire web application.
 *
 * <p>This filter catches common runtime issues thrown downstream and converts them into
 * appropriate HTTP responses. It is mapped to {@code /*} and therefore applies to all
 * requests.</p>
 */
public class ErrorHandlingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            chain.doFilter(request, response);
        } catch (IllegalArgumentException e) {
            httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request data.");
        } catch (Exception e) {
            throw new ServletException("Unexpected application error", e);
        }
    }

    @Override
    public void destroy() {
    }
}
