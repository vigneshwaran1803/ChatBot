package com.example.chatbot.service;

import com.example.chatbot.client.GeminiClient;
import com.example.chatbot.dto.ChatRequest;
import com.example.chatbot.dto.ChatResponse;
import com.example.chatbot.entity.ChatMessage;
import com.example.chatbot.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository repository;
    private final GeminiClient geminiClient;

    public ChatResponse chat(ChatRequest request) {

        // 1. Get previous messages
        List<ChatMessage> history =
                repository.findBySessionIdOrderByTimestampAsc(request.getSessionId());

        // 2. Build prompt with context
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a Java expert helping in interviews.\n");

        for (ChatMessage msg : history) {
            prompt.append(msg.getRole()).append(": ").append(msg.getMessage()).append("\n");
        }

        prompt.append("USER: ").append(request.getMessage());

        // 3. Call Gemini
        String response = geminiClient.getResponse(prompt.toString());

        // ⚠️ For now, raw response (we’ll parse later)
        String aiReply = response;

        // 4. Save user message
        repository.save(ChatMessage.builder()
                .sessionId(request.getSessionId())
                .message(request.getMessage())
                .role("USER")
                .timestamp(LocalDateTime.now())
                .build());

        // 5. Save AI message
        repository.save(ChatMessage.builder()
                .sessionId(request.getSessionId())
                .message(aiReply)
                .role("AI")
                .timestamp(LocalDateTime.now())
                .build());

        return ChatResponse.builder()
                .reply(aiReply)
                .build();
    }
}