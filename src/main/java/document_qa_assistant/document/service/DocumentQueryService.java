package document_qa_assistant.document.service;

import document_qa_assistant.document.dto.DocumentDetailResponse;
import document_qa_assistant.document.dto.DocumentListResponse;
import document_qa_assistant.document.dto.DocumentPageResponse;
import document_qa_assistant.document.model.Document;
import document_qa_assistant.document.repository.DocumentQueryRepository;
import document_qa_assistant.document.repository.DocumentRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentQueryService {

    private final DocumentRepository documentRepository;
    private final DocumentQueryRepository documentQueryRepository;

    public DocumentQueryService(
            DocumentRepository documentRepository,
            DocumentQueryRepository documentQueryRepository) {

        this.documentRepository = documentRepository;
        this.documentQueryRepository = documentQueryRepository;
    }

    public DocumentPageResponse list(
            String tenantId,
            int page,
            int size) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must be greater than or equal to zero");
        }

        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException(
                    "Size must be between 1 and 100");
        }

        List<DocumentListResponse> documents = documentQueryRepository.findDocuments(
                tenantId,
                page,
                size);

        long totalElements = documentQueryRepository.countDocuments(tenantId);

        int totalPages = totalElements == 0
                ? 0
                : (int) Math.ceil(
                        (double) totalElements / size);

        return new DocumentPageResponse(
                documents,
                page,
                size,
                totalElements,
                totalPages);
    }

    public DocumentDetailResponse getById(
            String tenantId,
            UUID documentId) {

        Document document = documentRepository
                .findById(documentId)
                .filter(existingDocument -> tenantId.equals(existingDocument.getTenantId()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Document not found"));

        return new DocumentDetailResponse(
                document.getId(),
                document.getTitle(),
                document.getCategory(),
                document.getFilename(),
                document.getStatus(),
                document.getSizeBytes(),
                document.getErrorMessage(),
                document.getCreatedAt(),
                document.getUpdatedAt());
    }
}