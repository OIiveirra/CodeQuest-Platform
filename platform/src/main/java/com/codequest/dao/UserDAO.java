package com.codequest.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.codequest.model.User;
import com.codequest.util.JDBCUtils;

/**
 * 用户数据访问对象，负责 sys_user 表的查询与写入。
 * Author: 张雨泽
 */
public class UserDAO {

    public User findById(Long userId) throws SQLException {
        if (userId == null) {
            return null;
        }
        String sqlWithAvatar = "SELECT id, username, password, role, avatar_url, create_time, update_time "
                + "FROM sys_user WHERE id = ? LIMIT 1";
        String sqlWithoutAvatar = "SELECT id, username, password, role, create_time, update_time "
                + "FROM sys_user WHERE id = ? LIMIT 1";
        return findSingleUserWithFallback(sqlWithAvatar, sqlWithoutAvatar, userId);
    }

    public User findByUsername(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        String sqlWithAvatar = "SELECT id, username, password, role, avatar_url, create_time, update_time "
                + "FROM sys_user WHERE username = ? LIMIT 1";
        String sqlWithoutAvatar = "SELECT id, username, password, role, create_time, update_time "
                + "FROM sys_user WHERE username = ? LIMIT 1";
        return findSingleUserWithFallback(sqlWithAvatar, sqlWithoutAvatar, username.trim());
    }

    public User findByUsernameAndPassword(String username, String encryptedPassword) throws SQLException {
        String sql = "SELECT id, username, password, role, create_time, update_time "
                + "FROM sys_user WHERE username = ? AND password = ? LIMIT 1";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, encryptedPassword);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                user.setCreateTime(rs.getTimestamp("create_time"));
                user.setUpdateTime(rs.getTimestamp("update_time"));
                return user;
            }
        }
    }

    public boolean existsByUsername(String username) throws SQLException {
        String sql = "SELECT id FROM sys_user WHERE username = ? LIMIT 1";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void createUser(String username, String encryptedPassword) throws SQLException {
        String sql = "INSERT INTO sys_user (username, password, role, create_time, update_time) "
                + "VALUES (?, ?, 'USER', NOW(), NOW())";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, encryptedPassword);
            ps.executeUpdate();
        }
    }

    public void updateAvatar(long userId, String avatarPath) throws SQLException {
        String sql = "UPDATE sys_user SET avatar_url = ?, update_time = NOW() WHERE id = ?";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, avatarPath);
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }

    private User findSingleUserWithFallback(String sqlWithAvatar, String sqlWithoutAvatar, Object param) throws SQLException {
        try {
            return findSingleUser(sqlWithAvatar, param, true);
        } catch (SQLException ex) {
            // 兼容 avatar_url 尚未升级的数据库结构。
            if (ex.getMessage() != null && ex.getMessage().contains("avatar_url")) {
                return findSingleUser(sqlWithoutAvatar, param, false);
            }
            throw ex;
        }
    }

    private User findSingleUser(String sql, Object param, boolean hasAvatarColumn) throws SQLException {
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                if (hasAvatarColumn) {
                    user.setAvatarUrl(rs.getString("avatar_url"));
                }
                user.setCreateTime(rs.getTimestamp("create_time"));
                user.setUpdateTime(rs.getTimestamp("update_time"));
                return user;
            }
        }
    }
}
