package com.codequest.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.codequest.dao.FavoriteDAO;
import com.codequest.dto.Result;
import com.codequest.model.Question;
import com.codequest.model.User;
import com.codequest.service.QuestionService;
import com.codequest.util.InterviewSessionUtils;

/**
 * 题目详情控制器，按题目 ID 加载详情页面。
 * Author: 张雨泽
 */
@WebServlet("/QuestionDetail")
public class QuestionDetailServlet extends HttpServlet {

    private final QuestionService questionService = new QuestionService();
    private final FavoriteDAO favoriteDAO = new FavoriteDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/questions");
            return;
        }

        Long id;
        try {
            id = Long.valueOf(idParam);
        } catch (NumberFormatException ex) {
            resp.sendRedirect(req.getContextPath() + "/questions");
            return;
        }

        Result<Question> result = questionService.getQuestionById(id);
        Question question = result.getData();

        if (!result.isSuccess() || question == null) {
            resp.sendRedirect(req.getContextPath() + "/questions");
            return;
        }

        HttpSession session = req.getSession(true);
        InterviewSessionUtils.ensureInterviewSessionId(session);

        boolean isFavorite = false;
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser != null && loginUser.getId() != null) {
            try {
                isFavorite = favoriteDAO.isFavorited(loginUser.getId(), question.getId());
            } catch (SQLException ignored) {
                isFavorite = false;
            }
        }

        req.setAttribute("question", question);
        req.setAttribute("isFavorite", isFavorite);
        req.getRequestDispatcher("/question_detail.jsp").forward(req, resp);
    }

}
