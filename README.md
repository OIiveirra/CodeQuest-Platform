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

### 2. 启动基础设施与应用（统一入口）
在上级目录（`CodeQuest-Platform`）执行：
```bash
cd ..
docker compose up -d
```

如果你当前就在 `platform` 目录，也可以直接指定配置文件：
```bash
docker compose -f ../docker-compose.yml up -d
```

### 2.1 配置 DeepSeek Key（推荐环境变量）

PowerShell：
```powershell
$env:DEEPSEEK_API_KEY="your_deepseek_api_key"
```

或创建 `.env` 文件（已被 `.gitignore` 忽略）：
```env
DEEPSEEK_API_KEY=your_deepseek_api_key
DEEPSEEK_API_URL=https://api.deepseek.com
DEEPSEEK_MODEL=deepseek-reasoner
```

可直接参考仓库中的 `.env.example` 复制为 `.env` 后填写。

### 3. 检查状态
```bash
docker compose -f ../docker-compose.yml ps
docker compose -f ../docker-compose.yml logs -f app
```

### 4. 访问地址
- 应用主页: http://localhost:8081
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

详细分工、责任文件清单与每人代码量统计已单独整理在 [WBS分工书.md](WBS分工书.md)。

该文件采用“文件唯一归属”口径，适合直接用于课程验收和分工说明。

---
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
docker compose -f ../docker-compose.yml down -v
docker compose -f ../docker-compose.yml up -d
```
确保从空卷恢复后可直接登录并看到题库数据。

---

## 安全与合规说明

- 请勿将真实 API Key 明文提交到仓库；建议使用环境变量或 Secret 管理。
- 生产环境请替换默认账号密码，并限制数据库端口暴露范围。
- 当前程序支持通过 `DEEPSEEK_API_KEY`、`DB_PASSWORD` 等环境变量覆盖本地配置。

---

## License

For educational and internal engineering evaluation use.
