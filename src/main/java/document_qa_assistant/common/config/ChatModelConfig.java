package document_qa_assistant.common.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import com.google.genai.Client;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import com.openai.client.OpenAIClient;
import com.openai.client.OpenAIClientImpl;
import com.openai.core.ClientOptions;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;

/**
 * Creates a single {@link ChatModel} bean based on the
 * {@code chat.provider} property (sourced from the {@code CHAT_AI_PROVIDER}
 * environment variable, defaults to {@code gemini}).
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
public class ChatModelConfig {

        @Bean
        @ConditionalOnProperty(name = "chat.provider", havingValue = "gemini", matchIfMissing = true)
        public ChatModel geminiChatModel(
                        @Value("${spring.ai.google.genai.api-key}") String apiKey,
                        @Value("${spring.ai.google.genai.chat.model:gemini-2.5-flash}") String model) {

                Client client = Client.builder().apiKey(apiKey).build();

                GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                                .model(model)
                                .build();

                return GoogleGenAiChatModel.builder()
                                .genAiClient(client)
                                .options(options)
                                .build();
        }

        @Bean
        @ConditionalOnProperty(name = "chat.provider", havingValue = "openai")
        public ChatModel openAiChatModel(
                        @Value("${spring.ai.openai.api-key}") String apiKey,
                        @Value("${spring.ai.openai.base-url:https://api.openai.com}") String baseUrl,
                        @Value("${spring.ai.openai.chat.model:gpt-4o-mini}") String model) {

                ClientOptions clientOptions = ClientOptions.builder()
                                .apiKey(apiKey)
                                .baseUrl(baseUrl)
                                .build();
                OpenAIClient client = new OpenAIClientImpl(clientOptions);

                OpenAiChatOptions options = OpenAiChatOptions.builder()
                                .model(model)
                                .build();

                return OpenAiChatModel.builder()
                                .openAiClient(client)
                                .options(options)
                                .build();
        }

        @Bean
        @ConditionalOnProperty(name = "chat.provider", havingValue = "ollama")
        public ChatModel ollamaChatModel(
                        @Value("${spring.ai.ollama.base-url:http://localhost:11434}") String baseUrl,
                        @Value("${spring.ai.ollama.chat.model:llama3.2}") String model) {

                OllamaApi api = OllamaApi.builder().baseUrl(baseUrl).build();

                OllamaChatOptions options = OllamaChatOptions.builder()
                                .model(model)
                                .build();

                return OllamaChatModel.builder()
                                .ollamaApi(api)
                                .options(options)
                                .build();
        }

        @Bean
        @ConditionalOnProperty(name = "chat.provider", havingValue = "grok")
        public ChatModel grokChatModel(
                        @Value("${XAI_API_KEY}") String apiKey,
                        @Value("${spring.ai.grok.base-url:https://api.x.ai/v1}") String baseUrl,
                        @Value("${spring.ai.grok.chat.options.model:grok-2-latest}") String model) {

                ClientOptions clientOptions = ClientOptions.builder()
                                .apiKey(apiKey)
                                .baseUrl(baseUrl)
                                .build();

                OpenAIClient client = new OpenAIClientImpl(clientOptions);

                OpenAiChatOptions options = OpenAiChatOptions.builder()
                                .model(model)
                                .build();

                return OpenAiChatModel.builder()
                                .openAiClient(client)
                                .options(options)
                                .build();
        }
}
