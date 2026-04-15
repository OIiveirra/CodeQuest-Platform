package com.codequest.util;

import java.util.Map;

/**
 * Prompt 模板占位符替换工具。
 */
public final class PromptTemplateUtils {

    private PromptTemplateUtils() {
    }

    public static String applyTemplate(String template, String userAnswer, String standardAnswer) {
        return applyTemplate(template, null, userAnswer, standardAnswer);
    }

    public static String applyTemplate(String template, String questionContent, String userAnswer, String standardAnswer) {
        if (template == null) {
            return "";
        }

        String result = template;
        result = result.replace("${userAnswer}", safe(userAnswer));
        result = result.replace("${standardAnswer}", safe(standardAnswer));
        result = result.replace("${questionContent}", safe(questionContent));
        return result;
    }

    public static String applyTemplate(String template, Map<String, String> variables) {
        if (template == null) {
            return "";
        }

        String result = template;
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String key = entry.getKey();
                if (key == null || key.trim().isEmpty()) {
                    continue;
                }
                result = result.replace("${" + key + "}", safe(entry.getValue()));
            }
        }
        return result;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
