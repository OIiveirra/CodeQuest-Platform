package com.codequest.model;

import java.io.Serializable;

/**
 * Prompt 模板实体，对应 sys_prompt_template 表。
 */
public class PromptTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String templateName;
    private String content;
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
