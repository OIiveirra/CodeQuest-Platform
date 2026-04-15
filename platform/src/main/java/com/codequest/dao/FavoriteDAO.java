package com.codequest.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.codequest.model.Question;

/**
 * 收藏夹数据访问对象。
 */
public class FavoriteDAO extends BaseDAO {

    public boolean isFavorited(long userId, long questionId) throws SQLException {
        String sql = "SELECT 1 FROM sys_favorite WHERE user_id = ? AND question_id = ? LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean toggleFavorite(long userId, long questionId) throws SQLException {
        if (isFavorited(userId, questionId)) {
            removeFavorite(userId, questionId);
            return false;
        }
        addFavorite(userId, questionId);
        return true;
    }

    public List<Question> findFavoritesByUserId(long userId, int limit) throws SQLException {
        List<Question> list = new ArrayList<>();
        int finalLimit = Math.max(1, Math.min(limit, 50));
        String sql = "SELECT q.id, q.title, q.difficulty, q.tags "
                + "FROM sys_favorite f "
                + "INNER JOIN t_question q ON q.id = f.question_id "
                + "WHERE f.user_id = ? "
                + "ORDER BY f.create_time DESC "
                + "LIMIT ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setInt(2, finalLimit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question q = new Question();
                    q.setId(rs.getLong("id"));
                    q.setTitle(rs.getString("title"));
                    int difficulty = rs.getInt("difficulty");
                    q.setDifficulty(rs.wasNull() ? null : difficulty);
                    q.setTags(rs.getString("tags"));
                    list.add(q);
                }
            }
        }
        return list;
    }

    private void addFavorite(long userId, long questionId) throws SQLException {
        String sql = "INSERT INTO sys_favorite (user_id, question_id, create_time) VALUES (?, ?, NOW())";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, questionId);
            ps.executeUpdate();
        }
    }

    private void removeFavorite(long userId, long questionId) throws SQLException {
        String sql = "DELETE FROM sys_favorite WHERE user_id = ? AND question_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, questionId);
            ps.executeUpdate();
        }
    }
}
