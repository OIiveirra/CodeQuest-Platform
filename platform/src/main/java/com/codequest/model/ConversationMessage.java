package com.codequest.model;

import java.io.Serializable;

/**
 * OpenAI / DeepSeek 消息对象。
 */
public class ConversationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String role;
    private String content;

    public ConversationMessage() {
    }

    public ConversationMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
