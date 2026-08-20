package document_qa_assistant.common.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingModel;
import org.springframework.ai.google.genai.text.GoogleGenAiTextEmbeddingOptions;
import org.springframework.ai.google.genai.embedding.GoogleGenAiEmbeddingConnectionDetails;

import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import com.openai.client.OpenAIClient;
import com.openai.client.OpenAIClientImpl;
import com.openai.core.ClientOptions;

import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;

/**
 * Creates a single {@link EmbeddingModel} bean based on the
 * {@code embedding.provider} property (sourced from the {@code EMBEDDING_AI_PROVIDER}
 * environment variable, defaults to {@code gemini}).
 *
 * <p>
 * All provider auto-configurations are excluded in
 * {@link document_qa_assistant.DocumentQaAssistantApplication} to prevent
 * startup failures from missing API keys for unused providers.
 *
 * <p>
 * Supported providers:
 * <ul>
 * <li>{@code gemini} — Google Gemini (default)</li>
 * <li>{@code openai} — OpenAI</li>
 * <li>{@code ollama} — Ollama (local)</li>
 * <li>{@code grok} — xAI Grok (OpenAI-compatible API)</li>
 * </ul>
 */
@Configuration
public class EmbeddingConfig {

    @Bean
    @ConditionalOnProperty(name = "embedding.provider", havingValue = "gemini", matchIfMissing = true)
    public EmbeddingModel geminiEmbeddingModel(
            @Value("${spring.ai.google.genai.embedding.api-key}") String apiKey,
            @Value("${spring.ai.google.genai.embedding.text.model:gemini-embedding-001}") String model,
            @Value("${spring.ai.google.genai.embedding.text.dimensions:1536}") int dimensions) {

        GoogleGenAiEmbeddingConnectionDetails connectionDetails = GoogleGenAiEmbeddingConnectionDetails.builder()
                .apiKey(apiKey)
                .build();

        GoogleGenAiTextEmbeddingOptions options = GoogleGenAiTextEmbeddingOptions.builder()
                .model(model)
                .dimensions(dimensions)
                .build();

        return new GoogleGenAiTextEmbeddingModel(connectionDetails, options);
    }

    @Bean
    @ConditionalOnProperty(name = "embedding.provider", havingValue = "openai")
    public EmbeddingModel openAiEmbeddingModel(
            @Value("${spring.ai.openai.api-key}") String apiKey,
            @Value("${spring.ai.openai.base-url:https://api.openai.com}") String baseUrl,
            @Value("${spring.ai.openai.embedding.options.model:text-embedding-3-small}") String model,
            @Value("${spring.ai.openai.embedding.options.dimensions:1536}") int dimensions) {

        ClientOptions clientOptions = ClientOptions.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();
        OpenAIClient client = new OpenAIClientImpl(clientOptions);

        OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                .model(model)
                .dimensions(dimensions)
                .build();

        return OpenAiEmbeddingModel.builder()
                .openAiClient(client)
                .metadataMode(org.springframework.ai.document.MetadataMode.EMBED)
                .options(options)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "embedding.provider", havingValue = "ollama")
    public EmbeddingModel ollamaEmbeddingModel(
            @Value("${spring.ai.ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${spring.ai.ollama.embedding.options.model:nomic-embed-text}") String model) {

        OllamaApi api = OllamaApi.builder().baseUrl(baseUrl).build();

        OllamaEmbeddingOptions options = OllamaEmbeddingOptions.builder()
                .model(model)
                .build();

        return OllamaEmbeddingModel.builder()
                .ollamaApi(api)
                .options(options)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "embedding.provider", havingValue = "grok")
    public EmbeddingModel grokEmbeddingModel(
            @Value("${XAI_API_KEY}") String apiKey,
            @Value("${spring.ai.grok.base-url:https://api.x.ai/v1}") String baseUrl,
            @Value("${spring.ai.grok.embedding.model}") String model,
            @Value("${spring.ai.grok.embedding.dimensions:1536}") int dimensions) {

        ClientOptions clientOptions = ClientOptions.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();

        OpenAIClient client = new OpenAIClientImpl(clientOptions);

        OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                .model(model)
                .dimensions(dimensions)
                .build();

        return OpenAiEmbeddingModel.builder()
                .openAiClient(client)
                .metadataMode(org.springframework.ai.document.MetadataMode.EMBED)
                .options(options)
                .build();
    }
}
