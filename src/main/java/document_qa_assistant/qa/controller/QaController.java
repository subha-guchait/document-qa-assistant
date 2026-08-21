package document_qa_assistant.qa.controller;

import document_qa_assistant.qa.dto.QaRequest;
import document_qa_assistant.qa.dto.QaResponse;
import document_qa_assistant.qa.service.QaService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/qa")
public class QaController {

    private final QaService qaService;

    public QaController(QaService qaService) {
        this.qaService = qaService;
    }

    @PostMapping
    public ResponseEntity<QaResponse> answer(
            @RequestHeader("X-Tenant-Id") @NotBlank(message = "X-Tenant-Id must not be blank") String tenantId,

            @Valid @RequestBody QaRequest request) {

        QaResponse response = qaService.answer(
                tenantId,
                request.question(),
                request.category());

        return ResponseEntity.ok(response);
    }
}