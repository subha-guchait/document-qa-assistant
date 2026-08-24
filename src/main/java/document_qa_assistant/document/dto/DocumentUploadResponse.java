package document_qa_assistant.document.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Response returned after a document upload is accepted for processing")
public record DocumentUploadResponse(

        @Schema(
                description = "Unique identifier of the uploaded document",
                example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        UUID id,

        @Schema(
                description = "Current processing status of the document",
                example = "PENDING",
                allowableValues = {"PENDING", "PROCESSING", "COMPLETED", "FAILED"})
        String status) {
}