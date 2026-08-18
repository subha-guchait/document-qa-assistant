package document_qa_assistant.ingestion.chunking;

import document_qa_assistant.ingestion.extraction.ExtractedSection;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChunkingServiceTest {

    private final ChunkingService chunkingService = new ChunkingService();

    @Test
    void shouldReturnNoChunksForEmptyInput() {

        List<DocumentChunk> chunks = chunkingService.chunk(List.of());

        assertTrue(chunks.isEmpty());
    }

    @Test
    void shouldCreateOneChunkForShortInput() {

        ExtractedSection section = new ExtractedSection(
                "Hello",
                1,
                1);

        List<DocumentChunk> chunks = chunkingService.chunk(List.of(section));

        assertEquals(1, chunks.size());

        assertEquals(
                "Hello",
                chunks.get(0).content());

        assertEquals(
                1,
                chunks.get(0).pageNumber());

        assertEquals(
                1,
                chunks.get(0).sectionNumber());

        assertEquals(
                0,
                chunks.get(0).chunkIndex());
    }

    @Test
    void shouldCreateMultipleOverlappingChunks() {

        String text = "a".repeat(7000);

        ExtractedSection section = new ExtractedSection(
                text,
                1,
                1);

        List<DocumentChunk> chunks = chunkingService.chunk(List.of(section));

        assertTrue(chunks.size() > 1);

        assertEquals(
                0,
                chunks.get(0).chunkIndex());

        assertEquals(
                1,
                chunks.get(1).chunkIndex());

        String firstChunk = chunks.get(0).content();

        String secondChunk = chunks.get(1).content();

        assertTrue(
                firstChunk.substring(
                        firstChunk.length() - 480).equals(
                                secondChunk.substring(0, 480)));
    }
}