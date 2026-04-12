package com.codequest.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.codequest.model.User;
import com.codequest.service.ReportService;

/**
 * 周报生成入口。
 */
@WebServlet("/report/weekly/generate")
public class WeeklyReportServlet extends HttpServlet {

    private final ReportService reportService = new ReportService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getId() == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            reportService.generateWeeklyReport(loginUser.getId(), loginUser.getUsername());
            resp.sendRedirect(req.getContextPath() + "/profile?weeklyReport=ok");
        } catch (SQLException ex) {
            resp.sendRedirect(req.getContextPath() + "/profile?weeklyReport=fail");
        }
    }
}
