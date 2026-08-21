package document_qa_assistant.conversation.controller;

import document_qa_assistant.conversation.dto.ConversationResponse;
import document_qa_assistant.conversation.dto.MessageResponse;
import document_qa_assistant.conversation.model.Conversation;
import document_qa_assistant.conversation.model.Message;
import document_qa_assistant.conversation.service.ConversationService;

import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(
            ConversationService conversationService) {

        this.conversationService = conversationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConversationResponse> getConversation(
            @RequestHeader("X-Tenant-Id") @NotBlank(message = "X-Tenant-Id must not be blank") String tenantId,

            @PathVariable UUID id) {

        Conversation conversation = conversationService.getConversation(
                tenantId,
                id);

        List<Message> messages = conversationService.getMessages(
                tenantId,
                id);

        List<MessageResponse> messageResponses = messages.stream()
                .map(message -> new MessageResponse(
                        message.getRole(),
                        message.getContent(),
                        message.getCreatedAt()))
                .toList();

        ConversationResponse response = new ConversationResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getLastMessageAt(),
                messageResponses);

        return ResponseEntity.ok(response);
    }
}