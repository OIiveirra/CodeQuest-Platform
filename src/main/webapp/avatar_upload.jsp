<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>CodeQuest - 头像上传</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <style>
        body {
            min-height: 100vh;
            margin: 0;
            background:
                radial-gradient(circle at 90% 10%, rgba(14, 165, 233, .16), transparent 28%),
                radial-gradient(circle at 12% 85%, rgba(249, 115, 22, .14), transparent 25%),
                #f6f7fb;
            font-family: "Microsoft YaHei", sans-serif;
        }

        .upload-shell {
            max-width: 760px;
            width: 100%;
        }

        .upload-card {
            border: 0;
            border-radius: 1.25rem;
            box-shadow: 0 18px 40px rgba(15, 23, 42, .12);
            overflow: hidden;
        }

        .upload-header {
            background: linear-gradient(135deg, #0f172a 0%, #2563eb 55%, #0ea5e9 100%);
            color: #fff;
        }

        .upload-tip {
            color: #64748b;
        }
    </style>
</head>
<body class="d-flex align-items-center justify-content-center p-3">
<div class="upload-shell">
    <div class="card upload-card">
        <div class="upload-header p-4 p-md-5">
            <div class="d-flex flex-column flex-md-row justify-content-between gap-3 align-items-md-center">
                <div>
                    <h1 class="h3 mb-2 fw-bold">头像上传</h1>
                    <p class="mb-0 text-white-50">仅支持 JPG / PNG，文件会保存到服务器绝对路径。</p>
                </div>
                <a class="btn btn-outline-light" href="${pageContext.request.contextPath}/">返回首页</a>
            </div>
        </div>

        <div class="card-body p-4 p-md-5">
            <c:if test="${param.success == 'true'}">
                <div class="alert alert-success">头像上传成功。</div>
            </c:if>

            <c:if test="${not empty sessionScope.loginUser}">
                <div class="alert alert-info d-flex flex-column flex-md-row justify-content-between gap-2 align-items-md-center">
                    <div>
                        当前登录用户：<strong>${sessionScope.loginUser.username}</strong>
                    </div>
                    <div class="small text-break">已保存路径：${sessionScope.loginUser.avatarUrl}</div>
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/avatar/upload" method="post" enctype="multipart/form-data">
                <div class="mb-3">
                    <label for="avatar" class="form-label fw-semibold">选择头像文件</label>
                    <input id="avatar" name="avatar" type="file" class="form-control" accept="image/jpeg,image/png" required>
                    <div class="form-text upload-tip">建议使用正方形图片，大小不超过 2MB。</div>
                </div>

                <div class="alert alert-light border">
                    <div class="fw-semibold mb-1">存储说明</div>
                    <div class="small text-secondary">
                        文件会按用户 ID 重命名后保存到项目根目录下的 <span class="fw-semibold">uploads/avatars/</span>，不会落到 webapps 目录。
                    </div>
                </div>

                <div class="d-flex flex-wrap gap-2">
                    <button type="submit" class="btn btn-primary px-4">上传头像</button>
                    <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/questions">去题库</a>
                </div>
            </form>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1N7N6jIeHz" crossorigin="anonymous"></script>
</body>
</html>