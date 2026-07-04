package com.example.minicodingagent.tool.impl;

import com.example.minicodingagent.tool.Tool;
import com.example.minicodingagent.tool.ToolResult;
import com.example.minicodingagent.tool.WorkspacePath;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class CreateFileTool implements Tool {
    private final Path workspaceRoot;

    public CreateFileTool(Path workspaceRoot) {
        this.workspaceRoot = workspaceRoot;
    }

    public String name() {
        return "create_file";
    }

    public String description() {
        return "Create a new text file inside the workspace. Fails if the file already exists.";
    }

    public String parameterSchema() {
        return "{\"type\":\"object\",\"properties\":{\"path\":{\"type\":\"string\",\"description\":\"New file path inside workspace\"},\"content\":{\"type\":\"string\",\"description\":\"File content\"}},\"required\":[\"path\",\"content\"]}";
    }

    public ToolResult execute(Map<String, Object> parameters) {
        String path = stringParam(parameters, "path");
        String content = stringParam(parameters, "content");
        if (path == null || path.trim().isEmpty()) {
            return ToolResult.failure("Missing required parameter: path");
        }
        if (content == null) {
            return ToolResult.failure("Missing required parameter: content");
        }

        try {
            Path file = WorkspacePath.resolve(workspaceRoot, path);
            WorkspacePath.checkWritablePath(workspaceRoot, file);
            if (Files.exists(file)) {
                return ToolResult.failure("File already exists: " + path);
            }
            Path parent = file.getParent();
            if (parent == null || !Files.exists(parent) || !Files.isDirectory(parent)) {
                return ToolResult.failure("Parent directory does not exist: " + path);
            }

            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            System.out.println("[write] create_file path: " + workspaceRoot.relativize(file));
            System.out.println("[write] summary: create new file, bytes=" + bytes.length);
            Files.write(file, bytes);
            return ToolResult.success("Created file: " + workspaceRoot.relativize(file));
        } catch (IllegalArgumentException e) {
            return ToolResult.failure(e.getMessage());
        } catch (IOException e) {
            return ToolResult.failure("Failed to create file: " + e.getMessage());
        }
    }

    private static String stringParam(Map<String, Object> parameters, String name) {
        Object value = parameters == null ? null : parameters.get(name);
        return value == null ? null : String.valueOf(value);
    }
}
