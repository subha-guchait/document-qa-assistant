package document_qa_assistant.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Response containing the AI-generated answer and source references")
public record ChatResponse(

                @Schema(
                                description = "ID of the conversation this answer belongs to (new or existing)",
                                example = "b2c3d4e5-f6a7-8901-bcde-f12345678901")
                UUID conversationId,

                @Schema(
                                description = "AI-generated answer grounded in the retrieved document chunks",
                                example = "The Q3 report highlights a 15% revenue increase driven by...")
                String answer,

                @Schema(description = "List of document chunks used to generate the answer")
                List<SourceReference> sources) {
}