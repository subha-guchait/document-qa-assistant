package document_qa_assistant.qa.dto;

import java.util.UUID;

public record SourceReference(
                UUID documentId,
                Integer pageNumber,
                Integer chunkIndex,
                String category) {
}