package document_qa_assistant.conversation.service;

import document_qa_assistant.common.config.ConversationProperties;
import document_qa_assistant.conversation.model.Conversation;
import document_qa_assistant.conversation.model.Message;
import document_qa_assistant.conversation.model.MessageSource;
import document_qa_assistant.conversation.repository.ConversationRepository;
import document_qa_assistant.conversation.repository.MessageRepository;
import document_qa_assistant.conversation.repository.MessageSourceRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ConversationProperties conversationProperties;
    private final TokenEstimator tokenEstimator;
    private final MessageSourceRepository messageSourceRepository;

    public ConversationService(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            MessageSourceRepository messageSourceRepository,
            ConversationProperties conversationProperties,
            TokenEstimator tokenEstimator) {

        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.messageSourceRepository = messageSourceRepository;
        this.conversationProperties = conversationProperties;
        this.tokenEstimator = tokenEstimator;
    }

    @Transactional
    public Conversation createConversation(
            String tenantId,
            String title) {

        Conversation conversation = new Conversation();

        conversation.setTenantId(tenantId);
        conversation.setTitle(title);
        conversation.setCreatedAt(OffsetDateTime.now());

        return conversationRepository.save(conversation);
    }

    public Conversation getConversation(
            String tenantId,
            UUID conversationId) {

        return conversationRepository
                .findById(conversationId)
                .filter(conversation -> tenantId.equals(conversation.getTenantId()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Conversation not found"));
    }

    @Transactional
    public Message saveMessage(
            String tenantId,
            UUID conversationId,
            String role,
            String content,
            Integer tokenCount,
            String model,
            Long latencyMs) {

        Conversation conversation = getConversation(tenantId, conversationId);

        Message message = new Message();

        message.setConversationId(conversation.getId());
        message.setRole(role);
        message.setContent(content);

        int estimatedTokens = tokenCount != null
                ? tokenCount
                : tokenEstimator.estimate(content);

        message.setTokenCount(estimatedTokens);
        message.setModel(model);
        message.setLatencyMs(latencyMs);
        message.setCreatedAt(OffsetDateTime.now());

        Message savedMessage = messageRepository.save(message);

        conversation.setLastMessageAt(
                savedMessage.getCreatedAt());

        conversationRepository.save(conversation);

        return savedMessage;
    }

    public List<Message> getHistory(
            String tenantId,
            UUID conversationId) {

        getConversation(tenantId, conversationId);

        List<Message> messages = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(
                        conversationId);

        if (messages.isEmpty()) {
            return List.of();
        }

        int maxTurns = conversationProperties.getMaxHistoryTurns();

        int maxTokens = conversationProperties.getMaxHistoryTokens();

        if (maxTurns <= 0 || maxTokens <= 0) {
            return List.of();
        }

        /*
         * Walk backwards through the conversation.
         *
         * A turn is considered a USER + ASSISTANT exchange.
         * We therefore allow up to maxTurns * 2 messages.
         */
        List<Message> selected = new ArrayList<>();

        int totalTokens = 0;
        int turns = 0;

        for (int i = messages.size() - 1; i >= 0;) {

            Message current = messages.get(i);

            /*
             * A USER message starts a conversational turn.
             * We first find the USER message and then include
             * the following ASSISTANT message when available.
             */
            if ("ASSISTANT".equals(current.getRole())) {

                selected.add(current);
                totalTokens += safeTokenCount(current);

                i--;

                if (i >= 0 && "USER".equals(messages.get(i).getRole())) {

                    Message userMessage = messages.get(i);

                    int userTokens = safeTokenCount(userMessage);

                    if (totalTokens + userTokens > maxTokens) {
                        selected.remove(selected.size() - 1);
                        break;
                    }

                    selected.add(userMessage);
                    totalTokens += userTokens;
                    turns++;

                    i--;

                    if (turns >= maxTurns) {
                        break;
                    }
                }

            } else {

                int messageTokens = safeTokenCount(current);

                if (totalTokens + messageTokens > maxTokens) {
                    break;
                }

                selected.add(current);
                totalTokens += messageTokens;
                turns++;

                i--;
            }
        }

        Collections.reverse(selected);

        return selected;
    }

    public List<Message> getMessages(
            String tenantId,
            UUID conversationId) {

        // Verify that the conversation belongs to the tenant.
        getConversation(tenantId, conversationId);

        return messageRepository
                .findByConversationIdOrderByCreatedAtAsc(
                        conversationId);
    }

    private int safeTokenCount(Message message) {

        if (message.getTokenCount() != null) {
            return message.getTokenCount();
        }

        return tokenEstimator.estimate(
                message.getContent());
    }

    @Transactional
    public void saveMessageSources(
            String tenantId,
            UUID conversationId,
            UUID messageId,
            List<MessageSource> sources) {

        // Verify conversation belongs to tenant.
        getConversation(tenantId, conversationId);

        if (sources == null || sources.isEmpty()) {
            return;
        }

        sources.forEach(source -> source.setMessageId(messageId));

        messageSourceRepository.saveAll(sources);
    }
}