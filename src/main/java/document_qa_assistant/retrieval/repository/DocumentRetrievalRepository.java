package document_qa_assistant.retrieval.repository;

import document_qa_assistant.retrieval.model.RetrievedChunk;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class DocumentRetrievalRepository {

    private final JdbcTemplate jdbcTemplate;

    public DocumentRetrievalRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RetrievedChunk> findSimilarChunks(
            String tenantId,
            UUID documentId,
            String category,
            float[] queryEmbedding,
            double similarityThreshold,
            int topK) {

        if (queryEmbedding == null || queryEmbedding.length == 0) {
            throw new IllegalArgumentException(
                    "Query embedding must not be empty");
        }

        if (topK <= 0) {
            throw new IllegalArgumentException(
                    "topK must be greater than zero");
        }

        if (similarityThreshold < 0.0 || similarityThreshold > 1.0) {
            throw new IllegalArgumentException(
                    "Similarity threshold must be between 0.0 and 1.0");
        }

        StringBuilder sql = new StringBuilder("""
                SELECT *
                FROM (
                    SELECT
                        dc.id,
                        dc.document_id,
                        dc.tenant_id,
                        d.category,
                        dc.content,
                        dc.page_number,
                        dc.chunk_index,
                        1 - (dc.embedding <=> CAST(? AS vector)) AS similarity
                    FROM document_chunks dc
                    JOIN documents d
                        ON d.id = dc.document_id
                    WHERE dc.tenant_id = ?
                      AND d.status = 'READY'
                """);

        if (documentId != null) {
            sql.append(" AND dc.document_id = ? ");
        }

        if (category != null && !category.isBlank()) {
            sql.append(" AND LOWER(d.category) = LOWER(?) ");
        }

        sql.append("""
                ) results
                WHERE results.similarity >= ?
                ORDER BY results.similarity DESC
                LIMIT ?
                """);

        String vector = toVectorLiteral(queryEmbedding);

        List<Object> args = new ArrayList<>();

        args.add(vector);
        args.add(tenantId);

        if (documentId != null) {
            args.add(documentId);
        }

        if (category != null && !category.isBlank()) {
            args.add(category.trim());
        }

        args.add(similarityThreshold);
        args.add(topK);

        return jdbcTemplate.query(
                sql.toString(),
                (resultSet, rowNum) -> mapRetrievedChunk(resultSet, rowNum),
                args.toArray());
    }

    private RetrievedChunk mapRetrievedChunk(
            java.sql.ResultSet resultSet,
            int rowNum) throws java.sql.SQLException {

        return new RetrievedChunk(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("document_id", UUID.class),
                resultSet.getString("tenant_id"),
                resultSet.getString("category"),
                resultSet.getString("content"),
                (Integer) resultSet.getObject("page_number"),
                resultSet.getInt("chunk_index"),
                resultSet.getDouble("similarity"));
    }

    private String toVectorLiteral(float[] embedding) {

        StringBuilder builder = new StringBuilder("[");

        for (int i = 0; i < embedding.length; i++) {

            if (i > 0) {
                builder.append(",");
            }

            builder.append(embedding[i]);
        }

        builder.append("]");

        return builder.toString();
    }
}