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

import com.codequest.dao.QuestionDAO;
import com.codequest.model.Question;
import com.codequest.model.User;

/**
 * 题目列表控制器，负责加载题库并转发到列表页。
 * Author: 张雨泽
 */
@WebServlet("/questions")
public class QuestionListServlet extends HttpServlet {

    private final QuestionDAO questionDAO = new QuestionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        String category = req.getParameter("category");
        String tags = req.getParameter("tags");
        String difficultyParam = req.getParameter("difficulty");
        String minDifficultyParam = req.getParameter("minDifficulty");
        String maxDifficultyParam = req.getParameter("maxDifficulty");
        String sortBy = req.getParameter("sortBy");
        String sortDir = req.getParameter("sortDir");
        String pageParam = req.getParameter("page");
        String pageSizeParam = req.getParameter("pageSize");
        User loginUser = req.getSession(false) == null ? null : (User) req.getSession(false).getAttribute("loginUser");
        Long currentUserId = loginUser == null ? null : loginUser.getId();

        Integer difficulty = parsePositiveInt(difficultyParam);
        Integer minDifficulty = parsePositiveInt(minDifficultyParam);
        Integer maxDifficulty = parsePositiveInt(maxDifficultyParam);
        Integer page = parsePositiveInt(pageParam);
        Integer pageSize = parsePositiveInt(pageSizeParam);
        if (page == null || page <= 0) {
            page = 1;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = 20;
        }

        QuestionDAO.SearchCriteria criteria = new QuestionDAO.SearchCriteria();
        criteria.setKeyword(keyword);
        criteria.setCategory(category);
        criteria.setTags(tags);
        criteria.setExactDifficulty(difficulty);
        criteria.setMinDifficulty(minDifficulty);
        criteria.setMaxDifficulty(maxDifficulty);
        criteria.setOrderBy(sortBy);
        criteria.setOrderDirection(sortDir);
        criteria.setPageNo(page);
        criteria.setPageSize(pageSize);

        List<Question> questionList;
        int totalCount = 0;
        int totalPages = 1;
        try {
            totalCount = questionDAO.countQuestionsByCriteria(criteria, currentUserId);
            totalPages = Math.max(1, (int) Math.ceil(totalCount / (double) pageSize));
            if (page > totalPages) {
                page = totalPages;
                criteria.setPageNo(page);
            }
            questionList = questionDAO.searchQuestions(criteria, currentUserId);
        } catch (SQLException e) {
            questionList = Collections.emptyList();
            req.setAttribute("errorMessage", "题目检索失败，请稍后重试。");
        }

        req.setAttribute("questionList", questionList);
        req.setAttribute("keyword", keyword);
        req.setAttribute("category", category);
        req.setAttribute("tags", tags);
        req.setAttribute("difficulty", difficultyParam);
        req.setAttribute("minDifficulty", minDifficultyParam);
        req.setAttribute("maxDifficulty", maxDifficultyParam);
        req.setAttribute("sortBy", sortBy);
        req.setAttribute("sortDir", sortDir);
        req.setAttribute("page", page);
        req.setAttribute("pageSize", pageSize);
        req.setAttribute("totalCount", totalCount);
        req.setAttribute("totalPages", totalPages);
        req.getRequestDispatcher("/questions.jsp").forward(req, resp);
    }

    private Integer parsePositiveInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : null;
        } catch (Exception ex) {
            return null;
        }
    }
}
