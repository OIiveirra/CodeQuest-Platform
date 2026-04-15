<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>CodeQuest - 题目列表</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <style>
        :root {
            --cq-bg: #f6f7fb;
            --cq-sidebar: #0f172a;
            --cq-text: #0f172a;
            --cq-muted: #64748b;
        }

        body {
            margin: 0;
            font-family: "Microsoft YaHei", sans-serif;
            background:
                radial-gradient(circle at 90% 8%, rgba(14, 165, 233, 0.18), transparent 32%),
                radial-gradient(circle at 5% 78%, rgba(249, 115, 22, 0.14), transparent 24%),
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

        .page-card {
            border: 0;
            border-radius: 1.25rem;
            box-shadow: 0 14px 36px rgba(15, 23, 42, 0.1);
        }

        .tag {
            display: inline-block;
            padding: 4px 10px;
            border-radius: 999px;
            background: #e0f2fe;
            color: #0c4a6e;
            font-size: 12px;
            font-weight: 600;
        }

        .title-wrap h1 {
            font-size: 1.7rem;
            margin: 0;
            font-weight: 700;
        }

        .search-toolbar {
            background: #f8f9fa;
            border-radius: 1rem;
            box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
        }

        .category-pills-bar {
            background: rgba(255, 255, 255, 0.95);
            border: 1px solid rgba(15, 23, 42, 0.08);
            border-radius: 1rem;
            box-shadow: 0 10px 28px rgba(15, 23, 42, 0.08);
        }

        .category-pills-bar .nav-link {
            color: #334155;
            background: #eef2ff;
            border-radius: 999px;
            padding: 0.55rem 1rem;
            font-weight: 600;
        }

        .category-pills-bar .nav-link:hover {
            color: #0f172a;
            background: #dbe4ff;
        }

        .category-pills-bar .nav-link.active {
            color: #fff;
            background: linear-gradient(135deg, #0d6efd 0%, #2563eb 100%);
        }

        .question-content-cell {
            max-width: 300px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            vertical-align: middle;
        }

        .question-content-cell > span {
            display: inline-block;
            max-width: 100%;
            vertical-align: middle;
        }

        .status-cell {
            width: 72px;
            text-align: center;
            vertical-align: middle;
        }

        .status-icon {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            font-size: 1.15rem;
            line-height: 1;
        }

        .question-table thead th {
            white-space: nowrap;
            vertical-align: middle;
            font-weight: 700;
            color: #0f172a;
            letter-spacing: 0.01em;
        }

        .col-no {
            min-width: 72px;
            text-align: center;
        }

        .col-type,
        .col-difficulty {
            min-width: 76px;
            text-align: center;
        }

        .col-actions {
            min-width: 116px;
            text-align: right;
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
<c:if test="${param.debug == 'true'}">
    <div class="alert alert-warning m-3" role="alert">
        <strong>[DEBUG MODE]</strong><br>
        列表对象是否存在: ${questionList != null} <br>
        列表长度: ${questionList.size()} <br>
        第一条题目标题: ${questionList[0].title}
    </div>
</c:if>
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
            <div class="small text-light-emphasis mt-3">Practice makes confidence.</div>
        </aside>

        <main class="col-12 col-lg-10 p-4 p-lg-5 cq-main">
            <div class="title-wrap mb-4">
                <h1>题目列表</h1>
                <div class="text-secondary">浏览并选择题目开始作答</div>
            </div>

            <c:if test="${not empty sessionScope.loginUser and (sessionScope.loginUser.role == 'admin' or sessionScope.loginUser.role == 'ADMIN')}">
                <div class="d-flex flex-wrap gap-2 mb-3">
                    <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/admin/question?action=list">进入后台管理</a>
                    <button class="btn btn-primary" type="button" data-bs-toggle="modal" data-bs-target="#quickAddModal">快速新增题目</button>
                </div>
            </c:if>

            <div class="search-toolbar bg-light p-3 p-md-4 mb-3">
                <form class="row g-3 align-items-end" method="get" action="${pageContext.request.contextPath}/questions">
                    <div class="col-12 col-md-6">
                        <label for="searchKeyword" class="form-label fw-semibold">关键词</label>
                        <input
                                id="searchKeyword"
                                name="keyword"
                                type="text"
                                class="form-control"
                                placeholder="请输入题目关键词"
                                value="${param.keyword}" />
                    </div>
                    <div class="col-6 col-md-2">
                        <label for="searchDifficulty" class="form-label fw-semibold">难度</label>
                        <select id="searchDifficulty" name="difficulty" class="form-select">
                            <option value="0">全部难度</option>
                            <option value="1" ${param.difficulty == '1' ? 'selected' : ''}>1</option>
                            <option value="2" ${param.difficulty == '2' ? 'selected' : ''}>2</option>
                            <option value="3" ${param.difficulty == '3' ? 'selected' : ''}>3</option>
                            <option value="4" ${param.difficulty == '4' ? 'selected' : ''}>4</option>
                            <option value="5" ${param.difficulty == '5' ? 'selected' : ''}>5</option>
                        </select>
                    </div>
                    <div class="col-6 col-md-2">
                        <label for="searchMinDifficulty" class="form-label fw-semibold">最小难度</label>
                        <input id="searchMinDifficulty" name="minDifficulty" type="number" min="1" max="5" class="form-control" value="${param.minDifficulty}" />
                    </div>
                    <div class="col-6 col-md-2">
                        <label for="searchMaxDifficulty" class="form-label fw-semibold">最大难度</label>
                        <input id="searchMaxDifficulty" name="maxDifficulty" type="number" min="1" max="5" class="form-control" value="${param.maxDifficulty}" />
                    </div>
                    <div class="col-12 col-md-4">
                        <label for="searchTags" class="form-label fw-semibold">标签（逗号分隔）</label>
                        <input id="searchTags" name="tags" type="text" class="form-control" placeholder="Java,算法" value="${param.tags}" />
                    </div>
                    <div class="col-6 col-md-2">
                        <label for="searchSortBy" class="form-label fw-semibold">排序字段</label>
                        <select id="searchSortBy" name="sortBy" class="form-select">
                            <option value="id" ${param.sortBy == 'id' || empty param.sortBy ? 'selected' : ''}>题号</option>
                            <option value="difficulty" ${param.sortBy == 'difficulty' ? 'selected' : ''}>难度</option>
                            <option value="title" ${param.sortBy == 'title' ? 'selected' : ''}>标题</option>
                            <option value="type" ${param.sortBy == 'type' ? 'selected' : ''}>类型</option>
                        </select>
                    </div>
                    <div class="col-6 col-md-2">
                        <label for="searchSortDir" class="form-label fw-semibold">排序方向</label>
                        <select id="searchSortDir" name="sortDir" class="form-select">
                            <option value="ASC" ${param.sortDir == 'ASC' || empty param.sortDir ? 'selected' : ''}>升序</option>
                            <option value="DESC" ${param.sortDir == 'DESC' ? 'selected' : ''}>降序</option>
                        </select>
                    </div>
                    <div class="col-6 col-md-2">
                        <label for="searchPageSize" class="form-label fw-semibold">每页数量</label>
                        <select id="searchPageSize" name="pageSize" class="form-select">
                            <option value="10" ${param.pageSize == '10' ? 'selected' : ''}>10</option>
                            <option value="20" ${param.pageSize == '20' || empty param.pageSize ? 'selected' : ''}>20</option>
                            <option value="50" ${param.pageSize == '50' ? 'selected' : ''}>50</option>
                        </select>
                    </div>
                    <div class="col-6 col-md-2 d-grid">
                        <button type="submit" class="btn btn-primary">搜索</button>
                    </div>
                    <div class="col-6 col-md-2 d-grid">
                        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/questions">重置</a>
                    </div>
                    <div class="col-12 col-md-4 d-flex gap-2">
                        <a class="btn btn-outline-success flex-fill" href="${pageContext.request.contextPath}/questions/export?format=csv&keyword=${param.keyword}&category=${param.category}&difficulty=${param.difficulty}&minDifficulty=${param.minDifficulty}&maxDifficulty=${param.maxDifficulty}&tags=${param.tags}&sortBy=${param.sortBy}&sortDir=${param.sortDir}">导出 CSV</a>
                    </div>
                </form>
            </div>

            <div id="activeFilterBar" class="d-none mb-3">
                <div class="card border-0 shadow-sm">
                    <div class="card-body py-2">
                        <div class="d-flex flex-wrap align-items-center gap-2">
                            <span class="small fw-semibold text-secondary">当前筛选</span>
                            <div id="activeFilterChips" class="d-flex flex-wrap gap-2"></div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="category-pills-bar p-3 mb-4">
                <div class="d-flex flex-wrap align-items-center gap-3">
                    <span class="small fw-semibold text-secondary flex-shrink-0">快速分类</span>
                    <nav class="nav nav-pills flex-wrap gap-2">
                        <c:url var="categoryAllUrl" value="/questions">
                            <c:param name="keyword" value="${param.keyword}" />
                            <c:param name="difficulty" value="${param.difficulty}" />
                        </c:url>
                        <a class="nav-link ${empty param.category ? 'active' : ''}" href="${categoryAllUrl}">全部</a>

                        <c:url var="categoryJavaUrl" value="/questions">
                            <c:param name="keyword" value="${param.keyword}" />
                            <c:param name="difficulty" value="${param.difficulty}" />
                            <c:param name="category" value="Java" />
                        </c:url>
                        <a class="nav-link ${param.category == 'Java' ? 'active' : ''}" href="${categoryJavaUrl}">Java</a>

                        <c:url var="categoryDatabaseUrl" value="/questions">
                            <c:param name="keyword" value="${param.keyword}" />
                            <c:param name="difficulty" value="${param.difficulty}" />
                            <c:param name="category" value="数据库" />
                        </c:url>
                        <a class="nav-link ${param.category == '数据库' ? 'active' : ''}" href="${categoryDatabaseUrl}">数据库</a>

                        <c:url var="categoryAlgorithmUrl" value="/questions">
                            <c:param name="keyword" value="${param.keyword}" />
                            <c:param name="difficulty" value="${param.difficulty}" />
                            <c:param name="category" value="算法" />
                        </c:url>
                        <a class="nav-link ${param.category == '算法' ? 'active' : ''}" href="${categoryAlgorithmUrl}">算法</a>

                        <c:url var="categoryConcurrencyUrl" value="/questions">
                            <c:param name="keyword" value="${param.keyword}" />
                            <c:param name="difficulty" value="${param.difficulty}" />
                            <c:param name="category" value="并发" />
                        </c:url>
                        <a class="nav-link ${param.category == '并发' ? 'active' : ''}" href="${categoryConcurrencyUrl}">并发</a>

                        <c:url var="categoryDesignUrl" value="/questions">
                            <c:param name="keyword" value="${param.keyword}" />
                            <c:param name="difficulty" value="${param.difficulty}" />
                            <c:param name="category" value="系统设计" />
                        </c:url>
                        <a class="nav-link ${param.category == '系统设计' ? 'active' : ''}" href="${categoryDesignUrl}">系统设计</a>
                    </nav>
                </div>
            </div>

            <div class="card page-card">
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0 question-table">
                            <c:set var="questionColumnCount" value="8" />
                            <c:if test="${not empty sessionScope.loginUser and (sessionScope.loginUser.role == 'admin' or sessionScope.loginUser.role == 'ADMIN')}">
                                <c:set var="questionColumnCount" value="9" />
                            </c:if>
                            <thead class="table-light">
                            <tr>
                                <th class="status-cell">状态</th>
                                <th class="col-no">题号</th>
                                <th>标题</th>
                                <th>内容</th>
                                <th class="col-type">类型</th>
                                <th class="col-difficulty">难度</th>
                                <th>标签</th>
                                <th>标准答案</th>
                                <c:if test="${not empty sessionScope.loginUser and (sessionScope.loginUser.role == 'admin' or sessionScope.loginUser.role == 'ADMIN')}">
                                    <th class="col-actions">管理操作</th>
                                </c:if>
                            </tr>
                            </thead>
                            <tbody>
                            <c:if test="${empty questionList}">
                                <tr>
                                    <td colspan="${questionColumnCount}" class="text-center text-secondary py-4">暂无题目数据</td>
                                </tr>
                            </c:if>
                            <c:if test="${not empty questionList}">
                                <c:forEach var="q" items="${questionList}" varStatus="st">
                                    <tr>
                                        <td class="status-cell">
                                            <c:choose>
                                                <c:when test="${q.completed}">
                                                    <span class="status-icon text-success" title="已完成" aria-label="已完成">&#10003;</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="status-icon text-secondary" title="未开始" aria-label="未开始">&#9679;</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="col-no">${(page - 1) * pageSize + st.count}</td>
                                        <td>
                                            <a class="link-primary fw-semibold text-decoration-none" href="QuestionDetail?id=${q.id}">${q.title}</a>
                                        </td>
                                        <td class="question-content-cell" title="<c:out value='${q.content}' />"><span><c:out value="${q.content}" /></span></td>
                                        <td class="col-type">
                                            <c:choose>
                                                <c:when test="${q.type != null}">${q.type}</c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="col-difficulty">
                                            <c:choose>
                                                <c:when test="${q.difficulty == 1 or q.difficulty == 2}">
                                                    <span class="badge bg-success">简单</span>
                                                </c:when>
                                                <c:when test="${q.difficulty == 3 or q.difficulty == 4}">
                                                    <span class="badge bg-warning text-dark">中等</span>
                                                </c:when>
                                                <c:when test="${q.difficulty == 5}">
                                                    <span class="badge bg-danger">困难</span>
                                                </c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty q.tags}">
                                                    <span class="tag">${q.tags}</span>
                                                </c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty q.standardAnswer}">${q.standardAnswer}</c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <c:if test="${not empty sessionScope.loginUser and (sessionScope.loginUser.role == 'admin' or sessionScope.loginUser.role == 'ADMIN')}">
                                            <td class="col-actions">
                                                <div class="dropdown">
                                                    <button class="btn btn-sm btn-outline-primary dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                                                        操作
                                                    </button>
                                                    <ul class="dropdown-menu dropdown-menu-end">
                                                        <li>
                                                            <a class="dropdown-item" href="${pageContext.request.contextPath}/admin/question?action=edit&id=${q.id}">编辑</a>
                                                        </li>
                                                        <li><hr class="dropdown-divider"></li>
                                                        <li>
                                                            <form method="post" action="${pageContext.request.contextPath}/admin/question" class="px-3 py-1 m-0">
                                                                <input type="hidden" name="action" value="delete" />
                                                                <input type="hidden" name="id" value="${q.id}" />
                                                                <button type="submit" class="btn btn-link text-danger text-decoration-none p-0 w-100 text-start"
                                                                        onclick="return confirm('确认删除该题目吗？');">删除</button>
                                                            </form>
                                                        </li>
                                                    </ul>
                                                </div>
                                            </td>
                                        </c:if>
                                    </tr>
                                </c:forEach>
                            </c:if>
                            </tbody>
                        </table>
                    </div>

                    <c:if test="${totalPages > 1}">
                        <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 p-3 border-top bg-light-subtle">
                            <div class="small text-secondary">共 ${totalCount} 条，当前第 ${page}/${totalPages} 页</div>
                            <nav aria-label="questions pagination">
                                <ul class="pagination pagination-sm mb-0">
                                    <li class="page-item ${page <= 1 ? 'disabled' : ''}">
                                        <a class="page-link" href="${pageContext.request.contextPath}/questions?keyword=${param.keyword}&category=${param.category}&difficulty=${param.difficulty}&minDifficulty=${param.minDifficulty}&maxDifficulty=${param.maxDifficulty}&tags=${param.tags}&sortBy=${param.sortBy}&sortDir=${param.sortDir}&pageSize=${pageSize}&page=${page - 1}">上一页</a>
                                    </li>
                                    <li class="page-item disabled"><span class="page-link">${page}</span></li>
                                    <li class="page-item ${page >= totalPages ? 'disabled' : ''}">
                                        <a class="page-link" href="${pageContext.request.contextPath}/questions?keyword=${param.keyword}&category=${param.category}&difficulty=${param.difficulty}&minDifficulty=${param.minDifficulty}&maxDifficulty=${param.maxDifficulty}&tags=${param.tags}&sortBy=${param.sortBy}&sortDir=${param.sortDir}&pageSize=${pageSize}&page=${page + 1}">下一页</a>
                                    </li>
                                </ul>
                            </nav>
                        </div>
                    </c:if>
                </div>
            </div>
        </main>
    </div>
</div>

<c:if test="${not empty sessionScope.loginUser and (sessionScope.loginUser.role == 'admin' or sessionScope.loginUser.role == 'ADMIN')}">
    <div class="modal fade" id="quickAddModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-lg modal-dialog-scrollable">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">快速新增题目</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <form method="post" action="${pageContext.request.contextPath}/admin/question">
                    <div class="modal-body">
                        <input type="hidden" name="action" value="add" />
                        <div class="row g-3 align-items-end mb-2">
                            <div class="col-md-9">
                                <label class="form-label">AI 生成关键词</label>
                                <input id="aiKeyword" class="form-control" type="text" placeholder="例如：Java 集合" />
                            </div>
                            <div class="col-md-3 d-grid">
                                <button id="aiFillBtn" type="button" class="btn btn-outline-primary">AI 自动填充</button>
                            </div>
                        </div>
                        <div id="aiFillAlert" class="alert d-none" role="alert"></div>
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label">标题</label>
                                <input id="questionTitle" class="form-control" name="title" required />
                            </div>
                            <div class="col-md-3">
                                <label class="form-label">类型</label>
                                <input id="questionType" class="form-control" name="type" placeholder="如：1" />
                            </div>
                            <div class="col-md-3">
                                <label class="form-label">难度</label>
                                <input id="questionDifficulty" class="form-control" name="difficulty" placeholder="1-5" />
                            </div>
                            <div class="col-12">
                                <label class="form-label">内容</label>
                                <textarea id="questionContent" class="form-control" rows="4" name="content" required></textarea>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">标签</label>
                                <input id="questionTags" class="form-control" name="tags" placeholder="例如：Java基础,算法" />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">标准答案</label>
                                <textarea id="questionStandardAnswer" class="form-control" rows="3" name="standardAnswer"></textarea>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">取消</button>
                        <button type="submit" class="btn btn-primary">提交新增</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</c:if>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script><script>
    (function () {
        var keywordInput = document.getElementById("aiKeyword");
        var fillBtn = document.getElementById("aiFillBtn");
        var alertBox = document.getElementById("aiFillAlert");
        var titleInput = document.getElementById("questionTitle");
        var typeInput = document.getElementById("questionType");
        var difficultyInput = document.getElementById("questionDifficulty");
        var contentInput = document.getElementById("questionContent");
        var tagsInput = document.getElementById("questionTags");
        var standardAnswerInput = document.getElementById("questionStandardAnswer");

        if (!fillBtn || !keywordInput) {
            return;
        }

        function showAlert(type, message) {
            if (!alertBox) {
                return;
            }
            alertBox.className = "alert alert-" + type;
            alertBox.textContent = message;
            alertBox.classList.remove("d-none");
        }

        function hideAlert() {
            if (!alertBox) {
                return;
            }
            alertBox.className = "alert d-none";
            alertBox.textContent = "";
        }

        function fillField(field, value) {
            if (!field || value === undefined || value === null) {
                return;
            }
            field.value = String(value);
        }

        fillBtn.addEventListener("click", async function () {
            var keyword = keywordInput.value.trim();
            if (!keyword) {
                showAlert("warning", "请输入关键词，例如：Java 集合。");
                return;
            }

            hideAlert();
            fillBtn.disabled = true;
            fillBtn.textContent = "生成中...";

            try {
                var response = await fetch("${pageContext.request.contextPath}/admin/ai", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                        "X-Requested-With": "fetch"
                    },
                    body: new URLSearchParams({
                        keyword: keyword,
                        model: "deepseek-reasoner"
                    })
                });

                var result = await response.json();
                if (!response.ok || !result.success) {
                    throw new Error(result.message || "AI 返回失败");
                }

                var data = result.data || {};
                fillField(titleInput, data.title);
                fillField(typeInput, data.type);
                fillField(difficultyInput, data.difficulty);
                fillField(contentInput, data.content);
                fillField(tagsInput, data.tags);
                fillField(standardAnswerInput, data.standardAnswer);
                showAlert("success", "AI 自动填充完成，可直接修改后提交。");
            } catch (error) {
                showAlert("danger", error && error.message ? error.message : "AI 自动填充失败，请重试。" );
            } finally {
                fillBtn.disabled = false;
                fillBtn.textContent = "AI 自动填充";
            }
        });
    })();

    (function () {
        var addButtonRow = document.querySelector('.d-flex.flex-wrap.gap-2.mb-3');
        if (!addButtonRow) {
            return;
        }

        var importBtn = document.createElement('button');
        importBtn.type = 'button';
        importBtn.className = 'btn btn-outline-success';
        importBtn.setAttribute('data-bs-toggle', 'modal');
        importBtn.setAttribute('data-bs-target', '#csvImportModal');
        importBtn.textContent = '导入题库 CSV';
        addButtonRow.appendChild(importBtn);
    })();

    (function () {
        var modalHtml = ''
            + '<div class="modal fade" id="csvImportModal" tabindex="-1" aria-hidden="true">'
            + '  <div class="modal-dialog">'
            + '    <div class="modal-content">'
            + '      <div class="modal-header"><h5 class="modal-title">导入题库 CSV</h5><button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button></div>'
            + '      <div class="modal-body">'
            + '        <div class="mb-3"><label class="form-label">CSV 文件</label><input id="csvImportFile" type="file" class="form-control" accept=".csv" /></div>'
            + '        <div id="csvImportAlert" class="alert d-none" role="alert"></div>'
            + '        <div class="small text-secondary">CSV 列顺序建议：标题,内容,类型,难度,标签,标准答案。首行可为表头。同名题目会自动合并更新。</div>'
            + '      </div>'
            + '      <div class="modal-footer">'
            + '        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">取消</button>'
            + '        <button type="button" id="csvImportSubmit" class="btn btn-success">开始导入</button>'
            + '      </div>'
            + '    </div>'
            + '  </div>'
            + '</div>';
        document.body.insertAdjacentHTML('beforeend', modalHtml);

        var submitBtn = document.getElementById('csvImportSubmit');
        var fileInput = document.getElementById('csvImportFile');
        var alertBox = document.getElementById('csvImportAlert');
        if (!submitBtn || !fileInput || !alertBox) {
            return;
        }

        function show(type, message) {
            alertBox.className = 'alert alert-' + type;
            alertBox.textContent = message;
            alertBox.classList.remove('d-none');
        }

        function showImportErrors(lines) {
            var old = document.getElementById('csvImportErrorLines');
            if (old) {
                old.remove();
            }
            if (!lines || !lines.length) {
                return;
            }
            var box = document.createElement('pre');
            box.id = 'csvImportErrorLines';
            box.className = 'small bg-light border rounded p-2 mt-2 mb-0';
            box.style.whiteSpace = 'pre-wrap';
            box.textContent = lines.join('\n');
            alertBox.insertAdjacentElement('afterend', box);
        }

        submitBtn.addEventListener('click', async function () {
            var file = fileInput.files && fileInput.files[0];
            if (!file) {
                show('warning', '请先选择 CSV 文件。');
                return;
            }

            submitBtn.disabled = true;
            submitBtn.textContent = '导入中...';
            try {
                var formData = new FormData();
                formData.append('action', 'importCsv');
                formData.append('file', file);

                var response = await fetch('${pageContext.request.contextPath}/questions/export', {
                    method: 'POST',
                    body: formData,
                    headers: {
                        'X-Requested-With': 'fetch'
                    }
                });
                var result;
                try {
                    result = await response.json();
                } catch (e) {
                    result = {};
                }
                if (!response.ok || !result.success) {
                    throw new Error((result && result.message) ? result.message : '导入失败');
                }
                if (result.invalid && result.invalid > 0) {
                    show('warning', '导入完成：解析 ' + result.parsed + ' 条，成功写入 ' + result.inserted + ' 条，失败 ' + result.invalid + ' 条。');
                    var errLines = [];
                    if (Array.isArray(result.errors)) {
                        for (var i = 0; i < result.errors.length; i += 1) {
                            var item = result.errors[i] || {};
                            errLines.push('第 ' + (item.line || '?') + ' 行：' + (item.message || '格式错误'));
                        }
                    }
                    showImportErrors(errLines);
                } else {
                    show('success', '导入完成：解析 ' + result.parsed + ' 条，成功写入 ' + result.inserted + ' 条。');
                    showImportErrors(null);
                }
            } catch (err) {
                show('danger', err && err.message ? err.message : '导入失败，请检查 CSV 格式。');
                showImportErrors(null);
            } finally {
                submitBtn.disabled = false;
                submitBtn.textContent = '开始导入';
            }
        });
    })();

    (function () {
        var bar = document.getElementById('activeFilterBar');
        var chips = document.getElementById('activeFilterChips');
        if (!bar || !chips) {
            return;
        }

        var url = new URL(window.location.href);
        var params = url.searchParams;
        var labels = {
            keyword: '关键词',
            category: '分类',
            difficulty: '难度',
            minDifficulty: '最小难度',
            maxDifficulty: '最大难度',
            tags: '标签',
            sortBy: '排序字段',
            sortDir: '排序方向',
            pageSize: '每页'
        };

        function addChip(key, value) {
            if (!value) {
                return;
            }
            var wrapper = document.createElement('span');
            wrapper.className = 'badge text-bg-light border d-inline-flex align-items-center gap-1 py-2 px-2';

            var text = document.createElement('span');
            text.textContent = labels[key] + '：' + value;
            wrapper.appendChild(text);

            var closeBtn = document.createElement('button');
            closeBtn.type = 'button';
            closeBtn.className = 'btn btn-sm btn-link text-danger p-0 text-decoration-none';
            closeBtn.textContent = 'x';
            closeBtn.addEventListener('click', function () {
                var next = new URL(window.location.href);
                next.searchParams.delete(key);
                next.searchParams.delete('page');
                window.location.href = next.toString();
            });
            wrapper.appendChild(closeBtn);

            chips.appendChild(wrapper);
        }

        var hasAny = false;
        Object.keys(labels).forEach(function (key) {
            var value = params.get(key);
            if (value && value !== '0') {
                hasAny = true;
                addChip(key, value);
            }
        });

        if (hasAny) {
            var reset = document.createElement('a');
            reset.className = 'badge text-bg-secondary text-decoration-none py-2 px-2';
            reset.href = '${pageContext.request.contextPath}/questions';
            reset.textContent = '清空全部';
            chips.appendChild(reset);
            bar.classList.remove('d-none');
        }
    })();
</script></body>
</html>
