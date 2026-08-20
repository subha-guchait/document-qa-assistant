package document_qa_assistant.retrieval.controller;

import document_qa_assistant.retrieval.model.RetrievedChunk;
import document_qa_assistant.retrieval.service.RetrievalService;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/retrieval")
public class RetrievalController {

    private final RetrievalService retrievalService;

    public RetrievalController(RetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

@GetMapping
public List<RetrievedChunk> retrieve(
        @RequestHeader("X-Tenant-Id") String tenantId,
        @RequestParam String question,
        @RequestParam(required = false) UUID documentId,
        @RequestParam(required = false) String category) {

    return retrievalService.retrieve(
            tenantId,
            documentId,
            category,
            question);
}
}