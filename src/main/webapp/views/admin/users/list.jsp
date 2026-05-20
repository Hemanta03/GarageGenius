<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Manage Users" scope="request" />
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/navbar.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />

<h2>Manage Users</h2>

<c:if test="${not empty param.tempPass}">
    <div>
        Temporary password (show once): <strong><c:out value="${param.tempPass}" /></strong>
    </div>
    <br>
</c:if>

<a href="${pageContext.request.contextPath}/admin/users?action=add">Create Admin/Staff User</a>
<br><br>

<table>
    <thead>
    <tr>
        <th>ID</th>
        <th>Name</th>
        <th>Email</th>
        <th>Role</th>
        <th>Phone</th>
        <th>Status</th>
        <th>Actions</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="u" items="${users}">
        <tr>
            <td><c:out value="${u.userId}" /></td>
            <td><c:out value="${u.fullName}" /></td>
            <td><c:out value="${u.email}" /></td>
            <td><c:out value="${u.role}" /></td>
            <td><c:out value="${u.phone}" /></td>
            <td><c:out value="${u.status}" /></td>
            <td>
                <form action="${pageContext.request.contextPath}/admin/users" method="POST">
                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                    <input type="hidden" name="action" value="status">
                    <input type="hidden" name="id" value="${u.userId}">
                    <select name="status">
                        <option value="active" ${u.status == 'active' ? 'selected' : ''}>active</option>
                        <option value="inactive" ${u.status == 'inactive' ? 'selected' : ''}>inactive</option>
                        <option value="pending" ${u.status == 'pending' ? 'selected' : ''}>pending</option>
                    </select>
                    <button type="submit">Update</button>
                </form>
                <form action="${pageContext.request.contextPath}/admin/users" method="POST" style="display:inline;">
                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                    <input type="hidden" name="action" value="resetPassword">
                    <input type="hidden" name="id" value="${u.userId}">
                    <button type="submit">Reset Password</button>
                </form>
                <form action="${pageContext.request.contextPath}/admin/users" method="POST" style="display:inline;" onsubmit="return confirm('Are you sure you want to delete this user?');">
                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="id" value="${u.userId}">
                    <button type="submit">Delete</button>
                </form>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>

<jsp:include page="/views/common/footer.jsp" />

