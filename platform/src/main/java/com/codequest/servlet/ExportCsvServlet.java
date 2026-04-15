package com.codequest.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
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
 * 题库 CSV 导出控制器。
 */
@WebServlet("/questions/exportCsv")
public class ExportCsvServlet extends HttpServlet {

    private final QuestionDAO questionDAO = new QuestionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        QuestionDAO.SearchCriteria criteria = buildCriteria(req);
        List<Question> questions;
        try {
            questions = questionDAO.searchQuestions(criteria);
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "导出题库失败");
            return;
        }

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/csv;charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment;filename=CodeQuest_Bank.csv");

        try (PrintWriter writer = resp.getWriter()) {
            // UTF-8 BOM，兼容 Excel 打开中文。
            writer.write('\ufeff');
            writer.println("ID,标题,分类,难度,类型,标签,标准答案");

            for (Question q : questions) {
                String category = q.getTags();
                String line = String.join(",",
                        escapeCsv(toSafeString(q.getId())),
                        escapeCsv(q.getTitle()),
                        escapeCsv(category),
                        escapeCsv(toSafeString(q.getDifficulty())),
                        escapeCsv(toSafeString(q.getType())),
                        escapeCsv(q.getTags()),
                        escapeCsv(q.getStandardAnswer()));
                writer.println(line);
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
        } catch (Exception ex) {
            return null;
        }
    }

    private String toSafeString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String escapeCsv(String value) {
        String source = value == null ? "" : value;
        String escaped = source.replace("\"", "\"\"");
        return '"' + escaped + '"';
    }
}
