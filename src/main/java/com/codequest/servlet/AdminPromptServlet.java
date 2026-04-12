package com.codequest.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.codequest.dao.PromptDAO;
import com.codequest.model.PromptTemplate;

/**
 * Prompt 模板管理后台控制器。
 */
@WebServlet("/admin/prompt")
public class AdminPromptServlet extends HttpServlet {

    private final PromptDAO promptDAO = new PromptDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = trim(req.getParameter("action"));
        if (action.isEmpty() || "list".equalsIgnoreCase(action)) {
            loadAndForward(req, resp, null, null);
            return;
        }

        if ("edit".equalsIgnoreCase(action)) {
            Long id = parseLong(req.getParameter("id"));
            if (id == null) {
                loadAndForward(req, resp, "模板 ID 不合法。", null);
                return;
            }

            try {
                PromptTemplate editing = promptDAO.findById(id);
                if (editing == null) {
                    loadAndForward(req, resp, "未找到该模板。", null);
                    return;
                }
                req.setAttribute("editingTemplate", editing);
                loadAndForward(req, resp, null, null);
                return;
            } catch (SQLException ex) {
                loadAndForward(req, resp, "查询模板失败。", null);
                return;
            }
        }

        loadAndForward(req, resp, "不支持的操作: " + action, null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String action = trim(req.getParameter("action"));
        if ("save".equalsIgnoreCase(action)) {
            try {
                PromptTemplate template = buildTemplateFromRequest(req);
                if (!isValidTemplate(template)) {
                    loadAndForward(req, resp, "模板名称和内容不能为空。", null);
                    return;
                }

                int affected = template.getId() == null ? promptDAO.insert(template) : promptDAO.update(template);
                loadAndForward(req, resp, affected > 0 ? null : "保存模板失败。", affected > 0 ? "保存模板成功。" : null);
            } catch (SQLException ex) {
                loadAndForward(req, resp, "保存模板失败。", null);
            }
            return;
        }

        if ("delete".equalsIgnoreCase(action)) {
            Long id = parseLong(req.getParameter("id"));
            if (id == null) {
                loadAndForward(req, resp, "模板 ID 不合法。", null);
                return;
            }
            try {
                int affected = promptDAO.delete(id);
                loadAndForward(req, resp, affected > 0 ? null : "未找到要删除的模板。", affected > 0 ? "删除模板成功。" : null);
            } catch (SQLException ex) {
                loadAndForward(req, resp, "删除模板失败。", null);
            }
            return;
        }

        loadAndForward(req, resp, "不支持的操作: " + action, null);
    }

    private void loadAndForward(HttpServletRequest req, HttpServletResponse resp, String error, String success)
            throws ServletException, IOException {
        List<PromptTemplate> list;
        try {
            list = promptDAO.findAllTemplates();
        } catch (SQLException ex) {
            list = Collections.emptyList();
            if (error == null) {
                error = "加载模板列表失败。";
            }
        }

        req.setAttribute("templateList", list);
        if (error != null) {
            req.setAttribute("error", error);
        }
        if (success != null) {
            req.setAttribute("success", success);
        }
        req.getRequestDispatcher("/admin_prompts.jsp").forward(req, resp);
    }

    private PromptTemplate buildTemplateFromRequest(HttpServletRequest req) {
        PromptTemplate template = new PromptTemplate();
        template.setId(parseLong(req.getParameter("id")));
        template.setTemplateName(trim(req.getParameter("templateName")));
        template.setContent(trim(req.getParameter("content")));
        template.setStatus(parseInteger(req.getParameter("status")));
        return template;
    }

    private boolean isValidTemplate(PromptTemplate template) {
        if (template == null) {
            return false;
        }
        return !trim(template.getTemplateName()).isEmpty() && !trim(template.getContent()).isEmpty();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private Long parseLong(String value) {
        try {
            return value == null || value.trim().isEmpty() ? null : Long.valueOf(value.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        try {
            return value == null || value.trim().isEmpty() ? null : Integer.valueOf(value.trim());
        } catch (Exception ex) {
            return null;
        }
    }
}
