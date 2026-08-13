package document_qa_assistant.document.dto;

import jakarta.validation.constraints.NotBlank;

public record DocumentUploadRequest(

        @NotBlank String title,

        String category) {
}