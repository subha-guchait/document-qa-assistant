package document_qa_assistant.qa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QaRequest(

                @NotBlank(message = "Question must not be blank") @Size(max = 2000, message = "Question must not exceed 2000 characters") String question,

                @Size(max = 100, message = "Category must not exceed 100 characters") String category) {
}