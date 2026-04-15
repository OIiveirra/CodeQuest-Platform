package com.codequest.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.codequest.model.Evaluation;
import com.codequest.model.EvaluationRecord;
import com.codequest.model.EvaluationReport;

/**
 * 面试评分数据访问对象，负责 sys_evaluation 表的写入与查询。
 * Author: 张雨泽
 */
public class EvaluationDAO extends BaseDAO {

    public int insert(Evaluation eval) throws SQLException {
        if (eval == null || eval.getUserId() == null || eval.getScore() == null) {
            return 0;
        }
        boolean shouldTryTokenColumn = eval.getTokenUsed() != null;
        if (shouldTryTokenColumn) {
            try {
                return insertInternal(eval, true);
            } catch (SQLException ex) {
                if (!containsColumn(ex, "token_used")) {
                    throw ex;
                }
            }
        }

        return insertInternal(eval, false);
    }

    private int insertInternal(Evaluation eval, boolean includeTokenUsed) throws SQLException {
        String sqlFull = includeTokenUsed
                ? "INSERT INTO sys_evaluation (user_id, question_id, session_id, user_answer, score, category, feedback, token_used, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())"
                : "INSERT INTO sys_evaluation (user_id, question_id, session_id, user_answer, score, category, feedback, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        String sqlNoFeedback = includeTokenUsed
                ? "INSERT INTO sys_evaluation (user_id, question_id, session_id, user_answer, score, category, token_used, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())"
                : "INSERT INTO sys_evaluation (user_id, question_id, session_id, user_answer, score, category, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())";
        String sqlNoSession = includeTokenUsed
                ? "INSERT INTO sys_evaluation (user_id, question_id, user_answer, score, category, feedback, token_used, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())"
                : "INSERT INTO sys_evaluation (user_id, question_id, user_answer, score, category, feedback, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())";
        String sqlNoSessionNoFeedback = includeTokenUsed
                ? "INSERT INTO sys_evaluation (user_id, question_id, user_answer, score, category, token_used, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())"
                : "INSERT INTO sys_evaluation (user_id, question_id, user_answer, score, category, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
        String sqlLegacyWithFeedback = includeTokenUsed
                ? "INSERT INTO sys_evaluation (user_id, question_id, score, category, feedback, token_used, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())"
                : "INSERT INTO sys_evaluation (user_id, question_id, score, category, feedback, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
        String sqlLegacyNoFeedback = includeTokenUsed
                ? "INSERT INTO sys_evaluation (user_id, question_id, score, category, token_used, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())"
                : "INSERT INTO sys_evaluation (user_id, question_id, score, category, created_at, updated_at) VALUES (?, ?, ?, ?, NOW(), NOW())";

        boolean hasSessionId = eval.getSessionId() != null && !eval.getSessionId().trim().isEmpty();
        if (hasSessionId) {
            try {
                return executeInsert(eval, sqlFull, true, true, true, includeTokenUsed);
            } catch (SQLException ex) {
                if (containsColumn(ex, "feedback")) {
                    return executeInsert(eval, sqlNoFeedback, true, true, false, includeTokenUsed);
                }
                if (containsColumn(ex, "session_id")) {
                    try {
                        return executeInsert(eval, sqlNoSession, false, true, true, includeTokenUsed);
                    } catch (SQLException noSessionEx) {
                        if (containsColumn(noSessionEx, "feedback")) {
                            return executeInsert(eval, sqlNoSessionNoFeedback, false, true, false, includeTokenUsed);
                        }
                        if (!containsColumn(noSessionEx, "user_answer")) {
                            throw noSessionEx;
                        }
                    }
                } else if (!containsColumn(ex, "user_answer")) {
                    throw ex;
                }
            }
        }

        try {
            return executeInsert(eval, sqlNoSession, false, true, true, includeTokenUsed);
        } catch (SQLException ex) {
            if (containsColumn(ex, "feedback")) {
                return executeInsert(eval, sqlNoSessionNoFeedback, false, true, false, includeTokenUsed);
            }
            if (!containsColumn(ex, "user_answer")) {
                throw ex;
            }
        }

        try {
            return executeInsert(eval, sqlLegacyWithFeedback, false, false, true, includeTokenUsed);
        } catch (SQLException ex) {
            if (containsColumn(ex, "feedback")) {
                return executeInsert(eval, sqlLegacyNoFeedback, false, false, false, includeTokenUsed);
            }
            throw ex;
        }
    }

    public void saveEvaluation(long userId, Long questionId, int score, String category) throws SQLException {
        saveEvaluation(userId, questionId, score, category, null);
    }

    public void saveEvaluation(long userId, Long questionId, int score, String category, String sessionId) throws SQLException {
        Evaluation evaluation = new Evaluation();
        evaluation.setUserId(userId);
        evaluation.setQuestionId(questionId);
        evaluation.setScore(score);
        evaluation.setCategory(category);
        evaluation.setSessionId(sessionId);
        insert(evaluation);
    }

    public List<EvaluationRecord> findRecentByUserId(long userId, int limit) throws SQLException {
        int finalLimit = limit <= 0 ? 5 : Math.min(limit, 10);
        return queryHistoryByUserId(userId, finalLimit, 0);
    }

    public List<EvaluationRecord> getHistoryByUserId(int userId) throws SQLException {
        return getHistoryByUserId(userId, 1, 10);
    }

    public List<EvaluationRecord> getHistoryByUserId(int userId, int page, int pageSize) throws SQLException {
        int finalPage = Math.max(1, page);
        int finalPageSize = Math.max(1, Math.min(50, pageSize));
        int offset = (finalPage - 1) * finalPageSize;
        return queryHistoryByUserId(userId, finalPageSize, offset);
    }

    public int countHistoryByUserId(int userId) throws SQLException {
        String sqlWithSession = "SELECT COUNT(DISTINCT COALESCE(session_id, CONCAT('legacy-', id))) FROM sys_evaluation WHERE user_id = ?";
        String sqlWithoutSession = "SELECT COUNT(*) FROM sys_evaluation WHERE user_id = ?";
        try {
            return queryCount(userId, sqlWithSession);
        } catch (SQLException ex) {
            if (containsColumn(ex, "session_id")) {
                return queryCount(userId, sqlWithoutSession);
            }
            throw ex;
        }
    }

    public List<EvaluationRecord> findEvaluationsBySessionId(String sessionId) throws SQLException {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String finalSessionId = sessionId.trim();
        if (finalSessionId.startsWith("legacy-")) {
            Long legacyId = parseLegacyId(finalSessionId);
            if (legacyId == null) {
                return new ArrayList<>();
            }
            return queryRecordsByLegacyEvaluationId(legacyId);
        }

        String sqlWithSession = "SELECT e.id AS evaluation_id, e.user_id, u.username, e.session_id, e.question_id, "
            + "COALESCE(sq.title, tq.title, CONCAT('题目 #', e.question_id)) AS question_title, "
            + "COALESCE(sq.content, tq.content, '') AS question_content, "
            + "COALESCE(NULLIF(e.user_answer, ''), ir.user_answer, '') AS user_answer, "
            + "e.score, e.category, COALESCE(e.feedback, '') AS feedback, e.created_at "
            + "FROM sys_evaluation e "
            + "LEFT JOIN sys_user u ON e.user_id = u.id "
            + "LEFT JOIN sys_question sq ON e.question_id = sq.id "
            + "LEFT JOIN t_question tq ON e.question_id = tq.id "
            + "LEFT JOIN t_interview_record ir ON ir.id = ("
            + "SELECT ir2.id FROM t_interview_record ir2 "
            + "WHERE ir2.user_id = e.user_id AND ir2.question_id = e.question_id "
            + "ORDER BY ABS(TIMESTAMPDIFF(SECOND, ir2.created_at, e.created_at)) ASC LIMIT 1"
            + ") "
                + "WHERE e.session_id = ? ORDER BY e.created_at ASC, e.id ASC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlWithSession)) {
            ps.setString(1, finalSessionId);
            try (ResultSet rs = ps.executeQuery()) {
                return readDetailRecords(rs);
            }
        }
    }

    private int queryCount(int userId, String sql) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        }
    }

    public EvaluationReport findReportByEvaluationId(long evaluationId, long currentUserId, boolean isAdmin) throws SQLException {
        String sqlWithEvalUserAnswer = "SELECT e.id AS evaluation_id, e.user_id, u.username, e.question_id, q.title AS question_title, q.content AS question_content, "
            + "COALESCE(NULLIF(e.user_answer, ''), ir.user_answer, '') AS user_answer, e.score AS ai_score, "
                + "COALESCE(NULLIF(e.feedback, ''), ir.ai_suggestion, '') AS ai_suggestion, "
                + "COALESCE(e.category, '') AS category, e.created_at "
                + "FROM sys_evaluation e "
                + "LEFT JOIN sys_user u ON e.user_id = u.id "
                + "LEFT JOIN t_question q ON e.question_id = q.id "
                + "LEFT JOIN t_interview_record ir ON ir.id = ("
                + "SELECT ir2.id FROM t_interview_record ir2 "
                + "WHERE ir2.user_id = e.user_id AND ir2.question_id = e.question_id "
                + "ORDER BY ABS(TIMESTAMPDIFF(SECOND, ir2.created_at, e.created_at)) ASC LIMIT 1"
                + ") "
                + "WHERE e.id = ?" + (isAdmin ? "" : " AND e.user_id = ?") + " LIMIT 1";

        String sqlLegacy = "SELECT e.id AS evaluation_id, e.user_id, u.username, e.question_id, q.title AS question_title, q.content AS question_content, "
                + "COALESCE(ir.user_answer, '') AS user_answer, e.score AS ai_score, "
                + "COALESCE(NULLIF(e.feedback, ''), ir.ai_suggestion, '') AS ai_suggestion, "
                + "COALESCE(e.category, '') AS category, e.created_at "
                + "FROM sys_evaluation e "
                + "LEFT JOIN sys_user u ON e.user_id = u.id "
                + "LEFT JOIN t_question q ON e.question_id = q.id "
                + "LEFT JOIN t_interview_record ir ON ir.id = ("
                + "SELECT ir2.id FROM t_interview_record ir2 "
                + "WHERE ir2.user_id = e.user_id AND ir2.question_id = e.question_id "
                + "ORDER BY ABS(TIMESTAMPDIFF(SECOND, ir2.created_at, e.created_at)) ASC LIMIT 1"
                + ") "
                + "WHERE e.id = ?" + (isAdmin ? "" : " AND e.user_id = ?") + " LIMIT 1";

        try {
            return queryReportBySql(sqlWithEvalUserAnswer, evaluationId, currentUserId, isAdmin);
        } catch (SQLException ex) {
            if (containsColumn(ex, "user_answer")) {
                return queryReportBySql(sqlLegacy, evaluationId, currentUserId, isAdmin);
            }
            throw ex;
        }
    }

    /**
     * 查询用户最近 10 次测评趋势（时间 + 分数），按时间升序返回。
     */
    public List<EvaluationRecord> getRecentTrendByUserId(int userId) throws SQLException {
        String sql = "SELECT score, created_at FROM ("
                + "SELECT score, created_at FROM sys_evaluation WHERE user_id = ? ORDER BY created_at DESC LIMIT 10"
                + ") t ORDER BY created_at ASC";
        List<EvaluationRecord> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EvaluationRecord record = new EvaluationRecord();
                    int score = rs.getInt("score");
                    record.setScore(rs.wasNull() ? null : score);
                    record.setNeedRetrain(!rs.wasNull() && score < 60);
                    record.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(record);
                }
            }
        }
        return list;
    }

    public Map<String, Double> findCategoryAveragesByUserId(long userId) throws SQLException {
        String sql = "SELECT category, AVG(score) AS avg_score "
                + "FROM sys_evaluation WHERE user_id = ? GROUP BY category ORDER BY avg_score DESC";
        Map<String, Double> result = new LinkedHashMap<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String category = rs.getString("category");
                    if (category == null || category.trim().isEmpty()) {
                        category = "综合能力";
                    }
                    result.put(category, rs.getDouble("avg_score"));
                }
            }
        }
        return result;
    }

    /**
     * 获取指定用户按分类聚合后的平均分。
     */
    public Map<String, Double> getAverageScoresByCategory(int userId) throws SQLException {
        return findCategoryAveragesByUserId(userId);
    }

    public List<EvaluationRecord> findEvaluationsByUserIdWithinDays(long userId, int days) throws SQLException {
        int finalDays = days <= 0 ? 7 : Math.min(days, 30);
        String sql = "SELECT COALESCE(e.session_id, CONCAT('legacy-', e.id)) AS session_id, "
                + "e.question_id, COALESCE(q.title, CONCAT('题目 #', e.question_id)) AS question_title, "
                + "e.score, COALESCE(e.category, '') AS category, COALESCE(e.feedback, '') AS feedback, e.created_at "
                + "FROM sys_evaluation e "
                + "LEFT JOIN t_question q ON q.id = e.question_id "
                + "WHERE e.user_id = ? AND e.created_at >= DATE_SUB(NOW(), INTERVAL ? DAY) "
                + "ORDER BY e.created_at ASC, e.id ASC";

        List<EvaluationRecord> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setInt(2, finalDays);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EvaluationRecord record = new EvaluationRecord();
                    record.setSessionId(rs.getString("session_id"));
                    long questionId = rs.getLong("question_id");
                    record.setQuestionId(rs.wasNull() ? null : questionId);
                    record.setQuestionTitle(rs.getString("question_title"));
                    int score = rs.getInt("score");
                    record.setScore(rs.wasNull() ? null : score);
                    record.setNeedRetrain(!rs.wasNull() && score < 60);
                    record.setCategory(rs.getString("category"));
                    record.setFeedback(rs.getString("feedback"));
                    record.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(record);
                }
            }
        }
        return list;
    }

    public List<EvaluationRecord> findWrongQuestionsByUserId(long userId) throws SQLException {
        return queryWrongQuestionsByUserId(userId, null);
    }

    public List<EvaluationRecord> findWrongQuestionsByUserId(long userId, int limit) throws SQLException {
        Integer finalLimit = limit <= 0 ? null : Math.min(limit, 50);
        return queryWrongQuestionsByUserId(userId, finalLimit);
    }

    private List<EvaluationRecord> queryWrongQuestionsByUserId(long userId, Integer limit) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT e.id AS evaluation_id, e.question_id, COALESCE(q.title, CONCAT('题目 #', e.question_id)) AS question_title, ")
            .append("e.score, e.created_at ")
            .append("FROM sys_evaluation e ")
            .append("INNER JOIN ( ")
            .append("    SELECT question_id, MAX(id) AS max_id ")
            .append("    FROM sys_evaluation ")
            .append("    WHERE user_id = ? AND question_id IS NOT NULL ")
            .append("    GROUP BY question_id ")
            .append(") latest ON latest.max_id = e.id ")
            .append("LEFT JOIN t_question q ON q.id = e.question_id ")
            .append("WHERE e.score < 60 ")
            .append("ORDER BY e.created_at DESC, e.id DESC");
        if (limit != null) {
            sql.append(" LIMIT ?");
        }

        List<EvaluationRecord> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setLong(1, userId);
            if (limit != null) {
                ps.setInt(2, limit);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EvaluationRecord record = new EvaluationRecord();
                    long evaluationId = rs.getLong("evaluation_id");
                    record.setEvaluationId(rs.wasNull() ? null : evaluationId);
                    long questionId = rs.getLong("question_id");
                    record.setQuestionId(rs.wasNull() ? null : questionId);
                    record.setQuestionTitle(rs.getString("question_title"));
                    int score = rs.getInt("score");
                    record.setScore(rs.wasNull() ? null : score);
                    record.setNeedRetrain(!rs.wasNull() && score < 60);
                    record.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(record);
                }
            }
        }
        return list;
    }

    private void bindInsertCommon(PreparedStatement ps, Evaluation eval) throws SQLException {
        ps.setLong(1, eval.getUserId());
        if (eval.getQuestionId() == null) {
            ps.setNull(2, java.sql.Types.BIGINT);
        } else {
            ps.setLong(2, eval.getQuestionId());
        }
    }

    private List<EvaluationRecord> queryHistoryByUserId(long userId, int limit, int offset) throws SQLException {
        String sessionExpr = "COALESCE(e.session_id, CONCAT('legacy-', e.id))";
        String sqlGrouped = "SELECT MAX(e.id) AS evaluation_id, "
                + sessionExpr + " AS session_id, "
                + "MAX(e.question_id) AS question_id, "
                + "CONCAT('本场面试（', COUNT(*), '题）') AS question_title, "
                + "ROUND(AVG(e.score)) AS score, "
                + "COALESCE(MAX(NULLIF(e.category, '')), '综合能力') AS category, "
                + "GROUP_CONCAT(COALESCE(NULLIF(e.feedback, ''), '暂无反馈详情') ORDER BY e.created_at SEPARATOR '\\n\\n---\\n\\n') AS feedback, "
                + "MAX(e.created_at) AS created_at, "
                + "COUNT(*) AS question_count "
                + "FROM sys_evaluation e "
                + "WHERE e.user_id = ? "
                + "GROUP BY " + sessionExpr + " "
                + "ORDER BY MAX(e.created_at) DESC LIMIT ? OFFSET ?";

        String sqlLegacy = "SELECT e.id AS evaluation_id, CONCAT('legacy-', e.id) AS session_id, e.question_id, "
                + "COALESCE(q.title, CONCAT('题目 #', e.question_id)) AS question_title, e.score, e.category, "
                + "COALESCE(e.feedback, '暂无反馈详情') AS feedback, e.created_at, 1 AS question_count "
                + "FROM sys_evaluation e LEFT JOIN t_question q ON e.question_id = q.id "
                + "WHERE e.user_id = ? ORDER BY e.created_at DESC LIMIT ? OFFSET ?";

        try {
            return querySessionHistory(sqlGrouped, userId, limit, offset);
        } catch (SQLException ex) {
            if (containsColumn(ex, "session_id")) {
                return querySessionHistory(sqlLegacy, userId, limit, offset);
            }
            throw ex;
        }
    }

    private List<EvaluationRecord> querySessionHistory(String sql, long userId, int limit, int offset) throws SQLException {
        List<EvaluationRecord> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setInt(2, Math.max(1, limit));
            ps.setInt(3, Math.max(0, offset));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EvaluationRecord record = new EvaluationRecord();
                    long evaluationId = rs.getLong("evaluation_id");
                    record.setEvaluationId(rs.wasNull() ? null : evaluationId);
                    record.setSessionId(rs.getString("session_id"));
                    long questionId = rs.getLong("question_id");
                    record.setQuestionId(rs.wasNull() ? null : questionId);
                    record.setQuestionTitle(rs.getString("question_title"));
                    int score = rs.getInt("score");
                    record.setScore(rs.wasNull() ? null : score);
                    record.setNeedRetrain(!rs.wasNull() && score < 60);
                    record.setCategory(rs.getString("category"));
                    record.setFeedback(rs.getString("feedback"));
                    record.setCreatedAt(rs.getTimestamp("created_at"));
                    int questionCount = rs.getInt("question_count");
                    record.setQuestionCount(rs.wasNull() ? null : questionCount);
                    list.add(record);
                }
            }
        }
        return list;
    }

    private List<EvaluationRecord> readDetailRecords(ResultSet rs) throws SQLException {
        List<EvaluationRecord> list = new ArrayList<>();
        while (rs.next()) {
            EvaluationRecord record = new EvaluationRecord();
            long evaluationId = rs.getLong("evaluation_id");
            record.setEvaluationId(rs.wasNull() ? null : evaluationId);
            long userId = rs.getLong("user_id");
            record.setUserId(rs.wasNull() ? null : userId);
            record.setUsername(rs.getString("username"));
            record.setSessionId(rs.getString("session_id"));
            long questionId = rs.getLong("question_id");
            record.setQuestionId(rs.wasNull() ? null : questionId);
            record.setQuestionTitle(rs.getString("question_title"));
            record.setQuestionContent(rs.getString("question_content"));
            record.setUserAnswer(rs.getString("user_answer"));
            int score = rs.getInt("score");
            record.setScore(rs.wasNull() ? null : score);
            record.setNeedRetrain(!rs.wasNull() && score < 60);
            record.setCategory(rs.getString("category"));
            record.setFeedback(rs.getString("feedback"));
            record.setCreatedAt(rs.getTimestamp("created_at"));
            record.setQuestionCount(1);
            list.add(record);
        }
        return list;
    }

    private List<EvaluationRecord> queryRecordsByLegacyEvaluationId(Long legacyId) throws SQLException {
        String sql = "SELECT e.id AS evaluation_id, e.user_id, u.username, CONCAT('legacy-', e.id) AS session_id, e.question_id, "
            + "COALESCE(sq.title, tq.title, CONCAT('题目 #', e.question_id)) AS question_title, "
            + "COALESCE(sq.content, tq.content, '') AS question_content, "
            + "COALESCE(NULLIF(e.user_answer, ''), ir.user_answer, '') AS user_answer, "
            + "e.score, e.category, COALESCE(e.feedback, '') AS feedback, e.created_at "
            + "FROM sys_evaluation e "
            + "LEFT JOIN sys_user u ON e.user_id = u.id "
            + "LEFT JOIN sys_question sq ON e.question_id = sq.id "
            + "LEFT JOIN t_question tq ON e.question_id = tq.id "
            + "LEFT JOIN t_interview_record ir ON ir.id = ("
            + "SELECT ir2.id FROM t_interview_record ir2 "
            + "WHERE ir2.user_id = e.user_id AND ir2.question_id = e.question_id "
            + "ORDER BY ABS(TIMESTAMPDIFF(SECOND, ir2.created_at, e.created_at)) ASC LIMIT 1"
            + ") "
            + "WHERE e.id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, legacyId);
            try (ResultSet rs = ps.executeQuery()) {
                return readDetailRecords(rs);
            }
        }
    }

    private int executeInsert(Evaluation eval, String sql, boolean includeSessionId, boolean includeUserAnswer,
                              boolean includeFeedback, boolean includeTokenUsed) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindInsertCommon(ps, eval);
            int index = 3;
            if (includeSessionId) {
                ps.setString(index++, eval.getSessionId());
            }
            if (includeUserAnswer) {
                ps.setString(index++, eval.getUserAnswer());
            }
            ps.setInt(index++, eval.getScore());
            ps.setString(index++, eval.getCategory() == null || eval.getCategory().trim().isEmpty() ? "综合能力" : eval.getCategory().trim());
            if (includeFeedback) {
                ps.setString(index++, eval.getFeedback());
            }
            if (includeTokenUsed) {
                if (eval.getTokenUsed() == null) {
                    ps.setNull(index, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(index, Math.max(0, eval.getTokenUsed()));
                }
            }
            return ps.executeUpdate();
        }
    }

    private EvaluationReport queryReportBySql(String sql, long evaluationId, long currentUserId, boolean isAdmin) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, evaluationId);
            if (!isAdmin) {
                ps.setLong(2, currentUserId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                EvaluationReport report = new EvaluationReport();
                report.setEvaluationId(rs.getLong("evaluation_id"));
                report.setUserId(rs.getLong("user_id"));
                report.setUsername(rs.getString("username"));
                long qid = rs.getLong("question_id");
                report.setQuestionId(rs.wasNull() ? null : qid);
                report.setQuestionTitle(rs.getString("question_title"));
                report.setQuestionContent(rs.getString("question_content"));
                report.setUserAnswer(rs.getString("user_answer"));
                report.setAiScore(rs.getInt("ai_score"));
                report.setAiSuggestion(rs.getString("ai_suggestion"));
                report.setCategory(rs.getString("category"));
                report.setCreatedAt(rs.getTimestamp("created_at"));
                return report;
            }
        }
    }

    private boolean containsColumn(SQLException ex, String columnName) {
        return ex != null && ex.getMessage() != null && ex.getMessage().contains(columnName);
    }

    private Long parseLegacyId(String sessionId) {
        try {
            return Long.valueOf(sessionId.substring("legacy-".length()));
        } catch (Exception ex) {
            return null;
        }
    }
}