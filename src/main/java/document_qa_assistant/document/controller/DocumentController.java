package document_qa_assistant.document.controller;

import document_qa_assistant.common.dto.ErrorResponse;
import document_qa_assistant.document.dto.DocumentDetailResponse;
import document_qa_assistant.document.dto.DocumentPageResponse;
import document_qa_assistant.document.dto.DocumentUploadRequest;
import document_qa_assistant.document.dto.DocumentUploadResponse;
import document_qa_assistant.document.model.Document;
import document_qa_assistant.document.service.DocumentDeletionService;
import document_qa_assistant.document.service.DocumentQueryService;
import document_qa_assistant.document.service.DocumentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@Tag(name = "Documents", description = "Upload, list, inspect, and delete documents for AI-powered Q&A")
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

        @Operation(
                        summary = "Upload a document",
                        description = "Uploads a document file (PDF, DOCX, TXT, etc.) for asynchronous ingestion. "
                                        + "The document is stored and queued for text extraction and embedding generation. "
                                        + "Returns immediately with a PENDING status — poll GET /{id} to track progress.")
        @ApiResponses({
                        @ApiResponse(
                                        responseCode = "202",
                                        description = "Document accepted for processing",
                                        content = @Content(schema = @Schema(implementation = DocumentUploadResponse.class))),
                        @ApiResponse(
                                        responseCode = "400",
                                        description = "Invalid request — missing title or X-Tenant-Id header",
                                        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(
                                        responseCode = "409",
                                        description = "A document with the same filename already exists for this tenant",
                                        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(
                                        responseCode = "413",
                                        description = "File exceeds the maximum allowed size (20 MB)",
                                        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(
                                        responseCode = "415",
                                        description = "Unsupported file type",
                                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<DocumentUploadResponse> upload(
                        @Parameter(
                                        description = "Tenant identifier for multi-tenant isolation",
                                        required = true,
                                        example = "tenant-abc")
                        @RequestHeader("X-Tenant-Id") String tenantId,

                        @Parameter(
                                        description = "The document file to upload (PDF, DOCX, TXT, etc.)",
                                        required = true)
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

        @Operation(
                        summary = "List documents",
                        description = "Returns a paginated list of all documents belonging to the specified tenant.")
        @ApiResponses({
                        @ApiResponse(
                                        responseCode = "200",
                                        description = "Paginated list of documents"),
                        @ApiResponse(
                                        responseCode = "400",
                                        description = "Invalid request — missing X-Tenant-Id header",
                                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @GetMapping
        public ResponseEntity<DocumentPageResponse> list(
                        @Parameter(
                                        description = "Tenant identifier for multi-tenant isolation",
                                        required = true,
                                        example = "tenant-abc")
                        @RequestHeader("X-Tenant-Id") String tenantId,

                        @Parameter(
                                        description = "Page number (zero-based)",
                                        example = "0")
                        @RequestParam(defaultValue = "0") int page,

                        @Parameter(
                                        description = "Number of items per page",
                                        example = "10")
                        @RequestParam(defaultValue = "10") int size) {

                DocumentPageResponse response = documentQueryService.list(
                                tenantId,
                                page,
                                size);

                return ResponseEntity.ok(response);
        }

        @Operation(
                        summary = "Get document details",
                        description = "Returns detailed information about a specific document, including processing status and metadata.")
        @ApiResponses({
                        @ApiResponse(
                                        responseCode = "200",
                                        description = "Document details retrieved successfully"),
                        @ApiResponse(
                                        responseCode = "400",
                                        description = "Invalid request — missing X-Tenant-Id header",
                                        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(
                                        responseCode = "404",
                                        description = "Document not found for the given tenant and ID",
                                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @GetMapping("/{id}")
        public ResponseEntity<DocumentDetailResponse> getById(
                        @Parameter(
                                        description = "Tenant identifier for multi-tenant isolation",
                                        required = true,
                                        example = "tenant-abc")
                        @RequestHeader("X-Tenant-Id") String tenantId,

                        @Parameter(
                                        description = "Unique document identifier",
                                        required = true)
                        @PathVariable UUID id) {

                DocumentDetailResponse response = documentQueryService.getById(
                                tenantId,
                                id);

                return ResponseEntity.ok(response);
        }

        @Operation(
                        summary = "Delete a document",
                        description = "Permanently deletes a document and all its associated chunks and embeddings.")
        @ApiResponses({
                        @ApiResponse(
                                        responseCode = "204",
                                        description = "Document deleted successfully"),
                        @ApiResponse(
                                        responseCode = "400",
                                        description = "Invalid request — missing X-Tenant-Id header",
                                        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(
                                        responseCode = "404",
                                        description = "Document not found for the given tenant and ID",
                                        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> delete(
                        @Parameter(
                                        description = "Tenant identifier for multi-tenant isolation",
                                        required = true,
                                        example = "tenant-abc")
                        @RequestHeader("X-Tenant-Id") String tenantId,

                        @Parameter(
                                        description = "Unique document identifier",
                                        required = true)
                        @PathVariable UUID id) {

                documentDeletionService.delete(
                                tenantId,
                                id);

                return ResponseEntity.noContent().build();
        }
}