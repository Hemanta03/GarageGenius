<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Manage Invoices" scope="request" />
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/navbar.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />

<h2>Invoices</h2>

<table>
    <thead>
        <tr>
            <th>Invoice ID</th>
            <th>Source</th>
            <th>Date</th>
            <th>Due Date</th>
            <th>Subtotal</th>
            <th>Tax</th>
            <th>Discount</th>
            <th>Total</th>
            <th>Amount Paid</th>
            <th>Balance Due</th>
            <th>Status</th>
            <th>Actions</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="inv" items="${invoices}">
            <tr>
                <td><c:out value="${inv.invoiceId}" /></td>
                <td>
                    <c:choose>
                        <c:when test="${not empty inv.jobId and inv.jobId != 0}">
                            <a href="${pageContext.request.contextPath}/admin/jobcards?action=view&id=${inv.jobId}">Job #<c:out value="${inv.jobId}" /></a>
                        </c:when>
                        <c:when test="${not empty inv.orderId and inv.orderId != 0}">
                            <a href="${pageContext.request.contextPath}/admin/orders">Order #<c:out value="${inv.orderId}" /></a>
                        </c:when>
                        <c:otherwise>N/A</c:otherwise>
                    </c:choose>
                </td>
                <td><c:out value="${inv.invoiceDate}" /></td>
                <td><c:out value="${inv.dueDate}" /></td>
                <td>$<c:out value="${inv.subtotal}" /></td>
                <td>$<c:out value="${inv.taxAmount}" /> (<c:out value="${inv.taxRate}" />%)</td>
                <td>$<c:out value="${inv.discount}" /></td>
                <td><strong>$<c:out value="${inv.totalAmount}" /></strong></td>
                <td>$<c:out value="${inv.amountPaid}" /></td>
                <td>$<c:out value="${inv.totalAmount - inv.amountPaid}" /></td>
                <td><c:out value="${inv.paymentStatus}" /></td>
                <td>
                    <a href="${pageContext.request.contextPath}/admin/invoices?action=view&id=${inv.invoiceId}">View</a>
                    <c:if test="${inv.paymentStatus != 'paid'}">
                        | <a href="${pageContext.request.contextPath}/admin/invoices?action=pay&id=${inv.invoiceId}">Record Payment</a>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>

<jsp:include page="/views/common/footer.jsp" />
