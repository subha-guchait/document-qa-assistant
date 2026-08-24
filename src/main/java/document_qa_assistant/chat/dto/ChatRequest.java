package document_qa_assistant.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for asking a question against uploaded documents")
public record ChatRequest(

                @Schema(
                                description = "Existing conversation ID to continue a multi-turn conversation. "
                                                + "Omit or set to null to start a new conversation.",
                                example = "b2c3d4e5-f6a7-8901-bcde-f12345678901",
                                nullable = true,
                                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                String conversationId,

                @Schema(
                                description = "The natural-language question to ask",
                                example = "What were the key findings in the Q3 report?",
                                requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "Question must not be blank") String question,

                @Schema(
                                description = "Optional category to scope the document search",
                                example = "finance",
                                nullable = true,
                                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                String category) {
}