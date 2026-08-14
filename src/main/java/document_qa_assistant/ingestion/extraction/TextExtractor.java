package document_qa_assistant.ingestion.extraction;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface TextExtractor {

    boolean supports(String filename);

    List<ExtractedSection> extract(Path file) throws IOException;
}