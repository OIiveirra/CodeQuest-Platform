<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>CodeQuest - Prompt 模板管理后台</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <style>
        body {
            margin: 0;
            min-height: 100vh;
            background:
                    radial-gradient(circle at 88% 10%, rgba(14, 165, 233, 0.16), transparent 30%),
                    radial-gradient(circle at 10% 85%, rgba(56, 189, 248, 0.10), transparent 25%),
                    #f5f7fb;
            font-family: "Microsoft YaHei", sans-serif;
        }

        .admin-sidebar {
            background: linear-gradient(180deg, #0f172a 0%, #1e293b 100%);
            color: #e2e8f0;
        }

        .admin-nav-link {
            color: #cbd5e1;
            border-radius: .7rem;
        }

        .admin-nav-link:hover,
        .admin-nav-link.active {
            color: #fff;
            background: rgba(56, 189, 248, 0.22);
        }

        .panel-card {
            border: 0;
            border-radius: 1rem;
            box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
        }

        .template-content {
            min-height: 260px;
            white-space: pre-wrap;
            font-family: Consolas, "Microsoft YaHei", monospace;
        }
    </style>
</head>
<body>
<div class="container-fluid">
    <div class="row min-vh-100">
        <aside class="col-12 col-lg-2 p-3 p-lg-4 admin-sidebar d-flex flex-lg-column justify-content-between">
            <div>
                <div class="fw-bold fs-5 mb-3">CodeQuest Admin</div>
                <div class="small text-secondary-emphasis mb-3">Prompt 模板管理后台</div>
                <nav class="nav nav-pills flex-row flex-lg-column gap-2">
                    <a class="nav-link admin-nav-link" href="${pageContext.request.contextPath}/admin/question?action=list">题目管理</a>
                    <a class="nav-link admin-nav-link active" href="${pageContext.request.contextPath}/admin/prompt?action=list">Prompt 模板</a>
                    <a class="nav-link admin-nav-link" href="${pageContext.request.contextPath}/questions">前台题库</a>
                    <a class="nav-link admin-nav-link" href="${pageContext.request.contextPath}/profile">个人中心</a>
                </nav>
            </div>
            <div class="small text-light-emphasis mt-3">Manage prompts centrally.</div>
        </aside>

        <main class="col-12 col-lg-10 p-4 p-lg-5">
            <ul class="nav nav-tabs mb-3">
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/admin/question?action=list">题目管理</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link active" aria-current="page" href="${pageContext.request.contextPath}/admin/prompt?action=list">Prompt 模板</a>
                </li>
            </ul>

            <div class="d-flex justify-content-between align-items-center mb-3">
                <h3 class="mb-0">Prompt 模板管理</h3>
                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/questions">返回前台</a>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>
            <c:if test="${not empty success}">
                <div class="alert alert-success">${success}</div>
            </c:if>

            <div class="card panel-card mb-4">
                <div class="card-header bg-white">
                    <strong>${editingTemplate != null ? '编辑模板' : '新增模板'}</strong>
                </div>
                <div class="card-body">
                    <form method="post" action="${pageContext.request.contextPath}/admin/prompt">
                        <input type="hidden" name="action" value="save" />
                        <c:if test="${editingTemplate != null}">
                            <input type="hidden" name="id" value="${editingTemplate.id}" />
                        </c:if>

                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label">模板名称</label>
                                <input class="form-control" name="templateName" value="<c:out value='${editingTemplate.templateName}'/>" placeholder="例如：evaluation_prompt" required />
                            </div>
                            <div class="col-md-3">
                                <label class="form-label">状态</label>
                                <select class="form-select" name="status">
                                    <option value="1" <c:if test="${editingTemplate == null || editingTemplate.status == 1}">selected</c:if>>启用</option>
                                    <option value="0" <c:if test="${editingTemplate != null && editingTemplate.status == 0}">selected</c:if>>停用</option>
                                </select>
                            </div>
                            <div class="col-12">
                                <label class="form-label">模板内容</label>
                                <textarea class="form-control template-content" rows="14" name="content" required placeholder="支持 &#36;{userAnswer}、&#36;{standardAnswer}、&#36;{questionContent} 占位符。"><c:out value='${editingTemplate.content}'/></textarea>
                            </div>
                        </div>

                        <div class="mt-3 d-flex gap-2">
                            <button type="submit" class="btn btn-primary">${editingTemplate != null ? '保存修改' : '新增模板'}</button>
                            <c:if test="${editingTemplate != null}">
                                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/prompt?action=list">取消编辑</a>
                            </c:if>
                        </div>
                    </form>
                </div>
            </div>

            <div class="card panel-card">
                <div class="card-header bg-white"><strong>模板列表</strong></div>
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light">
                        <tr>
                            <th>ID</th>
                            <th>模板名称</th>
                            <th>状态</th>
                            <th>内容预览</th>
                            <th class="text-end">操作</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:if test="${empty templateList}">
                            <tr>
                                <td colspan="5" class="text-center text-secondary py-4">暂无模板数据</td>
                            </tr>
                        </c:if>
                        <c:forEach var="t" items="${templateList}">
                            <tr>
                                <td>${t.id}</td>
                                <td>${t.templateName}</td>
                                <td>
                                    <span class="badge ${t.status == 1 ? 'text-bg-success' : 'text-bg-secondary'}">
                                        ${t.status == 1 ? '启用' : '停用'}
                                    </span>
                                </td>
                                <td class="text-muted">
                                    <c:choose>
                                        <c:when test="${not empty t.content and fn:length(t.content) > 80}">
                                            ${fn:substring(t.content, 0, 80)}...
                                        </c:when>
                                        <c:otherwise>
                                            <c:out value="${t.content}" />
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-end">
                                    <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin/prompt?action=edit&id=${t.id}">编辑</a>
                                    <form method="post" action="${pageContext.request.contextPath}/admin/prompt" class="d-inline">
                                        <input type="hidden" name="action" value="delete" />
                                        <input type="hidden" name="id" value="${t.id}" />
                                        <button type="submit" class="btn btn-sm btn-outline-danger" onclick="return confirm('确认删除该模板吗？');">删除</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </main>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1N7N6jIeHz" crossorigin="anonymous"></script>
</body>
</html>
