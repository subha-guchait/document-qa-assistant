package document_qa_assistant.qa.model;

import java.util.List;

public record QaResponse(
        String answer,
        List<SourceReference> sources) {
}