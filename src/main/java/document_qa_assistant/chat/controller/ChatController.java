package document_qa_assistant.chat.controller;

import document_qa_assistant.chat.dto.ChatRequest;
import document_qa_assistant.chat.dto.ChatResponse;
import document_qa_assistant.chat.service.ChatService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @RequestHeader("X-Tenant-Id") @NotBlank(message = "X-Tenant-Id must not be blank") String tenantId,

            @Valid @RequestBody ChatRequest request) {

        ChatResponse response = chatService.answer(
                tenantId,
                request.question(),
                request.category(),
                request.conversationId());

        return ResponseEntity.ok(response);
    }
}