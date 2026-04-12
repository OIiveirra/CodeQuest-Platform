<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>CodeQuest - 登录</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <style>
        body {
            min-height: 100vh;
            margin: 0;
            background: radial-gradient(circle at 85% 10%, rgba(14, 165, 233, .18), transparent 32%), #f5f7fb;
            font-family: "Microsoft YaHei", sans-serif;
        }

        .login-card {
            border: 0;
            border-radius: 16px;
            box-shadow: 0 18px 40px rgba(15, 23, 42, .12);
        }
    </style>
</head>
<body class="d-flex align-items-center justify-content-center p-3">
<div class="card login-card p-4" style="max-width: 460px; width: 100%;">
    <h2 class="mb-3">登录 CodeQuest</h2>

    <c:if test="${param.registered == 'true'}">
        <div class="alert alert-success">注册成功，请登录。</div>
    </c:if>
    <c:if test="${param.logout == 'true'}">
        <div class="alert alert-info">你已安全退出。</div>
    </c:if>
    <c:if test="${not empty error}">
        <div class="alert alert-danger">${error}</div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/login">
        <div class="mb-3">
            <label for="username" class="form-label">用户名</label>
            <input id="username" name="username" class="form-control" required />
        </div>
        <div class="mb-3">
            <label for="password" class="form-label">密码</label>
            <input id="password" name="password" type="password" class="form-control" required />
        </div>
        <button type="submit" class="btn btn-primary w-100">登录</button>
    </form>

    <div class="mt-3 text-secondary">
        没有账号？
        <a href="${pageContext.request.contextPath}/register" class="text-decoration-none">立即注册</a>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>
</body>
</html>
