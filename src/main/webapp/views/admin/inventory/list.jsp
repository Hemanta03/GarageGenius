<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Manage Inventory" scope="request" />
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/navbar.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />

<h2>Spare Parts Inventory</h2>
<a href="${pageContext.request.contextPath}/admin/inventory?action=add">Add New Spare Part</a>
<br><br>

<table>
    <thead>
        <tr>
            <th>ID</th>
            <th>Part Name</th>
            <th>Part Number</th>
            <th>Category</th>
            <th>Stock</th>
            <th>Reorder Level</th>
            <th>Unit Price</th>
            <th>Actions</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="p" items="${parts}">
            <tr>
                <td><c:out value="${p.partId}" /></td>
                <td><c:out value="${p.partName}" /></td>
                <td><c:out value="${p.partNumber}" /></td>
                <td><c:out value="${p.category}" /></td>
                <td><c:out value="${p.quantityInStock}" /></td>
                <td><c:out value="${p.reorderLevel}" /></td>
                <td>$<c:out value="${p.unitPrice}" /></td>
                <td>
                    <a href="${pageContext.request.contextPath}/admin/inventory?action=edit&id=${p.partId}">Edit</a> |
                    <a href="${pageContext.request.contextPath}/admin/inventory?action=restock&id=${p.partId}">Restock</a> |
                    <form action="${pageContext.request.contextPath}/admin/inventory" method="POST" style="display:inline;" onsubmit="return confirm('Are you sure you want to delete this part?');">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="action" value="delete">
                        <input type="hidden" name="id" value="${p.partId}">
                        <button type="submit" style="background:none; border:none; color:inherit; text-decoration:underline; cursor:pointer; padding:0; font:inherit;">Delete</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>

<jsp:include page="/views/common/footer.jsp" />
