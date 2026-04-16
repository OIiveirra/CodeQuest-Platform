package com.codequest.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.codequest.dao.QuestionDAO;
import com.codequest.model.Question;

/**
 * 统一导出与导入控制器。
 *
 * GET  /questions/export?format=csv|json|markdown|xlsx
 * POST /questions/export?action=importCsv (multipart: file)
 */
@WebServlet({"/questions/export", "/admin/question/export"})
@MultipartConfig(maxFileSize = 4 * 1024 * 1024, maxRequestSize = 8 * 1024 * 1024)
public class ExportServlet extends HttpServlet {

    private final QuestionDAO questionDAO = new QuestionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        QuestionDAO.SearchCriteria criteria = buildCriteria(req);
        String format = safe(req.getParameter("format")).toLowerCase();
        if (format.isEmpty()) {
            format = "csv";
        }

        List<Question> questions;
        try {
            questions = questionDAO.searchQuestions(criteria);
        } catch (SQLException ex) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "题库查询失败，无法导出。");
            return;
        }

        switch (format) {
            case "xlsx":
                writeXlsxPlaceholder(resp);
                break;
            case "json":
                writeJson(resp, questions);
                break;
            case "markdown":
            case "md":
                writeMarkdown(resp, questions);
                break;
            case "csv":
            default:
                writeCsv(resp, questions);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = safe(req.getParameter("action"));
        if (!"importCsv".equalsIgnoreCase(action)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "不支持的操作。请使用 action=importCsv");
            return;
        }

        Part filePart;
        try {
            filePart = req.getPart("file");
        } catch (ServletException | IOException ex) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "未读取到上传文件。字段名必须为 file。");
            return;
        }

        if (filePart == null || filePart.getSize() <= 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "上传文件为空。");
            return;
        }

        ImportParseResult parseResult;
        try (InputStream in = filePart.getInputStream()) {
            parseResult = parseCsv(in);
        }

        int inserted;
        try {
            inserted = questionDAO.batchInsertQuestions(parseResult.questions);
        } catch (SQLException ex) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "导入失败，数据库写入异常。");
            return;
        }

        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json;charset=UTF-8");
        try (PrintWriter writer = resp.getWriter()) {
            StringBuilder json = new StringBuilder();
            json.append("{\"success\":true")
                    .append(",\"parsed\":").append(parseResult.questions.size())
                    .append(",\"inserted\":").append(inserted)
                    .append(",\"invalid\":").append(parseResult.errors.size())
                    .append(",\"errors\":[");

            int maxErrors = Math.min(20, parseResult.errors.size());
            for (int i = 0; i < maxErrors; i++) {
                if (i > 0) {
                    json.append(',');
                }
                ImportError err = parseResult.errors.get(i);
                json.append("{\"line\":").append(err.line)
                        .append(",\"message\":\"").append(json(err.message)).append("\"}");
            }
            json.append("]}");

            writer.write(json.toString());
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

    private void writeCsv(HttpServletResponse resp, List<Question> questions) throws IOException {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/csv;charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment;filename=CodeQuest_Bank.csv");

        try (PrintWriter writer = resp.getWriter()) {
            writer.write('\ufeff');
            writer.println("ID,标题,内容,类型,难度,标签,标准答案");
            for (Question q : questions) {
                writer.println(String.join(",",
                        csv(safe(q.getId())),
                        csv(q.getTitle()),
                        csv(q.getContent()),
                        csv(safe(q.getType())),
                        csv(safe(q.getDifficulty())),
                        csv(q.getTags()),
                        csv(q.getStandardAnswer())));
            }
            writer.flush();
        }
    }

    private void writeJson(HttpServletResponse resp, List<Question> questions) throws IOException {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment;filename=CodeQuest_Bank.json");

        try (PrintWriter writer = resp.getWriter()) {
            writer.println("[");
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                writer.print("  {");
                writer.print("\"id\":" + number(q.getId()) + ",");
                writer.print("\"title\":\"" + json(q.getTitle()) + "\",");
                writer.print("\"content\":\"" + json(q.getContent()) + "\",");
                writer.print("\"type\":" + number(q.getType()) + ",");
                writer.print("\"difficulty\":" + number(q.getDifficulty()) + ",");
                writer.print("\"tags\":\"" + json(q.getTags()) + "\",");
                writer.print("\"standardAnswer\":\"" + json(q.getStandardAnswer()) + "\"");
                writer.print("}");
                if (i < questions.size() - 1) {
                    writer.println(",");
                } else {
                    writer.println();
                }
            }
            writer.println("]");
            writer.flush();
        }
    }

    private void writeMarkdown(HttpServletResponse resp, List<Question> questions) throws IOException {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/markdown;charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment;filename=CodeQuest_Bank.md");

        try (PrintWriter writer = resp.getWriter()) {
            writer.println("# CodeQuest 题库导出");
            writer.println();
            writer.println("共 " + questions.size() + " 道题");
            writer.println();

            for (Question q : questions) {
                writer.println("## [" + safe(q.getId()) + "] " + md(q.getTitle()));
                writer.println();
                writer.println("- 类型: " + safe(q.getType()));
                writer.println("- 难度: " + safe(q.getDifficulty()));
                writer.println("- 标签: " + md(q.getTags()));
                writer.println();
                writer.println("### 题目内容");
                writer.println(md(q.getContent()));
                writer.println();
                writer.println("### 标准答案");
                writer.println(md(q.getStandardAnswer()));
                writer.println();
                writer.println("---");
                writer.println();
            }
            writer.flush();
        }
    }

    private void writeXlsxPlaceholder(HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json;charset=UTF-8");
        resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        try (PrintWriter writer = resp.getWriter()) {
            writer.write("{\"success\":false,\"message\":\"XLSX 导出尚未实现，请先使用 CSV/JSON/Markdown。\"}");
            writer.flush();
        }
    }

    private ImportParseResult parseCsv(InputStream inputStream) throws IOException {
        List<Question> list = new ArrayList<>();
        List<ImportError> errors = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            int lineNo = 0;
            CsvColumnMapping columnMapping = null;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.trim().isEmpty()) {
                    continue;
                }

                List<String> cols = splitCsvLine(line);
                if (firstLine) {
                    firstLine = false;
                    // 首行疑似表头时按列名映射；否则按默认列位次解析。
                    if (looksLikeHeader(cols)) {
                        columnMapping = buildColumnMapping(cols);
                        continue;
                    }
                    columnMapping = CsvColumnMapping.forDataRow(cols.size());
                }

                if (columnMapping == null) {
                    columnMapping = CsvColumnMapping.forDataRow(cols.size());
                }

                if (cols.size() < 2) {
                    errors.add(new ImportError(lineNo, "列数不足，至少包含标题与内容"));
                    continue;
                }

                Question q = new Question();
                String title = valueAt(cols, columnMapping.titleIndex);
                String content = valueAt(cols, columnMapping.contentIndex);
                String type = valueAt(cols, columnMapping.typeIndex);
                String difficulty = valueAt(cols, columnMapping.difficultyIndex);
                String tags = valueAt(cols, columnMapping.tagsIndex);
                String standardAnswer = valueAt(cols, columnMapping.standardAnswerIndex);

                if (!isBlank(title)) {
                    q.setTitle(unquote(title));
                }
                if (!isBlank(content)) {
                    q.setContent(unquote(content));
                }
                if (!isBlank(type)) {
                    q.setType(parsePositiveInt(unquote(type)));
                }
                if (!isBlank(difficulty)) {
                    q.setDifficulty(parsePositiveInt(unquote(difficulty)));
                }
                if (!isBlank(tags)) {
                    q.setTags(unquote(tags));
                }
                if (!isBlank(standardAnswer)) {
                    q.setStandardAnswer(unquote(standardAnswer));
                }

                if (!isBlank(q.getTitle()) && !isBlank(q.getContent())) {
                    list.add(q);
                } else {
                    errors.add(new ImportError(lineNo, "标题或内容为空"));
                }
            }
        }
        return new ImportParseResult(list, errors);
    }

    private boolean looksLikeHeader(List<String> cols) {
        for (String col : cols) {
            String normalized = normalizeHeader(col);
            switch (normalized) {
                case "title", "content", "standardanswer", "标准答案", "标题" -> {
                    return true;
                }
                default -> {
                }
            }
        }
        return false;
    }

    private CsvColumnMapping buildColumnMapping(List<String> headerCols) {
        CsvColumnMapping mapping = new CsvColumnMapping();
        // 兼容中英文和常见别名列名，减少用户手工改表头的成本。
        for (int i = 0; i < headerCols.size(); i++) {
            String normalized = normalizeHeader(headerCols.get(i));
            switch (normalized) {
                case "title", "标题", "题目标题" -> mapping.titleIndex = i;
                case "content", "内容", "题目内容" -> mapping.contentIndex = i;
                case "type", "类型" -> mapping.typeIndex = i;
                case "difficulty", "难度" -> mapping.difficultyIndex = i;
                case "tags", "标签" -> mapping.tagsIndex = i;
                case "standardanswer", "标准答案", "参考答案", "答案" -> mapping.standardAnswerIndex = i;
                default -> {
                }
            }
        }
        if (mapping.titleIndex < 0 || mapping.contentIndex < 0) {
            return CsvColumnMapping.forDataRow(headerCols.size());
        }
        return mapping;
    }

    private String valueAt(List<String> cols, int index) {
        if (index < 0 || index >= cols.size()) {
            return "";
        }
        return cols.get(index);
    }

    private String normalizeHeader(String header) {
        if (header == null) {
            return "";
        }
        String normalized = unquote(header)
                .replace("\ufeff", "")
                .replace(" ", "")
                .replace("_", "")
                .replace("-", "")
                .toLowerCase();
        return normalized;
    }

    private List<String> splitCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuote = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                // 连续双引号表示转义引号："" -> "
                if (inQuote && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuote = !inQuote;
                }
                continue;
            }
            if (c == ',' && !inQuote) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        result.add(current.toString());
        return result;
    }

    private String unquote(String text) {
        if (text == null) {
            return "";
        }
        String t = text.trim();
        if (t.startsWith("\ufeff")) {
            t = t.substring(1);
        }
        if (t.length() >= 2 && t.startsWith("\"") && t.endsWith("\"")) {
            t = t.substring(1, t.length() - 1);
        }
        return t.replace("\"\"", "\"").trim();
    }

    private Integer parsePositiveInt(String raw) {
        if (isBlank(raw)) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(raw.trim());
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String csv(String value) {
        String escaped = safe(value).replace("\"", "\"\"");
        return '"' + escaped + '"';
    }

    private String md(String value) {
        return safe(value).replace("\r\n", "\n").replace("\r", "\n");
    }

    private String json(String value) {
        return safe(value)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private String number(Object value) {
        return value == null ? "null" : String.valueOf(value);
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static final class ImportParseResult {
        private final List<Question> questions;
        private final List<ImportError> errors;

        private ImportParseResult(List<Question> questions, List<ImportError> errors) {
            this.questions = questions;
            this.errors = errors;
        }
    }

    private static final class ImportError {
        private final int line;
        private final String message;

        private ImportError(int line, String message) {
            this.line = line;
            this.message = message;
        }
    }

    private static final class CsvColumnMapping {
        private int titleIndex = -1;
        private int contentIndex = -1;
        private int typeIndex = -1;
        private int difficultyIndex = -1;
        private int tagsIndex = -1;
        private int standardAnswerIndex = -1;

        private static CsvColumnMapping forDataRow(int columnCount) {
            CsvColumnMapping mapping = new CsvColumnMapping();
            if (columnCount >= 7) {
                mapping.titleIndex = 1;
                mapping.contentIndex = 2;
                mapping.typeIndex = 3;
                mapping.difficultyIndex = 4;
                mapping.tagsIndex = 5;
                mapping.standardAnswerIndex = 6;
            } else {
                mapping.titleIndex = 0;
                mapping.contentIndex = 1;
                mapping.typeIndex = 2;
                mapping.difficultyIndex = 3;
                mapping.tagsIndex = 4;
                mapping.standardAnswerIndex = 5;
            }
            return mapping;
        }
    }
}
