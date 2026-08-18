package document_qa_assistant.ingestion.chunking;

public record DocumentChunk(
        String content,
        Integer pageNumber,
        Integer sectionNumber,
        int chunkIndex,
        int tokenCount) {
}