package com.codequest.service;

import java.sql.SQLException;
import java.util.List;

import com.codequest.dao.QuestionDAO;
import com.codequest.dto.Result;
import com.codequest.model.Question;
import com.codequest.util.AIService;

/**
 * 题目业务服务层，负责题目查询与 AI 面试流程编排。
 * Author: 张雨泽
 */
public class QuestionService {

    private final QuestionDAO questionDAO = new QuestionDAO();
    private final EvaluationService evaluationService = new EvaluationService();

    public Result<List<Question>> listQuestions() {
        try {
            return Result.success(questionDAO.findAllQuestions());
        } catch (SQLException ex) {
            return Result.failure(500, "题目查询失败。");
        }
    }

    public Result<Question> getQuestionById(Long questionId) {
        if (questionId == null) {
            return Result.failure(400, "题目 ID 不合法。");
        }

        try {
            Question question = questionDAO.findById(questionId);
            if (question == null) {
                return Result.failure(404, "未找到该题目。");
            }
            return Result.success(question);
        } catch (SQLException ex) {
            return Result.failure(500, "题目查询失败。");
        }
    }

    public Result<AIService.AIResult> evaluateAndSave(long userId, Long questionId, String userAnswer, String model,
                                                      String interviewSessionId) {
        // 参数校验与题目存在性校验集中在服务层，减少 Servlet 复杂度。
        Result<Question> questionResult = getQuestionById(questionId);
        if (!questionResult.isSuccess() || questionResult.getData() == null) {
            return Result.failure(questionResult.getCode(), questionResult.getMessage());
        }

        Question question = questionResult.getData();
        String safeAnswer = userAnswer == null ? "" : userAnswer;
        String category = resolveEvaluationCategory(question);

        AIService.AIResult aiResult = evaluationService.evaluateAndPersist(
            userId,
            questionId,
            category,
            question.getContent(),
            question.getStandardAnswer(),
            safeAnswer,
            model,
            interviewSessionId
        );

        try {
            // 将一次评测产生的回答、分数与建议作为同一业务动作持久化。
            questionDAO.saveInterviewRecord(userId, questionId, safeAnswer, aiResult.getScore(), aiResult.getFeedback());
            return Result.success(aiResult);
        } catch (SQLException ex) {
            return Result.failure(500, "保存面试记录失败。");
        }
    }

    public Result<AIService.AIResult> evaluateAndSave(long userId, Long questionId, String userAnswer, String model) {
        return evaluateAndSave(userId, questionId, userAnswer, model, null);
    }

    public Result<AIService.AIResult> evaluateAndSaveStreaming(long userId, Long questionId, String userAnswer,
                                                               String model, AIService.StreamTokenHandler tokenHandler,
                                                               String interviewSessionId) {
        Result<Question> questionResult = getQuestionById(questionId);
        if (!questionResult.isSuccess() || questionResult.getData() == null) {
            return Result.failure(questionResult.getCode(), questionResult.getMessage());
        }

        Question question = questionResult.getData();
        String safeAnswer = userAnswer == null ? "" : userAnswer;
        String category = resolveEvaluationCategory(question);

        AIService.AIResult aiResult = evaluationService.evaluateAndPersistStreaming(
                userId,
                questionId,
                category,
                question.getContent(),
                question.getStandardAnswer(),
                safeAnswer,
                model,
            tokenHandler,
            interviewSessionId
        );

        try {
            questionDAO.saveInterviewRecord(userId, questionId, safeAnswer, aiResult.getScore(), aiResult.getFeedback());
            return Result.success(aiResult);
        } catch (SQLException ex) {
            return Result.failure(500, "保存面试记录失败。");
        }
    }

    public Result<AIService.AIResult> evaluateAndSaveStreaming(long userId, Long questionId, String userAnswer,
                                                               String model, AIService.StreamTokenHandler tokenHandler) {
        return evaluateAndSaveStreaming(userId, questionId, userAnswer, model, tokenHandler, null);
    }

    public Result<Void> addQuestion(Question question) {
        if (!isValidQuestionInput(question)) {
            return Result.failure(400, "题目标题和内容不能为空。");
        }

        try {
            questionDAO.addQuestion(question);
            return Result.success(null);
        } catch (SQLException ex) {
            return Result.failure(500, "新增题目失败。");
        }
    }

    public Result<Void> updateQuestion(Question question) {
        if (question == null || question.getId() == null) {
            return Result.failure(400, "题目 ID 不合法。");
        }
        if (!isValidQuestionInput(question)) {
            return Result.failure(400, "题目标题和内容不能为空。");
        }

        try {
            int affected = questionDAO.updateQuestion(question);
            if (affected <= 0) {
                return Result.failure(404, "未找到要更新的题目。");
            }
            return Result.success(null);
        } catch (SQLException ex) {
            return Result.failure(500, "更新题目失败。");
        }
    }

    public Result<Void> deleteQuestion(Long questionId) {
        if (questionId == null) {
            return Result.failure(400, "题目 ID 不合法。");
        }

        try {
            int affected = questionDAO.deleteQuestion(questionId);
            if (affected <= 0) {
                return Result.failure(404, "未找到要删除的题目。");
            }
            return Result.success(null);
        } catch (SQLException ex) {
            return Result.failure(500, "删除题目失败。");
        }
    }

    private String resolveEvaluationCategory(Question question) {
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

        if (question.getType() != null) {
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

        return "综合能力";
    }

    private boolean isValidQuestionInput(Question question) {
        if (question == null) {
            return false;
        }
        return question.getTitle() != null && !question.getTitle().trim().isEmpty()
                && question.getContent() != null && !question.getContent().trim().isEmpty();
    }
}
