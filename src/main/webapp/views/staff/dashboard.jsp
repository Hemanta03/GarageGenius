<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Staff Dashboard" scope="request" />
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/navbar.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />

<h2>Staff Dashboard</h2>

<h3>My Assigned Jobs</h3>
<c:choose>
    <c:when test="${not empty jobs}">
        <table border="1">
            <thead>
                <tr>
                    <th>Job ID</th>
                    <th>Customer</th>
                    <th>Vehicle</th>
                    <th>Status</th>
                    <th>Date Created</th>
                    <th>Total Amount</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="job" items="${jobs}">
                    <tr>
                        <td>#<c:out value="${job.jobId}" /></td>
                        <td><c:out value="${job.customerName}" /></td>
                        <td><c:out value="${job.vehicleMake}" /> <c:out value="${job.vehicleModel}" /> (<c:out value="${job.licensePlate}" />)</td>
                        <td><c:out value="${job.status}" /></td>
                        <td><c:out value="${job.createdDate}" /></td>
                        <td>$<c:out value="${job.totalAmount}" /></td>
                        <td>
                            <a href="${pageContext.request.contextPath}/staff/jobcards?action=view&id=${job.jobId}">View Details</a>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </c:when>
    <c:otherwise>
        <p>No jobs assigned to you yet.</p>
    </c:otherwise>
</c:choose>

<hr>

<div>
    <h3>Quick Links</h3>
    <ul>
        <li><a href="${pageContext.request.contextPath}/staff/inventory">View Inventory</a></li>
    </ul>
</div>

<jsp:include page="/views/common/footer.jsp" />

