<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Job Messages" scope="request" />
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/navbar.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />

<h2>Job Messages (Job #<c:out value="${jobId}" />)</h2>

<a href="${pageContext.request.contextPath}/admin/messages?action=compose&jobId=${jobId}">Compose message about this job</a>
<br><br>

<c:forEach var="t" items="${thread}">
    <div>
        <div><strong><c:out value="${t.senderName}" /></strong> → <strong><c:out value="${t.receiverName}" /></strong> (<c:out value="${t.sentAt}" />)</div>
        <div><strong><c:out value="${t.subject}" /></strong></div>
        <pre><c:out value="${t.body}" /></pre>
    </div>
</c:forEach>

<a href="${pageContext.request.contextPath}/admin/messages">Back</a>

<jsp:include page="/views/common/footer.jsp" />

