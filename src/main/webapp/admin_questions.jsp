<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>CodeQuest - 题目管理后台</title>
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
    </style>
</head>
<body>
<div class="container-fluid">
    <div class="row min-vh-100">
        <aside class="col-12 col-lg-2 p-3 p-lg-4 admin-sidebar d-flex flex-lg-column justify-content-between">
            <div>
                <div class="fw-bold fs-5 mb-3">CodeQuest Admin</div>
                <div class="small text-secondary-emphasis mb-3">题目管理后台</div>
                <nav class="nav nav-pills flex-row flex-lg-column gap-2">
                    <a class="nav-link admin-nav-link active" href="${pageContext.request.contextPath}/admin/question?action=list">题目管理</a>
                    <a class="nav-link admin-nav-link" href="${pageContext.request.contextPath}/admin/prompt?action=list">Prompt 模板</a>
                    <a class="nav-link admin-nav-link" href="${pageContext.request.contextPath}/questions">前台题库</a>
                    <a class="nav-link admin-nav-link" href="${pageContext.request.contextPath}/profile">个人中心</a>
                </nav>
            </div>
            <div class="small text-light-emphasis mt-3">Manage question quality.</div>
        </aside>

        <main class="col-12 col-lg-10 p-4 p-lg-5">
            <ul class="nav nav-tabs mb-3">
                <li class="nav-item">
                    <a class="nav-link active" aria-current="page" href="${pageContext.request.contextPath}/admin/question?action=list">题目管理</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/admin/prompt?action=list">Prompt 模板</a>
                </li>
            </ul>

            <div class="d-flex justify-content-between align-items-center mb-3">
                <h3 class="mb-0">题目管理后台</h3>
                <div class="d-flex gap-2">
                    <a class="btn btn-success" href="${pageContext.request.contextPath}/admin/export">导出题库 (CSV)</a>
                    <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/questions">返回题目列表</a>
                </div>
            </div>

            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>
            <c:if test="${not empty success}">
                <div class="alert alert-success">${success}</div>
            </c:if>

            <div class="card panel-card mb-4">
                <div class="card-header bg-white">
                    <strong>${editingQuestion != null ? '编辑题目' : '新增题目'}</strong>
                </div>
                <div class="card-body">
                    <form method="post" action="${pageContext.request.contextPath}/admin/question">
                        <input type="hidden" name="action" value="${editingQuestion != null ? 'update' : 'add'}" />
                        <c:if test="${editingQuestion != null}">
                            <input type="hidden" name="id" value="${editingQuestion.id}" />
                        </c:if>

                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label">标题</label>
                                <input class="form-control" name="title" value="${editingQuestion.title}" required />
                            </div>
                            <div class="col-md-3">
                                <label class="form-label">类型</label>
                                <input class="form-control" name="type" value="${editingQuestion.type}" placeholder="如：1" />
                            </div>
                            <div class="col-md-3">
                                <label class="form-label">难度</label>
                                <input class="form-control" name="difficulty" value="${editingQuestion.difficulty}" placeholder="1-5" />
                            </div>
                            <div class="col-12">
                                <label class="form-label">题目内容</label>
                                <textarea class="form-control" rows="4" name="content" required>${editingQuestion.content}</textarea>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">分类标签</label>
                                <input class="form-control" name="tags" value="${editingQuestion.tags}" placeholder="例如：Java基础,算法" />
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">标准答案（重点维护）</label>
                                <textarea class="form-control" rows="3" name="standardAnswer" placeholder="建议写结构化标准答案，便于 AI 评分。">${editingQuestion.standardAnswer}</textarea>
                            </div>
                        </div>

                        <div class="mt-3 d-flex gap-2">
                            <button type="submit" class="btn btn-primary">${editingQuestion != null ? '保存修改' : '新增题目'}</button>
                            <c:if test="${editingQuestion != null}">
                                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/question?action=list">取消编辑</a>
                            </c:if>
                        </div>
                    </form>
                </div>
            </div>

            <div class="card panel-card">
                <div class="card-header bg-white"><strong>题目列表</strong></div>
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light">
                        <tr>
                            <th>ID</th>
                            <th>标题</th>
                            <th>类型</th>
                            <th>难度</th>
                            <th>标签</th>
                            <th class="text-end">操作</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:if test="${empty questionList}">
                            <tr>
                                <td colspan="6" class="text-center text-secondary py-4">暂无题目数据</td>
                            </tr>
                        </c:if>
                        <c:forEach var="q" items="${questionList}">
                            <tr>
                                <td>${q.id}</td>
                                <td>${q.title}</td>
                                <td>${q.type}</td>
                                <td>${q.difficulty}</td>
                                <td>${q.tags}</td>
                                <td class="text-end">
                                    <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin/question?action=edit&id=${q.id}">编辑</a>
                                    <form method="post" action="${pageContext.request.contextPath}/admin/question" class="d-inline">
                                        <input type="hidden" name="action" value="delete" />
                                        <input type="hidden" name="id" value="${q.id}" />
                                        <button type="submit" class="btn btn-sm btn-outline-danger"
                                                onclick="return confirm('确认删除该题目吗？');">删除</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>
</body>
</html>
