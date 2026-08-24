package document_qa_assistant.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.base-url:http://localhost:${server.port:8080}}")
    private String baseUrl;

    @Bean
    public OpenAPI documentQaOpenApi() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url(baseUrl)
                                .description("Current environment")));
    }

    private Info apiInfo() {
        return new Info()
                .title("Document QA Assistant API")
                .version("1.0.0")
                .description("""
                        REST API for the Document QA Assistant — an AI-powered \
                        document ingestion and question-answering system.

                        **Features:**
                        - Upload and manage documents (PDF, DOCX, TXT, etc.)
                        - Ask natural-language questions grounded in your documents
                        - Multi-turn conversations with context retention
                        - Multi-tenant isolation via X-Tenant-Id header

                        **Authentication:**
                        All endpoints require the `X-Tenant-Id` header for tenant isolation.""")
                .contact(new Contact()
                        .name("Document QA Assistant Team")
                        .url("https://github.com/subha-guchait/document-qa-assistant"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }
}
