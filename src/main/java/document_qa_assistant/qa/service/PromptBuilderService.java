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

                Your task is to answer the user's question using ONLY the
                document context provided below.

                IMPORTANT:
                The provided document context is the only source of truth.

                Rules:
                - Answer ONLY from the provided document context.
                - Do not use your own knowledge or information that is not present
                  in the provided context.
                - Every factual statement in your answer must be supported by the
                  provided context.
                - Do not infer, assume, or add related information that is not
                  explicitly supported by the context.
                - Do not use information from documents, pages, or sections that
                  were not provided in the context.
                - If the context does not contain enough information to answer
                  the question, say:
                  "I could not find enough information in the provided documents
                  to answer this question."
                - Preserve exact dates, amounts, names, rules, and conditions
                  from the documents.
                - Do not add recommendations, explanations, or background
                  information unless they are supported by the context.
                - Keep the answer concise and directly answer the user's question.
                - Prefer 1–3 short paragraphs or a concise bullet list.
                - For simple factual questions, answer in 1–3 sentences.
                - For questions requiring multiple items, use a concise bullet list.
                - Do not repeat information unnecessarily.
                - Keep the answer under 200 words unless the question requires
                  more detail.

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