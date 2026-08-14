package document_qa_assistant.ingestion.service;

import document_qa_assistant.document.model.Document;
import document_qa_assistant.document.repository.DocumentRepository;
import document_qa_assistant.ingestion.extraction.ExtractedSection;
import document_qa_assistant.ingestion.extraction.ExtractorResolver;
import document_qa_assistant.ingestion.extraction.TextExtractor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@Service
public class IngestionService {

    private final ExecutorService ingestionExecutor;
    private final DocumentRepository documentRepository;
    private final ExtractorResolver extractorResolver;
    private final Path storagePath;

    public IngestionService(
            ExecutorService ingestionExecutor,
            DocumentRepository documentRepository,
            ExtractorResolver extractorResolver,
            @Value("${storage.path}") String storagePath) {

        this.ingestionExecutor = ingestionExecutor;
        this.documentRepository = documentRepository;
        this.extractorResolver = extractorResolver;
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
                    document.getContentHash() + "_" + document.getFilename());

            TextExtractor extractor = extractorResolver.resolve(document.getFilename());

            List<ExtractedSection> sections = extractor.extract(file);

            System.out.println(
                    "Document " + documentId
                            + " extracted " + sections.size()
                            + " sections");

            for (ExtractedSection section : sections) {
                System.out.println(
                        "Page: " + section.pageNumber()
                                + ", characters: "
                                + section.text().length());
            }

        } catch (IOException | RuntimeException exception) {

            System.err.println(
                    "Ingestion failed for document "
                            + documentId
                            + ": "
                            + exception.getMessage());
        }
    }
}