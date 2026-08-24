package document_qa_assistant.conversation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Full conversation including metadata and message history")
public record ConversationResponse(

        @Schema(
                description = "Unique identifier of the conversation",
                example = "b2c3d4e5-f6a7-8901-bcde-f12345678901")
        UUID conversationId,

        @Schema(
                description = "Auto-generated title based on the first question",
                example = "Q3 Financial Report Analysis")
        String title,

        @Schema(
                description = "Timestamp when the conversation was created",
                example = "2025-08-21T10:30:00Z")
        OffsetDateTime createdAt,

        @Schema(
                description = "Timestamp of the most recent message in the conversation",
                example = "2025-08-21T11:15:00Z")
        OffsetDateTime lastMessageAt,

        @Schema(description = "Ordered list of messages in the conversation")
        List<MessageResponse> messages) {
}