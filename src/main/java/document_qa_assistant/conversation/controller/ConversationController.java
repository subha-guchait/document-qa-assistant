package document_qa_assistant.conversation.controller;

import document_qa_assistant.common.dto.ErrorResponse;
import document_qa_assistant.conversation.dto.ConversationResponse;
import document_qa_assistant.conversation.dto.MessageResponse;
import document_qa_assistant.conversation.model.Conversation;
import document_qa_assistant.conversation.model.Message;
import document_qa_assistant.conversation.service.ConversationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
@Tag(name = "Conversations", description = "Retrieve conversation history and message threads")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(
            ConversationService conversationService) {

        this.conversationService = conversationService;
    }

    @Operation(
            summary = "Get conversation with messages",
            description = "Retrieves a conversation and its full message history, ordered chronologically. "
                    + "Use the conversationId returned from the Chat endpoint to fetch the conversation thread.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Conversation retrieved successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request — missing or blank X-Tenant-Id header",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conversation not found for the given tenant and ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ConversationResponse> getConversation(
            @Parameter(
                    description = "Tenant identifier for multi-tenant isolation",
                    required = true,
                    example = "tenant-abc")
            @RequestHeader("X-Tenant-Id") @NotBlank(message = "X-Tenant-Id must not be blank") String tenantId,

            @Parameter(
                    description = "Unique conversation identifier",
                    required = true)
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