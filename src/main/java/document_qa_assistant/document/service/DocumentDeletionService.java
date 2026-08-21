package document_qa_assistant.document.service;

import document_qa_assistant.document.model.Document;
import document_qa_assistant.document.repository.DocumentRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class DocumentDeletionService {

    private final DocumentRepository documentRepository;
    private final JdbcTemplate jdbcTemplate;
    private final Path storagePath;

    public DocumentDeletionService(
            DocumentRepository documentRepository,
            JdbcTemplate jdbcTemplate,
            @Value("${storage.path}") String storagePath) {

        this.documentRepository = documentRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.storagePath = Paths.get(storagePath);
    }

    @Transactional
    public void delete(
            String tenantId,
            UUID documentId) {

        Document document = documentRepository
                .findById(documentId)
                .filter(existingDocument -> tenantId.equals(existingDocument.getTenantId()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Document not found"));

        deleteChunks(documentId);

        deleteDocument(documentId);

        deleteStoredFile(document);
    }

    private void deleteChunks(UUID documentId) {

        jdbcTemplate.update(
                """
                        DELETE FROM document_chunks
                        WHERE document_id = ?
                        """,
                documentId);
    }

    private void deleteDocument(UUID documentId) {

        jdbcTemplate.update(
                """
                        DELETE FROM documents
                        WHERE id = ?
                        """,
                documentId);
    }

    private void deleteStoredFile(Document document) {

        Path file = storagePath.resolve(
                document.getContentHash()
                        + "_"
                        + document.getFilename());

        try {
            Files.deleteIfExists(file);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to delete stored document file",
                    exception);
        }
    }
}