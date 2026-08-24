package document_qa_assistant.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Paginated response containing a list of documents")
public record DocumentPageResponse(

        @Schema(description = "List of documents on the current page")
        List<DocumentListResponse> content,

        @Schema(
                description = "Current page number (zero-based)",
                example = "0")
        int page,

        @Schema(
                description = "Number of items per page",
                example = "10")
        int size,

        @Schema(
                description = "Total number of documents across all pages",
                example = "47")
        long totalElements,

        @Schema(
                description = "Total number of pages available",
                example = "5")
        int totalPages) {
}