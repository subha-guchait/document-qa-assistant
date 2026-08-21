package document_qa_assistant.common.exception;

import document_qa_assistant.document.exception.DocumentAlreadyExistsException;
import document_qa_assistant.document.exception.FileTooLargeException;
import document_qa_assistant.document.exception.UnsupportedDocumentTypeException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;

import java.util.Map;
import java.util.stream.Collectors;

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

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, String>> handleValidation(
                        MethodArgumentNotValidException exception) {

                String message = exception.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> error.getDefaultMessage())
                                .collect(Collectors.joining(", "));

                return ResponseEntity
                                .badRequest()
                                .body(Map.of("error", message));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<Map<String, String>> handleBadRequest(
                        IllegalArgumentException exception) {

                return ResponseEntity
                                .badRequest()
                                .body(Map.of("error", exception.getMessage()));
        }

        @ExceptionHandler(MissingRequestHeaderException.class)
        public ResponseEntity<Map<String, String>> handleMissingHeader(
                        MissingRequestHeaderException exception) {

                return ResponseEntity
                                .badRequest()
                                .body(Map.of(
                                                "error",
                                                exception.getHeaderName() + " header is required"));
        }

        @ExceptionHandler(HandlerMethodValidationException.class)
        public ResponseEntity<Map<String, String>> handleMethodValidation(
                        HandlerMethodValidationException exception) {

                return ResponseEntity
                                .badRequest()
                                .body(Map.of(
                                                "error",
                                                "X-Tenant-Id must not be blank"));
        }
}