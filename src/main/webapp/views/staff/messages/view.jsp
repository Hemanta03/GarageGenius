<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="View Message" scope="request" />
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/navbar.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />

<h2>Message #<c:out value="${message.messageId}" /></h2>

<div>
    <p><strong>From:</strong> <c:out value="${message.senderName}" /></p>
    <p><strong>To:</strong> <c:out value="${message.receiverName}" /></p>
    <p><strong>Job ID:</strong> <c:out value="${message.jobId}" /></p>
    <p><strong>Subject:</strong> <c:out value="${message.subject}" /></p>
    <p><strong>Status:</strong> <c:out value="${message.status}" /></p>
    <p><strong>Sent:</strong> <c:out value="${message.sentAt}" /></p>
    <hr>
    <pre><c:out value="${message.body}" /></pre>
</div>

<c:if test="${not empty thread}">
    <hr>
    <h3>Job Thread</h3>
    <c:forEach var="t" items="${thread}">
        <div>
            <div><strong><c:out value="${t.senderName}" /></strong> → <strong><c:out value="${t.receiverName}" /></strong> (<c:out value="${t.sentAt}" />)</div>
            <div><strong><c:out value="${t.subject}" /></strong></div>
            <pre><c:out value="${t.body}" /></pre>
        </div>
    </c:forEach>
    <a href="${pageContext.request.contextPath}/staff/messages?action=thread&jobId=${message.jobId}">Open thread view</a>
</c:if>

<hr>
<h3>Reply</h3>
<form action="${pageContext.request.contextPath}/staff/messages" method="POST">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <input type="hidden" name="action" value="send">
    <input type="hidden" name="jobId" value="${message.jobId}">

    <c:choose>
        <c:when test="${message.senderId == sessionScope.userId}">
            <input type="hidden" name="receiverId" value="${message.receiverId}">
        </c:when>
        <c:otherwise>
            <input type="hidden" name="receiverId" value="${message.senderId}">
        </c:otherwise>
    </c:choose>

    <div>
        <label>Subject:</label><br>
        <input type="text" name="subject" value="Re: ${message.subject}" required>
    </div>
    <br>
    <div>
        <label>Message:</label><br>
        <textarea name="body" rows="6" cols="60" required></textarea>
    </div>
    <br>
    <button type="submit">Send Reply</button>
</form>

<br>
<a href="${pageContext.request.contextPath}/staff/messages">Back</a>

<jsp:include page="/views/common/footer.jsp" />

