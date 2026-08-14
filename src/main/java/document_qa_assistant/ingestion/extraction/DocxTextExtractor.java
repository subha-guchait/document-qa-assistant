package document_qa_assistant.ingestion.extraction;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class DocxTextExtractor implements TextExtractor {

    @Override
    public boolean supports(String filename) {
        return filename.toLowerCase().endsWith(".docx");
    }

    @Override
    public List<ExtractedSection> extract(Path file) throws IOException {

        List<ExtractedSection> sections = new ArrayList<>();

        try (InputStream inputStream = Files.newInputStream(file);
                XWPFDocument document = new XWPFDocument(inputStream)) {

            StringBuilder currentSection = new StringBuilder();

            int sectionNumber = 1;

            for (XWPFParagraph paragraph : document.getParagraphs()) {

                String text = paragraph.getText().trim();

                if (text.isBlank()) {
                    continue;
                }

                String style = paragraph.getStyle();

                boolean isHeading = style != null
                        && style.toLowerCase()
                                .startsWith("heading");

                if (isHeading && !currentSection.toString().isBlank()) {

                    sections.add(
                            new ExtractedSection(
                                    currentSection.toString().trim(),
                                    null,
                                    sectionNumber++));

                    currentSection.setLength(0);
                }

                if (!currentSection.isEmpty()) {
                    currentSection.append(
                            System.lineSeparator());
                }

                currentSection.append(text);
            }

            if (!currentSection.toString().isBlank()) {

                sections.add(
                        new ExtractedSection(
                                currentSection.toString().trim(),
                                null,
                                sectionNumber));
            }
        }

        return sections;
    }
}