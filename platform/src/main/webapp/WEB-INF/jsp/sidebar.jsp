<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<aside class="col-12 col-lg-2 p-3 p-lg-4 cq-sidebar d-flex flex-lg-column justify-content-between">
    <div>
        <div class="brand mb-3">CodeQuest</div>
        <div class="small text-secondary-emphasis mb-2">面试训练平台</div>
        <div class="small mb-3">当前在线人数：${applicationScope.onlineCount == null ? 0 : applicationScope.onlineCount}</div>
        <nav class="nav nav-pills flex-row flex-lg-column gap-2">
            <a class="nav-link cq-nav-link ${pageContext.request.requestURI == pageContext.request.contextPath.concat('/') ? 'active' : ''}" href="${pageContext.request.contextPath}/">首页</a>
            <a class="nav-link cq-nav-link ${fn:contains(pageContext.request.requestURI, '/questions') ? 'active' : ''}" href="${pageContext.request.contextPath}/questions">题目列表</a>

            <c:if test="${not empty sessionScope.loginUser and (sessionScope.loginUser.role == 'admin' or sessionScope.loginUser.role == 'ADMIN')}">
                <a class="nav-link cq-nav-link ${fn:contains(pageContext.request.requestURI, '/admin/') ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/question?action=list">管理后台</a>
            </c:if>

            <c:choose>
                <c:when test="${not empty sessionScope.loginUser}">
                    <a class="nav-link cq-nav-link ${fn:contains(pageContext.request.requestURI, '/profile') ? 'active' : ''}" href="${pageContext.request.contextPath}/profile">个人中心</a>
                    <a class="nav-link cq-nav-link" href="${pageContext.request.contextPath}/logout">退出登录</a>
                </c:when>
                <c:otherwise>
                    <a class="nav-link cq-nav-link ${fn:contains(pageContext.request.requestURI, '/login') ? 'active' : ''}" href="${pageContext.request.contextPath}/login">登录</a>
                    <a class="nav-link cq-nav-link ${fn:contains(pageContext.request.requestURI, '/register') ? 'active' : ''}" href="${pageContext.request.contextPath}/register">注册</a>
                </c:otherwise>
            </c:choose>
        </nav>
    </div>
    <div class="small text-light-emphasis mt-3">Build your coding intuition.</div>
</aside>
