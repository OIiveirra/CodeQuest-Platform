<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>CodeQuest - 页面不存在</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <style>
        body {
            margin: 0;
            min-height: 100vh;
            background: radial-gradient(circle at 92% 10%, rgba(37, 99, 235, 0.14), transparent 32%), #f5f7fb;
            font-family: "Microsoft YaHei", sans-serif;
        }

        .error-card {
            border: 0;
            border-radius: 1.1rem;
            box-shadow: 0 14px 34px rgba(15, 23, 42, 0.12);
        }

        .code {
            font-size: 68px;
            font-weight: 800;
            color: #2563eb;
            line-height: 1;
        }
    </style>
</head>
<body class="d-flex align-items-center justify-content-center p-3">
<div class="card error-card p-4 p-md-5" style="max-width: 520px; width: 100%;">
    <div class="code mb-2">404</div>
    <h2 class="h4 mb-2">页面走丢了</h2>
    <p class="text-secondary mb-4">你访问的页面不存在，可能链接已过期或地址输入有误。</p>
    <div>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/">返回首页</a>
    </div>
</div>
</body>
</html>