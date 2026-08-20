package document_qa_assistant.retrieval.model;

import java.util.UUID;

public record RetrievedChunk(
        UUID chunkId,
        UUID documentId,
        String tenantId,
        String category,
        String content,
        Integer pageNumber,
        Integer chunkIndex,
        double similarity) {
}