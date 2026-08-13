package document_qa_assistant.document.controller;

import document_qa_assistant.document.dto.DocumentUploadRequest;
import document_qa_assistant.document.dto.DocumentUploadResponse;
import document_qa_assistant.document.model.Document;
import document_qa_assistant.document.service.DocumentService;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

        private final DocumentService documentService;

        public DocumentController(DocumentService documentService) {
                this.documentService = documentService;
        }

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<DocumentUploadResponse> upload(
                        @RequestHeader("X-Tenant-Id") String tenantId,
                        @RequestPart("file") MultipartFile file,
                        @Valid @ModelAttribute DocumentUploadRequest request) throws IOException {

                Document document = documentService.upload(
                                file,
                                tenantId,
                                request.title(),
                                request.category());

                DocumentUploadResponse response = new DocumentUploadResponse(
                                document.getId(),
                                document.getStatus());

                return ResponseEntity
                                .accepted()
                                .location(
                                                URI.create("/api/v1/documents/" + document.getId()))
                                .body(response);
        }
}