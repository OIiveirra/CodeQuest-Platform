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

import com.codequest.dao.AdminQuestionDAO;
import com.codequest.model.Question;

/**
 * 题目管理后台控制器，通过 action 参数分发 list/add/edit/delete 操作。
 * Author: 张雨泽
 */
@WebServlet("/admin/question")
public class AdminQuestionServlet extends HttpServlet {

    private final AdminQuestionDAO adminQuestionDAO = new AdminQuestionDAO();

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
                loadAndForward(req, resp, "题目 ID 不合法。", null);
                return;
            }

            try {
                Question editing = adminQuestionDAO.findById(id);
                if (editing == null) {
                    loadAndForward(req, resp, "未找到该题目。", null);
                    return;
                }
                req.setAttribute("editingQuestion", editing);
                loadAndForward(req, resp, null, null);
                return;
            } catch (SQLException ex) {
                loadAndForward(req, resp, "查询题目失败。", null);
                return;
            }
        }

        loadAndForward(req, resp, "不支持的操作: " + action, null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String action = trim(req.getParameter("action"));
        if ("add".equalsIgnoreCase(action)) {
            try {
                Question question = buildQuestionFromRequest(req, false);
                if (!isValidQuestion(question)) {
                    loadAndForward(req, resp, "题目标题和内容不能为空。", null);
                    return;
                }
                adminQuestionDAO.addQuestion(question);
                loadAndForward(req, resp, null, "新增题目成功。");
            } catch (SQLException ex) {
                loadAndForward(req, resp, "新增题目失败。", null);
            }
            return;
        }

        if ("update".equalsIgnoreCase(action)) {
            Question question = buildQuestionFromRequest(req, true);
            if (question.getId() == null || !isValidQuestion(question)) {
                loadAndForward(req, resp, "题目参数不合法。", null);
                return;
            }
            try {
                int affected = adminQuestionDAO.updateQuestion(question);
                loadAndForward(req, resp, affected > 0 ? null : "未找到要更新的题目。", affected > 0 ? "更新题目成功。" : null);
            } catch (SQLException ex) {
                loadAndForward(req, resp, "更新题目失败。", null);
            }
            return;
        }

        if ("delete".equalsIgnoreCase(action)) {
            Long id = parseLong(req.getParameter("id"));
            if (id == null) {
                loadAndForward(req, resp, "题目 ID 不合法。", null);
                return;
            }
            try {
                int affected = adminQuestionDAO.deleteQuestion(id);
                loadAndForward(req, resp, affected > 0 ? null : "未找到要删除的题目。", affected > 0 ? "删除题目成功。" : null);
            } catch (SQLException ex) {
                loadAndForward(req, resp, "删除题目失败。", null);
            }
            return;
        }

        loadAndForward(req, resp, "不支持的操作: " + action, null);
    }

    private void loadAndForward(HttpServletRequest req, HttpServletResponse resp, String error, String success)
            throws ServletException, IOException {
        List<Question> list;
        try {
            list = adminQuestionDAO.findAllQuestions();
        } catch (SQLException ex) {
            list = Collections.emptyList();
            if (error == null) {
                error = "加载题目列表失败。";
            }
        }

        req.setAttribute("questionList", list);
        if (error != null) {
            req.setAttribute("error", error);
        }
        if (success != null) {
            req.setAttribute("success", success);
        }
        req.getRequestDispatcher("/admin_questions.jsp").forward(req, resp);
    }

    private boolean isValidQuestion(Question question) {
        if (question == null) {
            return false;
        }
        return !trim(question.getTitle()).isEmpty() && !trim(question.getContent()).isEmpty();
    }

    private Question buildQuestionFromRequest(HttpServletRequest req, boolean withId) {
        Question question = new Question();
        if (withId) {
            question.setId(parseLong(req.getParameter("id")));
        }

        question.setTitle(trim(req.getParameter("title")));
        question.setContent(trim(req.getParameter("content")));
        question.setType(parseInteger(req.getParameter("type")));
        question.setDifficulty(parseInteger(req.getParameter("difficulty")));
        question.setTags(trim(req.getParameter("tags")));
        question.setStandardAnswer(trim(req.getParameter("standardAnswer")));
        return question;
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
