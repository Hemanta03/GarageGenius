<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Staff Job View" scope="request" />
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/navbar.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />

<h2>Job Card #<c:out value="${job.jobId}" /></h2>

<div>
    <p><strong>Vehicle:</strong> <c:out value="${job.vehicleMake}" /> <c:out value="${job.vehicleModel}" /> (<c:out value="${job.licensePlate}" />)</p>
    <p><strong>Mileage:</strong> <c:out value="${job.mileageAtService}" /></p>
    <p><strong>Notes:</strong> <c:out value="${job.notes}" /></p>
    <p><strong>Current Status:</strong> <c:out value="${job.status}" /></p>
    <p><a href="${pageContext.request.contextPath}/staff/messages?action=compose&jobId=${job.jobId}">Message Admin about this job</a></p>
</div>

<form action="${pageContext.request.contextPath}/staff/jobcards?action=updateStatus" method="POST">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <input type="hidden" name="id" value="${job.jobId}">
    <label>Update Status:</label>
    <select name="status">
        <option value="pending" ${job.status == 'pending' ? 'selected' : ''}>Pending</option>
        <option value="in_progress" ${job.status == 'in_progress' ? 'selected' : ''}>In Progress</option>
        <option value="completed" ${job.status == 'completed' ? 'selected' : ''}>Completed</option>
    </select>
    <button type="submit">Update Status</button>
</form>

<hr>
<h3>Services to Perform</h3>
<ul>
    <c:forEach var="s" items="${services}">
        <li><c:out value="${s.service_name}" /> (x<c:out value="${s.quantity}" />)</li>
    </c:forEach>
</ul>

<h3>Parts Allocated</h3>
<ul>
    <c:forEach var="p" items="${parts}">
        <li><c:out value="${p.quantity_used}" />x <c:out value="${p.part_name}" /></li>
    </c:forEach>
</ul>

<hr>
<h3>Use Spare Parts</h3>
<form action="${pageContext.request.contextPath}/staff/jobcards?action=useParts" method="POST">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <input type="hidden" name="id" value="${job.jobId}">

    <table border="1">
        <thead>
        <tr>
            <th>Select</th>
            <th>Part</th>
            <th>In Stock</th>
            <th>Quantity to Use</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="p" items="${allParts}">
            <tr>
                <td><input type="checkbox" name="partId" value="${p.partId}"></td>
                <td><c:out value="${p.partNumber}" /> - <c:out value="${p.partName}" /></td>
                <td><c:out value="${p.quantityInStock}" /></td>
                <td><input type="number" min="1" name="qty_${p.partId}" value="1"></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <br>
    <button type="submit">Use Selected Parts</button>
</form>

<br>
<a href="${pageContext.request.contextPath}/staff/dashboard">Back to Dashboard</a>

<jsp:include page="/views/common/footer.jsp" />
