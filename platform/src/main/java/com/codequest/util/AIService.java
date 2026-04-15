package com.codequest.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.codequest.model.ConversationMessage;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * AI 服务工具类，负责调用 DeepSeek 接口并解析评分结果。
 * Author: 张雨泽
 */
public final class AIService {

    private static final Logger LOGGER = Logger.getLogger(AIService.class.getName());

    private static final Gson GSON = new Gson();
    private static final String DEFAULT_API_BASE_URL = "https://api.deepseek.com";
    private static final String DEFAULT_MODEL = "deepseek-reasoner";
    private static final String MODEL_CHAT = "deepseek-chat";
    private static final Pattern FOLLOW_UP_JSON_PATTERN = Pattern.compile("(?i)\\\"followUpQuestion\\\"\\s*[:：]\\s*\\\"([\\s\\S]*?)\\\"");
    private static final Pattern FOLLOW_UP_TEXT_PATTERN = Pattern.compile("(?i)(?:追问问题|followUpQuestion|follow-up question)\\s*[:：]\\s*([\\s\\S]+)$");
    private static final String MODEL_REASONER = "deepseek-reasoner";
    private static final int CONNECT_TIMEOUT_MS = 15000;
    private static final int READ_TIMEOUT_CHAT_MS = 30000;
    private static final int READ_TIMEOUT_REASONER_MS = 120000;

    private static final String DEEPSEEK_API_URL;
    private static final String DEEPSEEK_API_KEY;
    private static final String DEEPSEEK_MODEL;
    private static final Pattern SCORE_PATTERN = Pattern.compile("(?i)(?:\\\"score\\\"\\s*[:：]\\s*\\\"?(\\d{1,3})\\\"?)|(?:评分\\s*[:：]\\s*(\\d{1,3}))|(?:score\\s*[:：]\\s*(\\d{1,3}))");
    private static final Pattern FEEDBACK_JSON_PATTERN = Pattern.compile("(?i)\\\"feedback\\\"\\s*[:：]\\s*\\\"([\\s\\S]*?)\\\"");
    private static final Pattern FEEDBACK_TEXT_PATTERN = Pattern.compile("(?i)(?:反馈|建议|feedback)\\s*[:：]\\s*([\\s\\S]+)$");
    private static final Pattern CATEGORY_JSON_PATTERN = Pattern.compile("(?i)\\\"category\\\"\\s*[:：]\\s*\\\"([\\s\\S]*?)\\\"");
    private static final Pattern CATEGORY_TEXT_PATTERN = Pattern.compile("(?i)(?:分类|category)\\s*[:：]\\s*([\\u4e00-\\u9fa5A-Za-z0-9_\\- ]{2,40})");

    static {
        Properties properties = new Properties();
        try (InputStream inputStream = AIService.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException ignored) {
            // 配置加载失败时保留默认参数并走兜底策略。
        }
        String configuredUrl = properties.getProperty("deepseek.api.url", DEFAULT_API_BASE_URL).trim();
        String configuredKey = properties.getProperty("deepseek.api.key", "").trim();
        String configuredModel = properties.getProperty("deepseek.model", DEFAULT_MODEL).trim();

        String envUrl = safeTrim(System.getenv("DEEPSEEK_API_URL"));
        String envKey = safeTrim(System.getenv("DEEPSEEK_API_KEY"));
        String envModel = safeTrim(System.getenv("DEEPSEEK_MODEL"));

        DEEPSEEK_API_URL = normalizeApiUrl(firstNonBlank(envUrl, configuredUrl, DEFAULT_API_BASE_URL));
        DEEPSEEK_API_KEY = firstNonBlank(envKey, configuredKey, "");
        DEEPSEEK_MODEL = firstNonBlank(envModel, configuredModel, DEFAULT_MODEL);
    }

    private AIService() {
    }

    public static AIResult getAIResult(String content, String standardAnswer, String userAnswer, String model) {
        String prompt = buildDefaultPrompt(content, standardAnswer, userAnswer);
        return getAIResultFromPrompt(prompt, model);
    }

    public static AIResult getAIResultStreaming(String content, String standardAnswer, String userAnswer, String model,
                                                StreamTokenHandler tokenHandler) {
        String prompt = buildDefaultPrompt(content, standardAnswer, userAnswer);
        return getAIResultStreamingFromPrompt(prompt, model, tokenHandler);
    }

    public static AIResult getAIResultFromPrompt(String prompt, String model) {
        if (DEEPSEEK_API_KEY.isEmpty()) {
            return fallbackResult("缺少 API Key");
        }

        String finalModel = pickModel(model);

        try {
            String streamedContent = requestDeepSeek(
                    buildPayload(prompt, finalModel, false),
                    finalModel,
                    false,
                    null
            );
            AIResult parsed = parseAIResultFromResponse(streamedContent);
            if (parsed != null) {
                return parsed;
            }
            return fallbackResult("AI 返回格式异常");
        } catch (Exception ex) {
            return fallbackResult("调用超时或网络异常");
        }
    }

    public static AIResult getAIResultStreamingFromPrompt(String prompt, String model, StreamTokenHandler tokenHandler) {
        if (DEEPSEEK_API_KEY.isEmpty()) {
            return fallbackResult("缺少 API Key");
        }

        String finalModel = pickModel(model);

        try {
            String streamedContent = requestDeepSeek(
                    buildPayload(prompt, finalModel, true),
                    finalModel,
                    true,
                    tokenHandler
            );
            AIResult parsed = parseAIResultFromContent(streamedContent);
            if (parsed != null) {
                return parsed;
            }
            return fallbackResult("AI 返回格式异常");
        } catch (Exception ex) {
            return fallbackResult("调用超时或网络异常");
        }
    }

    public static AIResult getAIResultFromMessages(List<ConversationMessage> messages, String model) {
        if (DEEPSEEK_API_KEY.isEmpty()) {
            return fallbackResult("缺少 API Key");
        }

        String finalModel = pickModel(model);
        try {
            String response = requestDeepSeek(buildPayload(messages, finalModel, false), finalModel, false, null);
            AIResult parsed = parseAIResultFromResponse(response);
            if (parsed != null) {
                return parsed;
            }
            return fallbackResult("AI 返回格式异常");
        } catch (Exception ex) {
            return fallbackResult("调用超时或网络异常");
        }
    }

    public static AIResult getAIResultStreamingFromMessages(List<ConversationMessage> messages, String model,
                                                            StreamTokenHandler tokenHandler) {
        if (DEEPSEEK_API_KEY.isEmpty()) {
            return fallbackResult("缺少 API Key");
        }

        String finalModel = pickModel(model);
        try {
            String streamedContent = requestDeepSeek(buildPayload(messages, finalModel, true), finalModel, true, tokenHandler);
            AIResult parsed = parseAIResultFromContent(streamedContent);
            if (parsed != null) {
                return parsed;
            }
            return fallbackResult("AI 返回格式异常");
        } catch (Exception ex) {
            return fallbackResult("调用超时或网络异常");
        }
    }

    public static String getRawContentFromPrompt(String prompt, String model) {
        if (DEEPSEEK_API_KEY.isEmpty()) {
            return "";
        }

        String finalModel = pickModel(model);
        try {
            return readAssistantContent(requestDeepSeek(buildPayload(prompt, finalModel, false), finalModel, false, null));
        } catch (Exception ex) {
            return "";
        }
    }

    public static String getRawContentFromMessages(List<ConversationMessage> messages, String model) {
        if (DEEPSEEK_API_KEY.isEmpty()) {
            return "";
        }

        String finalModel = pickModel(model);
        try {
            return readAssistantContent(requestDeepSeek(buildPayload(messages, finalModel, false), finalModel, false, null));
        } catch (Exception ex) {
            return "";
        }
    }

    private static String buildPayload(String prompt, String model, boolean stream) {
        JsonObject root = new JsonObject();
        root.addProperty("model", model);
        root.addProperty("temperature", 0.2);
        root.addProperty("stream", stream);

        JsonArray messages = new JsonArray();

        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", "你是一个严谨的 IT 面试官，擅长根据题目和标准答案对候选人作答进行客观评分。请严格按照 JSON 输出。\n");
        messages.add(systemMsg);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", safe(prompt));
        messages.add(userMsg);

        root.add("messages", messages);
        return GSON.toJson(root);
    }

    private static String buildPayload(List<ConversationMessage> messages, String model, boolean stream) {
        JsonObject root = new JsonObject();
        root.addProperty("model", model);
        root.addProperty("temperature", 0.2);
        root.addProperty("stream", stream);

        JsonArray messageArray = new JsonArray();
        if (messages != null) {
            for (ConversationMessage message : messages) {
                if (message == null) {
                    continue;
                }
                JsonObject item = new JsonObject();
                item.addProperty("role", safe(message.getRole()));
                item.addProperty("content", safe(message.getContent()));
                messageArray.add(item);
            }
        }
        root.add("messages", messageArray);
        return GSON.toJson(root);
    }

    private static String buildDefaultPrompt(String content, String standardAnswer, String userAnswer) {
        return "你是一个严谨的 IT 面试官。请对比题目：" + safe(content)
                + " 和标准答案：" + safe(standardAnswer)
                + "，给用户的回答：" + safe(userAnswer)
                + " 进行评分（0-100）。请以 JSON 格式返回，包含字段：score (整数)、feedback (详细建议，不少于 100 字) 和 category (技术分类，如 Java、数据库、算法、系统设计)。不要输出任何多余的解释文字。";
    }

    private static String requestDeepSeek(String payload, String model, boolean stream, StreamTokenHandler tokenHandler) throws IOException {
        HttpURLConnection connection = null;
        long startNanos = System.nanoTime();
        TokenUsage usage = TokenUsage.empty();
        int status = -1;
        try {
            URL url = new URL(DEEPSEEK_API_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            connection.setReadTimeout(MODEL_REASONER.equals(model) ? READ_TIMEOUT_REASONER_MS : READ_TIMEOUT_CHAT_MS);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + DEEPSEEK_API_KEY);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] data = payload.getBytes(StandardCharsets.UTF_8);
                os.write(data);
            }

            status = connection.getResponseCode();
            InputStream responseStream = status >= 200 && status < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            if (stream) {
                String streamed = readStreamedContent(responseStream, status, tokenHandler);
                logApiCall(model, true, status, startNanos, usage);
                return streamed;
            }

            String responseBody = readAll(responseStream);
            usage = extractUsageFromResponse(responseBody);
            if (status < 200 || status >= 300) {
                throw new IOException("DeepSeek API error: HTTP " + status + ", body=" + responseBody);
            }
            logApiCall(model, false, status, startNanos, usage);
            return responseBody;
        } catch (IOException ex) {
            logApiCall(model, stream, status, startNanos, usage);
            throw ex;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String readStreamedContent(InputStream inputStream, int status, StreamTokenHandler tokenHandler) throws IOException {
        if (status < 200 || status >= 300) {
            String body = readAll(inputStream);
            throw new IOException("DeepSeek API error: HTTP " + status + ", body=" + body);
        }

        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data:")) {
                    continue;
                }

                String data = line.substring(5).trim();
                if (data.isEmpty() || "[DONE]".equals(data)) {
                    continue;
                }

                try {
                    JsonObject root = JsonParser.parseString(data).getAsJsonObject();
                    JsonArray choices = root.getAsJsonArray("choices");
                    if (choices == null || choices.size() == 0) {
                        continue;
                    }

                    JsonObject choice0 = choices.get(0).getAsJsonObject();
                    JsonObject delta = choice0.getAsJsonObject("delta");
                    if (delta == null) {
                        continue;
                    }

                    String chunk = "";
                    if (delta.has("content") && !delta.get("content").isJsonNull()) {
                        chunk = delta.get("content").getAsString();
                    } else if (delta.has("reasoning_content") && !delta.get("reasoning_content").isJsonNull()) {
                        chunk = delta.get("reasoning_content").getAsString();
                    }

                    if (chunk != null && !chunk.isEmpty()) {
                        contentBuilder.append(chunk);
                        if (tokenHandler != null) {
                            tokenHandler.onToken(chunk);
                        }
                    }
                } catch (Exception ignored) {
                    // 忽略单个事件解析失败，继续读取后续流。
                }
            }
        }

        return contentBuilder.toString();
    }

    private static String normalizeApiUrl(String configuredUrl) {
        if (configuredUrl == null || configuredUrl.isEmpty()) {
            return DEFAULT_API_BASE_URL + "/chat/completions";
        }

        String url = configuredUrl;
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        if (url.endsWith("/chat/completions") || url.endsWith("/v1/chat/completions")) {
            return url;
        }

        if (url.endsWith("/v1")) {
            return url + "/chat/completions";
        }

        return url + "/chat/completions";
    }

    private static String pickModel(String requestedModel) {
        String model = requestedModel == null ? "" : requestedModel.trim();
        if (MODEL_CHAT.equals(model) || MODEL_REASONER.equals(model)) {
            return model;
        }

        if (DEEPSEEK_MODEL != null) {
            String configuredModel = DEEPSEEK_MODEL.trim();
            if (MODEL_CHAT.equals(configuredModel) || MODEL_REASONER.equals(configuredModel)) {
                return configuredModel;
            }
        }

        return DEFAULT_MODEL;
    }

    private static AIResult parseAIResultFromResponse(String responseBody) {
        JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
        TokenUsage usage = extractUsage(root);
        JsonArray choices = root.getAsJsonArray("choices");
        if (choices == null || choices.size() == 0) {
            return null;
        }

        JsonObject choice0 = choices.get(0).getAsJsonObject();
        JsonObject message = choice0.getAsJsonObject("message");
        if (message == null || !message.has("content")) {
            return null;
        }

        AIResult parsed = parseAIResultFromContent(message.get("content").getAsString());
        if (parsed == null) {
            return null;
        }
        return parsed.withTokenUsage(usage.promptTokens, usage.completionTokens);
    }

    private static String readAssistantContent(String responseBody) {
        JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray choices = root.getAsJsonArray("choices");
        if (choices == null || choices.size() == 0) {
            return "";
        }

        JsonObject choice0 = choices.get(0).getAsJsonObject();
        JsonObject message = choice0.getAsJsonObject("message");
        if (message == null || !message.has("content")) {
            return "";
        }

        return message.get("content").getAsString();
    }

    private static AIResult parseAIResultFromContent(String content) {
        try {
            String jsonText = extractJson(content);

            JsonObject result = JsonParser.parseString(jsonText).getAsJsonObject();
            int score = 0;
            if (result.has("score")) {
                try {
                    score = result.get("score").getAsInt();
                } catch (Exception ignored) {
                    score = 0;
                }
            }
            score = Math.max(0, Math.min(100, score));

            String feedback = result.has("feedback") ? result.get("feedback").getAsString() : "";
            if (feedback == null || feedback.trim().isEmpty()) {
                feedback = "AI 已返回结果，但反馈内容为空。建议你补充回答中的核心思路、复杂度分析和边界条件处理，并结合一个具体示例展示你的解决路径。";
            }

            String category = result.has("category") ? result.get("category").getAsString() : "";
            if (category == null || category.trim().isEmpty()) {
                category = "综合能力";
            }

            String followUpQuestion = result.has("followUpQuestion") ? result.get("followUpQuestion").getAsString() : "";
            if (followUpQuestion == null) {
                followUpQuestion = "";
            }

            return new AIResult(score, feedback, category.trim(), followUpQuestion.trim());
        } catch (Exception ex) {
            // 当返回不是规范 JSON 时，回退到正则提取分数与建议。
            AIResult regexParsed = parseAIResultByRegex(content);
            if (regexParsed != null) {
                return regexParsed;
            }
            return null;
        }
    }

    private static AIResult parseAIResultByRegex(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }

        int score = -1;
        Matcher scoreMatcher = SCORE_PATTERN.matcher(content);
        if (scoreMatcher.find()) {
            for (int i = 1; i <= scoreMatcher.groupCount(); i++) {
                String g = scoreMatcher.group(i);
                if (g != null && !g.trim().isEmpty()) {
                    try {
                        score = Integer.parseInt(g.trim());
                        break;
                    } catch (Exception ignored) {
                        score = -1;
                    }
                }
            }
        }

        String feedback = null;
        Matcher feedbackJsonMatcher = FEEDBACK_JSON_PATTERN.matcher(content);
        if (feedbackJsonMatcher.find()) {
            feedback = feedbackJsonMatcher.group(1);
        }
        if (feedback == null || feedback.trim().isEmpty()) {
            Matcher feedbackTextMatcher = FEEDBACK_TEXT_PATTERN.matcher(content);
            if (feedbackTextMatcher.find()) {
                feedback = feedbackTextMatcher.group(1);
            }
        }

        String category = null;
        Matcher categoryJsonMatcher = CATEGORY_JSON_PATTERN.matcher(content);
        if (categoryJsonMatcher.find()) {
            category = categoryJsonMatcher.group(1);
        }
        if (category == null || category.trim().isEmpty()) {
            Matcher categoryTextMatcher = CATEGORY_TEXT_PATTERN.matcher(content);
            if (categoryTextMatcher.find()) {
                category = categoryTextMatcher.group(1);
            }
        }

        if (score < 0 && (feedback == null || feedback.trim().isEmpty()) && (category == null || category.trim().isEmpty())) {
            return null;
        }

        int finalScore = score < 0 ? 65 : Math.max(0, Math.min(100, score));
        String finalFeedback = (feedback == null || feedback.trim().isEmpty())
                ? "AI 返回内容格式异常，已根据可解析信息给出默认建议。建议你补充解题思路、复杂度分析和边界条件。"
                : feedback.trim();
        String finalCategory = (category == null || category.trim().isEmpty()) ? "综合能力" : category.trim();
        String finalFollowUp = "";

        Matcher followUpMatcher = FOLLOW_UP_JSON_PATTERN.matcher(content);
        if (followUpMatcher.find()) {
            finalFollowUp = followUpMatcher.group(1);
        }
        if (finalFollowUp == null || finalFollowUp.trim().isEmpty()) {
            Matcher followUpTextMatcher = FOLLOW_UP_TEXT_PATTERN.matcher(content);
            if (followUpTextMatcher.find()) {
                finalFollowUp = followUpTextMatcher.group(1);
            }
        }

        return new AIResult(finalScore, finalFeedback, finalCategory, finalFollowUp == null ? "" : finalFollowUp.trim());
    }

    private static String extractJson(String text) {
        String cleaned = text == null ? "" : text.trim();

        if (cleaned.startsWith("```")) {
            int firstLineEnd = cleaned.indexOf('\n');
            if (firstLineEnd > 0) {
                cleaned = cleaned.substring(firstLineEnd + 1);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
            }
        }

        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return cleaned.substring(start, end + 1);
        }
        return cleaned;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            String candidate = safeTrim(value);
            if (!candidate.isEmpty()) {
                return candidate;
            }
        }
        return "";
    }

    private static String readAll(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private static TokenUsage extractUsageFromResponse(String responseBody) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return TokenUsage.empty();
        }
        try {
            JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
            return extractUsage(root);
        } catch (Exception ex) {
            return TokenUsage.empty();
        }
    }

    private static TokenUsage extractUsage(JsonObject root) {
        if (root == null || !root.has("usage") || root.get("usage").isJsonNull()) {
            return TokenUsage.empty();
        }
        try {
            JsonObject usage = root.getAsJsonObject("usage");
            int promptTokens = usage != null && usage.has("prompt_tokens") && !usage.get("prompt_tokens").isJsonNull()
                    ? usage.get("prompt_tokens").getAsInt() : 0;
            int completionTokens = usage != null && usage.has("completion_tokens") && !usage.get("completion_tokens").isJsonNull()
                    ? usage.get("completion_tokens").getAsInt() : 0;
            return new TokenUsage(promptTokens, completionTokens);
        } catch (Exception ex) {
            return TokenUsage.empty();
        }
    }

    private static void logApiCall(String model, boolean stream, int status, long startNanos, TokenUsage usage) {
        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;
        TokenUsage finalUsage = usage == null ? TokenUsage.empty() : usage;
        LOGGER.log(Level.INFO,
                "DeepSeek call finished. model={0}, stream={1}, status={2}, durationMs={3}, tokens(total={4}, prompt={5}, completion={6})",
                new Object[]{model, stream, status, elapsedMs, finalUsage.totalTokens, finalUsage.promptTokens, finalUsage.completionTokens});
    }

    private static AIResult fallbackResult(String reason) {
        String feedback = "AI 暂时离线（" + reason + "），系统已返回兜底评价。你的回答有一定结构，但建议进一步强化三点："
                + "第一，先明确问题定义和目标，再给出核心思路；第二，补充复杂度分析与关键边界条件；"
                + "第三，结合一个输入示例逐步推导结果，体现工程化表达与可验证性。";
        return new AIResult(65, feedback, "综合能力");
    }

    @FunctionalInterface
    public interface StreamTokenHandler {
        void onToken(String token);
    }

    /**
     * AI 评测结果对象，封装分数与反馈建议。
     * Author: 张雨泽
     */
    public static final class AIResult {
        private final int score;
        private final String feedback;
        private final String category;
        private final String followUpQuestion;
        private final int promptTokens;
        private final int completionTokens;

        public AIResult(int score, String feedback, String category) {
            this(score, feedback, category, "", 0, 0);
        }

        public AIResult(int score, String feedback, String category, String followUpQuestion) {
            this(score, feedback, category, followUpQuestion, 0, 0);
        }

        public AIResult(int score, String feedback, String category, String followUpQuestion,
                        int promptTokens, int completionTokens) {
            this.score = score;
            this.feedback = feedback;
            this.category = category;
            this.followUpQuestion = followUpQuestion;
            this.promptTokens = Math.max(0, promptTokens);
            this.completionTokens = Math.max(0, completionTokens);
        }

        public int getScore() {
            return score;
        }

        public String getFeedback() {
            return feedback;
        }

        public String getCategory() {
            return category;
        }

        public String getFollowUpQuestion() {
            return followUpQuestion;
        }

        public int getPromptTokens() {
            return promptTokens;
        }

        public int getCompletionTokens() {
            return completionTokens;
        }

        public int getTotalTokens() {
            return promptTokens + completionTokens;
        }

        public AIResult withTokenUsage(int promptTokens, int completionTokens) {
            return new AIResult(score, feedback, category, followUpQuestion, promptTokens, completionTokens);
        }
    }

    private static final class TokenUsage {
        private final int promptTokens;
        private final int completionTokens;
        private final int totalTokens;

        private TokenUsage(int promptTokens, int completionTokens) {
            this.promptTokens = Math.max(0, promptTokens);
            this.completionTokens = Math.max(0, completionTokens);
            this.totalTokens = this.promptTokens + this.completionTokens;
        }

        private static TokenUsage empty() {
            return new TokenUsage(0, 0);
        }
    }
}