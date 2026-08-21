package document_qa_assistant.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "conversation")
public class ConversationProperties {

    private int maxHistoryTurns = 6;

    private int maxHistoryTokens = 3000;

    public int getMaxHistoryTurns() {
        return maxHistoryTurns;
    }

    public void setMaxHistoryTurns(int maxHistoryTurns) {
        this.maxHistoryTurns = maxHistoryTurns;
    }

    public int getMaxHistoryTokens() {
        return maxHistoryTokens;
    }

    public void setMaxHistoryTokens(int maxHistoryTokens) {
        this.maxHistoryTokens = maxHistoryTokens;
    }
}