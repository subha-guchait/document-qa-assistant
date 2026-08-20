package document_qa_assistant.retrieval.service;

import document_qa_assistant.common.config.RetrievalProperties;
import document_qa_assistant.retrieval.model.RetrievedChunk;
import document_qa_assistant.retrieval.repository.DocumentRetrievalRepository;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RetrievalService {

    private final EmbeddingModel embeddingModel;
    private final DocumentRetrievalRepository retrievalRepository;
    private final RetrievalProperties retrievalProperties;

    public RetrievalService(
            EmbeddingModel embeddingModel,
            DocumentRetrievalRepository retrievalRepository,
            RetrievalProperties retrievalProperties) {

        this.embeddingModel = embeddingModel;
        this.retrievalRepository = retrievalRepository;
        this.retrievalProperties = retrievalProperties;
    }

    public List<RetrievedChunk> retrieve(
            String tenantId,
            UUID documentId,
            String category,
            String question) {

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question must not be empty");
        }

        int topK = retrievalProperties.getTopK();

        if (topK <= 0) {
            throw new IllegalArgumentException(
                    "Retrieval topK must be greater than zero");
        }

        float[] queryEmbedding = embeddingModel.embed(question);
        double similarityThreshold = retrievalProperties.getSimilarityThreshold();

        return retrievalRepository.findSimilarChunks(
                tenantId,
                documentId,
                category,
                queryEmbedding,
                similarityThreshold,
                topK);
    }
}