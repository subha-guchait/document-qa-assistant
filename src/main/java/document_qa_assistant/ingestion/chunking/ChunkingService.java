package document_qa_assistant.ingestion.chunking;

import document_qa_assistant.ingestion.extraction.ExtractedSection;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingService {

    private static final int CHUNK_SIZE_TOKENS = 800;
    private static final int OVERLAP_TOKENS = 120;

    private static final int CHARS_PER_TOKEN = 4;

    private static final int CHUNK_SIZE_CHARS =
            CHUNK_SIZE_TOKENS * CHARS_PER_TOKEN;

    private static final int OVERLAP_CHARS =
            OVERLAP_TOKENS * CHARS_PER_TOKEN;

    public List<DocumentChunk> chunk(
            List<ExtractedSection> sections
    ) {

        List<DocumentChunk> chunks = new ArrayList<>();

        int globalChunkIndex = 0;

        for (ExtractedSection section : sections) {

            String text = section.text();

            if (text == null || text.isBlank()) {
                continue;
            }

            int start = 0;

            while (start < text.length()) {

                int end = Math.min(
                        start + CHUNK_SIZE_CHARS,
                        text.length()
                );

                String content = text
                        .substring(start, end)
                        .trim();

                if (!content.isBlank()) {

                    int tokenCount =
                            estimateTokenCount(content);

                    chunks.add(
                            new DocumentChunk(
                                    content,
                                    section.pageNumber(),
                                    section.sectionNumber(),
                                    globalChunkIndex++,
                                    tokenCount
                            )
                    );
                }

                if (end == text.length()) {
                    break;
                }

                start = end - OVERLAP_CHARS;
            }
        }

        return chunks;
    }

    private int estimateTokenCount(String text) {
        return (int) Math.ceil(
                text.length() / (double) CHARS_PER_TOKEN
        );
    }
}