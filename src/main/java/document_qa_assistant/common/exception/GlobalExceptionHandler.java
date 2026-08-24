package document_qa_assistant.common.exception;

import document_qa_assistant.common.dto.ErrorResponse;
import document_qa_assistant.document.exception.DocumentAlreadyExistsException;
import document_qa_assistant.document.exception.FileTooLargeException;
import document_qa_assistant.document.exception.UnsupportedDocumentTypeException;

import io.swagger.v3.oas.annotations.Hidden;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;

import java.util.stream.Collectors;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(DocumentAlreadyExistsException.class)
        public ResponseEntity<ErrorResponse> handleDuplicate(
                        DocumentAlreadyExistsException exception) {

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(new ErrorResponse(exception.getMessage()));
        }

        @ExceptionHandler(UnsupportedDocumentTypeException.class)
        public ResponseEntity<ErrorResponse> handleUnsupportedType(
                        UnsupportedDocumentTypeException exception) {

                return ResponseEntity
                                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                                .body(new ErrorResponse(exception.getMessage()));
        }

        @ExceptionHandler(FileTooLargeException.class)
        public ResponseEntity<ErrorResponse> handleFileTooLarge(
                        FileTooLargeException exception) {

                return ResponseEntity
                                .status(HttpStatus.CONTENT_TOO_LARGE)
                                .body(new ErrorResponse(exception.getMessage()));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidation(
                        MethodArgumentNotValidException exception) {

                String message = exception.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> error.getDefaultMessage())
                                .collect(Collectors.joining(", "));

                return ResponseEntity
                                .badRequest()
                                .body(new ErrorResponse(message));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleBadRequest(
                        IllegalArgumentException exception) {

                return ResponseEntity
                                .badRequest()
                                .body(new ErrorResponse(exception.getMessage()));
        }

        @ExceptionHandler(MissingRequestHeaderException.class)
        public ResponseEntity<ErrorResponse> handleMissingHeader(
                        MissingRequestHeaderException exception) {

                return ResponseEntity
                                .badRequest()
                                .body(new ErrorResponse(
                                                exception.getHeaderName() + " header is required"));
        }

        @ExceptionHandler(HandlerMethodValidationException.class)
        public ResponseEntity<ErrorResponse> handleMethodValidation(
                        HandlerMethodValidationException exception) {

                return ResponseEntity
                                .badRequest()
                                .body(new ErrorResponse(
                                                "X-Tenant-Id must not be blank"));
        }
}