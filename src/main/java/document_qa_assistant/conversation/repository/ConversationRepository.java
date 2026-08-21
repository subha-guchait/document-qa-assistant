package document_qa_assistant.conversation.repository;

import document_qa_assistant.conversation.model.Conversation;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ConversationRepository
        extends CrudRepository<Conversation, UUID> {
}