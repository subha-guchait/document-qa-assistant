package document_qa_assistant.document.controller;

import document_qa_assistant.document.dto.DocumentDetailResponse;
import document_qa_assistant.document.dto.DocumentPageResponse;
import document_qa_assistant.document.dto.DocumentUploadRequest;
import document_qa_assistant.document.dto.DocumentUploadResponse;
import document_qa_assistant.document.model.Document;
import document_qa_assistant.document.service.DocumentDeletionService;
import document_qa_assistant.document.service.DocumentQueryService;
import document_qa_assistant.document.service.DocumentService;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

        private final DocumentService documentService;
        private final DocumentQueryService documentQueryService;
        private final DocumentDeletionService documentDeletionService;

        public DocumentController(
                        DocumentService documentService,
                        DocumentQueryService documentQueryService,
                        DocumentDeletionService documentDeletionService) {

                this.documentService = documentService;
                this.documentQueryService = documentQueryService;
                this.documentDeletionService = documentDeletionService;
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

        @GetMapping
        public ResponseEntity<DocumentPageResponse> list(
                        @RequestHeader("X-Tenant-Id") String tenantId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                DocumentPageResponse response = documentQueryService.list(
                                tenantId,
                                page,
                                size);

                return ResponseEntity.ok(response);
        }

        @GetMapping("/{id}")
        public ResponseEntity<DocumentDetailResponse> getById(
                        @RequestHeader("X-Tenant-Id") String tenantId,
                        @PathVariable UUID id) {

                DocumentDetailResponse response = documentQueryService.getById(
                                tenantId,
                                id);

                return ResponseEntity.ok(response);
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> delete(
                        @RequestHeader("X-Tenant-Id") String tenantId,
                        @PathVariable UUID id) {

                documentDeletionService.delete(
                                tenantId,
                                id);

                return ResponseEntity.noContent().build();
        }
}