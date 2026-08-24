package document_qa_assistant.conversation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "A single message within a conversation")
public record MessageResponse(

        @Schema(
                description = "Role of the message sender",
                example = "USER",
                allowableValues = {"USER", "ASSISTANT"})
        String role,

        @Schema(
                description = "Content of the message",
                example = "What were the key findings in the Q3 report?")
        String content,

        @Schema(
                description = "Timestamp when the message was created",
                example = "2025-08-21T10:30:00Z")
        OffsetDateTime createdAt) {
}