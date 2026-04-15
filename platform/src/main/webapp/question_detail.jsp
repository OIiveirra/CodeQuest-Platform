<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>CodeQuest - 题目详情</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/highlight.js@11.11.1/styles/github.min.css">
    <style>
        :root {
            --cq-bg: #f6f7fb;
            --cq-sidebar: #0f172a;
            --cq-text: #0f172a;
        }

        body {
            margin: 0;
            font-family: "Microsoft YaHei", sans-serif;
            background:
                radial-gradient(circle at 95% 6%, rgba(14, 165, 233, 0.18), transparent 32%),
                radial-gradient(circle at 8% 90%, rgba(249, 115, 22, 0.14), transparent 24%),
                var(--cq-bg);
            color: var(--cq-text);
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

        .hero {
            background: linear-gradient(135deg, #0f172a 0%, #1d4ed8 100%);
            color: #fff;
            border-radius: 18px;
            padding: 28px 32px;
            box-shadow: 0 18px 40px rgba(15, 23, 42, 0.18);
            margin-bottom: 24px;
        }

        .hero h1 {
            margin: 0 0 10px;
            font-size: 2rem;
        }

        .meta {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            margin-top: 16px;
        }

        .pill {
            display: inline-block;
            padding: 6px 12px;
            border-radius: 999px;
            background: rgba(255, 255, 255, 0.16);
            border: 1px solid rgba(255, 255, 255, 0.24);
            font-size: 13px;
        }

        .favorite-btn {
            border: 1px solid rgba(255, 255, 255, 0.45);
            background: rgba(255, 255, 255, 0.16);
            color: #ffffff;
            border-radius: 999px;
            padding: 6px 14px;
            font-size: 14px;
            font-weight: 700;
        }

        .favorite-btn.active {
            background: #f59e0b;
            border-color: #f59e0b;
            color: #0f172a;
        }

        .card {
            background: #fff;
            border-radius: 18px;
            box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
            padding: 28px 32px;
            margin-bottom: 20px;
            border: 0;
        }

        .section-title {
            margin: 0 0 14px;
            font-size: 18px;
            color: #0f172a;
        }

        .content-box {
            line-height: 1.8;
            font-size: 16px;
            color: #334155;
            white-space: pre-wrap;
        }

        .markdown-body {
            color: #1f2937;
            line-height: 1.7;
            font-size: 15px;
            word-break: break-word;
        }

        .markdown-body > :first-child {
            margin-top: 0;
        }

        .markdown-body > :last-child {
            margin-bottom: 0;
        }

        .markdown-body table {
            width: 100%;
            border-collapse: collapse;
            margin: 12px 0;
            border: 1px solid #d0d7de;
            border-radius: 8px;
            overflow: hidden;
            display: block;
            overflow-x: auto;
        }

        .markdown-body th,
        .markdown-body td {
            border: 1px solid #d0d7de;
            padding: 8px 12px;
            text-align: left;
            background: #ffffff;
            white-space: nowrap;
        }

        .markdown-body th {
            background: #f6f8fa;
            font-weight: 700;
        }

        .markdown-body blockquote {
            margin: 12px 0;
            padding: 8px 14px;
            border-left: 4px solid #d0d7de;
            background: #f6f8fa;
            color: #57606a;
        }

        .markdown-body pre {
            background: #f6f8fa;
            border: 1px solid #d0d7de;
            border-radius: 10px;
            padding: 14px;
            overflow-x: auto;
            margin: 12px 0;
        }

        .markdown-body code {
            font-family: "Consolas", "Cascadia Code", monospace;
            font-size: 13px;
        }

        .markdown-body :not(pre) > code {
            background: #f6f8fa;
            border: 1px solid #d0d7de;
            border-radius: 6px;
            padding: 2px 6px;
        }

        .answer-preview {
            margin-top: 14px;
            border: 1px solid #dbe4f3;
            border-radius: 14px;
            background: #fbfdff;
            padding: 14px;
        }

        .answer-preview-title {
            font-size: 14px;
            color: #475569;
            margin-bottom: 8px;
            font-weight: 700;
        }

        .draft-version-hint {
            margin-top: 8px;
            font-size: 12px;
            color: #64748b;
        }

        .draft-conflict-panel {
            margin-top: 10px;
            border: 1px solid #fed7aa;
            background: #fff7ed;
            border-radius: 12px;
            padding: 10px 12px;
        }

        .draft-conflict-panel .conflict-title {
            font-size: 13px;
            font-weight: 700;
            color: #9a3412;
            margin-bottom: 6px;
        }

        .draft-conflict-panel .conflict-desc {
            font-size: 12px;
            color: #7c2d12;
            margin-bottom: 8px;
        }

        .draft-conflict-actions {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
        }

        .draft-action-btn {
            border: 1px solid #fdba74;
            background: #ffffff;
            color: #9a3412;
            border-radius: 999px;
            padding: 6px 12px;
            font-size: 12px;
            font-weight: 700;
            cursor: pointer;
        }

        .exam-toolbar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 10px;
            margin-bottom: 10px;
            padding: 10px 12px;
            border-radius: 12px;
            background: #eff6ff;
            border: 1px solid #bfdbfe;
        }

        .timer-chip {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            border-radius: 999px;
            padding: 5px 10px;
            background: #dbeafe;
            color: #1e3a8a;
            font-weight: 700;
            font-size: 13px;
        }

        .timer-chip.warning {
            background: #fee2e2;
            color: #991b1b;
        }

        .sync-chip {
            background: #e2e8f0;
            color: #334155;
        }

        .sync-chip.saving {
            background: #fef3c7;
            color: #92400e;
        }

        .sync-chip.synced {
            background: #dcfce7;
            color: #166534;
        }

        .sync-chip.failed {
            background: #fee2e2;
            color: #991b1b;
        }

        .answer-area {
            width: 100%;
            min-height: 220px;
            resize: vertical;
            border: 1px solid #cbd5e1;
            border-radius: 14px;
            padding: 16px;
            font-size: 16px;
            line-height: 1.6;
            box-sizing: border-box;
            outline: none;
            transition: border-color .2s ease, box-shadow .2s ease;
        }

        .answer-area:focus {
            border-color: #2563eb;
            box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.12);
        }

        .actions {
            display: flex;
            justify-content: flex-end;
            margin-top: 16px;
        }
        
        .stream-result {
            margin-top: 18px;
            border: 1px solid #dbe4f3;
            border-radius: 16px;
            background: #f8fbff;
            padding: 18px;
            display: none;
        }
        
        .stream-result.visible {
            display: block;
        }
        
        .stream-meta {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            margin-bottom: 12px;
        }
        
        .stream-badge {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            border-radius: 999px;
            background: #e2e8f0;
            color: #0f172a;
            padding: 6px 12px;
            font-size: 13px;
            font-weight: 700;
        }
        
        .stream-badge strong {
            color: #2563eb;
        }
        
        .stream-status {
            color: #475569;
            font-size: 14px;
            margin-bottom: 10px;
        }
        
        .stream-feedback {
            min-height: 120px;
            white-space: pre-wrap;
            line-height: 1.8;
            font-size: 15px;
            color: #334155;
        }

        .stream-followup {
            margin-top: 16px;
            padding: 16px;
            border-radius: 14px;
            background: #eef6ff;
            border: 1px solid #cfe0ff;
        }

        .stream-followup-title {
            font-size: 14px;
            font-weight: 700;
            color: #1d4ed8;
            margin-bottom: 8px;
        }

        .stream-followup-question {
            white-space: pre-wrap;
            line-height: 1.8;
            color: #1e293b;
            margin-bottom: 12px;
        }

        .continue-area {
            width: 100%;
            min-height: 140px;
            resize: vertical;
            border: 1px solid #cbd5e1;
            border-radius: 14px;
            padding: 14px;
            font-size: 15px;
            line-height: 1.6;
            box-sizing: border-box;
            outline: none;
            margin-bottom: 12px;
        }

        .stream-actions {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            justify-content: flex-end;
            margin-top: 12px;
        }

        .ghost-btn {
            border: 1px solid #cbd5e1;
            background: #fff;
            color: #0f172a;
            padding: 11px 20px;
            border-radius: 999px;
            font-size: 14px;
            font-weight: 700;
            cursor: pointer;
        }
        
        .stream-cursor {
            display: inline-block;
            width: 8px;
            height: 1em;
            margin-left: 2px;
            background: #2563eb;
            vertical-align: -2px;
            animation: blink 1s steps(1) infinite;
        }
        
        @keyframes blink {
            50% { opacity: 0; }
        }

        .reasoning-wrap {
            margin-top: 14px;
            padding: 12px 14px;
            border: 1px solid #dbe4f3;
            border-radius: 12px;
            background: #f8fbff;
        }

        .reasoning-tip {
            margin-top: 6px;
            color: #64748b;
            font-size: 13px;
        }

        .submit-btn {
            border: none;
            background: linear-gradient(135deg, #2563eb, #1d4ed8);
            color: #fff;
            padding: 12px 22px;
            border-radius: 999px;
            font-size: 15px;
            font-weight: 700;
            cursor: pointer;
            box-shadow: 0 10px 20px rgba(37, 99, 235, 0.22);
        }

        .submit-btn:hover {
            filter: brightness(1.03);
        }

        .empty {
            text-align: center;
            color: #64748b;
            padding: 24px 0;
        }

        .loading-overlay {
            position: fixed;
            inset: 0;
            z-index: 9999;
            background: rgba(15, 23, 42, 0.55);
            display: none;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }

        .loading-panel {
            width: min(480px, 92vw);
            border-radius: 16px;
            background: #ffffff;
            box-shadow: 0 14px 36px rgba(15, 23, 42, 0.22);
            padding: 22px 20px;
            text-align: center;
        }

        .loading-panel .spinner-border {
            width: 2.3rem;
            height: 2.3rem;
            color: #2563eb;
        }

        .loading-text {
            margin-top: 12px;
            color: #1e293b;
            font-weight: 600;
        }

        @media (max-width: 991.98px) {
            .cq-sidebar {
                position: sticky;
                top: 0;
                z-index: 10;
            }
        }

        @media (max-width: 575.98px) {
            .hero,
            .card {
                padding: 20px;
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
            <div class="small text-light-emphasis mt-3">Explain your thinking clearly.</div>
        </aside>

        <main class="col-12 col-lg-10 p-4 p-lg-5 cq-main">
            <a class="btn btn-outline-primary mb-3" href="${pageContext.request.contextPath}/questions">返回题目列表</a>

            <c:choose>
                <c:when test="${not empty question}">
                    <div class="hero">
                        <div class="d-flex flex-wrap justify-content-between align-items-center gap-2">
                            <h1 class="mb-0">${question.title}</h1>
                            <button id="favoriteBtn"
                                    type="button"
                                    class="favorite-btn ${isFavorite ? 'active' : ''}"
                                    data-question-id="${question.id}"
                                    data-favorited="${isFavorite}">
                                ${isFavorite ? '★ 已收藏' : '☆ 收藏'}
                            </button>
                        </div>
                        <div class="meta">
                            <span class="pill">题目 ID：${question.id}</span>
                            <span class="pill">类型：${question.type}</span>
                            <span class="pill">难度：${question.difficulty}</span>
                            <span class="pill">标签：${question.tags}</span>
                        </div>
                    </div>

                    <div class="card">
                        <h2 class="section-title">题目内容</h2>
                        <div id="questionContent" class="content-box markdown-body" data-markdown-source="true">${question.content}</div>
                    </div>

                    <div class="card">
                        <h2 class="section-title">请输入你的答案</h2>
                        <form id="answerForm" action="${pageContext.request.contextPath}/SubmitAnswer" method="post">
                            <input type="hidden" name="questionId" value="${question.id}">
                            <div class="exam-toolbar">
                                <div class="small text-secondary">建议在限定时间内独立完成作答</div>
                                <div class="d-flex align-items-center gap-2">
                                    <div id="draftSyncStatus" class="timer-chip sync-chip">草稿：初始化中</div>
                                    <div id="countdownTimer" class="timer-chip">剩余时间：30:00</div>
                                </div>
                            </div>
                            <textarea class="answer-area" name="userAnswer" placeholder="请在这里输入你的回答..."></textarea>
                            <div class="answer-preview">
                                <div class="answer-preview-title">我的回答（Markdown 预览）</div>
                                <div id="userAnswerPreview" class="markdown-body" data-markdown-source="true">请输入回答后将自动渲染 Markdown 预览。</div>
                                <div id="draftVersionHint" class="draft-version-hint">草稿版本：暂无</div>
                                <div id="draftConflictPanel" class="draft-conflict-panel d-none">
                                    <div class="conflict-title">检测到草稿冲突</div>
                                    <div class="conflict-desc">云端草稿与本地内容不同，请选择保留策略。</div>
                                    <div class="draft-conflict-actions">
                                        <button type="button" id="restoreCloudBtn" class="draft-action-btn">恢复云端草稿</button>
                                        <button type="button" id="pushLocalBtn" class="draft-action-btn">用本地覆盖云端</button>
                                    </div>
                                </div>
                            </div>
                            <div class="reasoning-wrap">
                                <div class="form-check form-switch">
                                    <input class="form-check-input" type="checkbox" role="switch" id="useReasoning" name="useReasoning">
                                    <label class="form-check-label fw-semibold" for="useReasoning">开启 AI 深度思考（响应较慢，建议复杂题目开启）</label>
                                </div>
                                <div class="reasoning-tip">关闭时优先速度，开启时优先思考深度。</div>
                            </div>
                            <div class="actions">
                                <button id="submitBtn" type="submit" class="submit-btn">提交回答</button>
                            </div>
                        </form>
                        
                        <div id="streamResult" class="stream-result" aria-live="polite">
                            <div class="stream-meta">
                                <span class="stream-badge">AI 评分：<strong id="streamScore">-</strong></span>
                                <span class="stream-badge">分类：<strong id="streamCategory">-</strong></span>
                            </div>
                            <div id="streamStatus" class="stream-status">等待提交后开始评测。</div>
                            <div id="streamFeedback" class="stream-feedback markdown-body" data-markdown-source="true"></div>
                            <div id="followUpWrap" class="stream-followup d-none">
                                <div class="stream-followup-title">追问问题</div>
                                <div id="followUpQuestion" class="stream-followup-question"></div>
                                <form id="continueForm" action="${pageContext.request.contextPath}/SubmitAnswerStream" method="post">
                                    <input type="hidden" name="questionId" value="${question.id}">
                                    <input type="hidden" name="useReasoning" id="continueUseReasoning" value="">
                                    <textarea id="continueAnswer" class="continue-area" name="continueAnswer" placeholder="请继续回答追问问题..."></textarea>
                                    <div class="stream-actions">
                                        <button type="button" id="endInterviewBtn" class="ghost-btn">结束面试</button>
                                        <button type="submit" id="continueBtn" class="submit-btn">继续回答</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="card">
                        <div class="empty">未找到该题目</div>
                    </div>
                </c:otherwise>
            </c:choose>
        </main>
    </div>
</div>
<div id="loadingOverlay" class="loading-overlay" aria-hidden="true">
    <div class="loading-panel">
        <div class="spinner-border" role="status" aria-label="loading"></div>
        <div class="loading-text">AI 正在判题中，请耐心等候...</div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>
<script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/highlight.js@11.11.1/lib/highlight.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/highlight.js@11.11.1/lib/languages/java.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/highlight.js@11.11.1/lib/languages/sql.min.js"></script>
<!-- 本地脚本替换示例（如需离线部署，可替换上方 CDN）： -->
<!-- <script src="${pageContext.request.contextPath}/assets/js/marked.min.js"></script> -->
<!-- <script src="${pageContext.request.contextPath}/assets/js/highlight.min.js"></script> -->
<script>
    (function () {
        var form = document.getElementById("answerForm");
        var continueForm = document.getElementById("continueForm");
        var submitBtn = document.getElementById("submitBtn");
        var continueBtn = document.getElementById("continueBtn");
        var endInterviewBtn = document.getElementById("endInterviewBtn");
        var overlay = document.getElementById("loadingOverlay");
        var resultBox = document.getElementById("streamResult");
        var scoreNode = document.getElementById("streamScore");
        var categoryNode = document.getElementById("streamCategory");
        var statusNode = document.getElementById("streamStatus");
        var feedbackNode = document.getElementById("streamFeedback");
        var followUpWrap = document.getElementById("followUpWrap");
        var followUpQuestionNode = document.getElementById("followUpQuestion");
        var continueAnswerNode = document.getElementById("continueAnswer");
        var userAnswerNode = form ? form.querySelector("textarea[name='userAnswer']") : null;
        var userAnswerPreviewNode = document.getElementById("userAnswerPreview");
        var useReasoningNode = document.getElementById("useReasoning");
        var continueUseReasoningNode = document.getElementById("continueUseReasoning");
        var favoriteBtn = document.getElementById("favoriteBtn");
        var countdownNode = document.getElementById("countdownTimer");
        var draftSyncStatusNode = document.getElementById("draftSyncStatus");
        var draftVersionHintNode = document.getElementById("draftVersionHint");
        var draftConflictPanelNode = document.getElementById("draftConflictPanel");
        var restoreCloudBtn = document.getElementById("restoreCloudBtn");
        var pushLocalBtn = document.getElementById("pushLocalBtn");
        var countdownSeconds = 30 * 60;
        var countdownTimer = null;
        var autoSaveTimer = null;
        var blurLastWarnAt = 0;
        var hasSubmitted = false;
        var questionIdNode = form ? form.querySelector("input[name='questionId']") : null;
        var questionId = questionIdNode && questionIdNode.value ? String(questionIdNode.value).trim() : "";
        var draftStorageKey = "cq:draft:question:" + questionId;
        var draftSyncUrl = "${pageContext.request.contextPath}/answer/draft";
        var lastDraftSyncAt = null;
        var serverDraftSnapshot = "";
        var localDraftSnapshot = "";
        
        var typingTimer = null;
        var interviewDirty = false;

        if (window.marked) {
            marked.setOptions({
                gfm: true,
                breaks: true
            });
        }

        function renderMarkdownElement(element, text) {
            if (!element) {
                return;
            }

            var sourceText = text !== undefined && text !== null ? String(text) : (element.textContent || "");
            if (window.marked) {
                element.innerHTML = marked.parse(sourceText);
            } else {
                element.textContent = sourceText;
            }
            if (window.hljs) {
                hljs.highlightAll();
            }
        }

        function renderPageMarkdown() {
            var nodes = document.querySelectorAll("[data-markdown-source='true']");
            for (var i = 0; i < nodes.length; i += 1) {
                renderMarkdownElement(nodes[i]);
            }
        }

        function renderFavoriteButtonState(favorited) {
            if (!favoriteBtn) {
                return;
            }
            if (favorited) {
                favoriteBtn.classList.add("active");
                favoriteBtn.textContent = "★ 已收藏";
                favoriteBtn.setAttribute("data-favorited", "true");
            } else {
                favoriteBtn.classList.remove("active");
                favoriteBtn.textContent = "☆ 收藏";
                favoriteBtn.setAttribute("data-favorited", "false");
            }
        }
        
        function setBusy(isBusy) {
            submitBtn.disabled = isBusy;
            submitBtn.textContent = isBusy ? "AI 正在判题中，请耐心等候..." : "提交回答";
            submitBtn.style.opacity = isBusy ? "0.75" : "1";
            submitBtn.style.cursor = isBusy ? "not-allowed" : "pointer";
            if (continueBtn) {
                continueBtn.disabled = isBusy;
                continueBtn.textContent = isBusy ? "AI 正在继续评测..." : "继续回答";
                continueBtn.style.opacity = isBusy ? "0.75" : "1";
                continueBtn.style.cursor = isBusy ? "not-allowed" : "pointer";
            }
            overlay.style.display = isBusy ? "flex" : "none";
            overlay.setAttribute("aria-hidden", isBusy ? "false" : "true");
        }

        function formatSeconds(totalSeconds) {
            var safe = Math.max(0, totalSeconds);
            var mins = Math.floor(safe / 60);
            var secs = safe % 60;
            return String(mins).padStart(2, "0") + ":" + String(secs).padStart(2, "0");
        }

        function updateCountdownUI() {
            if (!countdownNode) {
                return;
            }
            countdownNode.textContent = "剩余时间：" + formatSeconds(countdownSeconds);
            if (countdownSeconds <= 5 * 60) {
                countdownNode.classList.add("warning");
            } else {
                countdownNode.classList.remove("warning");
            }
        }

        function startCountdown() {
            updateCountdownUI();
            countdownTimer = window.setInterval(function () {
                if (hasSubmitted) {
                    return;
                }
                countdownSeconds -= 1;
                updateCountdownUI();
                if (countdownSeconds <= 0) {
                    window.clearInterval(countdownTimer);
                    countdownTimer = null;
                    hasSubmitted = true;
                    alert("作答时间已到，系统将自动提交当前答案。");
                    form.requestSubmit();
                }
            }, 1000);
        }

        function saveDraft() {
            if (!userAnswerNode || !draftStorageKey) {
                return;
            }
            try {
                localStorage.setItem(draftStorageKey, userAnswerNode.value || "");
            } catch (e) {
                // 本地存储不可用时静默降级。
            }
        }

        function restoreDraft() {
            if (!userAnswerNode || !draftStorageKey) {
                return;
            }
            try {
                var cached = localStorage.getItem(draftStorageKey);
                if (cached && !userAnswerNode.value) {
                    userAnswerNode.value = cached;
                    renderMarkdownElement(userAnswerPreviewNode, cached);
                }
                localDraftSnapshot = userAnswerNode.value || "";
            } catch (e) {
                // 本地存储不可用时静默降级。
            }
        }

        function startDraftAutoSave() {
            autoSaveTimer = window.setInterval(function () {
                saveDraft();
                saveDraftToServer(userAnswerNode ? userAnswerNode.value : "", false);
            }, 30000);
        }

        function formatSyncTime(epochMillis) {
            if (!epochMillis) {
                return "";
            }
            var date = new Date(epochMillis);
            if (isNaN(date.getTime())) {
                return "";
            }
            var hh = String(date.getHours()).padStart(2, "0");
            var mm = String(date.getMinutes()).padStart(2, "0");
            var ss = String(date.getSeconds()).padStart(2, "0");
            return hh + ":" + mm + ":" + ss;
        }

        function setDraftVersionHint(text) {
            if (!draftVersionHintNode) {
                return;
            }
            draftVersionHintNode.textContent = text || "草稿版本：暂无";
        }

        function showDraftConflict(show) {
            if (!draftConflictPanelNode) {
                return;
            }
            draftConflictPanelNode.classList.toggle("d-none", !show);
        }

        function setDraftSyncStatus(state, text) {
            if (!draftSyncStatusNode) {
                return;
            }
            draftSyncStatusNode.classList.remove("saving", "synced", "failed");
            if (state) {
                draftSyncStatusNode.classList.add(state);
            }
            draftSyncStatusNode.textContent = text || "草稿：待同步";
        }

        function saveDraftToServer(text, useBeacon) {
            if (!questionId) {
                return;
            }

            setDraftSyncStatus("saving", "草稿：同步中...");

            var payload = new URLSearchParams();
            payload.append("questionId", questionId);
            payload.append("draft", text || "");

            if (useBeacon && navigator.sendBeacon) {
                try {
                    navigator.sendBeacon(
                        draftSyncUrl,
                        new Blob([payload.toString()], { type: "application/x-www-form-urlencoded;charset=UTF-8" })
                    );
                    setDraftSyncStatus("synced", "草稿：已提交后台保存");
                    return;
                } catch (e) {
                    // sendBeacon 失败时降级为 fetch。
                }
            }

            fetch(draftSyncUrl, {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                    "X-Requested-With": "fetch"
                },
                body: payload.toString(),
                keepalive: !!useBeacon
            })
                .then(function (resp) {
                    if (!resp.ok) {
                        throw new Error("failed");
                    }
                    return resp.json();
                })
                .then(function (data) {
                    if (!data || !data.success) {
                        throw new Error("failed");
                    }
                    serverDraftSnapshot = text || "";
                    localDraftSnapshot = text || "";
                    if (data.updatedAt) {
                        lastDraftSyncAt = data.updatedAt;
                    }
                    var suffix = formatSyncTime(lastDraftSyncAt);
                    if (data.saved) {
                        setDraftSyncStatus("synced", "草稿：已同步" + (suffix ? " " + suffix : ""));
                        setDraftVersionHint("草稿版本：云端 " + (suffix || "刚刚"));
                    } else {
                        setDraftSyncStatus("synced", "草稿：已清空" + (suffix ? " " + suffix : ""));
                        setDraftVersionHint("草稿版本：已清空");
                    }
                    showDraftConflict(false);
                })
                .catch(function () {
                    setDraftSyncStatus("failed", "草稿：同步失败，稍后重试");
                });
        }

        function restoreDraftFromServer() {
            if (!questionId || !userAnswerNode) {
                return;
            }

            fetch(draftSyncUrl + "?questionId=" + encodeURIComponent(questionId), {
                headers: { "X-Requested-With": "fetch" }
            })
                .then(function (resp) {
                    if (!resp.ok) {
                        throw new Error("failed");
                    }
                    return resp.json();
                })
                .then(function (data) {
                    if (!data || !data.success) {
                        setDraftSyncStatus("failed", "草稿：拉取失败");
                        return;
                    }

                    if (data.updatedAt) {
                        lastDraftSyncAt = data.updatedAt;
                    }
                    var serverDraft = typeof data.draft === "string" ? data.draft : "";
                    serverDraftSnapshot = serverDraft;
                    if (serverDraft && !userAnswerNode.value) {
                        userAnswerNode.value = serverDraft;
                        localDraftSnapshot = serverDraft;
                        renderMarkdownElement(userAnswerPreviewNode, serverDraft);
                        saveDraft();
                        setDraftSyncStatus("synced", "草稿：已恢复云端内容");
                        setDraftVersionHint("草稿版本：云端 " + (formatSyncTime(lastDraftSyncAt) || "刚刚"));
                        showDraftConflict(false);
                        return;
                    }

                    if (serverDraft && userAnswerNode.value && serverDraft !== userAnswerNode.value) {
                        localDraftSnapshot = userAnswerNode.value || "";
                        setDraftSyncStatus("failed", "草稿：发现冲突，请选择保留策略");
                        setDraftVersionHint("草稿版本：本地与云端不一致");
                        showDraftConflict(true);
                        return;
                    }

                    var suffix = formatSyncTime(lastDraftSyncAt);
                    setDraftSyncStatus("synced", "草稿：已同步" + (suffix ? " " + suffix : ""));
                    setDraftVersionHint("草稿版本：云端 " + (suffix || "暂无时间"));
                    showDraftConflict(false);
                })
                .catch(function () {
                    setDraftSyncStatus("failed", "草稿：离线，仅本地草稿可用");
                    setDraftVersionHint("草稿版本：本地缓存");
                });
        }

        function syncReasoningFlag(targetNode) {
            if (!targetNode || !useReasoningNode) {
                return;
            }
            targetNode.value = useReasoningNode.checked ? "true" : "";
        }

        function showFollowUp(questionText) {
            renderMarkdownElement(followUpQuestionNode, questionText || "");
            followUpWrap.classList.remove("d-none");
            if (continueAnswerNode) {
                continueAnswerNode.focus();
            }
        }

        function hideFollowUp() {
            followUpWrap.classList.add("d-none");
            followUpQuestionNode.innerHTML = "";
            if (continueAnswerNode) {
                continueAnswerNode.value = "";
            }
        }

        function clearInterviewContext() {
            try {
                navigator.sendBeacon(
                    "${pageContext.request.contextPath}/interview/session",
                    new Blob([], { type: "application/x-www-form-urlencoded" })
                );
            } catch (err) {
                fetch("${pageContext.request.contextPath}/interview/session", {
                    method: "POST",
                    headers: { "Content-Type": "application/x-www-form-urlencoded" },
                    keepalive: true
                });
            }
        }
        
        function showResult() {
            resultBox.classList.add("visible");
        }
        
        function stopTyping() {
            if (typingTimer) {
                clearTimeout(typingTimer);
                typingTimer = null;
            }
        }
        
        function typewriter(text) {
            stopTyping();
            feedbackNode.textContent = "";
            var sourceText = typeof text === "string" ? text : "";
            var index = 0;
            var cursor = document.createElement("span");
            cursor.className = "stream-cursor";
            
            function step() {
                if (index >= sourceText.length) {
                    if (cursor.parentNode) {
                        cursor.parentNode.removeChild(cursor);
                    }
                    renderMarkdownElement(feedbackNode, sourceText);
                    typingTimer = null;
                    setBusy(false);
                    return;
                }
                
                feedbackNode.textContent = sourceText.slice(0, index + 1);
                if (!cursor.parentNode) {
                    feedbackNode.appendChild(cursor);
                }
                index += 1;
                typingTimer = window.setTimeout(step, 12);
            }
            
            feedbackNode.appendChild(cursor);
            step();
        }
        
        function handleSseBlock(block) {
            var lines = block.split(/\r?\n/);
            var eventName = "message";
            var dataParts = [];
            
            for (var i = 0; i < lines.length; i += 1) {
                var line = lines[i];
                if (line.indexOf("event:") === 0) {
                    eventName = line.slice(6).trim();
                } else if (line.indexOf("data:") === 0) {
                    dataParts.push(line.slice(5).trim());
                }
            }
            
            if (dataParts.length === 0) {
                return;
            }
            
            var payload = dataParts.join("\n");
            var data;
            try {
                data = JSON.parse(payload);
            } catch (err) {
                data = payload;
            }
            
            if (eventName === "status") {
                showResult();
                statusNode.textContent = typeof data === "string" ? data : "评测中...";
                return;
            }
            
            if (eventName === "score") {
                showResult();
                scoreNode.textContent = data && data.score !== undefined ? data.score : "-";
                categoryNode.textContent = data && data.category ? data.category : "-";
                return;
            }
            
            if (eventName === "feedback") {
                showResult();
                statusNode.textContent = "评测完成，正在输出结果...";
                typewriter(typeof data === "string" ? data : "");
                return;
            }

            if (eventName === "followup") {
                interviewDirty = true;
                showResult();
                showFollowUp(data && data.followUpQuestion ? data.followUpQuestion : (typeof data === "string" ? data : ""));
                statusNode.textContent = "请继续回答追问问题。";
                return;
            }
            
            if (eventName === "done") {
                showResult();
                statusNode.textContent = "评测已完成。";
                return;
            }
            
            if (eventName === "error") {
                showResult();
                statusNode.textContent = typeof data === "string" ? data : "评测失败。";
                setBusy(false);
            }
        }
        
        async function submitStreaming(formElement) {
            var actionUrl = formElement.action;
            if (actionUrl.indexOf("/SubmitAnswerStream") === -1) {
                actionUrl = actionUrl.replace("/SubmitAnswer", "/SubmitAnswerStream");
            }

            var formData = new FormData(formElement);
            var questionId = formData.get("questionId");
            if (!questionId) {
                var fallbackQuestionInput = document.querySelector("#answerForm input[name='questionId']");
                if (fallbackQuestionInput && fallbackQuestionInput.value) {
                    formData.set("questionId", fallbackQuestionInput.value);
                }
            }

            // Servlet 默认通过 getParameter 读取表单参数，使用 URL 编码可避免 multipart 参数读取为空。
            var requestBody = new URLSearchParams();
            formData.forEach(function (value, key) {
                if (value !== undefined && value !== null) {
                    requestBody.append(key, value);
                }
            });

            var response = await fetch(actionUrl, {
                method: "POST",
                body: requestBody.toString(),
                headers: {
                    "X-Requested-With": "fetch",
                    "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
                }
            });
            
            if (!response.ok || !response.body) {
                throw new Error("评测请求失败");
            }
            
            var reader = response.body.getReader();
            var decoder = new TextDecoder("utf-8");
            var buffer = "";
            
            while (true) {
                var result = await reader.read();
                if (result.done) {
                    break;
                }
                
                buffer += decoder.decode(result.value, { stream: true });
                
                var splitIndex = buffer.indexOf("\n\n");
                while (splitIndex !== -1) {
                    var block = buffer.slice(0, splitIndex).trim();
                    buffer = buffer.slice(splitIndex + 2);
                    if (block) {
                        handleSseBlock(block);
                    }
                    splitIndex = buffer.indexOf("\n\n");
                }
            }
            
            if (buffer.trim()) {
                handleSseBlock(buffer.trim());
            }
        }

        if (!form || !submitBtn || !overlay) {
            return;
        }

        renderPageMarkdown();
        setDraftSyncStatus("saving", "草稿：初始化中...");
        restoreDraft();
        restoreDraftFromServer();
        startDraftAutoSave();
        startCountdown();

        window.addEventListener("blur", function () {
            var now = Date.now();
            if (document.hidden && now - blurLastWarnAt > 15000) {
                blurLastWarnAt = now;
                alert("友情提醒：检测到你已切换窗口，请尽量独立作答，保持测评公平性。");
            }
        });

        if (favoriteBtn) {
            favoriteBtn.addEventListener("click", function () {
                var questionId = favoriteBtn.getAttribute("data-question-id");
                if (!questionId) {
                    return;
                }

                favoriteBtn.disabled = true;
                fetch("${pageContext.request.contextPath}/favorite/toggle", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                        "X-Requested-With": "fetch"
                    },
                    body: new URLSearchParams({ questionId: questionId }).toString()
                })
                    .then(function (resp) {
                        return resp.json().then(function (data) {
                            return { ok: resp.ok, data: data };
                        });
                    })
                    .then(function (result) {
                        if (!result.ok || !result.data || !result.data.success) {
                            throw new Error((result.data && result.data.message) || "收藏操作失败");
                        }
                        renderFavoriteButtonState(!!result.data.favorited);
                    })
                    .catch(function (error) {
                        alert(error && error.message ? error.message : "收藏操作失败，请稍后重试。");
                    })
                    .finally(function () {
                        favoriteBtn.disabled = false;
                    });
            });
        }

        if (userAnswerNode && userAnswerPreviewNode) {
            userAnswerNode.addEventListener("input", function () {
                var value = userAnswerNode.value.trim();
                localDraftSnapshot = userAnswerNode.value || "";
                if (!value) {
                    userAnswerPreviewNode.innerHTML = "请输入回答后将自动渲染 Markdown 预览。";
                    setDraftSyncStatus("saving", "草稿：待同步");
                    showDraftConflict(false);
                    return;
                }
                renderMarkdownElement(userAnswerPreviewNode, value);
                setDraftSyncStatus("saving", "草稿：内容已变更，等待同步");
            });
        }

        if (restoreCloudBtn) {
            restoreCloudBtn.addEventListener("click", function () {
                if (!userAnswerNode) {
                    return;
                }
                userAnswerNode.value = serverDraftSnapshot || "";
                localDraftSnapshot = userAnswerNode.value;
                if (!localDraftSnapshot.trim()) {
                    userAnswerPreviewNode.innerHTML = "请输入回答后将自动渲染 Markdown 预览。";
                } else {
                    renderMarkdownElement(userAnswerPreviewNode, localDraftSnapshot);
                }
                saveDraft();
                showDraftConflict(false);
                setDraftSyncStatus("synced", "草稿：已恢复云端版本");
            });
        }

        if (pushLocalBtn) {
            pushLocalBtn.addEventListener("click", function () {
                saveDraftToServer(userAnswerNode ? userAnswerNode.value : "", false);
            });
        }

        form.addEventListener("submit", function (event) {
            event.preventDefault();
            hasSubmitted = true;
            stopTyping();
            feedbackNode.textContent = "";
            statusNode.textContent = "正在提交并请求流式评测...";
            scoreNode.textContent = "-";
            categoryNode.textContent = "-";
            hideFollowUp();
            showResult();
            interviewDirty = true;
            showDraftConflict(false);
            saveDraftToServer("", false);
            if (continueUseReasoningNode) {
                syncReasoningFlag(continueUseReasoningNode);
            }
            setBusy(true);

            submitStreaming(form)
                .catch(function (error) {
                    statusNode.textContent = error && error.message ? error.message : "评测请求失败。";
                    setBusy(false);
                });
        });

        if (continueForm) {
            continueForm.addEventListener("submit", function (event) {
                event.preventDefault();
                stopTyping();
                if (continueAnswerNode && !continueAnswerNode.value.trim()) {
                    statusNode.textContent = "请输入继续回答内容。";
                    showResult();
                    return;
                }

                statusNode.textContent = "正在提交继续回答...";
                showResult();
                if (continueUseReasoningNode) {
                    syncReasoningFlag(continueUseReasoningNode);
                }
                setBusy(true);

                submitStreaming(continueForm)
                    .then(function () {
                        if (continueAnswerNode) {
                            continueAnswerNode.value = "";
                        }
                    })
                    .catch(function (error) {
                        statusNode.textContent = error && error.message ? error.message : "继续回答提交失败。";
                        setBusy(false);
                    });
            });
        }

        if (endInterviewBtn) {
            endInterviewBtn.addEventListener("click", function () {
                clearInterviewContext();
                interviewDirty = false;
                window.location.href = "${pageContext.request.contextPath}/questions";
            });
        }

        window.addEventListener("beforeunload", function () {
            saveDraft();
            if (userAnswerNode) {
                saveDraftToServer(userAnswerNode.value, true);
            }
            if (interviewDirty) {
                clearInterviewContext();
            }
        });
    })();
</script>
</body>
</html>
