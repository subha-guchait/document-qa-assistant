package document_qa_assistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import document_qa_assistant.common.config.RetrievalProperties;

@SpringBootApplication(exclude = {
		// Embedding auto-configs are excluded so that only the provider selected
		// via the AI_PROVIDER env var is activated. See EmbeddingConfig.
		org.springframework.ai.model.google.genai.autoconfigure.embedding.GoogleGenAiTextEmbeddingAutoConfiguration.class,
		org.springframework.ai.model.google.genai.autoconfigure.embedding.GoogleGenAiEmbeddingConnectionAutoConfiguration.class,
		org.springframework.ai.model.openai.autoconfigure.OpenAiEmbeddingAutoConfiguration.class,
		org.springframework.ai.model.openai.autoconfigure.OpenAiChatAutoConfiguration.class,
		org.springframework.ai.model.openai.autoconfigure.OpenAiImageAutoConfiguration.class,
		org.springframework.ai.model.openai.autoconfigure.OpenAiAudioSpeechAutoConfiguration.class,
		org.springframework.ai.model.openai.autoconfigure.OpenAiAudioTranscriptionAutoConfiguration.class,
		org.springframework.ai.model.openai.autoconfigure.OpenAiModerationAutoConfiguration.class,
		org.springframework.ai.model.ollama.autoconfigure.OllamaEmbeddingAutoConfiguration.class,
		org.springframework.ai.model.ollama.autoconfigure.OllamaChatAutoConfiguration.class,
		org.springframework.ai.model.google.genai.autoconfigure.chat.GoogleGenAiChatAutoConfiguration.class
})
@EnableConfigurationProperties(RetrievalProperties.class)
@ConfigurationPropertiesScan
public class DocumentQaAssistantApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocumentQaAssistantApplication.class, args);
	}

}
