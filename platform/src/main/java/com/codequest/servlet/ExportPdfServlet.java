package com.codequest.servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
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
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;

/**
 * PDF 导出控制器，生成技术测评报告并下载。
 * Author: 张雨泽
 */
@WebServlet("/report/exportPdf")
public class ExportPdfServlet extends HttpServlet {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final EvaluationDAO evaluationDAO = new EvaluationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String sessionId = trim(req.getParameter("sessionId"));
        if (sessionId.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "sessionId 参数不合法。");
            return;
        }

        boolean isAdmin = loginUser.getRole() != null && "admin".equalsIgnoreCase(loginUser.getRole().trim());
        List<EvaluationRecord> records;
        try {
            records = evaluationDAO.findEvaluationsBySessionId(sessionId);
        } catch (Exception ex) {
            throw new ServletException("读取测评报告数据失败。", ex);
        }

        if (records == null || records.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "未找到对应测评记录。");
            return;
        }

        Long ownerUserId = records.get(0).getUserId();
        if (!isAdmin && (ownerUserId == null || !ownerUserId.equals(loginUser.getId()))) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "无权导出该面试会话报告。");
            return;
        }

        String reportUserName = records.get(0).getUsername();
        if (reportUserName == null || reportUserName.trim().isEmpty()) {
            reportUserName = loginUser.getUsername();
        }

        int scoreCount = 0;
        int scoreTotal = 0;
        for (EvaluationRecord item : records) {
            if (item != null && item.getScore() != null) {
                scoreTotal += item.getScore();
                scoreCount += 1;
            }
        }
        int averageScore = scoreCount == 0 ? 0 : Math.round(scoreTotal / (float) scoreCount);

        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "attachment; filename=CodeQuest_Interview_" + sessionId + ".pdf");

        Document document = new Document();
        try {
            PdfWriter.getInstance(document, resp.getOutputStream());
            document.open();

            Font logoFont = createFont(24, Font.BOLD);
            Font titleFont = createFont(16, Font.BOLD);
            Font sectionFont = createFont(13, Font.BOLD);
            Font keyFont = createFont(11, Font.BOLD);
            Font normalFont = createFont(11, Font.NORMAL);

            Paragraph logo = new Paragraph("CodeQuest", logoFont);
            logo.setAlignment(Element.ALIGN_LEFT);
            document.add(logo);

            Paragraph subtitle = new Paragraph("DeepSeek 技术测评报告", titleFont);
            subtitle.setSpacingBefore(4f);
            subtitle.setSpacingAfter(12f);
            document.add(subtitle);

            PdfPTable basicTable = new PdfPTable(2);
            basicTable.setWidthPercentage(100);
            basicTable.setWidths(new int[] {1, 3});
            addRow(basicTable, "会话ID", sessionId, keyFont, normalFont);
            addRow(basicTable, "用户", safe(reportUserName), keyFont, normalFont);
            addRow(basicTable, "题目数量", String.valueOf(records.size()), keyFont, normalFont);
            addRow(basicTable, "总分(平均)", String.valueOf(averageScore), keyFont, normalFont);
            basicTable.setSpacingAfter(14f);
            document.add(basicTable);

            for (int i = 0; i < records.size(); i++) {
                EvaluationRecord item = records.get(i);
                int index = i + 1;

                Paragraph blockTitle = new Paragraph("题目 " + index, sectionFont);
                blockTitle.setSpacingBefore(8f);
                blockTitle.setSpacingAfter(6f);
                document.add(blockTitle);

                document.add(createContentBlock("标题", safe(item.getQuestionTitle()), keyFont, normalFont));
                document.add(createContentBlock("内容", safe(item.getQuestionContent()), keyFont, normalFont));
                document.add(createContentBlock("分类", safe(item.getCategory()), keyFont, normalFont));
                document.add(createContentBlock("评分", item.getScore() == null ? "" : String.valueOf(item.getScore()), keyFont, normalFont));
                document.add(createContentBlock("时间", item.getCreatedAt() == null ? "" : DATE_FORMAT.format(item.getCreatedAt()), keyFont, normalFont));

                Paragraph userAnswerTitle = new Paragraph("用户作答", sectionFont);
                userAnswerTitle.setSpacingBefore(8f);
                userAnswerTitle.setSpacingAfter(6f);
                document.add(userAnswerTitle);
                String userAnswerText = safe(item.getUserAnswer());
                if (userAnswerText.trim().isEmpty()) {
                    userAnswerText = "暂无用户作答内容（可能为历史数据未保存 user_answer 字段）。";
                }
                document.add(createLongTextBlock(userAnswerText, normalFont));

                Paragraph aiTitle = new Paragraph("AI 点评", sectionFont);
                aiTitle.setSpacingBefore(8f);
                aiTitle.setSpacingAfter(6f);
                document.add(aiTitle);
                document.add(createLongTextBlock(safe(item.getFeedback()), normalFont));

                if (index < records.size()) {
                    Paragraph separator = new Paragraph();
                    separator.setSpacingBefore(6f);
                    separator.setSpacingAfter(10f);
                    separator.add(new Chunk(new LineSeparator()));
                    document.add(separator);
                }
            }
        } catch (Exception ex) {
            throw new ServletException("生成 PDF 报告失败。", ex);
        } finally {
            document.close();
        }
    }

    private Font createFont(float size, int style) {
        try {
            // OpenPDF 内置中文字体，避免中文在 PDF 中显示为空白。
            BaseFont baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            return new Font(baseFont, size, style);
        } catch (Exception ignored) {
            return new Font(Font.HELVETICA, size, style);
        }
    }

    private void addRow(PdfPTable table, String key, String value, Font keyFont, Font valueFont) {
        PdfPCell left = new PdfPCell(new Paragraph(key, keyFont));
        left.setBorder(Rectangle.BOX);
        left.setPadding(6f);
        PdfPCell right = new PdfPCell(new Paragraph(value, valueFont));
        right.setBorder(Rectangle.BOX);
        right.setPadding(6f);
        table.addCell(left);
        table.addCell(right);
    }

    private PdfPTable createContentBlock(String label, String value, Font keyFont, Font valueFont) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new int[] {1, 5});
        PdfPCell labelCell = new PdfPCell(new Paragraph(label, keyFont));
        labelCell.setPadding(6f);
        labelCell.setBorder(Rectangle.BOX);
        PdfPCell valueCell = new PdfPCell(new Paragraph(value, valueFont));
        valueCell.setPadding(6f);
        valueCell.setBorder(Rectangle.BOX);
        table.addCell(labelCell);
        table.addCell(valueCell);
        table.setSpacingAfter(6f);
        return table;
    }

    private PdfPTable createLongTextBlock(String text, Font font) {
        Paragraph paragraph = new Paragraph(safe(text), font);
        paragraph.setLeading(16f);

        PdfPCell cell = new PdfPCell(paragraph);
        cell.setPadding(8f);
        cell.setBorder(Rectangle.BOX);
        cell.setMinimumHeight(110f);

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.addCell(cell);
        table.setSpacingAfter(4f);
        return table;
    }

    private Long parseLong(String value) {
        try {
            return value == null || value.trim().isEmpty() ? null : Long.valueOf(value.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}