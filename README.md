# CodeQuest

CodeQuest 是一个基于 Servlet/JSP 的 IT 面试训练与评测系统，支持题库管理、作答评测、收藏错题、报告查看等功能。

## 目录结构

- `docker-compose.yml`：项目统一启动入口
- `platform/`：项目代码与运行资源
- `docs/`：课程提交文档（恢复说明、WBS 分工）

## 运行环境

- JDK 17
- Maven 3.8+
- Docker Desktop（Compose v2）

## 快速启动

1. 构建项目

```bash
cd platform
mvn clean package -DskipTests
```

2. 启动服务

```bash
cd ..
docker compose up -d
```

3. 访问系统

- 应用地址：`http://localhost:8081`
- MySQL：`localhost:3306`
- Redis：`localhost:6379`

## 默认账号

- 管理员：`Oliveira / 123456`
- 管理员：`admin / admin123`
- 测试用户：`testuser / 123456`

## 数据恢复

- 全量恢复脚本：`platform/scripts/recover_codequest_db.sql`
- 题库恢复脚本：`platform/scripts/recover_question_bank.sql`
- 恢复说明：`docs/项目恢复说明.md`

## 文档入口

- 项目恢复说明：`docs/项目恢复说明.md`
- WBS 分工书：`docs/WBS分工书.md`
