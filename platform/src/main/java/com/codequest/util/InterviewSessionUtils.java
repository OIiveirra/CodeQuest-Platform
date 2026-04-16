package com.codequest.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import com.codequest.model.ConversationMessage;

/**
 * 面试会话上下文管理工具。
 */
public final class InterviewSessionUtils {

    public static final String SESSION_MESSAGES_KEY = "interviewMessages";
    public static final String SESSION_QUESTION_ID_KEY = "interviewQuestionId";
    public static final String SESSION_FOLLOW_UP_KEY = "interviewFollowUpQuestion";
    public static final String SESSION_INTERVIEW_ID_KEY = "interviewSessionId";

    private InterviewSessionUtils() {
    }

    @SuppressWarnings("unchecked")
    public static List<ConversationMessage> getMessages(HttpSession session) {
        // 读取会话中的消息历史。
        if (session == null) {
            return new ArrayList<>();
        }
        Object value = session.getAttribute(SESSION_MESSAGES_KEY);
        if (value instanceof List<?>) {
            List<?> list = (List<?>) value;
            List<ConversationMessage> result = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof ConversationMessage) {
                    result.add((ConversationMessage) item);
                }
            }
            return result;
        }
        return new ArrayList<>();
    }

    public static void saveMessages(HttpSession session, List<ConversationMessage> messages) {
        // 保存会话中的消息历史。
        if (session == null) {
            return;
        }
        session.setAttribute(SESSION_MESSAGES_KEY, messages == null ? new ArrayList<ConversationMessage>() : new ArrayList<>(messages));
    }

    public static void clear(HttpSession session) {
        // 清理会话中的面试上下文。
        if (session == null) {
            return;
        }
        session.removeAttribute(SESSION_MESSAGES_KEY);
        session.removeAttribute(SESSION_QUESTION_ID_KEY);
        session.removeAttribute(SESSION_FOLLOW_UP_KEY);
        session.removeAttribute(SESSION_INTERVIEW_ID_KEY);
    }

    public static String ensureInterviewSessionId(HttpSession session) {
        // 初始化面试会话 ID。
        if (session == null) {
            return null;
        }
        String sessionId = getInterviewSessionId(session);
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = UUID.randomUUID().toString();
            session.setAttribute(SESSION_INTERVIEW_ID_KEY, sessionId);
        }
        return sessionId;
    }

    public static String getInterviewSessionId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(SESSION_INTERVIEW_ID_KEY);
        return value == null ? null : String.valueOf(value);
    }

    public static void setInterviewSessionId(HttpSession session, String interviewSessionId) {
        if (session == null) {
            return;
        }
        if (interviewSessionId == null || interviewSessionId.trim().isEmpty()) {
            session.removeAttribute(SESSION_INTERVIEW_ID_KEY);
            return;
        }
        session.setAttribute(SESSION_INTERVIEW_ID_KEY, interviewSessionId.trim());
    }

    public static void setQuestionId(HttpSession session, Long questionId) {
        // 记录当前题目 ID。
        if (session != null) {
            session.setAttribute(SESSION_QUESTION_ID_KEY, questionId);
        }
    }

    public static Long getQuestionId(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(SESSION_QUESTION_ID_KEY);
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    public static void setFollowUpQuestion(HttpSession session, String followUpQuestion) {
        // 记录当前追问内容。
        if (session != null) {
            session.setAttribute(SESSION_FOLLOW_UP_KEY, followUpQuestion);
        }
    }

    public static String getFollowUpQuestion(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(SESSION_FOLLOW_UP_KEY);
        return value == null ? null : String.valueOf(value);
    }

    public static List<ConversationMessage> copyMessages(List<ConversationMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return new ArrayList<>();
        }
        List<ConversationMessage> copy = new ArrayList<>();
        for (ConversationMessage message : messages) {
            if (message != null) {
                copy.add(new ConversationMessage(message.getRole(), message.getContent()));
            }
        }
        return copy;
    }
}
