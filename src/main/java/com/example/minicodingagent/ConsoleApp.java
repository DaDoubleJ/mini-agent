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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

public class ConsoleApp {
    private static final String DEFAULT_BASE_URL = "https://api.deepseek.com";
    private static final String DEFAULT_MODEL = "deepseek-v4-flash";
    private static final String SYSTEM_PROMPT =
            "You are Mini Coding Agent, a simple Claude Code style assistant. "
                    + "Help the user understand and work with Java projects. "
                    + "Keep answers practical, concise, and focused on the user's request.";

    private final Scanner scanner = new Scanner(System.in);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ToolRegistry toolRegistry;
    private final Properties configProperties;

    public ConsoleApp() {
        Path workspaceRoot = Paths.get("").toAbsolutePath().normalize();
        this.configProperties = loadConfig(workspaceRoot);
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
        String apiKey = configValue("DEEPSEEK_API_KEY", "deepseek.apiKey", null);
        String baseUrl = configValue("DEEPSEEK_BASE_URL", "deepseek.baseUrl", DEFAULT_BASE_URL);
        String model = configValue("DEEPSEEK_MODEL", "deepseek.model", DEFAULT_MODEL);

        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("Missing API key. Set DEEPSEEK_API_KEY or deepseek.apiKey in config.properties.");
            System.exit(1);
        }

        System.out.print("> ");
        String userPrompt = scanner.nextLine();

        List<Message> messages = Arrays.asList(
                new Message("system", SYSTEM_PROMPT),
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

    private static Properties loadConfig(Path workspaceRoot) {
        Properties properties = new Properties();
        Path configPath = workspaceRoot.resolve("config.properties");
        if (!Files.exists(configPath)) {
            return properties;
        }

        try (InputStream inputStream = Files.newInputStream(configPath)) {
            properties.load(inputStream);
        } catch (IOException e) {
            System.err.println("Failed to read config.properties: " + e.getMessage());
            System.exit(1);
        }
        return properties;
    }

    private String configValue(String envName, String propertyName, String defaultValue) {
        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue;
        }

        String propertyValue = configProperties.getProperty(propertyName);
        if (propertyValue != null && !propertyValue.trim().isEmpty()) {
            return propertyValue;
        }

        return defaultValue;
    }
}
