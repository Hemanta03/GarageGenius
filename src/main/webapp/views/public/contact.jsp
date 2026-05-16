<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Contact Us" scope="request" />
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/navbar.jsp" />

<div class="app-body">
    <main class="main-content">
        <div class="card auth-container" style="margin-top: 2rem;">
            <h2>Contact Us</h2>

            <c:if test="${not empty successMessage}">
                <div class="alert alert-success">
                    <c:out value="${successMessage}" />
                </div>
            </c:if>

            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger">
                    <c:out value="${errorMessage}" />
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/contact" method="POST">
                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                
                <div class="form-group">
                    <label class="form-label">Name:</label>
                    <input type="text" name="name" class="form-control" required>
                </div>
                <div class="form-group">
                    <label class="form-label">Email:</label>
                    <input type="email" name="email" class="form-control" required>
                </div>
                <div class="form-group">
                    <label class="form-label">Phone (optional):</label>
                    <input type="text" name="phone" class="form-control">
                </div>
                <div class="form-group">
                    <label class="form-label">Subject:</label>
                    <input type="text" name="subject" class="form-control" required>
                </div>
                <div class="form-group">
                    <label class="form-label">Message:</label>
                    <textarea name="message" rows="5" class="form-control" required></textarea>
                </div>
                
                <button type="submit" class="btn" style="width: 100%; margin-top: 1rem;">Send Message</button>
            </form>

            <p style="margin-top: 2rem; text-align: center;"><a href="${pageContext.request.contextPath}/" class="btn btn-secondary">Back to Home</a></p>
        </div>

<jsp:include page="/views/common/footer.jsp" />
