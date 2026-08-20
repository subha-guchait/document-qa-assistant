package document_qa_assistant.qa.service;

import document_qa_assistant.qa.model.QaResponse;
import document_qa_assistant.qa.model.SourceReference;
import document_qa_assistant.retrieval.model.RetrievedChunk;
import document_qa_assistant.retrieval.service.RetrievalService;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QaService {

    private final RetrievalService retrievalService;
    private final PromptBuilderService promptBuilderService;
    private final ChatModel chatModel;

    public QaService(
            RetrievalService retrievalService,
            PromptBuilderService promptBuilderService,
            ChatModel chatModel) {

        this.retrievalService = retrievalService;
        this.promptBuilderService = promptBuilderService;
        this.chatModel = chatModel;
    }

    public QaResponse answer(
            String tenantId,
            String question,
            String category) {

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question must not be empty");
        }

        List<RetrievedChunk> chunks = retrievalService.retrieve(
                tenantId,
                null,
                category,
                question);

        if (chunks.isEmpty()) {
            return new QaResponse(
                    "I could not find relevant information in the provided documents.",
                    List.of());
        }

        String prompt = promptBuilderService.build(
                question,
                chunks);

        String answer = chatModel.call(prompt);

        List<SourceReference> sources = chunks.stream()
                .map(chunk -> new SourceReference(
                        chunk.documentId(),
                        chunk.pageNumber(),
                        chunk.chunkIndex(),
                        chunk.category()))
                .toList();

        return new QaResponse(
                answer,
                sources);
    }
}