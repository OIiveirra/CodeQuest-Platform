<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>CodeQuest - 系统异常</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <style>
        body {
            margin: 0;
            min-height: 100vh;
            background: radial-gradient(circle at 8% 12%, rgba(37, 99, 235, 0.16), transparent 30%), #f5f7fb;
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
            color: #1d4ed8;
            line-height: 1;
        }
    </style>
</head>
<body class="d-flex align-items-center justify-content-center p-3">
<div class="card error-card p-4 p-md-5" style="max-width: 520px; width: 100%;">
    <div class="code mb-2">500</div>
    <h2 class="h4 mb-2">系统开了个小差</h2>
    <p class="text-secondary mb-4">服务端处理请求时出现异常，请稍后再试。</p>
    <div class="small text-secondary bg-light border rounded p-3 mb-4" style="white-space: pre-wrap;">
        <div>异常类型：${empty requestScope.errorType ? 'N/A' : requestScope.errorType}</div>
        <div>异常信息：${empty requestScope.errorMessage ? '请查看服务器日志获取更多信息。' : requestScope.errorMessage}</div>
    </div>
    <div>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/">返回首页</a>
    </div>
</div>
</body>
</html>