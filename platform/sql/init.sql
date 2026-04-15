-- CodeQuest 数据库一键恢复脚本（可重复执行）
-- 用法（PowerShell，推荐，避免中文编码问题）：
-- docker cp .\scripts\recover_codequest_db.sql codequest-mysql:/tmp/recover_codequest_db.sql
-- docker exec codequest-mysql sh -lc "mysql -uroot -proot < /tmp/recover_codequest_db.sql"

CREATE DATABASE IF NOT EXISTS codequest_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE codequest_db;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL,
  password VARCHAR(64) NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'USER',
  avatar_url VARCHAR(255) NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_question (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  type INT NULL,
  difficulty INT NULL,
  tags VARCHAR(255) NULL,
  standard_answer TEXT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_question (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  type INT NULL,
  difficulty INT NULL,
  tags VARCHAR(255) NULL,
  standard_answer TEXT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_interview_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  user_answer TEXT,
  ai_score INT,
  ai_suggestion TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_t_interview_user_question (user_id, question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_evaluation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  question_id BIGINT NULL,
  session_id VARCHAR(64) NULL,
  user_answer TEXT NULL,
  score INT NOT NULL,
  category VARCHAR(100) NULL,
  feedback TEXT NULL,
  token_used INT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_sys_eval_user_time (user_id, created_at),
  KEY idx_sys_eval_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_favorite (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_sys_favorite_user_question (user_id, question_id),
  KEY idx_sys_favorite_user_time (user_id, create_time),
  KEY idx_sys_favorite_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_answer_draft (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  question_id BIGINT NOT NULL,
  draft_content MEDIUMTEXT NULL,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_sys_answer_draft_user_question (user_id, question_id),
  KEY idx_sys_answer_draft_user_time (user_id, update_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_prompt_template (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  template_name VARCHAR(100) NOT NULL,
  content TEXT NOT NULL,
  status TINYINT NOT NULL DEFAULT 1,
  UNIQUE KEY uk_sys_prompt_template_name (template_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_report (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  period_start DATETIME NOT NULL,
  period_end DATETIME NOT NULL,
  title VARCHAR(255) NOT NULL,
  content MEDIUMTEXT NOT NULL,
  summary VARCHAR(500) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_sys_report_user_period (user_id, period_start, period_end),
  KEY idx_sys_report_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

START TRANSACTION;

INSERT INTO sys_user (username, password, role, create_time, update_time)
VALUES ('Oliveira', MD5('123456'), 'admin', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  password = VALUES(password),
  role = 'admin',
  update_time = NOW();

INSERT INTO sys_user (username, password, role, create_time, update_time)
VALUES ('testuser', MD5('123456'), 'USER', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  password = VALUES(password),
  role = 'USER',
  update_time = NOW();

INSERT INTO sys_user (username, password, role, create_time, update_time)
VALUES ('admin', MD5('admin123'), 'admin', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  password = VALUES(password),
  role = 'admin',
  update_time = NOW();

DELETE FROM sys_question;
DELETE FROM t_question;

ALTER TABLE t_question AUTO_INCREMENT = 1;
ALTER TABLE sys_question AUTO_INCREMENT = 1;

INSERT INTO t_question (title, content, type, difficulty, tags, standard_answer) VALUES
('Java 集合：ArrayList 与 LinkedList 对比', '请说明两者底层结构、随机访问复杂度、插入删除复杂度，并给出典型使用场景。', 4, 2, 'Java,集合', 'ArrayList 基于动态数组，随机访问 O(1)，中间插入删除通常 O(n)；LinkedList 基于双向链表，随机访问 O(n)，在已定位节点附近插入删除 O(1)。读多写少优先 ArrayList。'),
('MySQL 索引失效场景', '请列举至少 3 种 B+Tree 索引失效写法，并给出改写建议。', 4, 3, '数据库,MySQL', '常见失效：索引列参与函数计算、前导模糊 like "%xx"、违反最左前缀、隐式类型转换。应改写查询条件以命中索引。'),
('设计一个限流组件', '设计一个支持单机与分布式场景的限流组件，说明算法选择、降级策略与监控指标。', 5, 4, '系统设计,限流', '可用令牌桶或滑动窗口；分布式可基于 Redis + Lua 保证原子性。需具备降级、动态配置、监控告警能力。'),
('HTTP 与 HTTPS 的区别', '请说明 HTTP 与 HTTPS 的核心差异，TLS 握手关键步骤，以及常见性能优化手段。', 4, 2, '网络,安全', 'HTTPS 在 HTTP 上增加 TLS，提供加密与身份校验。可通过 HTTP/2、会话复用、OCSP Stapling、CDN 等优化性能。'),
('Java 线程池调优方法', '如何针对 CPU 密集型与 IO 密集型任务设置线程池参数？请说明关键监控指标。', 4, 3, 'Java,并发', 'CPU 密集型线程数接近核心数；IO 密集型可适当放大。重点监控队列长度、活跃线程数、拒绝率、响应时间。'),
('Redis 缓存击穿与雪崩', '解释缓存击穿与缓存雪崩，并给出工程上的防护方案。', 4, 3, 'Redis,缓存', '击穿可用互斥重建、逻辑过期；雪崩可用随机过期时间、多级缓存、限流降级。'),
('事务隔离级别对比', '比较读未提交、读已提交、可重复读、串行化在脏读、不可重复读、幻读上的差异。', 4, 3, '数据库,事务', '隔离级别越高一致性越强并发越低；串行化最严格但吞吐最低。'),
('消息队列近似 Exactly-Once', '设计一套接近 Exactly-Once 的消息处理方案，覆盖生产者、消费者与补偿机制。', 5, 4, 'MQ,可靠性', '可结合幂等键、事务外盒、发送确认、消费去重、重试与死信队列实现近似 Exactly-Once。'),
('二分查找实现与边界处理', '实现二分查找，并说明循环终止条件、边界更新和防溢出 mid 计算。', 3, 1, '算法,查找', '使用 left <= right；mid = left + (right - left) / 2；根据比较结果更新边界，并覆盖空数组和单元素等边界用例。'),
('微服务可观测性基线', '请定义微服务最小可观测性方案，覆盖日志、指标、链路追踪和告警。', 5, 2, '微服务,可观测性', '应具备结构化日志与 traceId、黄金指标监控、分布式追踪、SLO 告警和统一看板。');

INSERT INTO sys_question (title, content, type, difficulty, tags, standard_answer)
SELECT title, content, type, difficulty, tags, standard_answer
FROM t_question
ORDER BY id;

INSERT INTO sys_prompt_template (template_name, content, status)
VALUES (
  'evaluation_prompt',
  '你是一个严谨的 IT 面试官。请基于题目内容：${questionContent}、标准答案：${standardAnswer}、当前用户回答：${userAnswer} 和历史对话：${conversationHistory} 进行多轮面试评测。请只返回 JSON，包含 score、feedback、category、followUpQuestion 四个字段。规则：1）score 为 0-100 整数；2）feedback 要给出具体优点、问题和改进建议；3）category 为技术分类；4）当 score > 70 时，followUpQuestion 必须生成一个有针对性的追问问题；当 score <= 70 时，followUpQuestion 置为空字符串；5）不要输出任何多余解释。',
  1
)
ON DUPLICATE KEY UPDATE
  content = VALUES(content),
  status = VALUES(status);

COMMIT;
