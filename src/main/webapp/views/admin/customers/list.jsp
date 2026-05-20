<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Manage Customers" scope="request" />
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/navbar.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />

<h2>Customers</h2>
<a href="${pageContext.request.contextPath}/admin/customers?action=add">Add New Customer</a>
<br><br>

<table border="1">
    <thead>
        <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Email</th>
            <th>Phone</th>
            <th>Status</th>
            <th>Actions</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="c" items="${customers}">
            <tr>
                <td><c:out value="${c.customerId}" /></td>
                <td><c:out value="${c.fullName}" /></td>
                <td><c:out value="${c.email}" /></td>
                <td><c:out value="${c.phone}" /></td>
                <td><c:out value="${c.status}" /></td>
                <td>
                    <a href="${pageContext.request.contextPath}/admin/customers?action=view&id=${c.customerId}">View</a> |
                    <a href="${pageContext.request.contextPath}/admin/customers?action=edit&id=${c.customerId}">Edit</a>
                    <c:if test="${c.status == 'pending'}">
                        | <a href="${pageContext.request.contextPath}/admin/customers?action=approve&id=${c.customerId}">Approve</a>
                    </c:if>
                    | 
                    <form action="${pageContext.request.contextPath}/admin/customers" method="POST" style="display:inline;" onsubmit="return confirm('Are you sure you want to delete this customer?');">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="id" value="${c.customerId}">
                        <button type="submit" style="background:none; border:none; color:inherit; text-decoration:underline; cursor:pointer; padding:0; font:inherit;">Delete</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>

<jsp:include page="/views/common/footer.jsp" />
