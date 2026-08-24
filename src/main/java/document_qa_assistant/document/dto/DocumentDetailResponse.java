package document_qa_assistant.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Detailed information about a single document")
public record DocumentDetailResponse(

        @Schema(
                description = "Unique identifier of the document",
                example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        UUID id,

        @Schema(
                description = "Display title of the document",
                example = "Q3 Financial Report")
        String title,

        @Schema(
                description = "Category the document belongs to",
                example = "finance")
        String category,

        @Schema(
                description = "Original filename of the uploaded file",
                example = "report-q3-2025.pdf")
        String filename,

        @Schema(
                description = "Current processing status",
                example = "COMPLETED",
                allowableValues = {"PENDING", "PROCESSING", "COMPLETED", "FAILED"})
        String status,

        @Schema(
                description = "File size in bytes",
                example = "2048576")
        Long sizeBytes,

        @Schema(
                description = "Error message if processing failed, null otherwise",
                example = "Failed to extract text from document",
                nullable = true)
        String errorMessage,

        @Schema(
                description = "Timestamp when the document was uploaded",
                example = "2025-08-21T10:30:00Z")
        OffsetDateTime uploadedAt,

        @Schema(
                description = "Timestamp when the document was last updated",
                example = "2025-08-21T10:35:00Z")
        OffsetDateTime updatedAt) {
}