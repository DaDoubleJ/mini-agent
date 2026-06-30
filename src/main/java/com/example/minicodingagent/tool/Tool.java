package com.example.minicodingagent.tool;

import java.util.Map;

public interface Tool {
    String name();

    String description();

    String parameterSchema();

    ToolResult execute(Map<String, Object> parameters);
}
