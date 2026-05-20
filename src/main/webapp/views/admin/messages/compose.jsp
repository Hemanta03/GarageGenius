<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Compose Message" scope="request" />
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/navbar.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />

<h2>Compose (Admin → Staff)</h2>

<c:if test="${not empty errorMessage}">
    <div><c:out value="${errorMessage}" /></div>
    <br>
</c:if>

<form action="${pageContext.request.contextPath}/admin/messages" method="POST">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <input type="hidden" name="action" value="send">

    <c:if test="${not empty jobId}">
        <input type="hidden" name="jobId" value="${jobId}">
        <p><strong>Job ID:</strong> <c:out value="${jobId}" /></p>
    </c:if>

    <div>
        <label>To (Staff):</label><br>
        <select name="receiverId" required>
            <c:forEach var="u" items="${staff}">
                <option value="${u.userId}" ${receiverId == u.userId ? 'selected' : ''}><c:out value="${u.fullName}" /> (<c:out value="${u.email}" />)</option>
            </c:forEach>
        </select>
    </div>
    <br>
    <div>
        <label>Subject:</label><br>
        <input type="text" name="subject" required>
    </div>
    <br>
    <div>
        <label>Message:</label><br>
        <textarea name="body" rows="6" cols="60" required></textarea>
    </div>
    <br>
    <button type="submit">Send</button>
</form>

<br>
<a href="${pageContext.request.contextPath}/admin/messages">Back</a>

<jsp:include page="/views/common/footer.jsp" />

