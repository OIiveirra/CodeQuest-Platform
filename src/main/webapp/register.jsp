<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>CodeQuest - 注册</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <style>
        body {
            min-height: 100vh;
            margin: 0;
            background: radial-gradient(circle at 88% 12%, rgba(249, 115, 22, .15), transparent 30%), #f5f7fb;
            font-family: "Microsoft YaHei", sans-serif;
        }

        .register-card {
            border: 0;
            border-radius: 16px;
            box-shadow: 0 18px 40px rgba(15, 23, 42, .12);
        }
    </style>
</head>
<body class="d-flex align-items-center justify-content-center p-3">
<div class="card register-card p-4" style="max-width: 460px; width: 100%;">
    <h2 class="mb-3">创建账号</h2>

    <c:if test="${not empty error}">
        <div class="alert alert-danger">${error}</div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/register">
        <div class="mb-3">
            <label for="username" class="form-label">用户名</label>
            <input id="username" name="username" class="form-control" required />
        </div>
        <div class="mb-3">
            <label for="password" class="form-label">密码</label>
            <input id="password" name="password" type="password" class="form-control" required />
        </div>
        <button type="submit" class="btn btn-success w-100">注册</button>
    </form>

    <div class="mt-3 text-secondary">
        已有账号？
        <a href="${pageContext.request.contextPath}/login" class="text-decoration-none">去登录</a>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>
</body>
</html>
