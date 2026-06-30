package com.example.minicodingagent.tool.impl;

import com.example.minicodingagent.tool.Tool;
import com.example.minicodingagent.tool.ToolResult;
import com.example.minicodingagent.tool.WorkspacePath;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class SearchCodeTool implements Tool {
    private static final int MAX_MATCHES = 50;

    private final Path workspaceRoot;

    public SearchCodeTool(Path workspaceRoot) {
        this.workspaceRoot = workspaceRoot;
    }

    public String name() {
        return "search_code";
    }

    public String description() {
        return "Search keyword in text/code files inside the workspace.";
    }

    public String parameterSchema() {
        return "{\"type\":\"object\",\"properties\":{\"keyword\":{\"type\":\"string\",\"description\":\"Keyword to search\"},\"path\":{\"type\":\"string\",\"description\":\"Path inside workspace, default is .\"}},\"required\":[\"keyword\"]}";
    }

    public ToolResult execute(Map<String, Object> parameters) {
        String keyword = stringParam(parameters, "keyword");
        if (keyword == null || keyword.isEmpty()) {
            return ToolResult.failure("Missing required parameter: keyword");
        }

        try {
            Path start = WorkspacePath.resolve(workspaceRoot, stringParam(parameters, "path"));
            if (!Files.exists(start)) {
                return ToolResult.failure("Path does not exist: " + workspaceRoot.relativize(start));
            }
            WorkspacePath.checkInsideWorkspace(workspaceRoot, start);

            StringBuilder result = new StringBuilder();
            int[] count = new int[]{0};
            try (Stream<Path> paths = Files.walk(start)) {
                paths.filter(Files::isRegularFile)
                        .filter(this::isTextFile)
                        .forEach(path -> searchFile(path, keyword, result, count));
            }

            if (result.length() == 0) {
                return ToolResult.success("No matches found.");
            }
            if (count[0] >= MAX_MATCHES) {
                result.append("... match limit reached: ").append(MAX_MATCHES).append(System.lineSeparator());
            }
            return ToolResult.success(result.toString());
        } catch (IllegalArgumentException e) {
            return ToolResult.failure(e.getMessage());
        } catch (IOException e) {
            return ToolResult.failure("Failed to search code: " + e.getMessage());
        }
    }

    private void searchFile(Path path, String keyword, StringBuilder result, int[] count) {
        if (count[0] >= MAX_MATCHES) {
            return;
        }

        try {
            WorkspacePath.checkInsideWorkspace(workspaceRoot, path);
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                if (count[0] >= MAX_MATCHES) {
                    return;
                }
                String line = lines.get(i);
                if (line.contains(keyword)) {
                    result.append(workspaceRoot.relativize(path))
                            .append(":")
                            .append(i + 1)
                            .append(": ")
                            .append(line.trim())
                            .append(System.lineSeparator());
                    count[0]++;
                }
            }
        } catch (IOException ignored) {
            // Ignore files that cannot be read as UTF-8 text.
        }
    }

    private boolean isTextFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return fileName.endsWith(".java")
                || fileName.endsWith(".xml")
                || fileName.endsWith(".md")
                || fileName.endsWith(".txt")
                || fileName.endsWith(".json")
                || fileName.endsWith(".properties")
                || fileName.endsWith(".yml")
                || fileName.endsWith(".yaml");
    }

    private static String stringParam(Map<String, Object> parameters, String name) {
        Object value = parameters == null ? null : parameters.get(name);
        return value == null ? null : String.valueOf(value);
    }
}
