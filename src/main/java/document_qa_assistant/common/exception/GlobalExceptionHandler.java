package document_qa_assistant.common.exception;

import document_qa_assistant.document.exception.DocumentAlreadyExistsException;
import document_qa_assistant.document.exception.FileTooLargeException;
import document_qa_assistant.document.exception.UnsupportedDocumentTypeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DocumentAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleDuplicate(
            DocumentAlreadyExistsException exception) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(UnsupportedDocumentTypeException.class)
    public ResponseEntity<Map<String, String>> handleUnsupportedType(
            UnsupportedDocumentTypeException exception) {
        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(FileTooLargeException.class)
    public ResponseEntity<Map<String, String>> handleFileTooLarge(
            FileTooLargeException exception) {
        return ResponseEntity
                .status(HttpStatus.CONTENT_TOO_LARGE)
                .body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(
            IllegalArgumentException exception) {
        return ResponseEntity
                .badRequest()
                .body(Map.of("error", exception.getMessage()));
    }
}