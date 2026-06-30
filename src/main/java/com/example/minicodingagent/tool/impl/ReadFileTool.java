package com.example.minicodingagent.tool.impl;

import com.example.minicodingagent.tool.Tool;
import com.example.minicodingagent.tool.ToolResult;
import com.example.minicodingagent.tool.WorkspacePath;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ReadFileTool implements Tool {
    private final Path workspaceRoot;

    public ReadFileTool(Path workspaceRoot) {
        this.workspaceRoot = workspaceRoot;
    }

    public String name() {
        return "read_file";
    }

    public String description() {
        return "Read a text file inside the workspace.";
    }

    public String parameterSchema() {
        return "{\"type\":\"object\",\"properties\":{\"path\":{\"type\":\"string\",\"description\":\"File path inside workspace\"}},\"required\":[\"path\"]}";
    }

    public ToolResult execute(Map<String, Object> parameters) {
        String path = stringParam(parameters, "path");
        if (path == null || path.trim().isEmpty()) {
            return ToolResult.failure("Missing required parameter: path");
        }

        try {
            Path file = WorkspacePath.resolve(workspaceRoot, path);
            if (!Files.exists(file)) {
                return ToolResult.failure("File does not exist: " + path);
            }
            WorkspacePath.checkInsideWorkspace(workspaceRoot, file);
            if (!Files.isRegularFile(file)) {
                return ToolResult.failure("Path is not a file: " + path);
            }
            return ToolResult.success(new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            return ToolResult.failure(e.getMessage());
        } catch (IOException e) {
            return ToolResult.failure("Failed to read file: " + e.getMessage());
        }
    }

    private static String stringParam(Map<String, Object> parameters, String name) {
        Object value = parameters == null ? null : parameters.get(name);
        return value == null ? null : String.valueOf(value);
    }
}
