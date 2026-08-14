package document_qa_assistant.ingestion.extraction;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class PlainTextExtractor implements TextExtractor {

    @Override
    public boolean supports(String filename) {
        String lowerCaseFilename = filename.toLowerCase();

        return lowerCaseFilename.endsWith(".txt")
                || lowerCaseFilename.endsWith(".md")
                || lowerCaseFilename.endsWith(".markdown");
    }

    @Override
    public List<ExtractedSection> extract(Path file) throws IOException {

        String filename = file.getFileName().toString().toLowerCase();

        String text = Files.readString(
                file,
                StandardCharsets.UTF_8);

        if (text.isBlank()) {
            return List.of();
        }

        if (filename.endsWith(".md")
                || filename.endsWith(".markdown")) {

            return extractMarkdownSections(text);
        }

        return extractTextSections(text);
    }

    private List<ExtractedSection> extractMarkdownSections(
            String text) {

        String[] lines = text.split("\\R");

        List<ExtractedSection> sections = new ArrayList<>();

        StringBuilder currentSection = new StringBuilder();

        int sectionNumber = 1;

        for (String line : lines) {

            if (line.matches("^#{1,6}\\s+.+")) {

                if (!currentSection.toString().isBlank()) {

                    sections.add(
                            new ExtractedSection(
                                    currentSection.toString().trim(),
                                    null,
                                    sectionNumber++));

                    currentSection.setLength(0);
                }
            }

            if (!currentSection.isEmpty()) {
                currentSection.append(System.lineSeparator());
            }

            currentSection.append(line);
        }

        if (!currentSection.toString().isBlank()) {

            sections.add(
                    new ExtractedSection(
                            currentSection.toString().trim(),
                            null,
                            sectionNumber));
        }

        return sections;
    }

    private List<ExtractedSection> extractTextSections(
            String text) {

        String[] blocks = text.split("\\R\\s*\\R+");

        List<ExtractedSection> sections = new ArrayList<>();

        int sectionNumber = 1;

        for (String block : blocks) {

            String cleanedText = block.trim();

            if (cleanedText.isBlank()) {
                continue;
            }

            sections.add(
                    new ExtractedSection(
                            cleanedText,
                            null,
                            sectionNumber++));
        }

        return sections;
    }
}