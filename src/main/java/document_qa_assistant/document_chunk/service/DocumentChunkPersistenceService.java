package document_qa_assistant.document_chunk.service;

import document_qa_assistant.document_chunk.model.DocumentChunkEntity;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentChunkPersistenceService {

    private final JdbcTemplate jdbcTemplate;

    public DocumentChunkPersistenceService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void saveChunks(
            UUID documentId,
            String tenantId,
            List<DocumentChunkEntity> chunks,
            List<float[]> embeddings) {

        validateInput(chunks, embeddings);

        if (chunks.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO document_chunks (
                    id,
                    document_id,
                    tenant_id,
                    chunk_index,
                    content,
                    page_number,
                    token_count,
                    embedding,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, CAST(? AS vector), ?)
                """;

        OffsetDateTime createdAt = OffsetDateTime.now();

        jdbcTemplate.batchUpdate(
                sql,
                new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(
                            PreparedStatement statement,
                            int index) throws SQLException {

                        DocumentChunkEntity chunk = chunks.get(index);
                        float[] embedding = embeddings.get(index);

                        statement.setObject(1, chunk.getId());
                        statement.setObject(2, documentId);
                        statement.setString(3, tenantId);
                        statement.setInt(4, chunk.getChunkIndex());
                        statement.setString(5, chunk.getContent());

                        if (chunk.getPageNumber() != null) {
                            statement.setInt(6, chunk.getPageNumber());
                        } else {
                            statement.setNull(6, Types.INTEGER);
                        }

                        if (chunk.getTokenCount() != null) {
                            statement.setInt(7, chunk.getTokenCount());
                        } else {
                            statement.setNull(7, Types.INTEGER);
                        }

                        statement.setString(
                                8,
                                toVectorLiteral(embedding));

                        statement.setObject(9, createdAt);
                    }

                    @Override
                    public int getBatchSize() {
                        return chunks.size();
                    }
                });
    }

    private void validateInput(
            List<DocumentChunkEntity> chunks,
            List<float[]> embeddings) {

        if (chunks == null || embeddings == null) {
            throw new IllegalArgumentException(
                    "Chunks and embeddings are required");
        }

        if (chunks.size() != embeddings.size()) {
            throw new IllegalArgumentException(
                    "Number of chunks must match number of embeddings");
        }
    }

    private String toVectorLiteral(float[] embedding) {

        if (embedding == null) {
            throw new IllegalArgumentException(
                    "Embedding is required");
        }

        StringBuilder vector = new StringBuilder("[");

        for (int i = 0; i < embedding.length; i++) {

            if (i > 0) {
                vector.append(",");
            }

            vector.append(embedding[i]);
        }

        vector.append("]");

        return vector.toString();
    }
}