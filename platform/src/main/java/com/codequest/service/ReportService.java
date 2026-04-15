package com.codequest.service;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.codequest.dao.EvaluationDAO;
import com.codequest.dao.ReportDAO;
import com.codequest.model.EvaluationRecord;
import com.codequest.model.WeeklyReport;
import com.codequest.util.AIService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 面试周报服务：聚合近 7 天测评数据并生成职业发展教练周报。
 */
public class ReportService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("MM-dd HH:mm");
    private final EvaluationDAO evaluationDAO = new EvaluationDAO();
    private final ReportDAO reportDAO = new ReportDAO();

    public WeeklyReport generateWeeklyReport(long userId, String username) throws SQLException {
        Map<String, Timestamp> range = getCurrentWeekRange();
        Timestamp periodStart = range.get("start");
        Timestamp periodEnd = range.get("end");

        WeeklyReport existing = reportDAO.findByUserAndPeriod(userId, periodStart, periodEnd);
        if (existing != null) {
            return existing;
        }

        List<EvaluationRecord> records = evaluationDAO.findEvaluationsByUserIdWithinDays(userId, 7);
        String digest = buildDigest(records);
        String prompt = buildWeeklyPrompt(username, periodStart, periodEnd, digest);
        String aiContent = AIService.getRawContentFromPrompt(prompt, "deepseek-reasoner");
        aiContent = normalizeWeeklyContent(aiContent);
        if (aiContent == null || aiContent.trim().isEmpty()) {
            aiContent = "本周样本不足或模型暂不可用。建议继续完成至少 5 次完整会话测评，以便生成更具针对性的周报。";
        }

        WeeklyReport report = new WeeklyReport();
        report.setUserId(userId);
        report.setPeriodStart(periodStart);
        report.setPeriodEnd(periodEnd);
        report.setTitle("面试周报（职业发展教练）");
        report.setSummary(buildSummary(records));
        report.setContent(aiContent);
        reportDAO.insert(report);

        WeeklyReport persisted = reportDAO.findByUserAndPeriod(userId, periodStart, periodEnd);
        return persisted == null ? report : persisted;
    }

    public List<WeeklyReport> listRecentReports(long userId, int limit) throws SQLException {
        return reportDAO.findRecentByUserId(userId, limit);
    }

    private String buildDigest(List<EvaluationRecord> records) {
        if (records == null || records.isEmpty()) {
            return "近 7 天暂无有效测评记录。";
        }

        StringBuilder builder = new StringBuilder();
        int idx = 1;
        for (EvaluationRecord r : records) {
            if (r == null) {
                continue;
            }
            builder.append(idx++).append(") ")
                    .append("session=").append(safe(r.getSessionId())).append("; ")
                    .append("time=").append(r.getCreatedAt() == null ? "" : TIME_FORMATTER.format(r.getCreatedAt().toLocalDateTime())).append("; ")
                    .append("title=").append(safe(r.getQuestionTitle())).append("; ")
                    .append("score=").append(r.getScore() == null ? "" : r.getScore()).append("; ")
                    .append("category=").append(safe(r.getCategory())).append("; ")
                    .append("feedback=").append(safe(r.getFeedback()))
                    .append("\n");
        }
        return builder.toString().trim();
    }

    private String buildWeeklyPrompt(String username, Timestamp periodStart, Timestamp periodEnd, String digest) {
        String start = periodStart == null ? "" : periodStart.toString();
        String end = periodEnd == null ? "" : periodEnd.toString();
        return "你是资深职业发展教练，请基于该用户近 7 天的技术面试测评摘要生成周报。"
                + "用户：" + safe(username)
                + "；统计区间：" + start + " 到 " + end + "。"
                + "请只输出 Markdown 正文，不要输出 JSON、不要输出代码块、不要输出前后缀说明。\n"
                + "必须包含以下 3 个板块：\n"
                + "1) 本周能力涨幅（按技术维度分析，指出提升证据）\n"
                + "2) 高频错点（至少 3 条，给出错误模式）\n"
                + "3) 下周复习路线图（按天给出可执行计划）\n"
                + "另外请在文末增加“行动优先级 Top 3”。\n\n"
                + "测评摘要如下：\n" + digest;
    }

    private String buildSummary(List<EvaluationRecord> records) {
        if (records == null || records.isEmpty()) {
            return "近 7 天暂无有效测评数据。";
        }
        int total = 0;
        int count = 0;
        int lowCount = 0;
        for (EvaluationRecord r : records) {
            if (r == null || r.getScore() == null) {
                continue;
            }
            total += r.getScore();
            count += 1;
            if (r.getScore() < 60) {
                lowCount += 1;
            }
        }
        int avg = count == 0 ? 0 : Math.round(total / (float) count);
        return "近 7 天共 " + records.size() + " 条测评记录，均分 " + avg + "，低分（<60）" + lowCount + " 条。";
    }

    private Map<String, Timestamp> getCurrentWeekRange() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);
        LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(today, LocalTime.MAX);

        Map<String, Timestamp> map = new LinkedHashMap<>();
        map.put("start", Timestamp.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant()));
        map.put("end", Timestamp.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant()));
        return map;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeWeeklyContent(String rawContent) {
        if (rawContent == null) {
            return "";
        }

        String text = rawContent.trim();
        if (text.isEmpty()) {
            return "";
        }

        text = stripCodeFence(text);
        text = stripLeadingJsonLabel(text);
        text = unwrapJsonPrimitive(text);
        text = extractReportBodyFromJson(text);
        text = stripCodeFence(text);
        text = text.replace("\\r\\n", "\n").replace("\\n", "\n").replace("\\t", "\t").trim();

        if (text.startsWith("\"") && text.endsWith("\"") && text.length() > 1) {
            text = text.substring(1, text.length() - 1);
        }

        return text.trim();
    }

    private String stripCodeFence(String text) {
        String result = text == null ? "" : text.trim();
        if (result.startsWith("```")) {
            int firstBreak = result.indexOf('\n');
            if (firstBreak > 0) {
                result = result.substring(firstBreak + 1).trim();
            } else {
                result = result.substring(3).trim();
            }
        }

        int closingFence = result.lastIndexOf("```");
        if (closingFence >= 0) {
            result = result.substring(0, closingFence).trim();
        }
        return result;
    }

    private String stripLeadingJsonLabel(String text) {
        if (text == null) {
            return "";
        }
        String result = text.trim();
        if (result.regionMatches(true, 0, "json", 0, 4)) {
            result = result.substring(4).trim();
            if (result.startsWith(":")) {
                result = result.substring(1).trim();
            }
        }
        return result;
    }

    private String unwrapJsonPrimitive(String text) {
        try {
            JsonElement element = JsonParser.parseString(text);
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                return element.getAsString();
            }
        } catch (RuntimeException ignored) {
            // 不是 JSON 字符串时保持原文。
        }
        return text;
    }

    private String extractReportBodyFromJson(String text) {
        try {
            JsonElement element = JsonParser.parseString(text);
            if (!element.isJsonObject()) {
                return text;
            }

            JsonObject root = element.getAsJsonObject();
            String output = readStringField(root, "output");
            if (!output.isEmpty()) {
                return output;
            }

            String content = readStringField(root, "content");
            if (!content.isEmpty()) {
                return content;
            }

            String summary = readStringField(root, "summary");
            if (!summary.isEmpty()) {
                return summary;
            }

            if (root.has("thoughts") && root.get("thoughts").isJsonObject()) {
                JsonObject thoughts = root.getAsJsonObject("thoughts");
                String thoughtOutput = readStringField(thoughts, "output");
                if (!thoughtOutput.isEmpty()) {
                    return thoughtOutput;
                }
                String analysis = readStringField(thoughts, "analysis");
                if (!analysis.isEmpty()) {
                    return analysis;
                }
            }
        } catch (RuntimeException ignored) {
            // 非 JSON 文本直接返回。
        }
        return text;
    }

    private String readStringField(JsonObject root, String fieldName) {
        if (root == null || fieldName == null || !root.has(fieldName) || root.get(fieldName).isJsonNull()) {
            return "";
        }
        try {
            return root.get(fieldName).getAsString().trim();
        } catch (Exception ignored) {
            return "";
        }
    }
}
