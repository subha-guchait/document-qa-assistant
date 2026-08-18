package document_qa_assistant.document.service;

import document_qa_assistant.document.enums.DocumentStatus;
import document_qa_assistant.document.exception.DocumentAlreadyExistsException;
import document_qa_assistant.document.exception.FileTooLargeException;
import document_qa_assistant.document.exception.UnsupportedDocumentTypeException;
import document_qa_assistant.document.model.Document;
import document_qa_assistant.document.repository.DocumentRepository;
import document_qa_assistant.ingestion.service.IngestionService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;

import org.springframework.jdbc.core.JdbcTemplate;

@Service
public class DocumentService {

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;

    private final DocumentRepository documentRepository;
    private final Path storagePath;
    private final IngestionService ingestionService;

    public DocumentService(
            DocumentRepository documentRepository,
            IngestionService ingestionService,
            JdbcTemplate jdbcTemplate,
            @Value("${storage.path}") String storagePath) {

        this.documentRepository = documentRepository;
        this.ingestionService = ingestionService;
        this.storagePath = Paths.get(storagePath);
    }

    public Document upload(
            MultipartFile file,
            String tenantId,
            String title,
            String category) throws IOException {

        validateFile(file);

        String contentHash = calculateSha256(file);

        if (documentRepository
                .findByTenantIdAndContentHash(tenantId, contentHash)
                .isPresent()) {

            throw new DocumentAlreadyExistsException(
                    "Document has already been uploaded");
        }

        Files.createDirectories(storagePath);

        String filename = file.getOriginalFilename();

        Path targetPath = storagePath.resolve(contentHash + "_" + filename);

        file.transferTo(targetPath);

        OffsetDateTime now = OffsetDateTime.now();

        Document document = new Document();

        document.setTenantId(tenantId);
        document.setTitle(
                title == null || title.isBlank()
                        ? filename
                        : title);
        document.setCategory(category);
        document.setFilename(filename);
        document.setContentHash(contentHash);
        document.setSizeBytes(file.getSize());
        document.setStatus(DocumentStatus.PROCESSING.name());
        document.setCreatedAt(now);
        document.setUpdatedAt(now);

        Document saved = documentRepository.save(document);
        ingestionService.submit(saved.getId());
        return saved;
    }

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileTooLargeException(
                    "File size must not exceed 20 MB");
        }

        String filename = file.getOriginalFilename();

        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Filename is required");
        }

        String lowerCaseFilename = filename.toLowerCase();

        boolean supported = lowerCaseFilename.endsWith(".pdf")
                || lowerCaseFilename.endsWith(".docx")
                || lowerCaseFilename.endsWith(".txt")
                || lowerCaseFilename.endsWith(".md")
                || lowerCaseFilename.endsWith(".markdown");

        if (!supported) {
            throw new UnsupportedDocumentTypeException(
                    "Unsupported file type. Supported types: PDF, DOCX, TXT, Markdown");
        }
    }

    private String calculateSha256(MultipartFile file) throws IOException {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(file.getBytes());

            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    e);
        }
    }
}