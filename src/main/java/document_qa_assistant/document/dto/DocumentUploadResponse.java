package document_qa_assistant.document.dto;

import java.util.UUID;

public record DocumentUploadResponse(
        UUID id,
        String status) {
}