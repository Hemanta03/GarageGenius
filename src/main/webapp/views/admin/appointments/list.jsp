<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Manage Appointments" scope="request" />
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/navbar.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />

<h2>Appointments</h2>

<table border="1">
    <thead>
        <tr>
            <th>ID</th>
            <th>Customer Name</th>
            <th>Vehicle</th>
            <th>Service</th>
            <th>Date</th>
            <th>Time</th>
            <th>Status</th>
            <th>Customer Notes</th>
            <th>Admin Notes</th>
            <th>Actions</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="a" items="${appointments}">
            <tr>
                <td><c:out value="${a.appointmentId}" /></td>
                <td><c:out value="${a.customerName}" /></td>
                <td><c:out value="${a.vehicleMake}" /> <c:out value="${a.vehicleModel}" /> (<c:out value="${a.licensePlate}" />)</td>
                <td><c:out value="${a.serviceName}" /></td>
                <td><c:out value="${a.requestedDate}" /></td>
                <td><c:out value="${a.preferredTime}" /></td>
                <td><c:out value="${a.status}" /></td>
                <td><c:out value="${a.notes}" /></td>
                <td><c:out value="${a.adminNotes}" /></td>
                <td>
                    <form action="${pageContext.request.contextPath}/admin/appointments?action=status" method="POST">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="id" value="${a.appointmentId}">
                        <select name="status">
                            <option value="pending" ${a.status == 'pending' ? 'selected' : ''}>pending</option>
                            <option value="approved" ${a.status == 'approved' ? 'selected' : ''}>approved</option>
                            <option value="rejected" ${a.status == 'rejected' ? 'selected' : ''}>rejected</option>
                            <option value="completed" ${a.status == 'completed' ? 'selected' : ''}>completed</option>
                            <option value="cancelled" ${a.status == 'cancelled' ? 'selected' : ''}>cancelled</option>
                        </select>
                        <input type="text" name="adminNotes" placeholder="Admin notes (optional)" />
                        <button type="submit">Update</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>

<jsp:include page="/views/common/footer.jsp" />
