package document_qa_assistant.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard error response returned for all API errors")
public record ErrorResponse(

        @Schema(
                description = "Human-readable error message describing what went wrong",
                example = "Document with this filename already exists for the tenant")
        String error) {
}
