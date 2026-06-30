package com.example.minicodingagent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatClient {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String baseUrl;
    private final String model;

    public ChatClient(String apiKey, String baseUrl, String model) {
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.apiKey = apiKey;
        this.baseUrl = trimTrailingSlash(baseUrl);
        this.model = model;
    }

    public String chat(List<Message> messages) throws IOException {
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("model", model);
        payload.put("messages", messages);

        String json = objectMapper.writeValueAsString(payload);
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(baseUrl + "/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        Response response = httpClient.newCall(request).execute();
        try {
            String responseBody = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                throw new IOException("Chat API request failed: HTTP " + response.code() + " " + responseBody);
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode()) {
                throw new IOException("Chat API response missing choices[0].message.content: " + responseBody);
            }
            return content.asText();
        } finally {
            response.close();
        }
    }

    private static String trimTrailingSlash(String value) {
        if (value == null || value.endsWith("/") == false) {
            return value;
        }
        return value.substring(0, value.length() - 1);
    }
}
