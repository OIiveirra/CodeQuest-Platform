package com.codequest.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.codequest.dao.QuestionDAO;
import com.codequest.model.Question;

/**
 * 题库导出 CSV。
 */
@WebServlet({"/admin/export", "/questions/exportBank"})
public class ExportBankServlet extends HttpServlet {

    private final QuestionDAO questionDAO = new QuestionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        QuestionDAO.SearchCriteria criteria = buildCriteria(req);
        List<Question> questionList;
        try {
            questionList = questionDAO.searchQuestions(criteria);
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "导出失败，请稍后重试");
            return;
        }

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=CodeQuest_Questions.csv");

        try (PrintWriter writer = resp.getWriter()) {
            // BOM for Excel UTF-8 compatibility.
            writer.write('\ufeff');
            writer.println("ID,标题,分类,难度,标签,标准答案");

            for (Question q : questionList) {
                String category = nullSafe(q.getTags());
                String row = String.join(",",
                        csv(q.getId() == null ? "" : String.valueOf(q.getId())),
                        csv(q.getTitle()),
                        csv(category),
                        csv(q.getDifficulty() == null ? "" : String.valueOf(q.getDifficulty())),
                        csv(q.getTags()),
                        csv(q.getStandardAnswer()));
                writer.println(row);
            }
            writer.flush();
        }
    }

    private QuestionDAO.SearchCriteria buildCriteria(HttpServletRequest req) {
        QuestionDAO.SearchCriteria criteria = new QuestionDAO.SearchCriteria();
        criteria.setKeyword(req.getParameter("keyword"));
        criteria.setCategory(req.getParameter("category"));
        criteria.setTags(req.getParameter("tags"));
        criteria.setOrderBy(req.getParameter("sortBy"));
        criteria.setOrderDirection(req.getParameter("sortDir"));
        criteria.setExactDifficulty(parsePositiveInt(req.getParameter("difficulty")));
        criteria.setMinDifficulty(parsePositiveInt(req.getParameter("minDifficulty")));
        criteria.setMaxDifficulty(parsePositiveInt(req.getParameter("maxDifficulty")));
        return criteria;
    }

    private Integer parsePositiveInt(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(raw.trim());
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private String csv(String value) {
        String normalized = nullSafe(value).replace("\r\n", "\n").replace("\r", "\n");
        String escaped = normalized.replace("\"", "\"\"");
        return '"' + escaped + '"';
    }
}
