# CodeQuest

> A Servlet/JSP-native intelligent IT assessment terminal deeply integrated with DeepSeek R1.

## 项目简介

CodeQuest 是一个基于 **原生 Servlet/JSP（无 Spring）** 的智能 IT 测评终端，面向面试训练、能力画像与报告沉淀场景。

项目核心定位：
- 低框架依赖：坚持 Servlet + JSP + JDBC 的工程可控性与学习透明度。
- 深度 AI 评测：集成 DeepSeek（R1/Reasoner & Chat），支持评分、建议、追问、流式输出。
- 数据闭环：题库 -> 作答 -> AI 评测 -> 错题沉淀 -> 周报/报告 -> 可视化画像。

---

## 架构亮点（No Spring, High Throughput）

### 1) BaseDAO + Service + Controller 分层
- `BaseDAO` 统一连接能力与 SQL 访问入口。
- `Service` 聚合业务编排（缓存、AI 调度、容错、回写）。
- `Servlet Controller` 专注协议处理（HTTP/SSE/文件下载）。

收益：
- 业务逻辑与传输层解耦。
- SQL 演进可控，便于在教学与面试场景下精准定位问题。

### 2) Filter 责任链（安全 + 观测 + 兜底）
通过多 Filter 串联实现请求生命周期治理：
- 编码统一
- XSS 防护
- 登录与 RBAC 鉴权
- 请求日志与耗时观测
- 全局异常兜底

收益：
- 在不引入 AOP 框架前提下，完成横切关注点治理。

### 3) Listener 全局初始化
`AppStartupListener` 在应用生命周期中完成：
- 启动阶段初始化 HikariCP 连接池
- 销毁阶段释放连接池，降低热部署内存泄漏风险

收益：
- 统一资源管理，避免懒加载导致的首请求抖动。

### 4) Redis 缓存与评测复用
评测服务引入 Redis 缓存，对重复输入结果进行复用，降低 AI 调用与数据库压力。

收益：
- 降低响应时间
- 降低外部模型调用成本

### 5) SSE 流式输出
针对长文本 AI 返回过程，支持 SSE 增量输出，前端可实时渲染 token 级内容。

收益：
- 提升用户体感速度
- 支持“生成中”交互体验

### 6) AI Token 成本可观测
已实现：
- 解析 DeepSeek `usage.prompt_tokens` / `usage.completion_tokens`
- 记录单次调用耗时与 token 总量日志
- 在 `sys_evaluation.token_used` 持久化总 token，用于成本看板

---

## 技术栈

- Java 17
- Servlet 3.1 / JSP / JSTL
- Maven
- MySQL 8.0
- Redis 6
- Tomcat 9
- HikariCP
- Gson
- ECharts / Bootstrap / marked.js / highlight.js
- DeepSeek API

---

## 快速启动（Docker Compose 一键运行）

### 0. 前置条件
- 已安装 Docker Desktop（含 Compose v2）
- 本机可执行 Maven

### 1. 编译项目
```bash
mvn clean package -DskipTests
```

### 2. 启动基础设施与应用
```bash
docker compose up -d
```

### 3. 检查状态
```bash
docker compose ps
docker compose logs -f app
```

### 4. 访问地址
- 应用主页: http://localhost:8080
- MySQL: `localhost:3306`
- Redis: `localhost:6379`

### 默认账号
- 管理员: `Oliveira / 123456`
- 管理员: `admin / admin123`
- 测试用户: `testuser / 123456`

> 以上账号来自初始化脚本 `sql/init.sql`。

---

## 仓库部署文件说明

- `docker-compose.yml`
  - 服务：`mysql`、`redis`、`app`
  - 依赖：`app` 依赖 `mysql/redis` 健康检查
  - 端口：`8080`、`3306`、`6379`
- `sql/init.sql`
  - 容器首次初始化自动执行，创建表并导入测试数据
- `docker/db.properties`
  - 容器环境数据库与 Redis 连接配置（使用服务名 `mysql` / `redis`）

---

## 团队分工（WBS）

| 成员 | 模块职责 | 主交付类型 |
|---|---|---|
| 张雨泽 | 核心引擎与架构（分层、AIService、Filter/Listener、部署脚本） | 后端/架构 |
| 张芷宁 | 用户中心与文件模块（资料、头像、RBAC 菜单） | 后端 + 页面 |
| 韩博文 | 题库深度检索（多条件筛选、导入导出） | 后端 |
| 靳道童 | 错题本与面试报告（收藏、周报、记录沉淀） | 后端 |
| 李鑫浩 | 核心答题终端（Markdown 渲染、交互体验） | 前端 |
| 高嵩 | 后台管理与可视化（管理 UI、能力图、大屏） | 前端 + 少量后端 |

---

## WBS 完成度核查（后端覆盖 + 代码量）

> 核查口径：基于当前仓库按 WBS 主责任文件进行粗粒度统计（含 SQL/YAML/JSP/Java），用于项目管理评估，不等同于 Git blame 精确归属。

| 成员 | 目标代码量 | 当前统计 | 是否达标 | 是否包含后端 |
|---|---:|---:|---|---|
| 张雨泽 | 1000+ | 1154 | ✅ | ✅ |
| 张芷宁 | 800+ | 1372 | ✅ | ✅ |
| 韩博文 | 800+ | 311 | ❌ | ✅ |
| 靳道童 | 800+ | 850 | ✅ | ✅ |
| 李鑫浩 | 800+ | 1185 | ✅ | ❌（前端主责） |
| 高嵩 | 800+ | 985 | ✅ | ✅（含 AdminServlet） |

审计结论：
- “每个人都包含后端”这一条件 **不成立**（李鑫浩为前端主责）。
- 代码量目标目前 **仅韩博文未达标**（且其 WBS 中 `ExportServlet` 交付在仓库中未检出同名文件，需要补齐或调整统计口径）。

建议：
1. 为李鑫浩补一个轻量后端交付（如答题会话聚合 API/草稿同步 API）。
2. 为韩博文补齐检索与导出后端链路（含 `ExportServlet` 或等价实现）并补测试。

---

## 数据库 init.sql 导出建议（Navicat / IDEA）

目标：导出“可一键初始化”的完整脚本（结构 + 数据，含 50+ 精品题）。

### Navicat
1. 右键数据库 `codequest_db` -> 转储 SQL 文件。
2. 勾选：
   - 导出结构
   - 导出数据
   - 使用 INSERT 语句
   - 字符集 `utf8mb4`
3. 选择核心表：`sys_question`、`t_question`、`sys_user`、`sys_prompt_template` 等。
4. 输出保存为 `sql/init.sql`。

### IntelliJ IDEA
1. 打开 Database 工具窗口并连接 MySQL。
2. 选中 `codequest_db` -> SQL Scripts / Dump with Data。
3. 选择 DDL + Data、UTF-8、多行 INSERT。
4. 保存为 `sql/init.sql`。

验证建议：
```bash
docker compose down -v
docker compose up -d
```
确保从空卷恢复后可直接登录并看到题库数据。

---

## 安全与合规说明

- 请勿将真实 API Key 明文提交到仓库；建议使用环境变量或 Secret 管理。
- 生产环境请替换默认账号密码，并限制数据库端口暴露范围。

---

## License

For educational and internal engineering evaluation use.
