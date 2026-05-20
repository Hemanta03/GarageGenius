<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Manage Services" scope="request" />
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/navbar.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />

<h2>Services Catalog</h2>
<a href="${pageContext.request.contextPath}/admin/services?action=add">Add New Service</a>
<br><br>

<table border="1">
    <thead>
        <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Category</th>
            <th>Base Price</th>
            <th>Est. Duration (Hrs)</th>
            <th>Actions</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="s" items="${services}">
            <tr>
                <td><c:out value="${s.serviceId}" /></td>
                <td><c:out value="${s.serviceName}" /></td>
                <td><c:out value="${s.category}" /></td>
                <td>$<c:out value="${s.basePrice}" /></td>
                <td><c:out value="${s.estimatedDurationHrs}" /></td>
                <td>
                    <a href="${pageContext.request.contextPath}/admin/services?action=edit&id=${s.serviceId}">Edit</a>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>

<jsp:include page="/views/common/footer.jsp" />
