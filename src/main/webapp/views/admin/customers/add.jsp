<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Add Customer" scope="request" />
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/navbar.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />

<h2>Add New Customer</h2>

<form action="${pageContext.request.contextPath}/admin/customers?action=create" method="POST">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    
    <div>
        <label>Full Name:</label><br>
        <input type="text" name="fullName" required>
    </div>
    <br>
    <div>
        <label>Email (Leave blank for walk-ins):</label><br>
        <input type="email" name="email">
    </div>
    <br>
    <div>
        <label>Phone:</label><br>
        <input type="text" name="phone" required>
    </div>
    <br>
    <div>
        <label>Address (Optional):</label><br>
        <input type="text" name="address">
    </div>
    <br>
    <div>
        <label>City (Optional):</label><br>
        <input type="text" name="city">
    </div>
    <br>
    <button type="submit">Add Customer</button>
</form>

<br>
<a href="${pageContext.request.contextPath}/admin/customers">Cancel</a>

<jsp:include page="/views/common/footer.jsp" />
