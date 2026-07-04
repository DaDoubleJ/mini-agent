package com.example.minicodingagent.agent;

import com.example.minicodingagent.ChatClient;
import com.example.minicodingagent.Message;
import com.example.minicodingagent.tool.Tool;
import com.example.minicodingagent.tool.ToolRegistry;
import com.example.minicodingagent.tool.ToolResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Agent {
    private static final int MAX_ROUNDS = 6;

    private final ChatClient chatClient;
    private final ToolRegistry toolRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Agent(ChatClient chatClient, ToolRegistry toolRegistry) {
        this.chatClient = chatClient;
        this.toolRegistry = toolRegistry;
    }

    public String run(String userTask) throws IOException {
        List<Message> messages = new ArrayList<Message>();
        messages.add(new Message("system", AgentPrompts.systemPrompt(toolRegistry)));
        messages.add(new Message("user", "User task: " + userTask));

        for (int round = 1; round <= MAX_ROUNDS; round++) {
            String modelOutput = chatClient.chat(messages);
            System.out.println();
            System.out.println("[Round " + round + "] Model decision:");
            System.out.println(shortText(modelOutput));

            AgentDecision decision = parseDecision(modelOutput);
            if (decision == null) {
                messages.add(new Message("assistant", modelOutput));
                messages.add(new Message("user",
                        "Your previous response was invalid. Output exactly one JSON object using only type=tool_call or type=final."));
                continue;
            }

            if ("tool_call".equals(decision.getType())) {
                messages.add(new Message("assistant", modelOutput));
                ToolResult result = executeTool(decision);
                System.out.println("[Round " + round + "] Tool result:");
                System.out.println(shortText(toolResultText(result)));
                messages.add(new Message("user", toolResultMessage(decision, result)));
                continue;
            }

            if ("final".equals(decision.getType())) {
                return decision.getAnswer() == null ? "" : decision.getAnswer();
            }

            messages.add(new Message("assistant", modelOutput));
            messages.add(new Message("user",
                    "Unknown type. Output exactly one JSON object with type=tool_call or type=final."));
        }

        return "Agent stopped because it reached the maximum loop count: " + MAX_ROUNDS;
    }

    private ToolResult executeTool(AgentDecision decision) {
        if (decision.getTool() == null || decision.getTool().trim().isEmpty()) {
            return ToolResult.failure("Missing tool name.");
        }

        Tool tool = toolRegistry.find(decision.getTool());
        if (tool == null) {
            return ToolResult.failure("Unknown tool: " + decision.getTool());
        }

        Map<String, Object> arguments = decision.getArguments();
        if (arguments == null) {
            arguments = new HashMap<String, Object>();
        }
        return tool.execute(arguments);
    }

    private AgentDecision parseDecision(String modelOutput) {
        try {
            AgentDecision decision = objectMapper.readValue(extractJson(modelOutput), AgentDecision.class);
            if (decision.getType() == null || decision.getType().trim().isEmpty()) {
                return null;
            }
            return decision;
        } catch (IOException e) {
            return null;
        }
    }

    private String extractJson(String text) {
        String value = text == null ? "" : text.trim();
        if (value.startsWith("```")) {
            int firstLineEnd = value.indexOf('\n');
            int lastFence = value.lastIndexOf("```");
            if (firstLineEnd >= 0 && lastFence > firstLineEnd) {
                value = value.substring(firstLineEnd + 1, lastFence).trim();
            }
        }
        return value;
    }

    private String toolResultMessage(AgentDecision decision, ToolResult result) {
        StringBuilder builder = new StringBuilder();
        builder.append("Tool execution result.\n");
        builder.append("tool: ").append(decision.getTool()).append("\n");
        builder.append("success: ").append(result.isSuccess()).append("\n");
        if (result.isSuccess()) {
            builder.append("content:\n").append(result.getContent());
        } else {
            builder.append("errorMessage:\n").append(result.getErrorMessage());
        }
        builder.append("\nUse this result to decide the next JSON response.");
        return builder.toString();
    }

    private String toolResultText(ToolResult result) {
        if (result.isSuccess()) {
            return result.getContent();
        }
        return result.getErrorMessage();
    }

    private String shortText(String text) {
        if (text == null) {
            return "";
        }
        String oneLine = text.replace('\r', ' ').replace('\n', ' ').trim();
        if (oneLine.length() <= 500) {
            return oneLine;
        }
        return oneLine.substring(0, 500) + "...";
    }
}
