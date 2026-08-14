package document_qa_assistant.ingestion.extraction;

public record ExtractedSection(
        String text,
        Integer pageNumber,
        Integer sectionNumber) {
}