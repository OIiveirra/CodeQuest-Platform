# CodeQuest（platform）

本目录是项目主工程，包含代码、SQL、脚本和运行数据目录。

## 目录说明

- `src/`：Java 源码与 JSP 页面
- `sql/`：初始化脚本
- `scripts/`：数据库恢复与自检脚本
- `docker/`：容器内配置文件
- `data/`：运行数据目录

## 本地构建

```bash
mvn clean package -DskipTests
```

构建完成后会在 `target/platform` 生成可部署内容。

## 启动方式

统一从仓库根目录执行：

```bash
cd ..
docker compose up -d
```

## 交付前检查清单

- 代码状态：确认工作区无临时调试代码、无真实密钥文件。
- 配置状态：`.env` 仅本地使用，仓库仅保留 `.env.example`。
- 容器状态：`docker compose ps` 中 MySQL、Redis、Web 服务均为 `Up`。
- 题库能力：导入导出入口可访问，CSV 导入可返回成功统计信息。
- 面试链路：题目详情页可完成提交、流式评测、草稿同步与恢复。

建议在项目根目录执行以下命令进行快速自检：

```bash
docker compose ps
docker compose logs --tail=100 app
docker compose logs --tail=100 mysql
```

如果本机已安装 Maven，可额外执行：

```bash
mvn clean package -DskipTests
```

## 环境变量

复制模板并填写本地密钥：

```bash
copy .env.example .env
```

关键变量：

- `DEEPSEEK_API_KEY`
- `DEEPSEEK_API_URL`
- `DEEPSEEK_MODEL`
- `DB_USERNAME`
- `DB_PASSWORD`

说明：`.env` 已在 `.gitignore` 中忽略，请勿提交真实密钥。

## 默认账号

- `Oliveira / 123456`
- `admin / admin123`
- `testuser / 123456`

## 相关文档

- 项目总体说明：`../README.md`
- 项目恢复说明：`../docs/项目恢复说明.md`
- WBS 分工书：`../docs/WBS分工书.md`

## 数据恢复（交付后）

数据库恢复脚本位于 `scripts/recover_codequest_db.sql`，支持重复执行。

在项目根目录执行：

```bash
docker cp ./platform/scripts/recover_codequest_db.sql codequest-mysql:/tmp/recover_codequest_db.sql
docker exec codequest-mysql sh -lc "mysql -uroot -proot < /tmp/recover_codequest_db.sql"
```

恢复后可快速验证：

```bash
docker exec codequest-mysql sh -lc "mysql -uroot -proot -e 'USE codequest_db; SELECT COUNT(*) AS question_count FROM t_question;'"
```
