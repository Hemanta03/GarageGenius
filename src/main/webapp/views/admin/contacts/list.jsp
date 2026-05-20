<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Contact Messages" scope="request" />
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/navbar.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />

<h2>Contact Messages</h2>

<table border="1">
    <thead>
        <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Email</th>
            <th>Phone</th>
            <th>Subject</th>
            <th>Message</th>
            <th>Date Sent</th>
            <th>Status</th>
            <th>Actions</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="contact" items="${contacts}">
            <tr>
                <td><c:out value="${contact.contactId}" /></td>
                <td><c:out value="${contact.name}" /></td>
                <td><c:out value="${contact.email}" /></td>
                <td><c:out value="${contact.phone}" /></td>
                <td><c:out value="${contact.subject}" /></td>
                <td><c:out value="${contact.message}" /></td>
                <td><c:out value="${contact.submittedAt}" /></td>
                <td><c:out value="${contact.status}" /></td>
                <td>
                    <c:if test="${contact.status == 'new'}">
                        <form action="${pageContext.request.contextPath}/admin/contacts" method="POST">
                            <input type="hidden" name="action" value="status">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                            <input type="hidden" name="id" value="${contact.contactId}">
                            <input type="hidden" name="status" value="read">
                            <button type="submit">Mark as Read</button>
                        </form>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>

<jsp:include page="/views/common/footer.jsp" />
