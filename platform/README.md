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
