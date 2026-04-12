package com.codequest.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.codequest.dao.EvaluationDAO;
import com.codequest.dao.PromptDAO;
import com.codequest.model.ConversationMessage;
import com.codequest.model.Evaluation;
import com.codequest.util.AIService;
import com.codequest.util.InterviewSessionUtils;
import com.codequest.util.MD5Utils;
import com.codequest.util.PromptTemplateUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 评测服务层：封装 AI 调用与 Redis 缓存逻辑。
 * Author: 张雨泽
 */
public class EvaluationService {

    private static final int CACHE_SECONDS = 24 * 60 * 60;
    private static final String EVALUATION_PROMPT_NAME = "evaluation_prompt";
    private static final Gson GSON = new Gson();
    private static final JedisPool JEDIS_POOL = buildJedisPool();
    private final EvaluationDAO evaluationDAO = new EvaluationDAO();
    private final PromptDAO promptDAO = new PromptDAO();

    public AIService.AIResult evaluateAndPersist(long userId, Long questionId, String category,
                                                 String content, String standardAnswer,
                                                 String userAnswer, String model) {
        return evaluateAndPersist(userId, questionId, category, content, standardAnswer, userAnswer, model, null);
    }

    public AIService.AIResult evaluateAndPersist(long userId, Long questionId, String category,
                                                 String content, String standardAnswer,
                                                 String userAnswer, String model,
                                                 String interviewSessionId) {
        String safeAnswer = userAnswer == null ? "" : userAnswer;
        String key = buildCacheKey(questionId, safeAnswer);

        AIService.AIResult cached = readFromCache(key);
        if (cached != null) {
            String finalCategory = pickCategory(cached.getCategory(), category);
            persistEvaluationQuietly(buildEvaluation(userId, questionId, safeAnswer, cached, finalCategory, interviewSessionId));
            return cached;
        }

        String prompt = buildPrompt(content, standardAnswer, safeAnswer);
        AIService.AIResult aiResult = invokeAI(prompt, content, standardAnswer, safeAnswer, model, false, null);
        writeToCache(key, aiResult);
        String finalCategory = pickCategory(aiResult.getCategory(), category);
        persistEvaluationQuietly(buildEvaluation(userId, questionId, safeAnswer, aiResult, finalCategory, interviewSessionId));
        return aiResult;
    }

    public AIService.AIResult evaluateAndPersistStreaming(long userId, Long questionId, String category,
                                                          String content, String standardAnswer,
                                                          String userAnswer, String model,
                                                          AIService.StreamTokenHandler tokenHandler) {
        return evaluateAndPersistStreaming(userId, questionId, category, content, standardAnswer, userAnswer, model, tokenHandler, null);
    }

    public AIService.AIResult evaluateAndPersistStreaming(long userId, Long questionId, String category,
                                                          String content, String standardAnswer,
                                                          String userAnswer, String model,
                                                          AIService.StreamTokenHandler tokenHandler,
                                                          String interviewSessionId) {
        String safeAnswer = userAnswer == null ? "" : userAnswer;
        String key = buildCacheKey(questionId, safeAnswer);

        AIService.AIResult cached = readFromCache(key);
        if (cached != null) {
            String finalCategory = pickCategory(cached.getCategory(), category);
            persistEvaluationQuietly(buildEvaluation(userId, questionId, safeAnswer, cached, finalCategory, interviewSessionId));
            if (tokenHandler != null) {
                streamText(tokenHandler, cached.getFeedback());
            }
            return cached;
        }

        String prompt = buildPrompt(content, standardAnswer, safeAnswer);
        AIService.AIResult aiResult = invokeAI(prompt, content, standardAnswer, safeAnswer, model, true, tokenHandler);
        writeToCache(key, aiResult);
        String finalCategory = pickCategory(aiResult.getCategory(), category);
        persistEvaluationQuietly(buildEvaluation(userId, questionId, safeAnswer, aiResult, finalCategory, interviewSessionId));
        return aiResult;
    }

    public AIService.AIResult evaluateAndPersistConversation(long userId, Long questionId, String category,
                                                             String content, String standardAnswer,
                                                             String userAnswer, String model,
                                                             List<ConversationMessage> conversationHistory,
                                                             AIService.StreamTokenHandler tokenHandler) {
        return evaluateAndPersistConversation(userId, questionId, category, content, standardAnswer, userAnswer,
            model, conversationHistory, tokenHandler, null);
        }

        public AIService.AIResult evaluateAndPersistConversation(long userId, Long questionId, String category,
                                     String content, String standardAnswer,
                                     String userAnswer, String model,
                                     List<ConversationMessage> conversationHistory,
                                     AIService.StreamTokenHandler tokenHandler,
                                     String interviewSessionId) {
        String safeAnswer = userAnswer == null ? "" : userAnswer;
        String conversationKey = buildConversationKey(conversationHistory);
        String key = buildCacheKey(questionId, safeAnswer, conversationKey);

        AIService.AIResult cached = readFromCache(key);
        if (cached != null) {
            String finalCategory = pickCategory(cached.getCategory(), category);
            persistEvaluationQuietly(buildEvaluation(userId, questionId, safeAnswer, cached, finalCategory, interviewSessionId));
            if (tokenHandler != null) {
                streamText(tokenHandler, cached.getFeedback());
            }
            return cached;
        }

        String prompt = buildPrompt(content, standardAnswer, safeAnswer, conversationHistory);
        List<ConversationMessage> messages = new ArrayList<>(InterviewSessionUtils.copyMessages(conversationHistory));
        if (prompt != null && !prompt.trim().isEmpty()) {
            messages.add(new ConversationMessage("user", prompt));
        }

        AIService.AIResult aiResult;
        if (tokenHandler != null) {
            aiResult = AIService.getAIResultStreamingFromMessages(messages, model, tokenHandler);
        } else {
            aiResult = AIService.getAIResultFromMessages(messages, model);
        }

        writeToCache(key, aiResult);
        String finalCategory = pickCategory(aiResult.getCategory(), category);
        persistEvaluationQuietly(buildEvaluation(userId, questionId, safeAnswer, aiResult, finalCategory, interviewSessionId));
        return aiResult;
    }

    public String renderConversationPrompt(String content, String standardAnswer, String userAnswer,
                                           List<ConversationMessage> conversationHistory) {
        return buildPrompt(content, standardAnswer, userAnswer, conversationHistory);
    }

    private AIService.AIResult invokeAI(String prompt, String content, String standardAnswer, String userAnswer,
                                        String model, boolean streaming, AIService.StreamTokenHandler tokenHandler) {
        if (prompt == null || prompt.trim().isEmpty()) {
            if (streaming) {
                return AIService.getAIResultStreaming(content, standardAnswer, userAnswer, model, tokenHandler);
            }
            return AIService.getAIResult(content, standardAnswer, userAnswer, model);
        }

        if (streaming) {
            return AIService.getAIResultStreamingFromPrompt(prompt, model, tokenHandler);
        }
        return AIService.getAIResultFromPrompt(prompt, model);
    }

    private String buildPrompt(String content, String standardAnswer, String userAnswer, List<ConversationMessage> conversationHistory) {
        try {
            String template = promptDAO.findActiveContentByTemplateName(EVALUATION_PROMPT_NAME);
            if (template == null || template.trim().isEmpty()) {
                return null;
            }

            java.util.Map<String, String> variables = new java.util.HashMap<>();
            variables.put("questionContent", content);
            variables.put("standardAnswer", standardAnswer);
            variables.put("userAnswer", userAnswer);
            variables.put("conversationHistory", renderConversationHistory(conversationHistory));
            return PromptTemplateUtils.applyTemplate(template, variables);
        } catch (Exception ex) {
            return null;
        }
    }

    private String buildPrompt(String content, String standardAnswer, String userAnswer) {
        try {
            String template = promptDAO.findActiveContentByTemplateName(EVALUATION_PROMPT_NAME);
            if (template == null || template.trim().isEmpty()) {
                return null;
            }
            java.util.Map<String, String> variables = new java.util.HashMap<>();
            variables.put("questionContent", content);
            variables.put("standardAnswer", standardAnswer);
            variables.put("userAnswer", userAnswer);
            variables.put("conversationHistory", "");
            return PromptTemplateUtils.applyTemplate(template, variables);
        } catch (Exception ex) {
            return null;
        }
    }

    private Evaluation buildEvaluation(long userId, Long questionId, int score, String feedback, String category) {
        return buildEvaluation(userId, questionId, null, score, feedback, category, null);
    }

    private Evaluation buildEvaluation(long userId, Long questionId, String userAnswer, int score, String feedback, String category,
                                       String interviewSessionId) {
        Evaluation eval = new Evaluation();
        eval.setUserId(userId);
        eval.setQuestionId(questionId);
        eval.setSessionId(interviewSessionId);
        eval.setUserAnswer(userAnswer);
        eval.setScore(score);
        eval.setFeedback(feedback);
        eval.setCategory(category);
        return eval;
    }

    private Evaluation buildEvaluation(long userId, Long questionId, String userAnswer, AIService.AIResult aiResult,
                                       String category, String interviewSessionId) {
        Evaluation eval = buildEvaluation(userId, questionId, userAnswer,
                aiResult == null ? 0 : aiResult.getScore(),
                aiResult == null ? "" : aiResult.getFeedback(),
                category,
                interviewSessionId);
        if (aiResult != null) {
            eval.setTokenUsed(aiResult.getTotalTokens());
        }
        return eval;
    }

    private String pickCategory(String aiCategory, String fallbackCategory) {
        if (aiCategory != null && !aiCategory.trim().isEmpty()) {
            return aiCategory.trim();
        }
        if (fallbackCategory != null && !fallbackCategory.trim().isEmpty()) {
            return fallbackCategory.trim();
        }
        return "综合能力";
    }

    private void streamText(AIService.StreamTokenHandler tokenHandler, String text) {
        if (tokenHandler == null || text == null || text.isEmpty()) {
            return;
        }
        for (int i = 0; i < text.length(); i++) {
            tokenHandler.onToken(String.valueOf(text.charAt(i)));
        }
    }

    private void persistEvaluationQuietly(Evaluation evaluation) {
        try {
            evaluationDAO.insert(evaluation);
        } catch (Exception ignored) {
            // 评分历史落库失败不影响主流程。
        }
    }

    private AIService.AIResult readFromCache(String key) {
        if (JEDIS_POOL == null) {
            return null;
        }

        try (Jedis jedis = JEDIS_POOL.getResource()) {
            String cached = jedis.get(key);
            if (cached == null || cached.isEmpty()) {
                return null;
            }

            JsonObject obj = JsonParser.parseString(cached).getAsJsonObject();
            int score = obj.has("score") ? obj.get("score").getAsInt() : 65;
            String feedback = obj.has("feedback") ? obj.get("feedback").getAsString() : "";
            String category = obj.has("category") ? obj.get("category").getAsString() : "综合能力";
            if (feedback == null || feedback.trim().isEmpty()) {
                return null;
            }
            int promptTokens = obj.has("promptTokens") ? obj.get("promptTokens").getAsInt() : 0;
            int completionTokens = obj.has("completionTokens") ? obj.get("completionTokens").getAsInt() : 0;
            return new AIService.AIResult(score, feedback, category, "", promptTokens, completionTokens);
        } catch (Exception ex) {
            return null;
        }
    }

    private void writeToCache(String key, AIService.AIResult result) {
        if (JEDIS_POOL == null || result == null) {
            return;
        }

        try (Jedis jedis = JEDIS_POOL.getResource()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("score", result.getScore());
            obj.addProperty("feedback", result.getFeedback());
            obj.addProperty("category", result.getCategory());
            obj.addProperty("promptTokens", result.getPromptTokens());
            obj.addProperty("completionTokens", result.getCompletionTokens());
            jedis.setex(key, CACHE_SECONDS, GSON.toJson(obj));
        } catch (Exception ignored) {
            // Redis 不可用时不影响主流程。
        }
    }

    private String buildCacheKey(Long questionId, String userAnswer) {
        return buildCacheKey(questionId, userAnswer, "");
    }

    private String buildCacheKey(Long questionId, String userAnswer, String conversationKey) {
        String qid = questionId == null ? "0" : String.valueOf(questionId);
        String safeAnswer = userAnswer == null ? "" : userAnswer;
        String safeConversation = conversationKey == null ? "" : conversationKey;
        String keyHash = MD5Utils.md5(qid + safeAnswer + safeConversation);
        return "cq:evaluation:" + keyHash;
    }

    private String buildConversationKey(List<ConversationMessage> conversationHistory) {
        if (conversationHistory == null || conversationHistory.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (ConversationMessage message : conversationHistory) {
            if (message == null) {
                continue;
            }
            builder.append('[').append(message.getRole()).append(']')
                    .append(message.getContent() == null ? "" : message.getContent())
                    .append('\n');
        }
        return builder.toString();
    }

    private String renderConversationHistory(List<ConversationMessage> conversationHistory) {
        if (conversationHistory == null || conversationHistory.isEmpty()) {
            return "暂无历史对话。";
        }

        StringBuilder builder = new StringBuilder();
        int index = 1;
        for (ConversationMessage message : conversationHistory) {
            if (message == null) {
                continue;
            }
            builder.append(index++).append(". ")
                    .append(message.getRole()).append(": ")
                    .append(message.getContent() == null ? "" : message.getContent())
                    .append('\n');
        }
        return builder.toString().trim();
    }

    private static JedisPool buildJedisPool() {
        Properties p = loadProperties();
        String host = p.getProperty("redis.host", "127.0.0.1").trim();
        int port = parseInt(p.getProperty("redis.port"), 6379);
        int timeout = parseInt(p.getProperty("redis.timeout"), 2000);
        String password = p.getProperty("redis.password", "").trim();

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(20);
        config.setMaxIdle(10);
        config.setMinIdle(1);

        Set<String> candidates = new LinkedHashSet<>();
        candidates.add(host);
        candidates.add("codequest-redis");
        candidates.add("127.0.0.1");

        for (String candidate : candidates) {
            try {
                JedisPool pool;
                if (password.isEmpty()) {
                    pool = new JedisPool(config, candidate, port, timeout);
                } else {
                    pool = new JedisPool(config, candidate, port, timeout, password);
                }

                try (Jedis jedis = pool.getResource()) {
                    String pong = jedis.ping();
                    if (pong != null && pong.equalsIgnoreCase("PONG")) {
                        return pool;
                    }
                }
                pool.close();
            } catch (Exception ignored) {
                // 尝试下一个候选地址，兼容 Docker 和本机两种环境。
            }
        }
        return null;
    }

    private static Properties loadProperties() {
        Properties p = new Properties();
        try (InputStream in = EvaluationService.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                p.load(in);
            }
        } catch (IOException ignored) {
        }
        return p;
    }

    private static int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return defaultValue;
        }
    }
}
