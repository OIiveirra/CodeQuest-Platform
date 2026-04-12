package com.codequest.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.codequest.dao.EvaluationDAO;
import com.codequest.dao.FavoriteDAO;
import com.codequest.dao.UserDAO;
import com.codequest.model.EvaluationRecord;
import com.codequest.model.Question;
import com.codequest.model.User;
import com.codequest.model.WeeklyReport;
import com.codequest.service.ReportService;
import com.google.gson.Gson;

/**
 * 个人中心控制器，加载当前登录用户的完整信息并渲染资料页。
 * Author: 张雨泽
 */
@WebServlet("/profile")
public class UserProfileServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final EvaluationDAO evaluationDAO = new EvaluationDAO();
    private final FavoriteDAO favoriteDAO = new FavoriteDAO();
    private final ReportService reportService = new ReportService();
    private static final Gson GSON = new Gson();
    private static final SimpleDateFormat TREND_TIME_FORMAT = new SimpleDateFormat("MM-dd HH:mm");
    private static final int HISTORY_PAGE_SIZE = 10;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            User profileUser = loadProfileUser(loginUser);
            if (profileUser == null) {
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }

            session.setAttribute("loginUser", profileUser);
            req.setAttribute("profileUser", profileUser);
            req.setAttribute("profileUserInitial", buildInitial(profileUser.getUsername()));
            req.setAttribute("avatarCacheToken", buildAvatarCacheToken(profileUser));

            long userId = profileUser.getId() == null ? 0L : profileUser.getId();
            int intUserId = profileUser.getId() == null ? 0 : profileUser.getId().intValue();

            List<EvaluationRecord> recentEvaluations;
            List<EvaluationRecord> evaluationHistory;
            Map<String, Object> trendPayload;
            Map<String, Double> categoryAvgMap;
            List<Question> favoriteQuestions;
            List<EvaluationRecord> wrongQuestions;
            List<WeeklyReport> weeklyReports;

            try {
                recentEvaluations = evaluationDAO.findRecentByUserId(userId, 8);
            } catch (Exception ex) {
                recentEvaluations = new ArrayList<>();
            }

            int page = parsePage(req.getParameter("page"));
            int totalHistory;
            try {
                totalHistory = evaluationDAO.countHistoryByUserId(intUserId);
            } catch (Exception ex) {
                totalHistory = 0;
            }
            int totalPages = Math.max(1, (int) Math.ceil(totalHistory / (double) HISTORY_PAGE_SIZE));
            int currentPage = Math.min(page, totalPages);

            try {
                evaluationHistory = evaluationDAO.getHistoryByUserId(intUserId, currentPage, HISTORY_PAGE_SIZE);
            } catch (Exception ex) {
                evaluationHistory = new ArrayList<>();
            }

            try {
                trendPayload = buildTrendPayload(evaluationDAO.getRecentTrendByUserId(intUserId));
            } catch (Exception ex) {
                trendPayload = buildTrendPayload(new ArrayList<EvaluationRecord>());
            }

            try {
                categoryAvgMap = evaluationDAO.getAverageScoresByCategory(intUserId);
            } catch (Exception ex) {
                categoryAvgMap = new LinkedHashMap<>();
            }

            try {
                favoriteQuestions = favoriteDAO.findFavoritesByUserId(userId, 8);
            } catch (Exception ex) {
                favoriteQuestions = new ArrayList<>();
            }

            try {
                wrongQuestions = evaluationDAO.findWrongQuestionsByUserId(userId);
            } catch (Exception ex) {
                wrongQuestions = new ArrayList<>();
            }

            try {
                weeklyReports = reportService.listRecentReports(userId, 6);
            } catch (Exception ex) {
                weeklyReports = new ArrayList<>();
            }

            req.setAttribute("recentEvaluations", recentEvaluations);
            req.setAttribute("evaluationHistory", evaluationHistory);
            req.setAttribute("historyCurrentPage", currentPage);
            req.setAttribute("historyTotalPages", totalPages);
            req.setAttribute("categoryAvgJson", GSON.toJson(categoryAvgMap));
            req.setAttribute("trendJson", GSON.toJson(trendPayload));
            req.setAttribute("favoriteQuestions", favoriteQuestions);
            req.setAttribute("wrongQuestions", wrongQuestions);
            req.setAttribute("wrongQuestionCount", wrongQuestions.size());
            req.setAttribute("weeklyReports", weeklyReports);
            req.setAttribute("weeklyReportStatus", req.getParameter("weeklyReport"));

            req.getRequestDispatcher("/WEB-INF/jsp/profile.jsp").forward(req, resp);
        } catch (Exception ex) {
            User fallbackUser = loginUser;
            if (fallbackUser == null) {
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }

            req.setAttribute("profileUser", fallbackUser);
            req.setAttribute("profileUserInitial", buildInitial(fallbackUser.getUsername()));
            req.setAttribute("avatarCacheToken", buildAvatarCacheToken(fallbackUser));
            req.setAttribute("recentEvaluations", new ArrayList<EvaluationRecord>());
            req.setAttribute("evaluationHistory", new ArrayList<EvaluationRecord>());
            req.setAttribute("historyCurrentPage", 1);
            req.setAttribute("historyTotalPages", 1);
            req.setAttribute("categoryAvgJson", GSON.toJson(new LinkedHashMap<String, Double>()));
            req.setAttribute("trendJson", GSON.toJson(buildTrendPayload(new ArrayList<EvaluationRecord>())));
            req.setAttribute("favoriteQuestions", new ArrayList<Question>());
            req.setAttribute("wrongQuestions", new ArrayList<EvaluationRecord>());
            req.setAttribute("wrongQuestionCount", 0);
            req.setAttribute("weeklyReports", new ArrayList<WeeklyReport>());
            req.setAttribute("weeklyReportStatus", req.getParameter("weeklyReport"));
            req.setAttribute("profileLoadWarning", "个人中心部分数据加载失败，已降级展示基础信息。" + (ex.getMessage() == null ? "" : " 原因：" + ex.getMessage()));
            try {
                req.getRequestDispatcher("/WEB-INF/jsp/profile.jsp").forward(req, resp);
            } catch (Exception forwardEx) {
                throw new ServletException("加载个人中心信息失败。", forwardEx);
            }
        }
    }

    private User loadProfileUser(User loginUser) throws SQLException {
        if (loginUser == null) {
            return null;
        }

        User profileUser = null;
        if (loginUser.getId() != null) {
            profileUser = userDAO.findById(loginUser.getId());
        }
        if (profileUser == null && loginUser.getUsername() != null && !loginUser.getUsername().trim().isEmpty()) {
            profileUser = userDAO.findByUsername(loginUser.getUsername());
        }
        return profileUser == null ? loginUser : profileUser;
    }

    private Map<String, Object> buildTrendPayload(List<EvaluationRecord> trendList) {
        List<String> times = new ArrayList<>();
        List<Integer> scores = new ArrayList<>();

        if (trendList != null) {
            for (EvaluationRecord record : trendList) {
                if (record == null) {
                    continue;
                }
                times.add(record.getCreatedAt() == null ? "" : TREND_TIME_FORMAT.format(record.getCreatedAt()));
                scores.add(record.getScore() == null ? 0 : record.getScore());
            }
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("times", times);
        payload.put("scores", scores);
        return payload;
    }

    private int parsePage(String pageParam) {
        try {
            int page = Integer.parseInt(pageParam);
            return Math.max(1, page);
        } catch (Exception ex) {
            return 1;
        }
    }

    private String buildInitial(String username) {
        if (username == null) {
            return "U";
        }
        String trimmed = username.trim();
        if (trimmed.isEmpty()) {
            return "U";
        }
        return trimmed.substring(0, 1);
    }

    private long buildAvatarCacheToken(User profileUser) {
        if (profileUser == null) {
            return System.currentTimeMillis();
        }
        if (profileUser.getUpdateTime() != null) {
            return profileUser.getUpdateTime().getTime();
        }
        if (profileUser.getCreateTime() != null) {
            return profileUser.getCreateTime().getTime();
        }
        return System.currentTimeMillis();
    }
}