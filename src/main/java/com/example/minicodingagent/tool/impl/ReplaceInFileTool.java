package com.example.minicodingagent.tool.impl;

import com.example.minicodingagent.tool.Tool;
import com.example.minicodingagent.tool.ToolResult;
import com.example.minicodingagent.tool.WorkspacePath;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ReplaceInFileTool implements Tool {
    private final Path workspaceRoot;

    public ReplaceInFileTool(Path workspaceRoot) {
        this.workspaceRoot = workspaceRoot;
    }

    public String name() {
        return "replace_in_file";
    }

    public String description() {
        return "Replace exact text in an existing text file inside the workspace.";
    }

    public String parameterSchema() {
        return "{\"type\":\"object\",\"properties\":{\"path\":{\"type\":\"string\",\"description\":\"File path inside workspace\"},\"oldText\":{\"type\":\"string\",\"description\":\"Exact text to replace\"},\"newText\":{\"type\":\"string\",\"description\":\"Replacement text\"}},\"required\":[\"path\",\"oldText\",\"newText\"]}";
    }

    public ToolResult execute(Map<String, Object> parameters) {
        String path = stringParam(parameters, "path");
        String oldText = stringParam(parameters, "oldText");
        String newText = stringParam(parameters, "newText");
        if (path == null || path.trim().isEmpty()) {
            return ToolResult.failure("Missing required parameter: path");
        }
        if (oldText == null || oldText.isEmpty()) {
            return ToolResult.failure("Missing required parameter: oldText");
        }
        if (newText == null) {
            return ToolResult.failure("Missing required parameter: newText");
        }

        try {
            Path file = WorkspacePath.resolve(workspaceRoot, path);
            if (!Files.exists(file)) {
                return ToolResult.failure("File does not exist: " + path);
            }
            WorkspacePath.checkWritablePath(workspaceRoot, file);
            WorkspacePath.checkInsideWorkspace(workspaceRoot, file);
            if (!Files.isRegularFile(file)) {
                return ToolResult.failure("Path is not a file: " + path);
            }

            String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            if (!content.contains(oldText)) {
                return ToolResult.failure("oldText was not found in file: " + path);
            }

            int occurrences = countOccurrences(content, oldText);
            String updatedContent = content.replace(oldText, newText);
            System.out.println("[write] replace_in_file path: " + workspaceRoot.relativize(file));
            System.out.println("[write] summary: occurrences=" + occurrences
                    + ", oldTextLength=" + oldText.length()
                    + ", newTextLength=" + newText.length());
            Files.write(file, updatedContent.getBytes(StandardCharsets.UTF_8));
            return ToolResult.success("Replaced " + occurrences + " occurrence(s) in file: " + workspaceRoot.relativize(file));
        } catch (IllegalArgumentException e) {
            return ToolResult.failure(e.getMessage());
        } catch (IOException e) {
            return ToolResult.failure("Failed to replace text in file: " + e.getMessage());
        }
    }

    private static int countOccurrences(String content, String oldText) {
        int count = 0;
        int index = 0;
        while (true) {
            index = content.indexOf(oldText, index);
            if (index < 0) {
                return count;
            }
            count++;
            index += oldText.length();
        }
    }

    private static String stringParam(Map<String, Object> parameters, String name) {
        Object value = parameters == null ? null : parameters.get(name);
        return value == null ? null : String.valueOf(value);
    }
}
