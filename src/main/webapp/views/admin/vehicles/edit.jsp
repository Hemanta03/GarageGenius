<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Edit Vehicle" scope="request" />
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/navbar.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />

<h2>Edit Vehicle: <c:out value="${vehicle.licensePlate}" /></h2>

<form action="${pageContext.request.contextPath}/admin/vehicles?action=update" method="POST">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <input type="hidden" name="id" value="${vehicle.vehicleId}">
    
    <div>
        <label>Customer:</label><br>
        <select name="customerId" required>
            <c:forEach var="c" items="${customers}">
                <option value="${c.customerId}" ${c.customerId == vehicle.customerId ? 'selected' : ''}><c:out value="${c.fullName}" /></option>
            </c:forEach>
        </select>
    </div>
    <br>
    <div>
        <label>Company:</label><br>
        <input type="text" name="make" value="${vehicle.make}" required>
    </div>
    <br>
    <div>
        <label>Name (Model):</label><br>
        <input type="text" name="model" value="${vehicle.model}" required>
    </div>
    <br>
    <div>
        <label>Year:</label><br>
        <input type="number" name="year" value="${vehicle.year}" required>
    </div>
    <br>
    <div>
        <label>License Plate:</label><br>
        <input type="text" name="licensePlate" value="${vehicle.licensePlate}" required>
    </div>
    <br>
    <div>
        <label>Color:</label><br>
        <input type="text" name="color" value="${vehicle.color}">
    </div>
    <br>
    <div>
        <label>VIN Number (optional):</label><br>
        <input type="text" name="vinNumber" value="${vehicle.vinNumber}">
    </div>
    <br>
    <div>
        <label>Mileage:</label><br>
        <input type="number" name="mileage" value="${vehicle.mileage}" required>
    </div>
    <br>
    <div>
        <label>Fuel Type:</label><br>
        <input type="text" name="fuelType" value="${vehicle.fuelType}">
    </div>
    <br>
    <button type="submit">Update Vehicle</button>
</form>

<br>
<a href="${pageContext.request.contextPath}/admin/vehicles">Cancel</a>

<jsp:include page="/views/common/footer.jsp" />
