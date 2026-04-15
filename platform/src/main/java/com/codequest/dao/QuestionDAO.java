package com.codequest.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.codequest.model.Question;
import com.codequest.util.JDBCUtils;

/**
 * 题目与面试记录数据访问对象，负责题库读取与作答结果落库。
 * Author: 张雨泽
 */
public class QuestionDAO extends BaseDAO {

    private static final Set<String> ORDER_BY_WHITELIST;

    static {
        Set<String> sortColumns = new HashSet<>();
        sortColumns.add("id");
        sortColumns.add("title");
        sortColumns.add("difficulty");
        sortColumns.add("type");
        sortColumns.add("created_at");
        ORDER_BY_WHITELIST = Collections.unmodifiableSet(sortColumns);
    }

    public List<Question> findQuestionsByCriteria(String keyword, String category, Integer difficulty) throws SQLException {
        return findQuestionsByCriteria(keyword, category, difficulty, null);
    }

    public List<Question> findQuestionsByCriteria(String keyword, String category, Integer difficulty, Long currentUserId) throws SQLException {
        SearchCriteria criteria = new SearchCriteria();
        criteria.setKeyword(keyword);
        criteria.setCategory(category);
        criteria.setExactDifficulty(difficulty);
        criteria.setOrderBy("id");
        criteria.setOrderDirection("ASC");
        return searchQuestions(criteria, currentUserId);
    }

    public int countQuestionsByCriteria(SearchCriteria criteria) throws SQLException {
        return countQuestionsByCriteria(criteria, null);
    }

    public int countQuestionsByCriteria(SearchCriteria criteria, Long currentUserId) throws SQLException {
        SearchCriteria safeCriteria = criteria == null ? new SearchCriteria() : criteria;

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM t_question q ");
        List<Object> params = new ArrayList<>();
        appendUserJoin(sql, params, currentUserId);
        appendWhereClause(sql, params, safeCriteria);

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public List<Question> searchQuestions(SearchCriteria criteria) throws SQLException {
        return searchQuestions(criteria, null);
    }

    public List<Question> searchQuestions(SearchCriteria criteria, Long currentUserId) throws SQLException {
        SearchCriteria safeCriteria = criteria == null ? new SearchCriteria() : criteria;

        List<Question> questions = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        if (currentUserId != null) {
            sql.append("SELECT q.*, CASE WHEN completed.question_id IS NULL THEN 0 ELSE 1 END AS completed ");
        } else {
            sql.append("SELECT q.*, 0 AS completed ");
        }
        sql.append("FROM t_question q ");
        List<Object> params = new ArrayList<>();
        appendUserJoin(sql, params, currentUserId);
        appendWhereClause(sql, params, safeCriteria);
        appendOrderByClause(sql, safeCriteria);
        appendPagination(sql, params, safeCriteria);

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bindParams(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                Set<String> columns = readColumns(rs.getMetaData());
                while (rs.next()) {
                    questions.add(mapQuestion(rs, columns));
                }
            }
        }

        return questions;
    }

    public int batchInsertQuestions(List<Question> questions) throws SQLException {
        if (questions == null || questions.isEmpty()) {
            return 0;
        }

        List<Question> uniqueQuestions = deduplicateByTitle(questions);
        if (uniqueQuestions.isEmpty()) {
            return 0;
        }

        int affected = 0;
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                long nextQuestionId = findNextGlobalQuestionId(conn);
                for (Question question : uniqueQuestions) {
                    if (question == null || isBlank(question.getTitle()) || isBlank(question.getContent())) {
                        continue;
                    }

                    String normalizedTitle = normalizeTitle(question.getTitle());
                    List<Long> existingIds = findQuestionIdsByTitle(conn, normalizedTitle);
                    if (existingIds.isEmpty()) {
                        long questionId = nextQuestionId++;
                        insertQuestion(conn, questionId, question);
                        upsertSysQuestion(conn, questionId, question);
                    } else {
                        long keepId = existingIds.get(0);
                        mergeDuplicateQuestions(conn, keepId, existingIds);
                        updateQuestionById(conn, keepId, question);
                        upsertSysQuestion(conn, keepId, question);
                    }
                    affected++;
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }

        return affected;
    }

    public void addQuestion(Question question) throws SQLException {
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO t_question (id, title, content, type, difficulty, tags, standard_answer) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            long nextId = findNextGlobalQuestionId(conn);
            bindQuestionInsert(ps, nextId, question);
            ps.executeUpdate();
        }
    }

    public int updateQuestion(Question question) throws SQLException {
        String sql = "UPDATE t_question SET title = ?, content = ?, type = ?, difficulty = ?, tags = ?, standard_answer = ? "
                + "WHERE id = ?";

        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, question.getTitle());
            ps.setString(2, question.getContent());
            ps.setObject(3, question.getType(), Types.INTEGER);
            ps.setObject(4, question.getDifficulty(), Types.INTEGER);
            ps.setString(5, question.getTags());
            ps.setString(6, question.getStandardAnswer());
            ps.setLong(7, question.getId());
            return ps.executeUpdate();
        }
    }

    public int deleteQuestion(Long id) throws SQLException {
        String sql = "DELETE FROM t_question WHERE id = ?";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        }
    }

    private List<Question> deduplicateByTitle(List<Question> questions) {
        Map<String, Question> uniqueMap = new LinkedHashMap<>();
        for (Question question : questions) {
            if (question == null || isBlank(question.getTitle()) || isBlank(question.getContent())) {
                continue;
            }
            uniqueMap.put(normalizeTitle(question.getTitle()), question);
        }
        return new ArrayList<>(uniqueMap.values());
    }

    private List<Long> findQuestionIdsByTitle(Connection conn, String title) throws SQLException {
        List<Long> ids = new ArrayList<>();
        String sql = "SELECT id FROM t_question WHERE title = ? ORDER BY id ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getLong("id"));
                }
            }
        }
        return ids;
    }

    private long insertQuestion(Connection conn, long questionId, Question question) throws SQLException {
        String sql = "INSERT INTO t_question (id, title, content, type, difficulty, tags, standard_answer) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bindQuestionInsert(ps, questionId, question);
            ps.executeUpdate();
        }
        return questionId;
    }

    private int updateQuestionById(Connection conn, long questionId, Question question) throws SQLException {
        String sql = "UPDATE t_question SET title = ?, content = ?, type = ?, difficulty = ?, tags = ?, standard_answer = ?, updated_at = NOW() "
                + "WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, question.getTitle().trim());
            ps.setString(2, question.getContent().trim());
            ps.setObject(3, question.getType(), Types.INTEGER);
            ps.setObject(4, question.getDifficulty(), Types.INTEGER);
            ps.setString(5, question.getTags());
            ps.setString(6, question.getStandardAnswer());
            ps.setLong(7, questionId);
            return ps.executeUpdate();
        }
    }

    private void upsertSysQuestion(Connection conn, long questionId, Question question) throws SQLException {
        String sql = "INSERT INTO sys_question (id, title, content, type, difficulty, tags, standard_answer, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW()) "
                + "ON DUPLICATE KEY UPDATE title = VALUES(title), content = VALUES(content), type = VALUES(type), "
                + "difficulty = VALUES(difficulty), tags = VALUES(tags), standard_answer = VALUES(standard_answer), updated_at = NOW()";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, questionId);
            ps.setString(2, question.getTitle().trim());
            ps.setString(3, question.getContent().trim());
            ps.setObject(4, question.getType(), Types.INTEGER);
            ps.setObject(5, question.getDifficulty(), Types.INTEGER);
            ps.setString(6, question.getTags());
            ps.setString(7, question.getStandardAnswer());
            ps.executeUpdate();
        }
    }

    private void mergeDuplicateQuestions(Connection conn, long keepId, List<Long> existingIds) throws SQLException {
        if (existingIds.size() <= 1) {
            return;
        }

        for (Long duplicateId : existingIds) {
            if (duplicateId == null || duplicateId == keepId) {
                continue;
            }
            reassignQuestionReferences(conn, duplicateId, keepId);
            deleteSysQuestionById(conn, duplicateId);
            deleteQuestionById(conn, duplicateId);
        }
    }

    private void reassignQuestionReferences(Connection conn, long fromQuestionId, long toQuestionId) throws SQLException {
        updateQuestionReference(conn, "sys_evaluation", fromQuestionId, toQuestionId);
        updateQuestionReference(conn, "sys_favorite", fromQuestionId, toQuestionId);
        updateQuestionReference(conn, "sys_answer_draft", fromQuestionId, toQuestionId);
        updateQuestionReference(conn, "t_interview_record", fromQuestionId, toQuestionId);
    }

    private void updateQuestionReference(Connection conn, String tableName, long fromQuestionId, long toQuestionId) throws SQLException {
        String sql = "UPDATE " + tableName + " SET question_id = ? WHERE question_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, toQuestionId);
            ps.setLong(2, fromQuestionId);
            ps.executeUpdate();
        }
    }

    private void deleteQuestionById(Connection conn, long questionId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM t_question WHERE id = ?")) {
            ps.setLong(1, questionId);
            ps.executeUpdate();
        }
    }

    private void deleteSysQuestionById(Connection conn, long questionId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM sys_question WHERE id = ?")) {
            ps.setLong(1, questionId);
            ps.executeUpdate();
        }
    }

    private long findNextGlobalQuestionId(Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(GREATEST((SELECT COALESCE(MAX(id), 0) FROM t_question), "
                + "(SELECT COALESCE(MAX(id), 0) FROM sys_question)), 0) + 1 AS next_id";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("next_id");
            }
        }
        throw new SQLException("无法计算下一条题目ID。");
    }

    private void bindQuestionInsert(PreparedStatement ps, long questionId, Question question) throws SQLException {
        ps.setLong(1, questionId);
        ps.setString(2, question.getTitle().trim());
        ps.setString(3, question.getContent().trim());
        ps.setObject(4, question.getType(), Types.INTEGER);
        ps.setObject(5, question.getDifficulty(), Types.INTEGER);
        ps.setString(6, question.getTags());
        ps.setString(7, question.getStandardAnswer());
    }

    private String normalizeTitle(String title) {
        return title == null ? "" : title.trim();
    }

    public List<Question> findAllQuestions() throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM t_question ORDER BY id ASC";

        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            // 动态读取列集合，避免表结构轻微变动时直接报错。
            Set<String> columns = readColumns(rs.getMetaData());
            while (rs.next()) {
                questions.add(mapQuestion(rs, columns));
            }
        }
        return questions;
    }

    public List<Question> findAll() throws SQLException {
        return findAllQuestions();
    }

    public Question findById(Long id) throws SQLException {
        String sql = "SELECT * FROM t_question WHERE id = ?";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                Set<String> columns = readColumns(rs.getMetaData());
                if (!rs.next()) {
                    return null;
                }
                return mapQuestion(rs, columns);
            }
        }
    }

    public void saveInterviewRecord(long userId, long questionId, String userAnswer, int aiScore, String aiSuggestion)
            throws SQLException {
        // 将用户回答与 AI 评分建议一起保存，便于后续复盘。
        String sql = "INSERT INTO t_interview_record "
                + "(user_id, question_id, user_answer, ai_score, ai_suggestion, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, NOW(), NOW())";

        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, questionId);
            ps.setString(3, userAnswer);
            ps.setInt(4, aiScore);
            ps.setString(5, aiSuggestion);
            ps.executeUpdate();
        }
    }

    private Question mapQuestion(ResultSet rs, Set<String> columns) throws SQLException {
        Question question = new Question();
        if (columns.contains("id")) {
            question.setId(rs.getLong("id"));
        }
        if (columns.contains("title")) {
            question.setTitle(rs.getString("title"));
        }
        if (columns.contains("content")) {
            question.setContent(rs.getString("content"));
        }
        if (columns.contains("type")) {
            int type = rs.getInt("type");
            question.setType(rs.wasNull() ? null : type);
        }
        if (columns.contains("difficulty")) {
            int difficulty = rs.getInt("difficulty");
            question.setDifficulty(rs.wasNull() ? null : difficulty);
        }
        if (columns.contains("tags")) {
            question.setTags(rs.getString("tags"));
        }
        if (columns.contains("standard_answer")) {
            question.setStandardAnswer(rs.getString("standard_answer"));
        }
        if (columns.contains("completed")) {
            int completed = rs.getInt("completed");
            question.setCompleted(!rs.wasNull() && completed == 1);
        }
        return question;
    }

    private Set<String> readColumns(ResultSetMetaData metaData) throws SQLException {
        Set<String> columns = new HashSet<>();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            String label = metaData.getColumnLabel(i);
            if (label != null) {
                columns.add(label.toLowerCase());
            }
        }
        return columns;
    }

    private void appendUserJoin(StringBuilder sql, List<Object> params, Long currentUserId) {
        if (currentUserId == null) {
            return;
        }
        sql.append("LEFT JOIN (SELECT DISTINCT question_id FROM sys_evaluation WHERE user_id = ?) completed ON completed.question_id = q.id ");
        params.add(currentUserId);
    }

    private void appendWhereClause(StringBuilder sql, List<Object> params, SearchCriteria criteria) {
        sql.append("WHERE 1=1");

        if (!isBlank(criteria.getKeyword())) {
            sql.append(" AND (q.title LIKE ? OR q.content LIKE ?)");
            String keywordLike = "%" + criteria.getKeyword().trim() + "%";
            params.add(keywordLike);
            params.add(keywordLike);
        }

        if (!isBlank(criteria.getCategory())) {
            sql.append(" AND (COALESCE(q.tags, '') LIKE ? OR COALESCE(q.title, '') LIKE ?)");
            String categoryLike = "%" + criteria.getCategory().trim() + "%";
            params.add(categoryLike);
            params.add(categoryLike);
        }

        if (criteria.getExactDifficulty() != null && criteria.getExactDifficulty() > 0) {
            sql.append(" AND q.difficulty = ?");
            params.add(criteria.getExactDifficulty());
        } else {
            if (criteria.getMinDifficulty() != null && criteria.getMinDifficulty() > 0) {
                sql.append(" AND q.difficulty >= ?");
                params.add(criteria.getMinDifficulty());
            }
            if (criteria.getMaxDifficulty() != null && criteria.getMaxDifficulty() > 0) {
                sql.append(" AND q.difficulty <= ?");
                params.add(criteria.getMaxDifficulty());
            }
        }

        if (!isBlank(criteria.getTags())) {
            String[] tags = criteria.getTags().split(",");
            for (String tag : tags) {
                if (isBlank(tag)) {
                    continue;
                }
                sql.append(" AND COALESCE(q.tags, '') LIKE ?");
                params.add("%" + tag.trim() + "%");
            }
        }
    }

    private void appendOrderByClause(StringBuilder sql, SearchCriteria criteria) {
        String requested = criteria.getOrderBy();
        String orderBy = ORDER_BY_WHITELIST.contains(requested) ? requested : "id";
        String direction = "DESC".equalsIgnoreCase(criteria.getOrderDirection()) ? "DESC" : "ASC";
        sql.append(" ORDER BY q.").append(orderBy).append(' ').append(direction);
    }

    private void appendPagination(StringBuilder sql, List<Object> params, SearchCriteria criteria) {
        Integer pageSize = criteria.getPageSize();
        Integer pageNo = criteria.getPageNo();
        if (pageSize == null || pageSize <= 0) {
            return;
        }

        int safePageSize = Math.min(100, pageSize);
        int safePageNo = pageNo == null || pageNo <= 0 ? 1 : pageNo;
        int offset = (safePageNo - 1) * safePageSize;
        sql.append(" LIMIT ? OFFSET ?");
        params.add(safePageSize);
        params.add(Math.max(0, offset));
    }

    private void bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    public static final class SearchCriteria {
        private String keyword;
        private String category;
        private Integer exactDifficulty;
        private Integer minDifficulty;
        private Integer maxDifficulty;
        private String tags;
        private String orderBy;
        private String orderDirection;
        private Integer pageNo;
        private Integer pageSize;

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public Integer getExactDifficulty() {
            return exactDifficulty;
        }

        public void setExactDifficulty(Integer exactDifficulty) {
            this.exactDifficulty = exactDifficulty;
        }

        public Integer getMinDifficulty() {
            return minDifficulty;
        }

        public void setMinDifficulty(Integer minDifficulty) {
            this.minDifficulty = minDifficulty;
        }

        public Integer getMaxDifficulty() {
            return maxDifficulty;
        }

        public void setMaxDifficulty(Integer maxDifficulty) {
            this.maxDifficulty = maxDifficulty;
        }

        public String getTags() {
            return tags;
        }

        public void setTags(String tags) {
            this.tags = tags;
        }

        public String getOrderBy() {
            return orderBy;
        }

        public void setOrderBy(String orderBy) {
            this.orderBy = orderBy == null ? null : orderBy.trim().toLowerCase();
        }

        public String getOrderDirection() {
            return orderDirection;
        }

        public void setOrderDirection(String orderDirection) {
            this.orderDirection = orderDirection;
        }

        public Integer getPageNo() {
            return pageNo;
        }

        public void setPageNo(Integer pageNo) {
            this.pageNo = pageNo;
        }

        public Integer getPageSize() {
            return pageSize;
        }

        public void setPageSize(Integer pageSize) {
            this.pageSize = pageSize;
        }
    }
}
