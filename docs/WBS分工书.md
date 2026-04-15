# CodeQuest WBS分工书（调整版）

> 统计时间：2026-04-13
>
> 统计口径：按 WBS 主责任文件做粗粒度统计（文件行数累加），用于项目管理与验收说明，不等同于 Git 提交归属。
>
> 本版目标：组长张雨泽代码量最高，其余成员代码量更均衡。

## 成员模块与代码量汇总

| 成员 | 负责模块 | 责任文件数 | 代码量（行） |
|---|---|---:|---:|
| 张雨泽 | 核心引擎与架构中台、流式答题基础设施、统一导出控制器 | 15 | 1909 |
| 张芷宁 | 用户中心与账号文件模块 | 10 | 1482 |
| 韩博文 | 题库检索与分格式导出链路 | 5 | 1388 |
| 靳道童 | 错题收藏与报告沉淀服务 | 7 | 1256 |
| 李鑫浩 | 核心答题前端终端与基础提交链路 | 4 | 1582 |
| 高嵩 | 后台管理与可视化、周报与PDF展示层 | 10 | 1319 |

---

## 张雨泽（组长）

### 负责模块

- 核心引擎与架构中台（AI 调度、连接池、全局过滤链）
- 流式答题基础设施（会话上下文、草稿同步）
- 统一导出控制器与部署脚本

### 主责任文件

- platform/src/main/java/com/codequest/util/AIService.java
- platform/src/main/java/com/codequest/util/JDBCUtils.java
- platform/src/main/java/com/codequest/listener/AppStartupListener.java
- platform/src/main/java/com/codequest/filter/GlobalExceptionFilter.java
- platform/src/main/java/com/codequest/filter/CharacterEncodingFilter.java
- platform/src/main/java/com/codequest/filter/LoggingFilter.java
- platform/src/main/java/com/codequest/filter/XssFilter.java
- platform/src/main/java/com/codequest/dao/BaseDAO.java
- docker-compose.yml
- platform/scripts/recover_codequest_db.sql
- platform/src/main/java/com/codequest/servlet/SubmitAnswerStreamServlet.java
- platform/src/main/java/com/codequest/servlet/InterviewSessionServlet.java
- platform/src/main/java/com/codequest/servlet/AnswerDraftServlet.java
- platform/src/main/java/com/codequest/dao/AnswerDraftDAO.java
- platform/src/main/java/com/codequest/servlet/ExportServlet.java

## 张芷宁

### 负责模块

- 用户中心与账号体系
- 头像文件上传与访问

### 主责任文件

- platform/src/main/java/com/codequest/servlet/UserProfileServlet.java
- platform/src/main/java/com/codequest/servlet/AvatarUploadServlet.java
- platform/src/main/java/com/codequest/servlet/AvatarImageServlet.java
- platform/src/main/java/com/codequest/servlet/LoginServlet.java
- platform/src/main/java/com/codequest/servlet/RegisterServlet.java
- platform/src/main/java/com/codequest/servlet/LogoutServlet.java
- platform/src/main/java/com/codequest/service/UserService.java
- platform/src/main/java/com/codequest/dao/UserDAO.java
- platform/src/main/webapp/WEB-INF/jsp/profile.jsp
- platform/src/main/webapp/avatar_upload.jsp

## 韩博文

### 负责模块

- 题库深度检索
- 分格式导出链路（CSV/题库导出）

### 主责任文件

- platform/src/main/java/com/codequest/dao/QuestionDAO.java
- platform/src/main/java/com/codequest/servlet/QuestionListServlet.java
- platform/src/main/java/com/codequest/servlet/ExportCsvServlet.java
- platform/src/main/java/com/codequest/servlet/ExportBankServlet.java
- platform/src/main/webapp/questions.jsp

## 靳道童

### 负责模块

- 错题收藏沉淀
- 面试报告查询与下载服务

### 主责任文件

- platform/src/main/java/com/codequest/dao/EvaluationDAO.java
- platform/src/main/java/com/codequest/service/EvaluationService.java
- platform/src/main/java/com/codequest/dao/FavoriteDAO.java
- platform/src/main/java/com/codequest/servlet/FavoriteServlet.java
- platform/src/main/java/com/codequest/dao/ReportDAO.java
- platform/src/main/java/com/codequest/service/ReportService.java
- platform/src/main/java/com/codequest/servlet/ReportDownloadServlet.java

## 李鑫浩

### 负责模块

- 核心答题终端页面与交互体验
- 基础作答提交流程

### 主责任文件

- platform/src/main/webapp/question_detail.jsp
- platform/src/main/webapp/result.jsp
- platform/src/main/java/com/codequest/servlet/QuestionDetailServlet.java
- platform/src/main/java/com/codequest/servlet/SubmitAnswerServlet.java

## 高嵩

### 负责模块

- 后台管理与可视化页面
- 周报与PDF展示层交互

### 主责任文件

- platform/src/main/webapp/admin_questions.jsp
- platform/src/main/webapp/admin_prompts.jsp
- platform/src/main/java/com/codequest/servlet/AdminQuestionServlet.java
- platform/src/main/java/com/codequest/servlet/AdminPromptServlet.java
- platform/src/main/java/com/codequest/servlet/AdminAiServlet.java
- platform/src/main/java/com/codequest/dao/AdminQuestionDAO.java
- platform/src/main/java/com/codequest/dao/PromptDAO.java
- platform/src/main/java/com/codequest/servlet/SessionDetailServlet.java
- platform/src/main/java/com/codequest/servlet/WeeklyReportServlet.java
- platform/src/main/java/com/codequest/servlet/ExportPdfServlet.java

---

## 备注

- 本分工书用于课程/项目验收说明，强调“模块主责”而非“行级作者归属”。
- 为避免重复计数，本版采用“文件唯一归属”原则。
