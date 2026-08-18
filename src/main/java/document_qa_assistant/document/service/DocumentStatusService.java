package document_qa_assistant.document.service;

import document_qa_assistant.document.enums.DocumentStatus;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DocumentStatusService {

    private final JdbcTemplate jdbcTemplate;

    public DocumentStatusService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void markReady(UUID documentId) {

        jdbcTemplate.update(
                """
                        UPDATE documents
                        SET status = ?,
                            error_message = NULL,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                DocumentStatus.READY.name(),
                documentId);
    }

    public void markFailed(
            UUID documentId,
            String errorMessage) {

        jdbcTemplate.update(
                """
                        UPDATE documents
                        SET status = ?,
                            error_message = ?,
                            updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """,
                DocumentStatus.FAILED.name(),
                errorMessage,
                documentId);
    }
}