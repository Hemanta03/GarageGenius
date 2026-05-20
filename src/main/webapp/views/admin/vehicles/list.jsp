<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Manage Vehicles" scope="request" />
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/navbar.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />

<h2>Vehicles</h2>
<a href="${pageContext.request.contextPath}/admin/vehicles?action=add">Add New Vehicle</a>
<br><br>

<table border="1">
    <thead>
        <tr>
            <th>ID</th>
            <th>Owner (Customer ID)</th>
            <th>Make</th>
            <th>Model</th>
            <th>Year</th>
            <th>License Plate</th>
            <th>Actions</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="v" items="${vehicles}">
            <tr>
                <td><c:out value="${v.vehicleId}" /></td>
                <td><c:out value="${v.customerId}" /></td>
                <td><c:out value="${v.make}" /></td>
                <td><c:out value="${v.model}" /></td>
                <td><c:out value="${v.year}" /></td>
                <td><c:out value="${v.licensePlate}" /></td>
                <td>
                    <a href="${pageContext.request.contextPath}/admin/vehicles?action=edit&id=${v.vehicleId}">Edit</a> | 
                    <form action="${pageContext.request.contextPath}/admin/vehicles" method="POST" style="display:inline;" onsubmit="return confirm('Are you sure you want to delete this vehicle?');">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="id" value="${v.vehicleId}">
                        <button type="submit" style="background:none; border:none; color:inherit; text-decoration:underline; cursor:pointer; padding:0; font:inherit;">Delete</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>

<jsp:include page="/views/common/footer.jsp" />
