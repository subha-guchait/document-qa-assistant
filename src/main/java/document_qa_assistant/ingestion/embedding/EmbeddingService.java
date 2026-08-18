package document_qa_assistant.ingestion.embedding;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public List<float[]> embed(List<String> texts) {

        if (texts == null || texts.isEmpty()) {
            return List.of();
        }

        return embeddingModel.embed(texts);
    }
}