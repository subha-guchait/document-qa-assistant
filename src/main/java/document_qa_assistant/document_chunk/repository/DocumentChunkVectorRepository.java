package document_qa_assistant.document_chunk.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class DocumentChunkVectorRepository {

    private final JdbcTemplate jdbcTemplate;

    public DocumentChunkVectorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveEmbedding(
            UUID chunkId,
            float[] embedding) {
        String vector = toVectorLiteral(embedding);

        jdbcTemplate.update(
                """
                        UPDATE document_chunks
                        SET embedding = CAST(? AS vector)
                        WHERE id = ?
                        """,
                vector,
                chunkId);
    }

    private String toVectorLiteral(float[] embedding) {

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