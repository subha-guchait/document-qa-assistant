package document_qa_assistant.document.repository;

import document_qa_assistant.document.dto.DocumentListResponse;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DocumentQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public DocumentQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<DocumentListResponse> findDocuments(
            String tenantId,
            int page,
            int size) {

        int offset = page * size;

        String sql = """
                SELECT
                    d.id,
                    d.title,
                    d.filename,
                    d.category,
                    d.status,
                    d.size_bytes,
                    d.created_at,
                    COUNT(dc.id) AS chunk_count
                FROM documents d
                LEFT JOIN document_chunks dc
                    ON dc.document_id = d.id
                WHERE d.tenant_id = ?
                GROUP BY
                    d.id,
                    d.title,
                    d.filename,
                    d.category,
                    d.status,
                    d.size_bytes,
                    d.created_at
                ORDER BY d.created_at DESC
                LIMIT ? OFFSET ?
                """;

        return jdbcTemplate.query(
                sql,
                (resultSet, rowNum) -> new DocumentListResponse(
                        resultSet.getObject("id", java.util.UUID.class),
                        resultSet.getString("title"),
                        resultSet.getString("filename"),
                        resultSet.getString("category"),
                        resultSet.getString("status"),
                        resultSet.getLong("size_bytes"),
                        resultSet.getLong("chunk_count"),
                        resultSet.getObject(
                                "created_at",
                                java.time.OffsetDateTime.class)),
                tenantId,
                size,
                offset);
    }

    public long countDocuments(String tenantId) {

        String sql = """
                SELECT COUNT(*)
                FROM documents
                WHERE tenant_id = ?
                """;

        Long count = jdbcTemplate.queryForObject(
                sql,
                Long.class,
                tenantId);

        return count == null ? 0 : count;
    }
}