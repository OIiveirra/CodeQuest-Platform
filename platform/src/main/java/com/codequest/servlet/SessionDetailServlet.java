package com.codequest.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.codequest.dao.EvaluationDAO;
import com.codequest.model.EvaluationRecord;
import com.codequest.model.User;
import com.google.gson.Gson;

/**
 * 会话详情接口：返回指定 sessionId 下每道题的结构化评测数据。
 */
@WebServlet("/profile/sessionDetail")
public class SessionDetailServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Gson GSON = new Gson();
    private final EvaluationDAO evaluationDAO = new EvaluationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        HttpSession session = req.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("[]");
            return;
        }

        String sessionId = trim(req.getParameter("sessionId"));
        if (sessionId.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("[]");
            return;
        }

        boolean isAdmin = loginUser.getRole() != null && "admin".equalsIgnoreCase(loginUser.getRole().trim());

        try {
            List<EvaluationRecord> records = evaluationDAO.findEvaluationsBySessionId(sessionId);
            if (records.isEmpty()) {
                resp.getWriter().write("[]");
                return;
            }

            Long ownerUserId = records.get(0).getUserId();
            if (!isAdmin && (ownerUserId == null || !ownerUserId.equals(loginUser.getId()))) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("[]");
                return;
            }

            List<SessionDetailItem> result = new ArrayList<>();
            for (EvaluationRecord record : records) {
                SessionDetailItem item = new SessionDetailItem();
                item.questionTitle = safe(record.getQuestionTitle());
                item.userAnswer = safe(record.getUserAnswer());
                item.score = record.getScore() == null ? 0 : record.getScore();
                item.feedback = safe(record.getFeedback());
                result.add(item);
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(GSON.toJson(result));
        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("[]");
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private static final class SessionDetailItem {
        private String questionTitle;
        private String userAnswer;
        private int score;
        private String feedback;
    }
}
