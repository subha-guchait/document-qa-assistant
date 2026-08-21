package document_qa_assistant.chat.service;

import document_qa_assistant.chat.dto.ChatResponse;
import document_qa_assistant.chat.dto.SourceReference;
import document_qa_assistant.chat.prompt.PromptBuilder;
import document_qa_assistant.conversation.model.Conversation;
import document_qa_assistant.conversation.model.Message;
import document_qa_assistant.conversation.model.MessageSource;
import document_qa_assistant.conversation.service.ConversationService;
import document_qa_assistant.retrieval.model.RetrievedChunk;
import document_qa_assistant.retrieval.service.RetrievalService;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

        private final RetrievalService retrievalService;
        private final PromptBuilder promptBuilder;
        private final ConversationService conversationService;
        private final ChatModel chatModel;

        public ChatService(
                        RetrievalService retrievalService,
                        PromptBuilder promptBuilder,
                        ConversationService conversationService,
                        ChatModel chatModel) {

                this.retrievalService = retrievalService;
                this.promptBuilder = promptBuilder;
                this.conversationService = conversationService;
                this.chatModel = chatModel;
        }

        public ChatResponse answer(
                        String tenantId,
                        String question,
                        String category,
                        String conversationId) {

                if (question == null || question.isBlank()) {
                        throw new IllegalArgumentException(
                                        "Question must not be empty");
                }

                Conversation conversation;

                if (conversationId == null || conversationId.isBlank()) {

                        conversation = conversationService.createConversation(
                                        tenantId,
                                        question);

                } else {

                        conversation = conversationService.getConversation(
                                        tenantId,
                                        UUID.fromString(conversationId));
                }

                UUID conversationUuid = conversation.getId();

                // Load previous conversation history before saving
                // the current question.
                List<Message> history = conversationService.getHistory(
                                tenantId,
                                conversationUuid);

                // Save the current user message.
                conversationService.saveMessage(
                                tenantId,
                                conversationUuid,
                                "USER",
                                question,
                                null,
                                null,
                                null);

                // Retrieve relevant document chunks.
                List<RetrievedChunk> chunks = retrievalService.retrieve(
                                tenantId,
                                null,
                                category,
                                question);

                // Refuse when no relevant document context exists.
                if (chunks.isEmpty()) {

                        String refusal = "I could not find relevant information in the provided documents.";

                        conversationService.saveMessage(
                                        tenantId,
                                        conversationUuid,
                                        "ASSISTANT",
                                        refusal,
                                        null,
                                        null,
                                        null);

                        return new ChatResponse(
                                        conversationUuid,
                                        refusal,
                                        List.of());
                }

                // Build prompt using history, documents and current question.
                String prompt = promptBuilder.build(
                                question,
                                history,
                                chunks);

                long startTime = System.currentTimeMillis();

                String answer = chatModel.call(prompt);

                long latency = System.currentTimeMillis() - startTime;

                // Save assistant message and keep the returned message
                // so its ID can be used for source persistence.
                Message assistantMessage = conversationService.saveMessage(
                                tenantId,
                                conversationUuid,
                                "ASSISTANT",
                                answer,
                                null,
                                null,
                                latency);

                // Persist the document chunks used to generate the answer.
                List<MessageSource> messageSources = chunks.stream()
                                .map(chunk -> {

                                        MessageSource source = new MessageSource();

                                        source.setMessageId(assistantMessage.getId());
                                        source.setChunkId(chunk.chunkId());
                                        source.setSimilarityScore(chunk.similarity());

                                        return source;
                                })
                                .toList();

                conversationService.saveMessageSources(
                                tenantId,
                                conversationUuid,
                                assistantMessage.getId(),
                                messageSources);

                // Build API source references.
                List<SourceReference> sources = chunks.stream()
                                .map(chunk -> new SourceReference(
                                                chunk.documentTitle(),
                                                chunk.pageNumber(),
                                                chunk.similarity(),
                                                chunk.content()))
                                .toList();

                return new ChatResponse(
                                conversationUuid,
                                answer,
                                sources);
        }
}