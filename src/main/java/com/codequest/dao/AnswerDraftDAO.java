package com.codequest.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * 答题草稿数据访问对象，负责持久化用户题目草稿。
 */
public class AnswerDraftDAO extends BaseDAO {

    public DraftData findDraft(long userId, long questionId) throws SQLException {
        String sql = "SELECT draft_content, update_time FROM sys_answer_draft WHERE user_id = ? AND question_id = ? LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                DraftData draft = new DraftData();
                draft.setDraftContent(rs.getString("draft_content"));
                draft.setUpdateTime(rs.getTimestamp("update_time"));
                return draft;
            }
        }
    }

    public boolean saveOrClearDraft(long userId, long questionId, String draftContent) throws SQLException {
        String safeContent = draftContent == null ? "" : draftContent;
        if (safeContent.trim().isEmpty()) {
            return clearDraft(userId, questionId) > 0;
        }

        String sql = "INSERT INTO sys_answer_draft (user_id, question_id, draft_content, create_time, update_time) "
                + "VALUES (?, ?, ?, NOW(), NOW()) "
                + "ON DUPLICATE KEY UPDATE draft_content = VALUES(draft_content), update_time = NOW()";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, questionId);
            ps.setString(3, safeContent);
            ps.executeUpdate();
            return true;
        }
    }

    public int clearDraft(long userId, long questionId) throws SQLException {
        String sql = "DELETE FROM sys_answer_draft WHERE user_id = ? AND question_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, questionId);
            return ps.executeUpdate();
        }
    }

    public static final class DraftData {
        private String draftContent;
        private Timestamp updateTime;

        public String getDraftContent() {
            return draftContent;
        }

        public void setDraftContent(String draftContent) {
            this.draftContent = draftContent;
        }

        public Timestamp getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(Timestamp updateTime) {
            this.updateTime = updateTime;
        }
    }
}