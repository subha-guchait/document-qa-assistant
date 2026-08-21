package document_qa_assistant.ingestion.service;

import document_qa_assistant.document.model.Document;
import document_qa_assistant.document.repository.DocumentRepository;
import document_qa_assistant.document.service.DocumentStatusService;
import document_qa_assistant.document_chunk.model.DocumentChunkEntity;
import document_qa_assistant.document_chunk.service.DocumentChunkPersistenceService;
import document_qa_assistant.ingestion.chunking.ChunkingService;
import document_qa_assistant.ingestion.chunking.DocumentChunk;
import document_qa_assistant.ingestion.embedding.EmbeddingService;
import document_qa_assistant.ingestion.extraction.ExtractedSection;
import document_qa_assistant.ingestion.extraction.ExtractorResolver;
import document_qa_assistant.ingestion.extraction.TextExtractor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@Service
public class IngestionService {

        private final ExecutorService ingestionExecutor;
        private final DocumentRepository documentRepository;
        private final ExtractorResolver extractorResolver;
        private final Path storagePath;
        private final ChunkingService chunkingService;
        private final EmbeddingService embeddingService;
        private final DocumentChunkPersistenceService documentChunkPersistenceService;
        private final DocumentStatusService documentStatusService;

        public IngestionService(
                        ExecutorService ingestionExecutor,
                        DocumentRepository documentRepository,
                        ExtractorResolver extractorResolver,
                        ChunkingService chunkingService,
                        EmbeddingService embeddingService,
                        DocumentChunkPersistenceService documentChunkPersistenceService,
                        DocumentStatusService documentStatusService,
                        @Value("${storage.path}") String storagePath) {

                this.ingestionExecutor = ingestionExecutor;
                this.documentRepository = documentRepository;
                this.extractorResolver = extractorResolver;
                this.chunkingService = chunkingService;
                this.embeddingService = embeddingService;
                this.documentChunkPersistenceService = documentChunkPersistenceService;
                this.documentStatusService = documentStatusService;
                this.storagePath = Paths.get(storagePath);
        }

        public void submit(UUID documentId) {
                ingestionExecutor.submit(() -> ingest(documentId));
        }

        private void ingest(UUID documentId) {

                try {
                        Document document = documentRepository.findById(documentId)
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "Document not found: " + documentId));

                        Path file = storagePath.resolve(
                                        document.getContentHash()
                                                        + "_"
                                                        + document.getFilename());

                        TextExtractor extractor = extractorResolver.resolve(document.getFilename());

                        List<ExtractedSection> sections = extractor.extract(file);

                        List<DocumentChunk> chunks = chunkingService.chunk(sections);

                        List<String> texts = chunks.stream()
                                        .map(DocumentChunk::content)
                                        .toList();

                        List<float[]> embeddings = embeddingService.embed(texts);

                        System.out.println(
                                        "Generated " + embeddings.size()
                                                        + " embeddings for "
                                                        + chunks.size()
                                                        + " chunks");

                        if (!embeddings.isEmpty()) {
                                System.out.println(
                                                "Embedding dimensions: "
                                                                + embeddings.get(0).length);
                        }

                        List<DocumentChunkEntity> chunkEntities = new ArrayList<>();

                        OffsetDateTime now = OffsetDateTime.now();

                        for (DocumentChunk chunk : chunks) {

                                DocumentChunkEntity entity = new DocumentChunkEntity();

                                entity.setId(UUID.randomUUID());
                                entity.setDocumentId(documentId);
                                entity.setTenantId(document.getTenantId());
                                entity.setChunkIndex(chunk.chunkIndex());
                                entity.setContent(chunk.content());
                                entity.setPageNumber(chunk.pageNumber());
                                entity.setTokenCount(chunk.tokenCount());
                                entity.setCreatedAt(now);

                                chunkEntities.add(entity);
                        }

                        documentChunkPersistenceService.saveChunks(
                                        documentId,
                                        document.getTenantId(),
                                        chunkEntities,
                                        embeddings);

                        documentStatusService.markReady(documentId);

                        System.out.println(
                                        "Document " + documentId
                                                        + " extracted " + sections.size()
                                                        + " sections");

                        System.out.println(
                                        "Document " + documentId
                                                        + " persisted " + chunks.size()
                                                        + " chunks");

                        chunks.stream()
                                        .limit(5)
                                        .forEach(chunk -> System.out.println(
                                                        "Chunk: " + chunk.chunkIndex()
                                                                        + ", page: " + chunk.pageNumber()
                                                                        + ", section: " + chunk.sectionNumber()
                                                                        + ", tokens: " + chunk.tokenCount()));

                } catch (IOException | RuntimeException exception) {

                        documentStatusService.markFailed(
                                        documentId,
                                        exception.getMessage());

                        System.err.println(
                                        "Ingestion failed for document "
                                                        + documentId
                                                        + ": "
                                                        + exception.getMessage());
                }
        }
}