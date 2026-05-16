<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${pageTitle}" scope="request" />
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/navbar.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />

<h2>Staff Messages</h2>

<div>
    <a href="${pageContext.request.contextPath}/staff/messages">Inbox</a> |
    <a href="${pageContext.request.contextPath}/staff/messages?action=sent">Sent</a> |
    <a href="${pageContext.request.contextPath}/staff/messages?action=compose">Compose</a>
</div>
<br>

<table border="1">
    <thead>
    <tr>
        <th>ID</th>
        <th><c:out value="${box == 'sent' ? 'To' : 'From'}" /></th>
        <th>Job</th>
        <th>Subject</th>
        <th>Status</th>
        <th>Sent</th>
        <th>Actions</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="m" items="${messages}">
        <tr>
            <td><c:out value="${m.messageId}" /></td>
            <td>
                <c:choose>
                    <c:when test="${box == 'sent'}"><c:out value="${m.receiverName}" /></c:when>
                    <c:otherwise><c:out value="${m.senderName}" /></c:otherwise>
                </c:choose>
            </td>
            <td><c:out value="${m.jobId}" /></td>
            <td><c:out value="${m.subject}" /></td>
            <td><c:out value="${m.status}" /></td>
            <td><c:out value="${m.sentAt}" /></td>
            <td>
                <a href="${pageContext.request.contextPath}/staff/messages?action=view&id=${m.messageId}">View</a>
                <c:if test="${box == 'inbox' && m.status == 'unread'}">
                    <form action="${pageContext.request.contextPath}/staff/messages" method="POST">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="action" value="markRead">
                        <input type="hidden" name="id" value="${m.messageId}">
                        <button type="submit">Mark Read</button>
                    </form>
                </c:if>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>

<jsp:include page="/views/common/footer.jsp" />

