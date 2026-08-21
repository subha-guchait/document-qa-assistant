package document_qa_assistant.conversation.dto;

import java.time.OffsetDateTime;

public record MessageResponse(
        String role,
        String content,
        OffsetDateTime createdAt) {
}