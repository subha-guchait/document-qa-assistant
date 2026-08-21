package document_qa_assistant.chat.prompt;

import document_qa_assistant.conversation.model.Message;
import document_qa_assistant.retrieval.model.RetrievedChunk;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PromptBuilder {

    public String build(
            String question,
            List<Message> history,
            List<RetrievedChunk> chunks) {

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question must not be empty");
        }

        StringBuilder prompt = new StringBuilder();

        prompt.append(PromptRules.SYSTEM_RULES)
                .append("\n\n");

        appendConversationHistory(prompt, history);
        appendDocumentContext(prompt, chunks);

        prompt.append("""
                USER QUESTION
                --------------

                """)
                .append(question)
                .append("\n");

        return prompt.toString();
    }

    private void appendConversationHistory(
            StringBuilder prompt,
            List<Message> history) {

        if (history == null || history.isEmpty()) {
            return;
        }

        prompt.append("""
                CONVERSATION HISTORY
                --------------------

                The following conversation history is provided only
                to understand the context of the current question.

                """);

        for (Message message : history) {
            prompt.append(message.getRole())
                    .append(": ")
                    .append(message.getContent())
                    .append("\n\n");
        }
    }

    private void appendDocumentContext(
            StringBuilder prompt,
            List<RetrievedChunk> chunks) {

        prompt.append("""
                DOCUMENT CONTEXT
                ----------------

                """);

        if (chunks == null || chunks.isEmpty()) {
            prompt.append("No relevant document context was retrieved.\n\n");
            return;
        }

        for (int i = 0; i < chunks.size(); i++) {

            RetrievedChunk chunk = chunks.get(i);

            prompt.append("SOURCE ")
                    .append(i + 1)
                    .append("\n");

            prompt.append("Document ID: ")
                    .append(chunk.documentId())
                    .append("\n");

            if (chunk.documentTitle() != null) {
                prompt.append("Document Title: ")
                        .append(chunk.documentTitle())
                        .append("\n");
            }

            if (chunk.category() != null) {
                prompt.append("Category: ")
                        .append(chunk.category())
                        .append("\n");
            }

            if (chunk.pageNumber() != null) {
                prompt.append("Page: ")
                        .append(chunk.pageNumber())
                        .append("\n");
            }

            prompt.append("Chunk: ")
                    .append(chunk.chunkIndex())
                    .append("\n");

            prompt.append("Content:\n")
                    .append(chunk.content())
                    .append("\n\n");
        }
    }
}