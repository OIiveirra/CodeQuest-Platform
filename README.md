# CodeQuest

CodeQuest 是一个基于 Servlet/JSP 的 IT 面试训练与评测系统，支持题库管理、作答评测、错题与收藏、周报与会话记录查看。

## 1. 项目结构

- docker-compose.yml：统一启动入口（MySQL + Redis + Tomcat）
- platform/：Java Web 项目代码、SQL、脚本与配置
- docs/：项目说明文档（恢复说明、WBS 分工）

## 2. 运行要求

- JDK 17
- Maven 3.8+
- Docker Desktop（Compose v2）

建议先在仓库根目录检查：

```powershell
docker --version
docker compose version
java -version
```

## 3. 配置文件

首次运行前先准备 `platform/.env`：

```powershell
Copy-Item .\platform\.env.example .\platform\.env
```

然后编辑 `platform/.env`，至少填写：

- DEEPSEEK_API_KEY

修改 `platform/.env` 后需要重建 app：

```powershell
docker compose up -d --force-recreate app
```

## 4. 首次启动（推荐路径）

### 步骤 1：编译

```powershell
cd .\platform
mvn clean package -DskipTests
```

### 步骤 2：回到根目录启动容器

```powershell
cd ..
docker compose up -d
```

### 步骤 3：确认服务状态

```powershell
docker compose ps
```

预期状态：

- codequest-app：Up
- codequest-mysql：Up (healthy)
- codequest-redis：Up (healthy)

### 步骤 4：访问系统

- 应用：http://localhost:8081
- MySQL：localhost:3306
- Redis：localhost:6379

## 5. 默认账号

- 管理员：Oliveira / 123456
- 管理员：admin / admin123
- 测试用户：testuser / 123456

## 6. 数据恢复

### 6.1 全量恢复（新环境优先）

脚本：platform/scripts/recover_codequest_db.sql

```powershell
docker cp .\platform\scripts\recover_codequest_db.sql codequest-mysql:/tmp/recover_codequest_db.sql
docker exec codequest-mysql sh -lc "mysql -uroot -proot < /tmp/recover_codequest_db.sql"
```

### 6.2 恢复初始 10 道示例题（用于重置）

脚本：platform/sql/init.sql

```powershell
docker cp .\platform\sql\init.sql codequest-mysql:/tmp/init.sql
docker exec codequest-mysql sh -lc "mysql -uroot -proot < /tmp/init.sql"
```

注意：执行 init.sql 会重建 codequest_db，请先确认是否需要备份当前数据。

## 7. 常用运维命令

```powershell
# 查看服务
docker compose ps

# 查看应用日志
docker compose logs -f app

# 重建并重启 app（Java 代码改动后常用）
docker compose up -d --force-recreate app

# 停止服务
docker compose down
```

## 8. 常见问题

### 8.1 8081 访问失败

- 先执行 docker compose ps 确认 app 是否 Up
- 若未启动，执行 docker compose up -d
- 若已启动但不可访问，查看 docker compose logs --tail 200 app

### 8.2 数据库恢复后无法登录

- 确认 SQL 是否执行成功
- 确认使用的是本文档默认账号
- 可通过容器内查询核对用户：

```powershell
docker exec codequest-mysql mysql -uroot -proot -D codequest_db -e "SELECT username,role FROM sys_user;"
```

### 8.3 AI 功能离线

- 检查 platform/.env 中 DeepSeek 配置是否正确
- app 容器重建后再验证：docker compose up -d --force-recreate app

## 9. 文档入口

- 恢复说明：docs/项目恢复说明.md
- WBS 分工书：docs/WBS分工书.md
