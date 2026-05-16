<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="About Us" scope="request" />
<jsp:include page="/views/common/header.jsp" />
<jsp:include page="/views/common/navbar.jsp" />

<div class="app-body">
    <main class="main-content">
        <div class="card">
            <h2>About GarageGenius</h2>
            <p>GarageGenius is an advanced garage management system designed to streamline your automotive service business.</p>
            <p>We provide features for managing customers, vehicles, spare parts inventory, and complex job cards.</p>
            
            <p style="margin-top: 2rem;"><a href="${pageContext.request.contextPath}/" class="btn btn-secondary">Back to Home</a></p>
        </div>

<jsp:include page="/views/common/footer.jsp" />
