<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Staff Performance Report" scope="request" />
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/navbar.jsp" />
<jsp:include page="/views/common/sidebar.jsp" />

<h2>Staff Performance Report</h2>

<table border="1">
    <thead>
    <tr>
        <th>Staff ID</th>
        <th>Name</th>
        <th>Jobs Completed</th>
        <th>Avg Completion (hours)</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="r" items="${report}">
        <tr>
            <td><c:out value="${r.user_id}" /></td>
            <td><c:out value="${r.full_name}" /></td>
            <td><c:out value="${r.jobs_completed}" /></td>
            <td><c:out value="${r.avg_completion_hours}" /></td>
        </tr>
    </c:forEach>
    </tbody>
</table>

<jsp:include page="/views/common/footer.jsp" />

