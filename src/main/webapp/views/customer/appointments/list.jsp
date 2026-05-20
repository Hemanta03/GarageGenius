<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="My Appointments" scope="request" />
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/navbar.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />

<h2>My Appointments</h2>
<a href="${pageContext.request.contextPath}/customer/appointments?action=new">Request New Appointment</a>
<br><br>

<table border="1">
    <thead>
        <tr>
            <th>Date</th>
            <th>Time</th>
            <th>Vehicle</th>
            <th>Service</th>
            <th>Status</th>
            <th>Admin Notes</th>
            <th>Actions</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="a" items="${appointments}">
            <tr>
                <td><c:out value="${a.requestedDate}" /></td>
                <td><c:out value="${a.preferredTime}" /></td>
                <td><c:out value="${a.vehicleMake}" /> <c:out value="${a.vehicleModel}" /> (<c:out value="${a.licensePlate}" />)</td>
                <td><c:out value="${a.serviceName}" /></td>
                <td><c:out value="${a.status}" /></td>
                <td><c:out value="${a.adminNotes}" /></td>
                <td>
                    <c:if test="${a.status == 'pending'}">
                        <form action="${pageContext.request.contextPath}/customer/appointments?action=cancel" method="POST">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                            <input type="hidden" name="id" value="${a.appointmentId}">
                            <button type="submit">Cancel</button>
                        </form>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>

<jsp:include page="/views/common/footer.jsp" />
