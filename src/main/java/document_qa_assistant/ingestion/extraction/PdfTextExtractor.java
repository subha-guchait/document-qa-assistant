package document_qa_assistant.ingestion.extraction;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class PdfTextExtractor implements TextExtractor {

    @Override
    public boolean supports(String filename) {
        return filename.toLowerCase().endsWith(".pdf");
    }

    @Override
    public List<ExtractedSection> extract(Path file) throws IOException {

        List<ExtractedSection> sections = new ArrayList<>();

        byte[] pdfBytes = Files.readAllBytes(file);

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {

            PDFTextStripper stripper = new PDFTextStripper();

            int pageCount = document.getNumberOfPages();

            for (int pageNumber = 1; pageNumber <= pageCount; pageNumber++) {

                stripper.setStartPage(pageNumber);
                stripper.setEndPage(pageNumber);

                String text = stripper.getText(document).trim();

                if (!text.isBlank()) {
                    sections.add(
                            new ExtractedSection(
                                    text,
                                    pageNumber,
                                    pageNumber));
                }
            }
        }

        return sections;
    }
}