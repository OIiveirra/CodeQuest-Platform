package com.codequest.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.codequest.util.AIService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 后台 AI 智库辅助接口，按关键词生成题目草稿 JSON。
 */
@WebServlet("/admin/ai")
public class AdminAiServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Gson GSON = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String keyword = trim(req.getParameter("keyword"));
        String model = trim(req.getParameter("model"));
        if (keyword.isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "关键词不能为空。");
            return;
        }

        String prompt = buildPrompt(keyword);
        String raw = AIService.getRawContentFromPrompt(prompt, model);
        if (raw == null || raw.trim().isEmpty()) {
            writeError(resp, HttpServletResponse.SC_BAD_GATEWAY, "AI 没有返回有效内容。");
            return;
        }

        String jsonText = extractJson(raw);
        try {
            JsonObject data = JsonParser.parseString(jsonText).getAsJsonObject();
            JsonObject result = new JsonObject();
            result.addProperty("success", true);
            result.add("data", data);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(GSON.toJson(result));
        } catch (Exception ex) {
            writeError(resp, HttpServletResponse.SC_BAD_GATEWAY, "AI 返回内容不是合法 JSON，请重试。", raw);
        }
    }

    private String buildPrompt(String keyword) {
        return "你是 CodeQuest 的后台题库助手。请根据关键词生成一条适合面试训练平台的题目草稿，关键词是：" + keyword
                + "。请严格只返回 JSON，不要返回任何解释、Markdown 或代码块。JSON 必须包含以下字段："
                + "title（字符串，题目标题）、type（整数，根据题目类型选择：1=单选题、2=填空题、3=代码题、4=简答题、5=设计题）、difficulty（整数，1-5）、content（字符串，题目内容）、standardAnswer（字符串，标准答案）、tags（字符串，分类标签）。"
                + "重要：type 字段必须根据题目内容智能识别，选择最合适的分类。"
                + "请确保内容专业、简洁、可直接用于题库。";
    }

    private String extractJson(String raw) {
        String text = raw == null ? "" : raw.trim();
        if (text.startsWith("```")) {
            int firstLineEnd = text.indexOf('\n');
            if (firstLineEnd > 0) {
                text = text.substring(firstLineEnd + 1);
            }
            if (text.endsWith("```")) {
                text = text.substring(0, text.length() - 3).trim();
            }
        }

        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        writeError(resp, status, message, null);
    }

    private void writeError(HttpServletResponse resp, int status, String message, String raw) throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("success", false);
        root.addProperty("message", message);
        if (raw != null && !raw.trim().isEmpty()) {
            root.addProperty("raw", raw);
        }
        resp.setStatus(status);
        resp.getWriter().write(GSON.toJson(root));
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
