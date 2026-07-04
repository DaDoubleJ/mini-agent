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

    public static void checkWritablePath(Path workspaceRoot, Path path) throws IOException {
        if (!path.normalize().startsWith(workspaceRoot)) {
            throw new IllegalArgumentException("Path is outside workspace: " + path);
        }
        if (isInGitDirectory(workspaceRoot, path)) {
            throw new IllegalArgumentException("Refusing to modify .git directory: " + workspaceRoot.relativize(path));
        }

        Path existingPath = path;
        while (existingPath != null && !existingPath.equals(workspaceRoot) && !java.nio.file.Files.exists(existingPath)) {
            existingPath = existingPath.getParent();
        }
        if (existingPath != null) {
            checkInsideWorkspace(workspaceRoot, existingPath);
        }
    }

    public static boolean isInGitDirectory(Path workspaceRoot, Path path) {
        Path relativePath = workspaceRoot.relativize(path.normalize());
        for (Path part : relativePath) {
            if (".git".equals(part.toString())) {
                return true;
            }
        }
        return false;
    }
}
