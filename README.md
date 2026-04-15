# CodeQuest Workspace

该目录采用两层结构，兼顾部署入口稳定性与项目内容清晰度：

- `docker-compose.yml`：统一部署入口（从本目录执行 `docker compose up -d`）
- `platform/`：Java Web 项目主体（源码、SQL、脚本、运行数据）
- `docs/`：项目说明文档（WBS、恢复说明）

## 推荐使用方式

1. 进入 `platform/` 执行构建：`mvn clean package -DskipTests`
2. 回到当前目录执行部署：`docker compose up -d`
3. 访问应用：`http://localhost:8081`

## platform 内部目录说明

- `src/`：业务代码与页面
- `sql/`：初始化 SQL
- `scripts/`：恢复与自检脚本
- `data/`：运行时数据（如头像、题库导入文件）
