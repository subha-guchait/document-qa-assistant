package document_qa_assistant.ingestion.extraction;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExtractorResolver {

    private final List<TextExtractor> extractors;

    public ExtractorResolver(List<TextExtractor> extractors) {
        this.extractors = extractors;
    }

    public TextExtractor resolve(String filename) {
        return extractors.stream()
                .filter(extractor -> extractor.supports(filename))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unsupported document type: " + filename));
    }
}