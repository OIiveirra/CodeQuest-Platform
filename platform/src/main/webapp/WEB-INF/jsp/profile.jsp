<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>CodeQuest - 个人中心</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <script src="https://cdn.jsdelivr.net/npm/echarts@5.5.1/dist/echarts.min.js"></script>
    <style>
        body {
            margin: 0;
            min-height: 100vh;
            font-family: "Microsoft YaHei", sans-serif;
            background:
                radial-gradient(circle at 92% 8%, rgba(14, 165, 233, 0.18), transparent 30%),
                radial-gradient(circle at 6% 90%, rgba(249, 115, 22, 0.14), transparent 24%),
                #f6f7fb;
            color: #0f172a;
        }

        .cq-sidebar {
            background: linear-gradient(180deg, #111827 0%, #0f172a 70%, #1e293b 100%);
            color: #e2e8f0;
        }

        .cq-nav-link {
            color: #cbd5e1;
            border-radius: .75rem;
        }

        .cq-nav-link:hover,
        .cq-nav-link.active {
            color: #fff;
            background: rgba(14, 165, 233, 0.22);
        }

        .profile-card {
            border: 0;
            border-radius: 1rem;
            box-shadow: 0 12px 28px rgba(15, 23, 42, 0.08);
        }

        .avatar-preview {
            width: 160px;
            height: 160px;
            border-radius: 50%;
            object-fit: cover;
            border: 4px solid #e2e8f0;
            background: #fff;
        }

        .avatar-default {
            width: 160px;
            height: 160px;
            border-radius: 50%;
            border: 4px solid #e2e8f0;
            display: flex;
            align-items: center;
            justify-content: center;
            background: linear-gradient(135deg, #dbeafe, #e2e8f0);
            color: #2563eb;
            font-weight: 700;
            font-size: 40px;
        }

        .radar-box {
            height: 360px;
            width: 100%;
            border-radius: .75rem;
            background: linear-gradient(160deg, #f8fbff 0%, #eef5ff 100%);
        }

        .trend-box {
            height: 320px;
            width: 100%;
            border-radius: .75rem;
            background: linear-gradient(160deg, #f8fbff 0%, #edf4ff 100%);
        }

        .timeline {
            position: relative;
            margin: 0;
            padding: 0;
            list-style: none;
        }

        .timeline:before {
            content: "";
            position: absolute;
            left: 12px;
            top: 0;
            bottom: 0;
            width: 2px;
            background: #bfdbfe;
        }

        .timeline-item {
            position: relative;
            padding-left: 36px;
            margin-bottom: 18px;
        }

        .timeline-item:before {
            content: "";
            position: absolute;
            left: 6px;
            top: 8px;
            width: 14px;
            height: 14px;
            border-radius: 50%;
            background: #2563eb;
            border: 2px solid #dbeafe;
        }
    </style>
</head>
<body>
<div class="container-fluid">
    <div class="row min-vh-100">
        <aside class="col-12 col-lg-2 p-3 p-lg-4 cq-sidebar d-flex flex-lg-column justify-content-between">
            <div>
                <div class="fw-bold fs-5 mb-3">CodeQuest</div>
                <div class="small text-secondary-emphasis mb-3">面试训练平台</div>
                <nav class="nav nav-pills flex-row flex-lg-column gap-2">
                    <a class="nav-link cq-nav-link" href="${pageContext.request.contextPath}/">首页</a>
                    <a class="nav-link cq-nav-link" href="${pageContext.request.contextPath}/questions">题目列表</a>
                    <a class="nav-link cq-nav-link active" href="${pageContext.request.contextPath}/profile">个人中心</a>
                    <a class="nav-link cq-nav-link" href="${pageContext.request.contextPath}/logout">退出登录</a>
                </nav>
            </div>
            <div class="small text-light-emphasis mt-3">Own your interview growth.</div>
        </aside>

        <main class="col-12 col-lg-10 p-4 p-lg-5">
            <c:if test="${not empty profileLoadWarning}">
                <div class="alert alert-warning border-0 shadow-sm mb-4">${profileLoadWarning}</div>
            </c:if>

            <div class="row g-4">
                <div class="col-12 col-xl-4">
                    <div class="card profile-card">
                        <div class="card-body p-4 text-center">
                            <h5 class="fw-bold mb-3">头像设置</h5>
                            <c:choose>
                                <c:when test="${not empty profileUser.avatarUrl}">
                                    <img class="avatar-preview mb-3" src="${pageContext.request.contextPath}/avatar/image?v=${avatarCacheToken}" alt="用户头像"
                                         onerror="this.style.display='none';this.nextElementSibling.style.display='flex';" />
                                    <div class="avatar-default mx-auto mb-3" style="display:none;">${profileUserInitial}</div>
                                </c:when>
                                <c:otherwise>
                                    <div class="avatar-default mx-auto mb-3">${profileUserInitial}</div>
                                </c:otherwise>
                            </c:choose>

                            <form method="post" action="${pageContext.request.contextPath}/uploadAvatar" enctype="multipart/form-data">
                                <div class="mb-3 text-start">
                                    <label for="avatar" class="form-label fw-semibold">上传新头像</label>
                                    <input id="avatar" name="avatar" type="file" class="form-control" accept="image/jpeg,image/png" required>
                                    <div class="form-text">仅支持 JPG/PNG，建议小于 2MB。</div>
                                </div>
                                <button type="submit" class="btn btn-primary w-100">保存头像</button>
                            </form>
                        </div>
                    </div>
                </div>

                <div class="col-12 col-xl-8">
                    <div class="card profile-card">
                        <div class="card-body p-4">
                            <h5 class="fw-bold mb-3">基本信息</h5>
                            <div class="table-responsive">
                                <table class="table align-middle mb-0">
                                    <tbody>
                                    <tr>
                                        <th class="text-secondary" style="width: 160px;">用户 ID</th>
                                        <td>${profileUser.id}</td>
                                    </tr>
                                    <tr>
                                        <th class="text-secondary">用户名</th>
                                        <td>${profileUser.username}</td>
                                    </tr>
                                    <tr>
                                        <th class="text-secondary">角色</th>
                                        <td>${profileUser.role}</td>
                                    </tr>
                                    <tr>
                                        <th class="text-secondary">注册时间</th>
                                        <td>${profileUser.createTime}</td>
                                    </tr>
                                    <tr>
                                        <th class="text-secondary">更新时间</th>
                                        <td>${profileUser.updateTime}</td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>

                    <div class="card profile-card mt-4">
                        <div class="card-body p-4">
                            <h5 class="fw-bold mb-3">技术能力画像</h5>
                            <div id="abilityRadar" class="radar-box"></div>
                        </div>
                    </div>

                    <div class="row g-4 mt-1">
                        <div class="col-12 col-lg-6">
                            <div class="card profile-card h-100">
                                <div class="card-body p-4">
                                    <h5 class="fw-bold mb-3">我的收藏</h5>
                                    <ul class="list-group list-group-flush">
                                        <c:if test="${empty favoriteQuestions}">
                                            <li class="list-group-item text-secondary px-0">暂无收藏题目</li>
                                        </c:if>
                                        <c:forEach var="fav" items="${favoriteQuestions}">
                                            <li class="list-group-item px-0 d-flex justify-content-between align-items-start gap-2">
                                                <div>
                                                    <a class="fw-semibold text-decoration-none" href="${pageContext.request.contextPath}/QuestionDetail?id=${fav.id}">${fav.title}</a>
                                                    <div class="small text-secondary">标签：${empty fav.tags ? '-' : fav.tags}</div>
                                                </div>
                                                <span class="badge text-bg-light border">难度 ${empty fav.difficulty ? '-' : fav.difficulty}</span>
                                            </li>
                                        </c:forEach>
                                    </ul>
                                </div>
                            </div>
                        </div>

                        <div class="col-12 col-lg-6">
                                <div class="card profile-card h-100">
                                    <div class="card-body p-4">
                                        <div class="d-flex flex-wrap justify-content-between align-items-start gap-3 mb-2">
                                            <div>
                                                <h5 class="fw-bold mb-1">我的错题（需重练）</h5>
                                                <div class="small text-secondary">按题目最新一次测评结果统计，重新及格后会自动移出错题本。</div>
                                            </div>
                                            <c:if test="${wrongQuestionCount > 3}">
                                                <button type="button" class="btn btn-sm btn-outline-primary" data-bs-toggle="modal" data-bs-target="#wrongQuestionModal">
                                                    查看全部错题（${wrongQuestionCount}）
                                                </button>
                                            </c:if>
                                        </div>
                                        <ul class="list-group list-group-flush">
                                            <c:if test="${empty wrongQuestions}">
                                                <li class="list-group-item text-secondary px-0">暂无需重练题目</li>
                                            </c:if>
                                            <c:forEach var="wrong" items="${wrongQuestions}" varStatus="status">
                                                <c:if test="${status.index lt 3}">
                                                    <li class="list-group-item px-0 d-flex justify-content-between align-items-start gap-2">
                                                        <div>
                                                            <a class="fw-semibold text-decoration-none" href="${pageContext.request.contextPath}/QuestionDetail?id=${wrong.questionId}">${wrong.questionTitle}</a>
                                                            <div class="small text-secondary">最近测评：<fmt:formatDate value="${wrong.createdAt}" pattern="yyyy-MM-dd HH:mm" /></div>
                                                        </div>
                                                        <span class="badge text-bg-danger">${wrong.score} 分</span>
                                                    </li>
                                                </c:if>
                                            </c:forEach>
                                        </ul>
                                    </div>
                                </div>
                            </div>
                    </div>

                    <div class="card profile-card mt-4">
                        <div class="card-body p-4">
                            <h5 class="fw-bold mb-3">成长趋势图</h5>
                            <div id="growthTrend" class="trend-box"></div>
                        </div>
                    </div>

                    <div class="card profile-card mt-4">
                        <div class="card-body p-4">
                            <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
                                <h5 class="fw-bold mb-0">周报中心</h5>
                                <form id="weeklyReportForm" method="post" action="${pageContext.request.contextPath}/report/weekly/generate" class="m-0">
                                    <button id="weeklyReportSubmit" type="submit" class="btn btn-primary">
                                        <span class="weekly-btn-text">生成本周周报</span>
                                    </button>
                                </form>
                            </div>

                            <div id="weeklyReportAlert" class="d-none" role="alert"></div>

                            <c:if test="${weeklyReportStatus == 'ok'}">
                                <div class="alert alert-success py-2">周报已生成，如本周已存在则自动复用历史结果。</div>
                            </c:if>
                            <c:if test="${weeklyReportStatus == 'fail'}">
                                <div class="alert alert-danger py-2">周报生成失败，请稍后重试。</div>
                            </c:if>

                            <c:if test="${empty weeklyReports}">
                                <div id="weeklyReportEmpty" class="text-secondary">暂无周报记录，点击“生成本周周报”开始创建。</div>
                            </c:if>

                            <ul id="weeklyReportTimeline" class="timeline mb-0">
                                <c:forEach var="report" items="${weeklyReports}">
                                    <li class="timeline-item">
                                        <div class="card border-0 bg-light">
                                            <div class="card-body py-3">
                                                <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-2">
                                                    <div class="fw-semibold">${report.title}</div>
                                                    <span class="badge text-bg-primary">
                                                        <fmt:formatDate value="${report.periodStart}" pattern="MM-dd" /> ~ <fmt:formatDate value="${report.periodEnd}" pattern="MM-dd" />
                                                    </span>
                                                </div>
                                                <div class="small text-secondary mb-2"><c:out value="${report.summary}" /></div>
                                                    <div class="weekly-report-block">
                                                        <div class="weekly-report-source d-none"><c:out value="${report.content}" /></div>
                                                    <div class="weekly-report-rendered" style="white-space: pre-wrap; line-height: 1.7;"><c:out value="${report.content}" /></div>
                                                    </div>
                                            </div>
                                        </div>
                                    </li>
                                </c:forEach>
                            </ul>
                        </div>
                    </div>

                    <div class="card profile-card mt-4">
                        <div class="card-body p-4">
                            <h5 class="fw-bold mb-3">测评历史记录</h5>
                            <div class="table-responsive">
                                <table class="table align-middle mb-0">
                                    <thead class="table-light">
                                    <tr>
                                        <th>面试会话</th>
                                        <th>会话均分</th>
                                        <th>测评时间</th>
                                        <th>操作</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:if test="${empty evaluationHistory}">
                                        <tr>
                                            <td colspan="4" class="text-secondary text-center py-3">暂无评分记录</td>
                                        </tr>
                                    </c:if>
                                    <c:forEach var="item" items="${evaluationHistory}" varStatus="st">
                                        <tr>
                                            <td>
                                                <div class="fw-semibold">${empty item.questionTitle ? '本场面试' : item.questionTitle}</div>
                                                <div class="small text-secondary">分类：${item.category}</div>
                                                <div class="small text-secondary">会话ID：${item.sessionId}</div>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${item.score ge 80}">
                                                        <span class="badge text-bg-success">${item.score}</span>
                                                    </c:when>
                                                    <c:when test="${not item.needRetrain}">
                                                        <span class="badge text-bg-warning">${item.score}</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge text-bg-danger">${item.score}（需重练）</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td><fmt:formatDate value="${item.createdAt}" pattern="yyyy-MM-dd HH:mm" /></td>
                                            <td>
                                                <button type="button" class="btn btn-sm btn-outline-primary"
                                                        data-bs-toggle="modal" data-bs-target="#sessionDetailModal"
                                                        data-session-id="${item.sessionId}">
                                                    查看会话详情
                                                </button>
                                                <a class="btn btn-sm btn-primary" target="_blank"
                                                                    href="${pageContext.request.contextPath}/report/exportPdf?sessionId=${item.sessionId}">
                                                    导出PDF
                                                </a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>

                            <c:if test="${historyTotalPages > 1}">
                                <nav class="mt-3" aria-label="history pagination">
                                    <ul class="pagination pagination-sm justify-content-end mb-0">
                                        <li class="page-item ${historyCurrentPage <= 1 ? 'disabled' : ''}">
                                            <a class="page-link" href="${pageContext.request.contextPath}/profile?page=${historyCurrentPage - 1}">上一页</a>
                                        </li>
                                        <c:forEach begin="1" end="${historyTotalPages}" var="p">
                                            <li class="page-item ${p == historyCurrentPage ? 'active' : ''}">
                                                <a class="page-link" href="${pageContext.request.contextPath}/profile?page=${p}">${p}</a>
                                            </li>
                                        </c:forEach>
                                        <li class="page-item ${historyCurrentPage >= historyTotalPages ? 'disabled' : ''}">
                                            <a class="page-link" href="${pageContext.request.contextPath}/profile?page=${historyCurrentPage + 1}">下一页</a>
                                        </li>
                                    </ul>
                                </nav>
                            </c:if>

                            <div class="modal fade" id="sessionDetailModal" tabindex="-1" aria-hidden="true">
                                <div class="modal-dialog modal-lg modal-dialog-scrollable">
                                    <div class="modal-content">
                                        <div class="modal-header">
                                            <h5 class="modal-title">DeepSeek 会话评价详情</h5>
                                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                        </div>
                                        <div class="modal-body" id="sessionDetailBody">
                                            正在加载会话详情...
                                        </div>
                                        <div class="modal-footer">
                                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">关闭</button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="modal fade" id="wrongQuestionModal" tabindex="-1" aria-hidden="true">
                            <div class="modal-dialog modal-lg modal-dialog-scrollable">
                                <div class="modal-content">
                                    <div class="modal-header">
                                        <h5 class="modal-title">全部错题（${wrongQuestionCount}）</h5>
                                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                    </div>
                                    <div class="modal-body">
                                        <div class="small text-secondary mb-3">按题目最新一次测评结果统计，最新结果及格后会自动移出错题本。</div>
                                        <ul class="list-group list-group-flush">
                                            <c:if test="${empty wrongQuestions}">
                                                <li class="list-group-item text-secondary px-0">暂无需重练题目</li>
                                            </c:if>
                                            <c:forEach var="wrong" items="${wrongQuestions}">
                                                <li class="list-group-item px-0 d-flex justify-content-between align-items-start gap-2">
                                                    <div>
                                                        <a class="fw-semibold text-decoration-none" href="${pageContext.request.contextPath}/QuestionDetail?id=${wrong.questionId}" target="_blank">${wrong.questionTitle}</a>
                                                        <div class="small text-secondary">最近测评：<fmt:formatDate value="${wrong.createdAt}" pattern="yyyy-MM-dd HH:mm" /></div>
                                                    </div>
                                                    <span class="badge text-bg-danger">${wrong.score} 分</span>
                                                </li>
                                            </c:forEach>
                                        </ul>
                                    </div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">关闭</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>
<script>
    (function () {
        var raw = '<c:out value="${categoryAvgJson}" escapeXml="false" />';
        var dataObj = {};
        if (raw && raw !== 'null') {
            try {
                dataObj = JSON.parse(raw);
            } catch (e) {
                dataObj = {};
            }
        }

        var baseCategories = ['Java基础', '数据库', '算法', '并发编程', '系统设计'];
        var dynamicCategories = Object.keys(dataObj).filter(function (name) {
            return baseCategories.indexOf(name) === -1;
        });
        var categories = baseCategories.concat(dynamicCategories);

        var values = categories.map(function (name) {
            var v = dataObj[name];
            if (v == null || isNaN(v)) {
                return 0;
            }
            return Number(Number(v).toFixed(2));
        });

        var indicators = categories.map(function (name) {
            return { name: name, max: 100 };
        });

        var chartDom = document.getElementById('abilityRadar');
        if (!chartDom) {
            return;
        }
        var chart = echarts.init(chartDom);
        chart.setOption({
            color: ['#1d4ed8'],
            tooltip: {
                backgroundColor: 'rgba(15, 23, 42, 0.9)',
                borderColor: '#1d4ed8',
                textStyle: { color: '#e2e8f0' }
            },
            radar: {
                name: {
                    color: '#1e3a8a',
                    fontWeight: 600
                },
                splitLine: {
                    lineStyle: {
                        color: ['#dbeafe', '#bfdbfe', '#93c5fd', '#60a5fa', '#3b82f6']
                    }
                },
                splitArea: {
                    areaStyle: {
                        color: ['rgba(219,234,254,0.25)', 'rgba(191,219,254,0.22)', 'rgba(147,197,253,0.18)']
                    }
                },
                axisLine: {
                    lineStyle: {
                        color: '#93c5fd'
                    }
                },
                indicator: indicators,
                radius: '65%'
            },
            series: [{
                name: '能力画像',
                type: 'radar',
                data: [{
                    value: values,
                    name: '分类均分'
                }],
                areaStyle: {
                    color: 'rgba(37, 99, 235, 0.22)'
                },
                lineStyle: {
                    color: '#1d4ed8',
                    width: 2.5
                },
                itemStyle: {
                    color: '#1e40af'
                }
            }]
        });
        window.addEventListener('resize', function () {
            chart.resize();
        });
    })();

    (function () {
        var raw = '<c:out value="${trendJson}" escapeXml="false" />';
        var trend = { times: [], scores: [] };
        if (raw && raw !== 'null') {
            try {
                trend = JSON.parse(raw);
            } catch (e) {
                trend = { times: [], scores: [] };
            }
        }

        var xData = Array.isArray(trend.times) ? trend.times : [];
        var yData = Array.isArray(trend.scores) ? trend.scores : [];

        if (xData.length === 0) {
            xData = ['暂无数据'];
            yData = [0];
        }

        var lineDom = document.getElementById('growthTrend');
        if (!lineDom) {
            return;
        }
        var lineChart = echarts.init(lineDom);
        lineChart.setOption({
            color: ['#2563eb'],
            tooltip: {
                trigger: 'axis',
                backgroundColor: 'rgba(15, 23, 42, 0.9)',
                borderColor: '#2563eb',
                textStyle: { color: '#e2e8f0' }
            },
            grid: {
                left: '6%',
                right: '4%',
                top: '10%',
                bottom: '12%',
                containLabel: true
            },
            xAxis: {
                type: 'category',
                boundaryGap: false,
                data: xData,
                axisLine: { lineStyle: { color: '#93c5fd' } },
                axisLabel: { color: '#1e3a8a' }
            },
            yAxis: {
                type: 'value',
                min: 0,
                max: 100,
                splitLine: { lineStyle: { color: '#dbeafe' } },
                axisLabel: { color: '#1e3a8a' }
            },
            series: [{
                name: '测评分数',
                type: 'line',
                smooth: true,
                data: yData,
                symbolSize: 8,
                lineStyle: {
                    width: 3,
                    color: '#1d4ed8'
                },
                itemStyle: {
                    color: '#1e40af',
                    borderColor: '#dbeafe',
                    borderWidth: 2
                },
                areaStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: 'rgba(37, 99, 235, 0.40)' },
                        { offset: 1, color: 'rgba(37, 99, 235, 0.06)' }
                    ])
                }
            }]
        });

        window.addEventListener('resize', function () {
            lineChart.resize();
        });
    })();

    (function () {
        var modalElement = document.getElementById('sessionDetailModal');
        var modalBody = document.getElementById('sessionDetailBody');
        if (!modalElement || !modalBody) {
            return;
        }

        function escapeHtml(text) {
            if (text === null || text === undefined) {
                return '';
            }
            return String(text)
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#39;');
        }

        function formatFeedback(text) {
            var safe = escapeHtml(text || '暂无点评');
            safe = safe
                .replace(/优点：/g, '<strong>优点：</strong>')
                .replace(/问题：/g, '<strong>问题：</strong>')
                .replace(/改进建议：/g, '<strong>改进建议：</strong>')
                .replace(/\n/g, '<br>');
            return safe;
        }

        function renderRows(list, sessionId) {
            if (!Array.isArray(list) || list.length === 0) {
                modalBody.innerHTML = '<div class="text-secondary">该会话暂无详情数据。</div>';
                return;
            }

            var html = '<div class="small text-secondary mb-3">会话：' + escapeHtml(sessionId) + '</div>';
            for (var i = 0; i < list.length; i += 1) {
                var item = list[i] || {};
                var score = item.score === null || item.score === undefined || item.score === '' ? '-' : item.score;
                html += ''
                    + '<div class="card mb-3 shadow-sm">'
                    + '<div class="card-header d-flex justify-content-between align-items-center">'
                    + '<div class="fw-semibold">第 ' + (i + 1) + ' 题：' + escapeHtml(item.questionTitle || '未命名题目') + '</div>'
                    + '<span class="badge bg-primary">得分: ' + escapeHtml(score) + '</span>'
                    + '</div>'
                    + '<div class="card-body">'
                    + '<div class="small fw-semibold text-secondary mb-1">我的回答：</div>'
                    + '<div class="bg-light p-2 rounded mb-2" style="white-space: pre-wrap; line-height: 1.7;">' + escapeHtml(item.userAnswer || '暂无用户作答') + '</div>'
                    + '<div class="small fw-semibold text-secondary mb-1">DeepSeek 评价：</div>'
                    + '<div style="line-height: 1.7;">' + formatFeedback(item.feedback) + '</div>'
                    + '</div>'
                    + '</div>';
            }
            modalBody.innerHTML = html;
        }

        modalElement.addEventListener('show.bs.modal', function (event) {
            var trigger = event.relatedTarget;
            var sessionId = trigger && trigger.getAttribute('data-session-id');
            if (!sessionId) {
                modalBody.innerHTML = '<div class="text-danger">会话ID无效。</div>';
                return;
            }

            modalBody.innerHTML = '正在加载会话详情...';
            fetch('${pageContext.request.contextPath}/profile/sessionDetail?sessionId=' + encodeURIComponent(sessionId), {
                method: 'GET',
                headers: {
                    'X-Requested-With': 'fetch'
                }
            })
                .then(function (resp) {
                    if (!resp.ok) {
                        throw new Error('请求失败');
                    }
                    return resp.json();
                })
                .then(function (data) {
                    modalBody.innerHTML = '';
                    renderRows(data, sessionId);
                })
                .catch(function () {
                    modalBody.innerHTML = '<div class="text-danger">加载会话详情失败，请稍后重试。</div>';
                });
        });
    })();

    (function () {
        var form = document.getElementById('weeklyReportForm');
        var submitBtn = document.getElementById('weeklyReportSubmit');
        var alertBox = document.getElementById('weeklyReportAlert');
        var timeline = document.getElementById('weeklyReportTimeline');
        if (!form || !submitBtn || !alertBox || !timeline) {
            return;
        }

        var btnText = submitBtn.querySelector('.weekly-btn-text');

        function cleanWeeklyMarkdown(text) {
            if (text === null || text === undefined) {
                return '';
            }

            var result = String(text).replace(/\r\n/g, '\n').replace(/\r/g, '\n').trim();

            if (result.startsWith('```')) {
                var firstBreak = result.indexOf('\n');
                if (firstBreak > 0) {
                    result = result.slice(firstBreak + 1).trim();
                }
                var closingFence = result.lastIndexOf('```');
                if (closingFence >= 0) {
                    result = result.slice(0, closingFence).trim();
                }
            }

            if (/^json\s*:?/i.test(result)) {
                result = result.replace(/^json\s*:?/i, '').trim();
            }

            if (result.startsWith('{') && result.endsWith('}')) {
                try {
                    var parsed = JSON.parse(result);
                    if (parsed && typeof parsed === 'object') {
                        if (parsed.output) {
                            result = String(parsed.output);
                        } else if (parsed.content) {
                            result = String(parsed.content);
                        } else if (parsed.summary) {
                            result = String(parsed.summary);
                        } else if (parsed.thoughts && typeof parsed.thoughts === 'object') {
                            result = String(parsed.thoughts.output || parsed.thoughts.analysis || '');
                        }
                    }
                } catch (e) {
                    // 不是 JSON 就保持原样。
                }
            }

            return result.replace(/\\n/g, '\n').replace(/\\t/g, '\t').trim();
        }

        function escapeHtml(text) {
            if (text === null || text === undefined) {
                return '';
            }
            return String(text)
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#39;');
        }

        function applyInlineMarkdown(text) {
            var escaped = escapeHtml(text);
            escaped = escaped.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
            escaped = escaped.replace(/`([^`]+)`/g, '<code>$1</code>');
            return escaped;
        }

        function renderMarkdown(markdown) {
            var text = cleanWeeklyMarkdown(markdown);
            if (!text) {
                return '<div class="text-secondary">暂无周报内容。</div>';
            }

            var lines = text.split('\n');
            var html = [];
            var paragraph = [];
            var inUl = false;
            var inOl = false;

            function closeParagraph() {
                if (paragraph.length) {
                    html.push('<p style="white-space:pre-wrap; margin-bottom:0.75rem; line-height:1.75;">' + applyInlineMarkdown(paragraph.join('\n').trim()) + '</p>');
                    paragraph = [];
                }
            }

            function closeLists() {
                if (inUl) {
                    html.push('</ul>');
                    inUl = false;
                }
                if (inOl) {
                    html.push('</ol>');
                    inOl = false;
                }
            }

            function openUl() {
                if (!inUl) {
                    closeParagraph();
                    closeLists();
                    html.push('<ul style="margin-bottom:0.75rem;">');
                    inUl = true;
                }
            }

            function openOl() {
                if (!inOl) {
                    closeParagraph();
                    closeLists();
                    html.push('<ol style="margin-bottom:0.75rem;">');
                    inOl = true;
                }
            }

            for (var i = 0; i < lines.length; i += 1) {
                var line = lines[i].trim();
                if (!line) {
                    closeParagraph();
                    closeLists();
                    continue;
                }

                var hMatch = line.match(/^([#]{1,3})\s+(.+)$/);
                if (hMatch) {
                    closeParagraph();
                    closeLists();
                    var level = Math.min(hMatch[1].length + 1, 4);
                    html.push('<h' + level + ' style="margin:0.25rem 0 0.6rem; font-weight:700;">' + applyInlineMarkdown(hMatch[2]) + '</h' + level + '>');
                    continue;
                }

                var ulMatch = line.match(/^[-*+]\s+(.+)$/);
                if (ulMatch) {
                    openUl();
                    html.push('<li style="margin-bottom:0.25rem;">' + applyInlineMarkdown(ulMatch[1]) + '</li>');
                    continue;
                }

                var olMatch = line.match(/^\d+[.)]\s+(.+)$/);
                if (olMatch) {
                    openOl();
                    html.push('<li style="margin-bottom:0.25rem;">' + applyInlineMarkdown(olMatch[1]) + '</li>');
                    continue;
                }

                closeLists();
                paragraph.push(line);
            }

            closeParagraph();
            closeLists();
            return html.join('');
        }

        function renderWeeklyReportBlock(block) {
            if (!block) {
                return;
            }
            var source = block.querySelector('.weekly-report-source');
            var rendered = block.querySelector('.weekly-report-rendered');
            if (!source || !rendered) {
                return;
            }
            rendered.innerHTML = renderMarkdown(source.textContent || source.innerText || '');
        }

        function renderAllWeeklyReports() {
            var blocks = document.querySelectorAll('.weekly-report-block');
            for (var i = 0; i < blocks.length; i += 1) {
                renderWeeklyReportBlock(blocks[i]);
            }
        }

        renderAllWeeklyReports();

        function showAlert(type, message) {
            alertBox.className = 'alert alert-' + type + ' py-2';
            alertBox.textContent = message;
            alertBox.classList.remove('d-none');
        }

        function renderTimelineItem(report) {
            var title = escapeHtml(report && report.title ? report.title : '面试周报（职业发展教练）');
            var summary = escapeHtml(report && report.summary ? report.summary : '');
            var startLabel = escapeHtml(report && report.periodStartLabel ? report.periodStartLabel : '--');
            var endLabel = escapeHtml(report && report.periodEndLabel ? report.periodEndLabel : '--');
            var content = escapeHtml(report && report.content ? report.content : '');

            return ''
                + '<li class="timeline-item">'
                + '  <div class="card border-0 bg-light">'
                + '    <div class="card-body py-3">'
                + '      <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-2">'
                + '        <div class="fw-semibold">' + title + '</div>'
                + '        <span class="badge text-bg-primary">' + startLabel + ' ~ ' + endLabel + '</span>'
                + '      </div>'
                + '      <div class="small text-secondary mb-2">' + summary + '</div>'
                + '      <div class="weekly-report-block">'
                + '        <div class="weekly-report-source d-none">' + content + '</div>'
                + '        <div class="weekly-report-rendered" style="white-space: pre-wrap; line-height: 1.7;">' + content + '</div>'
                + '      </div>'
                + '    </div>'
                + '  </div>'
                + '</li>';
        }

        form.addEventListener('submit', function (event) {
            event.preventDefault();

            submitBtn.disabled = true;
            if (btnText) {
                btnText.textContent = '生成中...';
            }
            submitBtn.classList.add('disabled');
            showAlert('info', '周报生成中，请稍候...');

            fetch(form.action, {
                method: 'POST',
                headers: {
                    'X-Requested-With': 'fetch',
                    'Accept': 'application/json'
                }
            })
                .then(function (resp) {
                    return resp.json().then(function (json) {
                        return { ok: resp.ok, json: json || {} };
                    }).catch(function () {
                        return { ok: resp.ok, json: {} };
                    });
                })
                .then(function (result) {
                    if (!result.ok || result.json.code !== 0) {
                        throw new Error(result.json.message || '周报生成失败，请稍后重试。');
                    }

                    var emptyBlock = document.getElementById('weeklyReportEmpty');
                    if (emptyBlock) {
                        emptyBlock.remove();
                    }

                    var payload = result.json.data || {};
                    var latestItem = timeline.querySelector('.timeline-item');
                    if (latestItem) {
                        latestItem.insertAdjacentHTML('beforebegin', renderTimelineItem(payload));
                    } else {
                        timeline.innerHTML = renderTimelineItem(payload);
                    }

                    renderAllWeeklyReports();

                    showAlert('success', '周报生成成功，已更新到列表顶部。');
                })
                .catch(function (err) {
                    showAlert('danger', err && err.message ? err.message : '周报生成失败，请稍后重试。');
                })
                .finally(function () {
                    submitBtn.disabled = false;
                    submitBtn.classList.remove('disabled');
                    if (btnText) {
                        btnText.textContent = '生成本周周报';
                    }
                });
        });
    })();
</script>
</body>
</html>