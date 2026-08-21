package document_qa_assistant.document.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DocumentDetailResponse(
        UUID id,
        String title,
        String category,
        String filename,
        String status,
        Long sizeBytes,
        String errorMessage,
        OffsetDateTime uploadedAt,
        OffsetDateTime updatedAt) {
}