package com.codequest.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.codequest.dto.Result;
import com.codequest.model.User;
import com.codequest.model.WeeklyReport;
import com.codequest.service.ReportService;
import com.google.gson.Gson;

/**
 * 周报生成入口。
 */
@WebServlet("/report/weekly/generate")
public class WeeklyReportServlet extends HttpServlet {

    private final ReportService reportService = new ReportService();
    private static final Gson GSON = new Gson();
    private static final SimpleDateFormat DATE_LABEL_FORMAT = new SimpleDateFormat("MM-dd");

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getId() == null) {
            if (isAjaxRequest(req)) {
                writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, Result.failure(HttpServletResponse.SC_UNAUTHORIZED, "登录已失效，请重新登录。"));
            } else {
                resp.sendRedirect(req.getContextPath() + "/login");
            }
            return;
        }

        try {
            WeeklyReport report = reportService.generateWeeklyReport(loginUser.getId(), loginUser.getUsername());
            if (isAjaxRequest(req)) {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("id", report == null ? null : report.getId());
                payload.put("title", report == null ? "" : report.getTitle());
                payload.put("summary", report == null ? "" : report.getSummary());
                payload.put("content", report == null ? "" : report.getContent());
                payload.put("periodStartLabel", formatDate(report == null ? null : report.getPeriodStart()));
                payload.put("periodEndLabel", formatDate(report == null ? null : report.getPeriodEnd()));
                writeJson(resp, HttpServletResponse.SC_OK, Result.success(payload));
            } else {
                resp.sendRedirect(req.getContextPath() + "/profile?weeklyReport=ok");
            }
        } catch (SQLException ex) {
            if (isAjaxRequest(req)) {
                writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        Result.failure(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "周报生成失败，请稍后重试。"));
            } else {
                resp.sendRedirect(req.getContextPath() + "/profile?weeklyReport=fail");
            }
        }
    }

    private boolean isAjaxRequest(HttpServletRequest req) {
        String requestedWith = req.getHeader("X-Requested-With");
        if ("fetch".equalsIgnoreCase(requestedWith) || "XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            return true;
        }
        String accept = req.getHeader("Accept");
        return accept != null && accept.contains("application/json");
    }

    private void writeJson(HttpServletResponse resp, int status, Object payload) throws IOException {
        resp.setStatus(status);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(GSON.toJson(payload));
        resp.getWriter().flush();
    }

    private String formatDate(Timestamp time) {
        if (time == null) {
            return "";
        }
        return DATE_LABEL_FORMAT.format(time);
    }
}
