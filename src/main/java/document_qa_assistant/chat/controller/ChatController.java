package document_qa_assistant.chat.controller;

import document_qa_assistant.chat.dto.ChatRequest;
import document_qa_assistant.chat.dto.ChatResponse;
import document_qa_assistant.chat.service.ChatService;
import document_qa_assistant.common.dto.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
@Tag(name = "Chat", description = "Ask natural-language questions grounded in your uploaded documents")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @Operation(
            summary = "Ask a question",
            description = "Submits a natural-language question that is answered using Retrieval-Augmented Generation (RAG). "
                    + "Relevant document chunks are retrieved, and the AI generates a grounded answer with source references. "
                    + "Pass a conversationId to continue a multi-turn conversation, or omit it to start a new one.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Question answered successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request — blank question or missing X-Tenant-Id header",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @Parameter(
                    description = "Tenant identifier for multi-tenant isolation",
                    required = true,
                    example = "tenant-abc")
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