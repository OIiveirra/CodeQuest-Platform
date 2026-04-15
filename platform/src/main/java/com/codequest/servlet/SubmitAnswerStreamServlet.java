package com.codequest.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.codequest.dto.Result;
import com.codequest.model.ConversationMessage;
import com.codequest.model.Question;
import com.codequest.model.User;
import com.codequest.service.EvaluationService;
import com.codequest.service.QuestionService;
import com.codequest.util.AIService;
import com.codequest.util.InterviewSessionUtils;
import com.google.gson.Gson;

/**
 * 基于 SSE 的作答提交控制器，向前端实时推送评测过程与最终结果。
 */
@WebServlet("/SubmitAnswerStream")
public class SubmitAnswerStreamServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String MODEL_CHAT = "deepseek-chat";
    private static final String MODEL_REASONER = "deepseek-reasoner";
    private static final Pattern REFERER_ID_PATTERN = Pattern.compile("(?:[?&]|^)id=([0-9]+)(?:&|$)");
    private static final Gson GSON = new Gson();
    private final QuestionService questionService = new QuestionService();
    private final EvaluationService evaluationService = new EvaluationService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/event-stream");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setHeader("Connection", "keep-alive");
        resp.setHeader("X-Accel-Buffering", "no");

        PrintWriter out = resp.getWriter();

        HttpSession session = req.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            writeError(out, "请先登录后再提交回答。");
            return;
        }

        Long questionId = resolveQuestionId(req);
        if (questionId == null) {
            writeError(out, "题目 ID 不合法。");
            return;
        }

        String userAnswer = firstNonBlank(req.getParameter("continueAnswer"), req.getParameter("userAnswer"));
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            writeError(out, "请输入回答内容后再提交。");
            return;
        }

        boolean useReasoning = isTruthy(req.getParameter("useReasoning"));
        String model = useReasoning ? MODEL_REASONER : MODEL_CHAT;

        Result<Question> questionResult = questionService.getQuestionById(questionId);
        if (!questionResult.isSuccess() || questionResult.getData() == null) {
            writeError(out, questionResult.getMessage() == null ? "未找到该题目。" : questionResult.getMessage());
            return;
        }

        Question question = questionResult.getData();
        Long activeQuestionId = InterviewSessionUtils.getQuestionId(session);
        if (activeQuestionId != null && !activeQuestionId.equals(questionId)) {
            InterviewSessionUtils.clear(session);
        }

        String interviewSessionId = InterviewSessionUtils.ensureInterviewSessionId(session);

        List<ConversationMessage> conversationHistory = InterviewSessionUtils.getMessages(session);
        String prompt = evaluationService.renderConversationPrompt(
                question.getContent(),
                question.getStandardAnswer(),
                userAnswer,
                conversationHistory
        );

        writeEvent(out, "status", "正在连接 DeepSeek 并生成评测结果...");
        out.flush();

        String category = resolveCategory(question);

        Result<AIService.AIResult> serviceResult = Result.success(evaluationService.evaluateAndPersistConversation(
                loginUser.getId(),
                questionId,
                category,
                question.getContent(),
                question.getStandardAnswer(),
                userAnswer,
                model,
                conversationHistory,
                token -> {
                // 这里保留回调接口，便于后续需要将 DeepSeek 原始流同步给前端时扩展。
                },
                interviewSessionId
        ));

        if (!serviceResult.isSuccess() || serviceResult.getData() == null) {
            writeError(out, serviceResult.getMessage() == null ? "评测失败。" : serviceResult.getMessage());
            return;
        }

        AIService.AIResult aiResult = serviceResult.getData();
        ConversationMessage userMessage = new ConversationMessage("user", prompt == null ? userAnswer : prompt);
        ConversationMessage assistantMessage = new ConversationMessage("assistant", buildAssistantMessage(aiResult));
        conversationHistory.add(userMessage);
        conversationHistory.add(assistantMessage);
        InterviewSessionUtils.saveMessages(session, conversationHistory);
        InterviewSessionUtils.setQuestionId(session, questionId);
        InterviewSessionUtils.setFollowUpQuestion(session, aiResult.getFollowUpQuestion());

        writeEvent(out, "score", GSON.toJson(new ScorePayload(aiResult.getScore(), aiResult.getCategory())));
        writeEvent(out, "feedback", GSON.toJson(aiResult.getFeedback()));
        if (aiResult.getScore() > 70 && aiResult.getFollowUpQuestion() != null && !aiResult.getFollowUpQuestion().trim().isEmpty()) {
            writeEvent(out, "followup", GSON.toJson(new FollowUpPayload(aiResult.getFollowUpQuestion())));
        }
        writeEvent(out, "done", GSON.toJson(new DonePayload(aiResult.getScore(), aiResult.getCategory())));
        out.flush();
    }

    private void writeError(PrintWriter out, String message) {
        writeEvent(out, "error", GSON.toJson(message));
        out.flush();
    }

    private void writeEvent(PrintWriter out, String event, String dataJson) {
        out.write("event: " + event + "\n");
        out.write("data: " + dataJson + "\n\n");
    }

    private boolean isTruthy(String value) {
        return "true".equalsIgnoreCase(value)
                || "on".equalsIgnoreCase(value)
                || "1".equals(value);
    }

    private String resolveCategory(Question question) {
        if (question == null) {
            return "综合能力";
        }
        if (question.getTags() != null && !question.getTags().trim().isEmpty()) {
            String tags = question.getTags().trim();
            if (tags.contains("，")) {
                return tags.split("，")[0].trim();
            }
            if (tags.contains(",")) {
                return tags.split(",")[0].trim();
            }
            return tags;
        }
        if (question.getType() == null) {
            return "综合能力";
        }
        switch (question.getType()) {
            case 1:
                return "Java 基础";
            case 2:
                return "算法";
            case 3:
                return "数据库";
            case 4:
                return "系统设计";
            default:
                return "综合能力";
        }
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.trim().isEmpty()) {
            return first.trim();
        }
        if (second != null && !second.trim().isEmpty()) {
            return second.trim();
        }
        return "";
    }

    private String buildAssistantMessage(AIService.AIResult aiResult) {
        return GSON.toJson(new AssistantPayload(aiResult.getScore(), aiResult.getFeedback(), aiResult.getCategory(), aiResult.getFollowUpQuestion()));
    }

    private Long parseLong(String value) {
        try {
            return value == null || value.trim().isEmpty() ? null : Long.valueOf(value.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private Long resolveQuestionId(HttpServletRequest req) {
        Long fromQuestionId = parseLong(req.getParameter("questionId"));
        if (fromQuestionId != null) {
            return fromQuestionId;
        }

        Long fromId = parseLong(req.getParameter("id"));
        if (fromId != null) {
            return fromId;
        }

        String referer = req.getHeader("Referer");
        if (referer != null && !referer.trim().isEmpty()) {
            Matcher matcher = REFERER_ID_PATTERN.matcher(referer);
            if (matcher.find()) {
                return parseLong(matcher.group(1));
            }
        }

        return null;
    }

    private static final class ScorePayload {
        private final int score;
        private final String category;

        private ScorePayload(int score, String category) {
            this.score = score;
            this.category = category;
        }
    }

    private static final class FollowUpPayload {
        private final String followUpQuestion;

        private FollowUpPayload(String followUpQuestion) {
            this.followUpQuestion = followUpQuestion;
        }
    }

    private static final class AssistantPayload {
        private final int score;
        private final String feedback;
        private final String category;
        private final String followUpQuestion;

        private AssistantPayload(int score, String feedback, String category, String followUpQuestion) {
            this.score = score;
            this.feedback = feedback;
            this.category = category;
            this.followUpQuestion = followUpQuestion;
        }
    }

    private static final class DonePayload {
        private final int score;
        private final String category;

        private DonePayload(int score, String category) {
            this.score = score;
            this.category = category;
        }
    }
}
