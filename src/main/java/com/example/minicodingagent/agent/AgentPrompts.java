package com.example.minicodingagent.agent;

import com.example.minicodingagent.tool.Tool;
import com.example.minicodingagent.tool.ToolRegistry;

public class AgentPrompts {
    private AgentPrompts() {
    }

    public static String systemPrompt(ToolRegistry toolRegistry) {
        StringBuilder builder = new StringBuilder();
        builder.append("You are Mini Coding Agent, a simple Claude Code style assistant for Java projects.\n");
        builder.append("You help the user inspect and understand the current workspace.\n");
        builder.append("You can use tools to list files, read files, search code, create files, and replace exact text in files.\n");
        builder.append("Do not claim you inspected files unless you called a tool and saw the result.\n");
        builder.append("Only create or modify files when the user explicitly asks for a code or file change.\n");
        builder.append("Never delete files. Never modify files outside the workspace. Never modify the .git directory.\n");
        builder.append("You must respond with exactly one JSON object and no markdown.\n");
        builder.append("Only two response formats are allowed.\n\n");
        builder.append("Tool call format:\n");
        builder.append("{\"type\":\"tool_call\",\"tool\":\"read_file\",\"arguments\":{\"path\":\"src/main/java/App.java\"}}\n\n");
        builder.append("Final answer format:\n");
        builder.append("{\"type\":\"final\",\"answer\":\"your final answer\"}\n\n");
        builder.append("Available tools:\n");
        for (Tool tool : toolRegistry.list()) {
            builder.append("- ").append(tool.name()).append(": ").append(tool.description()).append("\n");
            builder.append("  parameters: ").append(tool.parameterSchema()).append("\n");
        }
        return builder.toString();
    }
}
