package document_qa_assistant.qa.dto;

import java.util.List;

public record QaResponse(
                String answer,
                List<SourceReference> sources) {
}