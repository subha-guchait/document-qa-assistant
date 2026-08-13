package document_qa_assistant.document.repository;

import org.springframework.data.repository.CrudRepository;

import document_qa_assistant.document.model.Document;

import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends CrudRepository<Document, UUID> {

    Optional<Document> findByTenantIdAndContentHash(
            String tenantId,
            String contentHash);
}