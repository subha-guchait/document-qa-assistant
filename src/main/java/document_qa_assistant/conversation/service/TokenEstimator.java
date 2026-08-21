package document_qa_assistant.conversation.service;

import org.springframework.stereotype.Component;

@Component
public class TokenEstimator {

    public int estimate(String text) {

        if (text == null || text.isBlank()) {
            return 0;
        }

        return Math.max(1, (text.length() + 3) / 4);
    }
}