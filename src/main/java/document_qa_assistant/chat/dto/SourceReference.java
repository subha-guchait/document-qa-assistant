package document_qa_assistant.chat.dto;

public record SourceReference(
                String documentTitle,
                Integer pageNumber,
                Double similarity,
                String snippet) {
}