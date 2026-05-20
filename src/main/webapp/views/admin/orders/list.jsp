<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/navbar.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />

<h2>Manage Customer Orders</h2>

<c:if test="${not empty sessionScope.successMessage}">
    <div><c:out value="${sessionScope.successMessage}" /></div>
    <c:remove var="successMessage" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.errorMessage}">
    <div><c:out value="${sessionScope.errorMessage}" /></div>
    <c:remove var="errorMessage" scope="session" />
</c:if>

<c:choose>
    <c:when test="${not empty orders}">
        <table>
            <thead>
                <tr>
                    <th>Order ID</th>
                    <th>Customer ID</th>
                    <th>Total Amount</th>
                    <th>Status</th>
                    <th>Date</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach var="order" items="${orders}">
                    <tr>
                        <td>#<c:out value="${order.orderId}" /></td>
                        <td><c:out value="${order.customerId}" /></td>
                        <td>$<c:out value="${order.totalAmount}" /></td>
                        <td><c:out value="${order.status}" /></td>
                        <td><c:out value="${order.createdAt}" /></td>
                        <td>
                            <form action="${pageContext.request.contextPath}/admin/orders" method="POST">
                                <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                                <input type="hidden" name="orderId" value="${order.orderId}">
                                <select name="status">
                                    <option value="pending" ${order.status == 'pending' ? 'selected' : ''}>Pending</option>
                                    <option value="processing" ${order.status == 'processing' ? 'selected' : ''}>Processing</option>
                                    <option value="completed" ${order.status == 'completed' ? 'selected' : ''}>Completed</option>
                                    <option value="cancelled" ${order.status == 'cancelled' ? 'selected' : ''}>Cancelled</option>
                                </select>
                                <button type="submit">Update</button>
                            </form>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="6">
                            <strong>Items:</strong>
                            <ul>
                                <c:forEach var="item" items="${order.items}">
                                    <li><c:out value="${item.partName}" /> x <c:out value="${item.quantity}" /> ($<c:out value="${item.unitPrice}" /> each)</li>
                                </c:forEach>
                            </ul>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </c:when>
    <c:otherwise>
        <p>No customer orders found.</p>
    </c:otherwise>
</c:choose>

<jsp:include page="/views/common/footer.jsp" />
