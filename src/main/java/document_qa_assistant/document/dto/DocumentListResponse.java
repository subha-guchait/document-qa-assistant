package document_qa_assistant.document.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DocumentListResponse(
        UUID id,
        String title,
        String filename,
        String category,
        String status,
        Long sizeBytes,
        long chunkCount,
        OffsetDateTime uploadedAt) {
}