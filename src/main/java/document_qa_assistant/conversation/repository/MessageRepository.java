package document_qa_assistant.conversation.repository;

import document_qa_assistant.conversation.model.Message;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepository
        extends CrudRepository<Message, UUID> {

    List<Message> findByConversationIdOrderByCreatedAtAsc(
            UUID conversationId);
}