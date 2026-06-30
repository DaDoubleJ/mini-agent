package com.example.minicodingagent.tool;

public class ToolResult {
    private boolean success;
    private String content;
    private String errorMessage;

    public ToolResult() {
    }

    public ToolResult(boolean success, String content, String errorMessage) {
        this.success = success;
        this.content = content;
        this.errorMessage = errorMessage;
    }

    public static ToolResult success(String content) {
        return new ToolResult(true, content, null);
    }

    public static ToolResult failure(String errorMessage) {
        return new ToolResult(false, null, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
