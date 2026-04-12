<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
	<meta charset="UTF-8" />
	<meta name="viewport" content="width=device-width, initial-scale=1.0" />
	<title>CodeQuest - 首页</title>
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
	<style>
		:root {
			--cq-bg: #f6f7fb;
			--cq-card: #ffffff;
			--cq-sidebar: #0f172a;
			--cq-accent: #0ea5e9;
			--cq-text: #0f172a;
			--cq-muted: #64748b;
		}

		body {
			background:
				radial-gradient(circle at 90% 8%, rgba(14, 165, 233, 0.18), transparent 32%),
				radial-gradient(circle at 5% 78%, rgba(249, 115, 22, 0.14), transparent 24%),
				var(--cq-bg);
			color: var(--cq-text);
			font-family: "Microsoft YaHei", "Segoe UI", sans-serif;
		}

		.cq-sidebar {
			background: linear-gradient(180deg, #111827 0%, #0f172a 70%, #1e293b 100%);
			color: #e2e8f0;
		}

		.cq-sidebar .brand {
			font-weight: 700;
			font-size: 1.15rem;
			letter-spacing: .02em;
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

		.hero-card {
			border: 0;
			border-radius: 1.25rem;
			overflow: hidden;
			background: linear-gradient(130deg, #0f172a, #1d4ed8 50%, #0ea5e9 100%);
			color: #fff;
			box-shadow: 0 18px 48px rgba(30, 64, 175, .22);
		}

		.feature-card {
			background: var(--cq-card);
			border: 1px solid #e2e8f0;
			border-radius: 1rem;
			box-shadow: 0 8px 22px rgba(15, 23, 42, .06);
			height: 100%;
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
		<jsp:include page="/WEB-INF/jsp/sidebar.jsp" />

		<main class="col-12 col-lg-10 p-4 p-lg-5 cq-main">
			<div class="mb-3">
				<c:choose>
					<c:when test="${not empty sessionScope.loginUser}">
						<div class="alert alert-success border-0 shadow-sm">欢迎您，${sessionScope.loginUser.username}</div>
						<c:if test="${not empty sessionScope.loginUser.avatarUrl}">
							<div class="alert alert-light border-0 shadow-sm d-flex align-items-center gap-3">
								<div class="rounded-circle bg-primary-subtle d-flex align-items-center justify-content-center text-primary fw-bold" style="width: 48px; height: 48px;">${sessionScope.loginUser.username.substring(0, 1)}</div>
								<div>
									<div class="fw-semibold">当前头像已上传</div>
									<div class="small text-secondary text-break">${sessionScope.loginUser.avatarUrl}</div>
								</div>
							</div>
						</c:if>
					</c:when>
					<c:otherwise>
						<div class="alert alert-warning border-0 shadow-sm">请登录后使用完整面试功能。</div>
					</c:otherwise>
				</c:choose>
			</div>
			<div class="card hero-card mb-4">
				<div class="card-body p-4 p-lg-5">
					<h1 class="display-6 fw-bold mb-3">准备好开始你的算法面试冲刺了吗？</h1>
					<p class="mb-4 text-white-50">
						在 CodeQuest 中浏览题目、提交回答、获取 AI 评分与建议，持续迭代你的表达和解题深度。
					</p>
						<div class="d-flex flex-wrap gap-2">
							<a class="btn btn-light btn-lg fw-semibold" href="${pageContext.request.contextPath}/questions">进入题库</a>
							<c:if test="${not empty sessionScope.loginUser}">
								<a class="btn btn-outline-light btn-lg fw-semibold" href="${pageContext.request.contextPath}/profile">个人中心</a>
							</c:if>
						</div>
				</div>
			</div>

			<div class="row g-3 g-lg-4">
				<div class="col-12 col-md-6 col-xl-4">
					<div class="feature-card p-4">
						<h5 class="fw-bold">题目分类</h5>
						<p class="text-secondary mb-0">覆盖常见面试方向，支持类型、难度和标签维度查看。</p>
					</div>
				</div>
				<div class="col-12 col-md-6 col-xl-4">
					<div class="feature-card p-4">
						<h5 class="fw-bold">在线作答</h5>
						<p class="text-secondary mb-0">直接输入答案并提交，模拟真实场景下的表达压力。</p>
					</div>
				</div>
				<div class="col-12 col-md-6 col-xl-4">
					<div class="feature-card p-4">
						<h5 class="fw-bold">即时反馈</h5>
						<p class="text-secondary mb-0">获得 AI 分数与改进建议，明确下一轮优化方向。</p>
					</div>
				</div>
			</div>
		</main>
	</div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>
</body>
</html>
