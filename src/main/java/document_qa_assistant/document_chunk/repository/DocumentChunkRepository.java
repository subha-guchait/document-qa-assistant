package document_qa_assistant.document_chunk.repository;

import document_qa_assistant.document_chunk.model.DocumentChunkEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentChunkRepository
        extends CrudRepository<DocumentChunkEntity, UUID> {

    List<DocumentChunkEntity> findByDocumentId(UUID documentId);

    List<DocumentChunkEntity> findByTenantId(String tenantId);
}