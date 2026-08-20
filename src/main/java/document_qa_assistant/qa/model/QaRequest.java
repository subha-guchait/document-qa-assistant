package document_qa_assistant.qa.model;

public record QaRequest(
        String question,
        String category) {
}