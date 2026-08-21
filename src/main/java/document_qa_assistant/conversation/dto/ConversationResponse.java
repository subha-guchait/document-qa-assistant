package document_qa_assistant.conversation.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ConversationResponse(
        UUID conversationId,
        String title,
        OffsetDateTime createdAt,
        OffsetDateTime lastMessageAt,
        List<MessageResponse> messages) {
}