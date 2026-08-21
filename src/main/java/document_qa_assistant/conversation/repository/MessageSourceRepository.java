package document_qa_assistant.conversation.repository;

import document_qa_assistant.conversation.model.MessageSource;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface MessageSourceRepository
        extends CrudRepository<MessageSource, UUID> {

    List<MessageSource> findByMessageId(UUID messageId);
}