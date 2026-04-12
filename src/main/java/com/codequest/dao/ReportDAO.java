package com.codequest.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.codequest.model.WeeklyReport;

/**
 * 周报数据访问对象。
 */
public class ReportDAO extends BaseDAO {

    public WeeklyReport findByUserAndPeriod(long userId, Timestamp periodStart, Timestamp periodEnd) throws SQLException {
        String sql = "SELECT id, user_id, period_start, period_end, title, content, summary, created_at, updated_at "
                + "FROM sys_report WHERE user_id = ? AND period_start = ? AND period_end = ? LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setTimestamp(2, periodStart);
            ps.setTimestamp(3, periodEnd);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return map(rs);
            }
        }
    }

    public void insert(WeeklyReport report) throws SQLException {
        String sql = "INSERT INTO sys_report (user_id, period_start, period_end, title, content, summary, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, report.getUserId());
            ps.setTimestamp(2, report.getPeriodStart());
            ps.setTimestamp(3, report.getPeriodEnd());
            ps.setString(4, report.getTitle());
            ps.setString(5, report.getContent());
            ps.setString(6, report.getSummary());
            ps.executeUpdate();
        }
    }

    public List<WeeklyReport> findRecentByUserId(long userId, int limit) throws SQLException {
        int finalLimit = Math.max(1, Math.min(20, limit));
        String sql = "SELECT id, user_id, period_start, period_end, title, content, summary, created_at, updated_at "
                + "FROM sys_report WHERE user_id = ? ORDER BY period_end DESC, id DESC LIMIT ?";
        List<WeeklyReport> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setInt(2, finalLimit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    private WeeklyReport map(ResultSet rs) throws SQLException {
        WeeklyReport report = new WeeklyReport();
        report.setId(rs.getLong("id"));
        report.setUserId(rs.getLong("user_id"));
        report.setPeriodStart(rs.getTimestamp("period_start"));
        report.setPeriodEnd(rs.getTimestamp("period_end"));
        report.setTitle(rs.getString("title"));
        report.setContent(rs.getString("content"));
        report.setSummary(rs.getString("summary"));
        report.setCreatedAt(rs.getTimestamp("created_at"));
        report.setUpdatedAt(rs.getTimestamp("updated_at"));
        return report;
    }
}
