package com.example.minicodingagent.tool;

import java.io.IOException;
import java.nio.file.Path;

public class WorkspacePath {
    private WorkspacePath() {
    }

    public static Path resolve(Path workspaceRoot, String inputPath) {
        String safeInputPath = inputPath == null || inputPath.trim().isEmpty() ? "." : inputPath.trim();
        Path resolved = workspaceRoot.resolve(safeInputPath).normalize();
        if (!resolved.startsWith(workspaceRoot)) {
            throw new IllegalArgumentException("Path is outside workspace: " + inputPath);
        }
        return resolved;
    }

    public static void checkInsideWorkspace(Path workspaceRoot, Path path) throws IOException {
        Path realWorkspaceRoot = workspaceRoot.toRealPath();
        Path realPath = path.toRealPath();
        if (!realPath.startsWith(realWorkspaceRoot)) {
            throw new IllegalArgumentException("Path is outside workspace: " + path);
        }
    }
}
