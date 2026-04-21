package com.maayn.notificationservice.controller;

import com.maayn.notificationservice.entity.WorkflowStep;
import com.maayn.notificationservice.entity.WorkflowTemplate;
import com.maayn.notificationservice.repository.WorkflowTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * CRUD for {@link WorkflowTemplate}. Admins use these routes to define which
 * employee roles must approve what kind of entity (LOAN / MORTGAGE /
 * CERTIFICATE / custom) and in what order. New {@link WorkflowInstance}s are
 * spawned off the latest active template for an entity type.
 *
 * <p>All routes live under {@code /api/workflow/templates} so they are covered
 * by the gateway's existing {@code /api/workflow/**} JWT-protected route.</p>
 */
@RestController
@RequestMapping("/api/workflow/templates")
@RequiredArgsConstructor
public class WorkflowTemplateController {

    private final WorkflowTemplateRepository templateRepository;

    @GetMapping
    public ResponseEntity<List<WorkflowTemplate>> list() {
        return ResponseEntity.ok(templateRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkflowTemplate> get(@PathVariable UUID id) {
        return templateRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-entity/{entityType}")
    public ResponseEntity<WorkflowTemplate> byEntity(@PathVariable String entityType) {
        return templateRepository.findByEntityType(entityType.toUpperCase())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<WorkflowTemplate> create(@RequestBody TemplateRequest req) {
        LocalDateTime now = LocalDateTime.now();
        List<WorkflowStep> steps = normaliseSteps(req.steps());
        WorkflowTemplate template = WorkflowTemplate.builder()
                .id(UUID.randomUUID())
                .entityType(req.entityType() == null ? "" : req.entityType().toUpperCase())
                .steps(steps)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(templateRepository.save(template));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkflowTemplate> update(
            @PathVariable UUID id,
            @RequestBody TemplateRequest req
    ) {
        return templateRepository.findById(id)
                .map(existing -> {
                    if (req.entityType() != null) {
                        existing.setEntityType(req.entityType().toUpperCase());
                    }
                    if (req.steps() != null) {
                        existing.setSteps(normaliseSteps(req.steps()));
                    }
                    existing.setUpdatedAt(LocalDateTime.now());
                    return ResponseEntity.ok(templateRepository.save(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!templateRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        templateRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Renumber supplied steps starting at 1 and generate ids where missing so
     * the frontend can push partial drafts without worrying about ordering
     * bookkeeping.
     */
    private List<WorkflowStep> normaliseSteps(List<StepInput> input) {
        if (input == null) return List.of();
        return java.util.stream.IntStream.range(0, input.size())
                .mapToObj(i -> {
                    StepInput s = input.get(i);
                    return WorkflowStep.builder()
                            .id(s.id() != null ? s.id() : UUID.randomUUID())
                            .step(i + 1)
                            .role(s.role() == null ? "" : s.role().toUpperCase())
                            .action(s.action() == null ? "" : s.action().toUpperCase())
                            .build();
                })
                .toList();
    }

    public record TemplateRequest(String entityType, List<StepInput> steps) {}

    public record StepInput(UUID id, String role, String action) {}
}
