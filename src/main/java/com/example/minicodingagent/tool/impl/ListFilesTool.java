package com.example.minicodingagent.tool.impl;

import com.example.minicodingagent.tool.Tool;
import com.example.minicodingagent.tool.ToolResult;
import com.example.minicodingagent.tool.WorkspacePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.DirectoryStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ListFilesTool implements Tool {
    private final Path workspaceRoot;

    public ListFilesTool(Path workspaceRoot) {
        this.workspaceRoot = workspaceRoot;
    }

    public String name() {
        return "list_files";
    }

    public String description() {
        return "List files and directories under a workspace path.";
    }

    public String parameterSchema() {
        return "{\"type\":\"object\",\"properties\":{\"path\":{\"type\":\"string\",\"description\":\"Path inside workspace, default is .\"}}}";
    }

    public ToolResult execute(Map<String, Object> parameters) {
        try {
            Path dir = WorkspacePath.resolve(workspaceRoot, stringParam(parameters, "path"));
            if (!Files.exists(dir)) {
                return ToolResult.failure("Path does not exist: " + workspaceRoot.relativize(dir));
            }
            WorkspacePath.checkInsideWorkspace(workspaceRoot, dir);
            if (!Files.isDirectory(dir)) {
                return ToolResult.failure("Path is not a directory: " + workspaceRoot.relativize(dir));
            }

            List<String> lines = new ArrayList<String>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path child : stream) {
                    String suffix = Files.isDirectory(child) ? "/" : "";
                    lines.add(workspaceRoot.relativize(child).toString() + suffix);
                }
            }
            Collections.sort(lines);
            return ToolResult.success(joinLines(lines));
        } catch (IllegalArgumentException e) {
            return ToolResult.failure(e.getMessage());
        } catch (IOException e) {
            return ToolResult.failure("Failed to list files: " + e.getMessage());
        }
    }

    private static String stringParam(Map<String, Object> parameters, String name) {
        Object value = parameters == null ? null : parameters.get(name);
        return value == null ? null : String.valueOf(value);
    }

    private static String joinLines(List<String> lines) {
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            builder.append(line).append(System.lineSeparator());
        }
        return builder.toString();
    }
}
