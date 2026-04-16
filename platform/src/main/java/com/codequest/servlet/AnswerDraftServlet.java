package com.codequest.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.codequest.dao.AnswerDraftDAO;
import com.codequest.model.User;
import com.google.gson.Gson;

/**
 * 答题草稿同步接口，支持按题目读取和保存数据库持久化草稿。
 */
@WebServlet("/answer/draft")
public class AnswerDraftServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String SESSION_LOGIN_USER = "loginUser";
    private static final int MAX_DRAFT_LENGTH = 20000;
    private static final Gson GSON = new Gson();
    private final AnswerDraftDAO answerDraftDAO = new AnswerDraftDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 草稿读取入口。
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        HttpSession session = req.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute(SESSION_LOGIN_USER);
        if (loginUser == null || loginUser.getId() == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "请先登录");
            return;
        }

        Long questionId = parseLong(req.getParameter("questionId"));
        if (questionId == null) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "questionId 无效");
            return;
        }

        try {
            AnswerDraftDAO.DraftData draftData = answerDraftDAO.findDraft(loginUser.getId(), questionId);
            String draft = draftData == null ? "" : draftData.getDraftContent();
            Long updatedAt = draftData == null || draftData.getUpdateTime() == null
                    ? null
                    : draftData.getUpdateTime().getTime();

            Map<String, Object> payload = new HashMap<>();
            payload.put("success", true);
            payload.put("questionId", questionId);
            payload.put("draft", draft == null ? "" : draft);
            payload.put("updatedAt", updatedAt);
            resp.getWriter().write(GSON.toJson(payload));
        } catch (SQLException ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "读取草稿失败");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 草稿保存入口。
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        HttpSession session = req.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute(SESSION_LOGIN_USER);
        if (loginUser == null || loginUser.getId() == null) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, "请先登录");
            return;
        }

        Long questionId = parseLong(req.getParameter("questionId"));
        if (questionId == null) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "questionId 无效");
            return;
        }

        String draft = req.getParameter("draft");
        String safeDraft = draft == null ? "" : draft;
        if (safeDraft.length() > MAX_DRAFT_LENGTH) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "草稿过长，最多允许 20000 个字符");
            return;
        }

        try {
            boolean saved = answerDraftDAO.saveOrClearDraft(loginUser.getId(), questionId, safeDraft);
            AnswerDraftDAO.DraftData draftData = answerDraftDAO.findDraft(loginUser.getId(), questionId);
            Long updatedAt = draftData == null || draftData.getUpdateTime() == null
                    ? null
                    : draftData.getUpdateTime().getTime();

            Map<String, Object> payload = new HashMap<>();
            payload.put("success", true);
            payload.put("questionId", questionId);
            payload.put("saved", saved && !safeDraft.trim().isEmpty());
            payload.put("length", safeDraft.length());
            payload.put("updatedAt", updatedAt);
            resp.getWriter().write(GSON.toJson(payload));
        } catch (SQLException ex) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "保存草稿失败");
        }
    }

    private Long parseLong(String value) {
        try {
            return value == null || value.trim().isEmpty() ? null : Long.valueOf(value.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        // 统一错误返回。
        resp.setStatus(status);
        Map<String, Object> payload = new HashMap<>();
        payload.put("success", false);
        payload.put("message", message);
        resp.getWriter().write(GSON.toJson(payload));
    }
}