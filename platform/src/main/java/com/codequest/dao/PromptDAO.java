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

import com.codequest.model.PromptTemplate;
import com.codequest.util.JDBCUtils;

/**
 * Prompt 模板 DAO，负责 sys_prompt_template 表的查询与维护。
 */
public class PromptDAO {

    public List<PromptTemplate> findAllTemplates() throws SQLException {
        List<PromptTemplate> templates = new ArrayList<>();
        String sql = "SELECT * FROM sys_prompt_template ORDER BY id DESC";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            Set<String> columns = readColumns(rs.getMetaData());
            while (rs.next()) {
                templates.add(mapTemplate(rs, columns));
            }
        }
        return templates;
    }

    public PromptTemplate findById(Long id) throws SQLException {
        if (id == null) {
            return null;
        }
        String sql = "SELECT * FROM sys_prompt_template WHERE id = ?";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                Set<String> columns = readColumns(rs.getMetaData());
                if (!rs.next()) {
                    return null;
                }
                return mapTemplate(rs, columns);
            }
        }
    }

    public PromptTemplate findByTemplateName(String templateName) throws SQLException {
        if (templateName == null || templateName.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT * FROM sys_prompt_template WHERE template_name = ? ORDER BY status DESC, id DESC LIMIT 1";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, templateName.trim());
            try (ResultSet rs = ps.executeQuery()) {
                Set<String> columns = readColumns(rs.getMetaData());
                if (!rs.next()) {
                    return null;
                }
                return mapTemplate(rs, columns);
            }
        }
    }

    public PromptTemplate findActiveByTemplateName(String templateName) throws SQLException {
        if (templateName == null || templateName.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT * FROM sys_prompt_template WHERE template_name = ? AND status = 1 ORDER BY id DESC LIMIT 1";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, templateName.trim());
            try (ResultSet rs = ps.executeQuery()) {
                Set<String> columns = readColumns(rs.getMetaData());
                if (!rs.next()) {
                    return null;
                }
                return mapTemplate(rs, columns);
            }
        }
    }

    public String findActiveContentByTemplateName(String templateName) throws SQLException {
        PromptTemplate template = findActiveByTemplateName(templateName);
        if (template == null || template.getContent() == null || template.getContent().trim().isEmpty()) {
            template = findByTemplateName(templateName);
        }
        return template == null ? null : template.getContent();
    }

    public int insert(PromptTemplate template) throws SQLException {
        if (template == null) {
            return 0;
        }
        String sql = "INSERT INTO sys_prompt_template (template_name, content, status) VALUES (?, ?, ?)";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, template);
            return ps.executeUpdate();
        }
    }

    public int update(PromptTemplate template) throws SQLException {
        if (template == null || template.getId() == null) {
            return 0;
        }
        String sql = "UPDATE sys_prompt_template SET template_name = ?, content = ?, status = ? WHERE id = ?";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, template);
            ps.setLong(4, template.getId());
            return ps.executeUpdate();
        }
    }

    public int delete(Long id) throws SQLException {
        if (id == null) {
            return 0;
        }
        String sql = "DELETE FROM sys_prompt_template WHERE id = ?";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, PromptTemplate template) throws SQLException {
        ps.setString(1, template.getTemplateName());
        ps.setString(2, template.getContent());
        if (template.getStatus() == null) {
            ps.setNull(3, Types.INTEGER);
        } else {
            ps.setInt(3, template.getStatus());
        }
    }

    private PromptTemplate mapTemplate(ResultSet rs, Set<String> columns) throws SQLException {
        PromptTemplate template = new PromptTemplate();
        if (columns.contains("id")) {
            template.setId(rs.getLong("id"));
        }
        if (columns.contains("template_name")) {
            template.setTemplateName(rs.getString("template_name"));
        }
        if (columns.contains("content")) {
            template.setContent(rs.getString("content"));
        }
        if (columns.contains("status")) {
            int status = rs.getInt("status");
            template.setStatus(rs.wasNull() ? null : status);
        }
        return template;
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
