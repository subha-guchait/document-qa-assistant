package document_qa_assistant.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for uploading a new document (sent as multipart form data)")
public record DocumentUploadRequest(

        @Schema(
                description = "Display title for the document",
                example = "Q3 Financial Report",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String title,

        @Schema(
                description = "Optional category to group related documents for scoped queries",
                example = "finance",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String category) {
}