<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Service Report" scope="request" />
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/navbar.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />

<h2>Service Summary Report</h2>

<table border="1">
    <thead>
    <tr>
        <th>Service</th>
        <th>Total Quantity</th>
        <th>Total Revenue</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="r" items="${report}">
        <tr>
            <td><c:out value="${r.service_name}" /></td>
            <td><c:out value="${r.total_quantity}" /></td>
            <td>$<c:out value="${r.total_revenue}" /></td>
        </tr>
    </c:forEach>
    </tbody>
</table>

<jsp:include page="/views/common/footer.jsp" />

