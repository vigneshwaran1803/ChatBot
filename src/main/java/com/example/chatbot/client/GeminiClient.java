package com.example.chatbot.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class GeminiClient {

    private final WebClient webClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    public GeminiClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    public String getResponse(String prompt) {

        String url = "/v1beta/models/gemini-flash-latest:generateContent";

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of(
                                "parts", new Object[]{
                                        Map.of("text", prompt)
                                }
                        )
                }
        );

        return webClient.post()
                .uri(url)
                .header("Content-Type", "application/json")
                .header("X-goog-api-key", "apiKey") // ✅ add this
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(
                        status -> status.isError(),
                        response -> response.bodyToMono(String.class)
                                .map(error -> new RuntimeException("Gemini Error: " + error))
                )
                .bodyToMono(String.class)
                .block();
    }
}