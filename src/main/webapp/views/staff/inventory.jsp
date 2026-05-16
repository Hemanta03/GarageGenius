<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Staff Inventory View" scope="request" />
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/navbar.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />

<h2>Inventory (Staff View)</h2>

<table border="1">
    <thead>
        <tr>
            <th>Part Number</th>
            <th>Part Name</th>
            <th>Category</th>
            <th>Quantity in Stock</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="p" items="${parts}">
            <tr>
                <td><c:out value="${p.partNumber}" /></td>
                <td><c:out value="${p.partName}" /></td>
                <td><c:out value="${p.category}" /></td>
                <td><c:out value="${p.quantityInStock}" /></td>
            </tr>
        </c:forEach>
    </tbody>
</table>

<jsp:include page="/views/common/footer.jsp" />
