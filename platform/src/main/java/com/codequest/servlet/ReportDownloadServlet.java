package com.codequest.servlet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 面试评价报告下载控制器，直接将 Markdown 字符串写入响应流。
 * Author: 张雨泽
 */
@WebServlet("/report/download")
public class ReportDownloadServlet extends HttpServlet {

    private static final String SESSION_REPORT_CONTENT = "interviewReportMarkdown";
    private static final String REQUEST_REPORT_CONTENT = "reportContent";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/markdown");
        resp.setHeader("Content-Disposition", "attachment; filename=InterviewReport.md");

        String reportContent = req.getParameter(REQUEST_REPORT_CONTENT);
        if (reportContent == null || reportContent.trim().isEmpty()) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                Object sessionContent = session.getAttribute(SESSION_REPORT_CONTENT);
                if (sessionContent instanceof String) {
                    reportContent = (String) sessionContent;
                }
            }
        }

        if (reportContent == null || reportContent.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "暂无可下载的面试评价报告。");
            return;
        }

        byte[] bytes = reportContent.getBytes(StandardCharsets.UTF_8);
        resp.setContentLength(bytes.length);
        resp.getOutputStream().write(bytes);
        resp.getOutputStream().flush();
    }
}