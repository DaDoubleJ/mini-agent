package com.example.minicodingagent;

import com.example.minicodingagent.tool.Tool;
import com.example.minicodingagent.tool.ToolRegistry;
import com.example.minicodingagent.tool.ToolResult;
import com.example.minicodingagent.tool.impl.ListFilesTool;
import com.example.minicodingagent.tool.impl.ReadFileTool;
import com.example.minicodingagent.tool.impl.SearchCodeTool;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ConsoleApp {
    private static final String DEFAULT_BASE_URL = "https://api.deepseek.com";
    private static final String DEFAULT_MODEL = "deepseek-v4-flash";

    private final Scanner scanner = new Scanner(System.in);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ToolRegistry toolRegistry;

    public ConsoleApp() {
        Path workspaceRoot = Paths.get("").toAbsolutePath().normalize();
        this.toolRegistry = new ToolRegistry();
        this.toolRegistry.register(new ListFilesTool(workspaceRoot));
        this.toolRegistry.register(new ReadFileTool(workspaceRoot));
        this.toolRegistry.register(new SearchCodeTool(workspaceRoot));
    }

    public void run(String[] args) {
        if (args != null && args.length > 0 && "--tool".equals(args[0])) {
            runToolMode();
        } else {
            runChatMode();
        }
    }

    private void runChatMode() {
        String apiKey = getenv("DEEPSEEK_API_KEY", null);
        String baseUrl = getenv("DEEPSEEK_BASE_URL", DEFAULT_BASE_URL);
        String model = getenv("DEEPSEEK_MODEL", DEFAULT_MODEL);

        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("Missing environment variable: DEEPSEEK_API_KEY");
            System.exit(1);
        }

        System.out.println("System prompt:");
        String systemPrompt = scanner.nextLine();
        System.out.println("User prompt:");
        String userPrompt = scanner.nextLine();

        List<Message> messages = Arrays.asList(
                new Message("system", systemPrompt),
                new Message("user", userPrompt)
        );

        ChatClient client = new ChatClient(apiKey, baseUrl, model);
        try {
            String answer = client.chat(messages);
            System.out.println();
            System.out.println("Assistant:");
            System.out.println(answer);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private void runToolMode() {
        System.out.println("Available tools:");
        for (Tool tool : toolRegistry.list()) {
            System.out.println("- " + tool.name() + ": " + tool.description());
            System.out.println("  parameters: " + tool.parameterSchema());
        }

        System.out.println("Tool name:");
        String toolName = scanner.nextLine().trim();
        Tool tool = toolRegistry.find(toolName);
        if (tool == null) {
            System.err.println("Unknown tool: " + toolName);
            System.exit(1);
        }

        System.out.println("Parameters JSON, or blank for {}:");
        String json = scanner.nextLine().trim();
        Map<String, Object> parameters = parseParameters(json);

        ToolResult result = tool.execute(parameters);
        if (result.isSuccess()) {
            System.out.println("Tool success:");
            System.out.println(result.getContent());
        } else {
            System.out.println("Tool failed:");
            System.out.println(result.getErrorMessage());
        }
    }

    private Map<String, Object> parseParameters(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new HashMap<String, Object>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            System.err.println("Invalid JSON parameters: " + e.getMessage());
            System.exit(1);
            return new HashMap<String, Object>();
        }
    }

    private static String getenv(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value;
    }
}
