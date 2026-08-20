package document_qa_assistant.qa.service;

import document_qa_assistant.retrieval.model.RetrievedChunk;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromptBuilderService {

    public String build(
            String question,
            List<RetrievedChunk> chunks) {

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question must not be empty");
        }

        if (chunks == null || chunks.isEmpty()) {
            return """
                    You are a document question-answering assistant.

                    The provided documents do not contain enough relevant
                    information to answer the user's question.

                    Do not invent or guess an answer.

                    User question:
                    %s
                    """.formatted(question);
        }

        StringBuilder context = new StringBuilder();

        for (int i = 0; i < chunks.size(); i++) {

            RetrievedChunk chunk = chunks.get(i);

            context.append("SOURCE ").append(i + 1).append("\n");
            context.append("Document ID: ")
                    .append(chunk.documentId())
                    .append("\n");

            if (chunk.category() != null) {
                context.append("Category: ")
                        .append(chunk.category())
                        .append("\n");
            }

            if (chunk.pageNumber() != null) {
                context.append("Page: ")
                        .append(chunk.pageNumber())
                        .append("\n");
            }

            context.append("Chunk: ")
                    .append(chunk.chunkIndex())
                    .append("\n");

            context.append("Content:\n")
                    .append(chunk.content())
                    .append("\n\n");
        }

        return """
                You are a document question-answering assistant.

                Answer the user's question using ONLY the provided document
                context.

                Rules:
                - Do not use outside knowledge.
                - Do not invent information.
                - If the context does not contain enough information to answer
                  the question, clearly say that the information was not found
                  in the provided documents.
                - Give a concise and accurate answer.
                - Preserve important numbers, dates, names, and conditions.

                DOCUMENT CONTEXT
                ----------------
                %s

                USER QUESTION
                --------------
                %s
                """.formatted(
                context,
                question);
    }
}