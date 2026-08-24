package document_qa_assistant.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Reference to a specific document chunk that contributed to the answer")
public record SourceReference(

                @Schema(
                                description = "Title of the source document",
                                example = "Q3 Financial Report")
                String documentTitle,

                @Schema(
                                description = "Page number where the relevant content was found",
                                example = "7",
                                nullable = true)
                Integer pageNumber,

                @Schema(
                                description = "Cosine similarity score between the query and this chunk (0.0 to 1.0)",
                                example = "0.87")
                Double similarity,

                @Schema(
                                description = "Text snippet from the matched chunk",
                                example = "Revenue increased by 15% compared to the previous quarter...")
                String snippet) {
}