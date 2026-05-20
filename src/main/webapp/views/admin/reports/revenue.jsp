<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Revenue Report" scope="request" />
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/navbar.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />

<h2>Revenue Overview</h2>
<p><strong>Total Revenue (All Time):</strong> $<c:out value="${totalRevenue}" /></p>
<p><strong>Today's Revenue:</strong> $<c:out value="${todayRevenue}" /></p>

<hr>

<h2>Monthly Breakdown</h2>

<table border="1">
    <thead>
        <tr>
            <th>Month</th>
            <th>Number of Invoices</th>
            <th>Total Revenue</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="r" items="${report}">
            <tr>
                <td><c:out value="${r.month}" /></td>
                <td><c:out value="${r.invoiceCount}" /></td>
                <td>$<c:out value="${r.totalRevenue}" /></td>
            </tr>
        </c:forEach>
    </tbody>
</table>

<jsp:include page="/views/common/footer.jsp" />
