package document_qa_assistant.document.exception;

public class DocumentAlreadyExistsException extends RuntimeException {

    public DocumentAlreadyExistsException(String message) {
        super(message);
    }
}