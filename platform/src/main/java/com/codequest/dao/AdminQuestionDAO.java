package com.codequest.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.codequest.model.Question;
import com.codequest.util.JDBCUtils;

/**
 * 题目管理后台 DAO，面向 sys_question 表执行增删改查。
 * Author: 张雨泽
 */
public class AdminQuestionDAO {

    public void addQuestion(Question question) throws SQLException {
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO sys_question (id, title, content, type, difficulty, tags, standard_answer) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            long nextId = findNextQuestionId(conn);
            ps.setLong(1, nextId);
            ps.setString(2, question.getTitle());
            ps.setString(3, question.getContent());
            ps.setObject(4, question.getType(), Types.INTEGER);
            ps.setObject(5, question.getDifficulty(), Types.INTEGER);
            ps.setString(6, question.getTags());
            ps.setString(7, question.getStandardAnswer());
            ps.executeUpdate();
        }
    }

    private long findNextQuestionId(Connection conn) throws SQLException {
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

    public int updateQuestion(Question question) throws SQLException {
        String sql = "UPDATE sys_question SET title = ?, content = ?, type = ?, difficulty = ?, tags = ?, standard_answer = ? "
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
        String sql = "DELETE FROM sys_question WHERE id = ?";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        }
    }

    public List<Question> findAllQuestions() throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM sys_question ORDER BY id DESC";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            Set<String> columns = readColumns(rs.getMetaData());
            while (rs.next()) {
                questions.add(mapQuestion(rs, columns));
            }
        }
        return questions;
    }

    public Question findById(Long id) throws SQLException {
        String sql = "SELECT * FROM sys_question WHERE id = ?";
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
}