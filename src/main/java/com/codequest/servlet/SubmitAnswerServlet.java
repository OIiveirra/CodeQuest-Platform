package com.codequest.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.codequest.dto.Result;
import com.codequest.model.User;
import com.codequest.service.QuestionService;
import com.codequest.util.AIService;
import com.codequest.util.InterviewSessionUtils;

/**
 * 作答提交控制器，负责触发 AI 评分并返回结果页。
 * Author: 张雨泽
 */
@WebServlet("/SubmitAnswer")
public class SubmitAnswerServlet extends HttpServlet {

    private static final String MODEL_CHAT = "deepseek-chat";
    private static final String MODEL_REASONER = "deepseek-reasoner";
    private static final String SESSION_REPORT_CONTENT = "interviewReportMarkdown";
    private final QuestionService questionService = new QuestionService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String questionIdParam = req.getParameter("questionId");
        String userAnswer = req.getParameter("userAnswer");
        String useReasoningParam = req.getParameter("useReasoning");

        Long questionId;
        try {
            questionId = Long.valueOf(questionIdParam);
        } catch (Exception ex) {
            // 题目 ID 不合法时直接返回题目列表。
            resp.sendRedirect(req.getContextPath() + "/questions");
            return;
        }

        if (userAnswer == null) {
            userAnswer = "";
        }

        boolean useReasoning = "true".equalsIgnoreCase(useReasoningParam)
            || "on".equalsIgnoreCase(useReasoningParam)
            || "1".equals(useReasoningParam);
        String model = useReasoning ? MODEL_REASONER : MODEL_CHAT;

        HttpSession session = req.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            // 过滤器已做登录校验，这里作为兜底保护。
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

            String interviewSessionId = InterviewSessionUtils.ensureInterviewSessionId(session);

        Result<AIService.AIResult> serviceResult = questionService.evaluateAndSave(
                loginUser.getId(),
                questionId,
                userAnswer,
                model,
                interviewSessionId
        );

        if (!serviceResult.isSuccess() || serviceResult.getData() == null) {
            // 服务层已封装业务错误信息，Servlet 统一抛出处理。
            throw new ServletException(serviceResult.getMessage());
        }

        AIService.AIResult aiResult = serviceResult.getData();
        int aiScore = aiResult.getScore();
        String aiSuggestion = aiResult.getFeedback();
        String reportMarkdown = buildReportMarkdown(questionId, aiScore, aiSuggestion);

        session.setAttribute(SESSION_REPORT_CONTENT, reportMarkdown);

        req.setAttribute("aiScore", aiScore);
        req.setAttribute("aiSuggestion", aiSuggestion);
        req.setAttribute("questionId", questionId);
        req.getRequestDispatcher("/result.jsp").forward(req, resp);
    }

    private String buildReportMarkdown(Long questionId, int aiScore, String aiSuggestion) {
        StringBuilder builder = new StringBuilder();
        builder.append("# 面试评价报告\n\n");
        builder.append("- 题目 ID：").append(questionId == null ? "未知" : questionId).append("\n");
        builder.append("- 评分：").append(aiScore).append("\n\n");
        builder.append("## DeepSeek 反馈建议\n\n");
        builder.append(aiSuggestion == null || aiSuggestion.trim().isEmpty() ? "暂无反馈内容。" : aiSuggestion.trim()).append("\n");
        return builder.toString();
    }
}
