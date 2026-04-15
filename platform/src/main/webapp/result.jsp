<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>CodeQuest - 作答结果</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <style>
        :root {
            --cq-bg: #f6f7fb;
            --cq-sidebar: #0f172a;
        }

        body {
            margin: 0;
            font-family: "Microsoft YaHei", sans-serif;
            background:
                radial-gradient(circle at 92% 10%, rgba(234, 179, 8, 0.2), transparent 30%),
                radial-gradient(circle at 6% 90%, rgba(14, 165, 233, 0.15), transparent 26%),
                var(--cq-bg);
            color: #1f2937;
        }

        .cq-sidebar {
            background: linear-gradient(180deg, #111827 0%, #0f172a 70%, #1e293b 100%);
            color: #e2e8f0;
        }

        .cq-sidebar .brand {
            font-weight: 700;
            font-size: 1.15rem;
        }

        .cq-nav-link {
            color: #cbd5e1;
            border-radius: .75rem;
            transition: all .2s ease;
        }

        .cq-nav-link:hover,
        .cq-nav-link.active {
            color: #fff;
            background: rgba(14, 165, 233, 0.22);
        }

        .cq-main {
            animation: cqFade .45s ease;
        }

        @keyframes cqFade {
            from { opacity: 0; transform: translateY(8px); }
            to { opacity: 1; transform: translateY(0); }
        }

        .panel {
            background: #ffffff;
            border-radius: 20px;
            box-shadow: 0 16px 36px rgba(15, 23, 42, 0.1);
            overflow: hidden;
        }

        .header {
            padding: 24px 28px;
            color: #fff;
            background: linear-gradient(135deg, #16a34a, #65a30d);
        }

        .header.warn {
            background: linear-gradient(135deg, #d97706, #eab308);
        }

        .title {
            margin: 0;
            font-size: 24px;
            font-weight: 700;
        }

        .score-wrap {
            text-align: center;
            padding: 30px 20px 8px;
        }

        .score {
            font-size: 88px;
            line-height: 1;
            margin: 0;
            font-weight: 800;
            color: #15803d;
        }

        .score.warn {
            color: #b45309;
        }

        .hint {
            margin: 10px 0 0;
            color: #475569;
            font-size: 14px;
        }

        .score-bar {
            max-width: 480px;
            margin: 12px auto 0;
            border-radius: 999px;
            height: 10px;
            background: #e2e8f0;
            overflow: hidden;
        }

        .score-bar-inner {
            height: 100%;
            background: linear-gradient(90deg, #0ea5e9, #22c55e);
        }

        .section {
            padding: 24px 28px 30px;
        }

        .section h2 {
            margin: 0 0 12px;
            font-size: 20px;
        }

        .advice {
            background: #f8fafc;
            border: 1px solid #e2e8f0;
            border-radius: 14px;
            padding: 16px;
            line-height: 1.8;
            color: #334155;
        }

        .actions {
            margin-top: 22px;
            display: flex;
            justify-content: center;
        }

        .btn {
            text-decoration: none;
            border: none;
            border-radius: 999px;
            padding: 11px 22px;
            color: #fff;
            background: linear-gradient(135deg, #2563eb, #1d4ed8);
            font-weight: 700;
        }

        @media (max-width: 991.98px) {
            .cq-sidebar {
                position: sticky;
                top: 0;
                z-index: 10;
            }
        }
    </style>
</head>
<body>
<div class="container-fluid">
    <div class="row min-vh-100">
        <aside class="col-12 col-lg-2 p-3 p-lg-4 cq-sidebar d-flex flex-lg-column justify-content-between">
            <div>
                <div class="brand mb-3">CodeQuest</div>
                <div class="small text-secondary-emphasis mb-3">面试训练平台</div>
                <nav class="nav nav-pills flex-row flex-lg-column gap-2">
                    <a class="nav-link cq-nav-link" href="${pageContext.request.contextPath}/">首页</a>
                    <a class="nav-link cq-nav-link active" href="${pageContext.request.contextPath}/questions">题目列表</a>
                </nav>
            </div>
            <div class="small text-light-emphasis mt-3">Review, refine, repeat.</div>
        </aside>

        <main class="col-12 col-lg-10 p-4 p-lg-5 cq-main">
            <div class="mx-auto" style="max-width: 920px;">
                <c:set var="score" value="${aiScore}" />
                <div class="panel">
                    <div class="header ${score lt 60 ? 'warn' : ''}">
                        <h1 class="title">AI 作答评分结果</h1>
                    </div>

                    <div class="score-wrap">
                        <p class="score ${score lt 60 ? 'warn' : ''}">${aiScore}</p>
                        <p class="hint">满分 100 分</p>
                        <div class="score-bar">
                            <div class="score-bar-inner" style="width: ${aiScore}%;"></div>
                        </div>
                    </div>

                    <div class="section">
                        <h2>AI 反馈建议</h2>
                        <div class="advice">${aiSuggestion}</div>
                        <div class="actions">
                            <a class="btn me-2" href="${pageContext.request.contextPath}/questions">继续练习</a>
                            <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/report/download">下载评价报告</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>
</body>
</html>
