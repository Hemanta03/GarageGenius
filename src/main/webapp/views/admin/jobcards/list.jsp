<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Manage Job Cards" scope="request" />
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/navbar.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />

<h2>Job Cards</h2>
<a href="${pageContext.request.contextPath}/admin/jobcards?action=add">Create New Job Card</a>
<br><br>

<table border="1">
    <thead>
        <tr>
            <th>Job ID</th>
            <th>Customer</th>
            <th>Vehicle</th>
            <th>Date</th>
            <th>Status</th>
            <th>Total</th>
            <th>Actions</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="jc" items="${jobCards}">
            <tr>
                <td><c:out value="${jc.jobId}" /></td>
                <td><c:out value="${jc.customerName}" /></td>
                <td><c:out value="${jc.vehicleMake}" /> <c:out value="${jc.vehicleModel}" /> (<c:out value="${jc.licensePlate}" />)</td>
                <td><c:out value="${jc.createdDate}" /></td>
                <td><c:out value="${jc.status}" /></td>
                <td>$<c:out value="${jc.totalAmount}" /></td>
                <td>
                    <a href="${pageContext.request.contextPath}/admin/jobcards?action=view&id=${jc.jobId}">View</a> |
                    <a href="${pageContext.request.contextPath}/admin/jobcards?action=edit&id=${jc.jobId}">Edit / Add Parts</a>
                    <c:if test="${jc.status == 'completed'}">
                        | <a href="${pageContext.request.contextPath}/admin/invoices?action=generate&jobId=${jc.jobId}">Generate Invoice</a>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>

<jsp:include page="/views/common/footer.jsp" />
