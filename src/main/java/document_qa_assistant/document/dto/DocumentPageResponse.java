package document_qa_assistant.document.dto;

import java.util.List;

public record DocumentPageResponse(
        List<DocumentListResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}