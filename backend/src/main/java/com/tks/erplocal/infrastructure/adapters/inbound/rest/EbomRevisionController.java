package com.tks.erplocal.infrastructure.adapters.inbound.rest;

import com.tks.erplocal.app.usecases.vault.ApproveEbomRevisionUseCase;
import com.tks.erplocal.app.usecases.vault.CreateEbomRevisionUseCase;
import com.tks.erplocal.app.usecases.vault.DiffEbomRevisionsUseCase;
import com.tks.erplocal.domain.vault.model.EbomRevision;
import com.tks.erplocal.domain.vault.ports.EbomRevisionRepositoryPort;
import com.tks.erplocal.infrastructure.security.JwtService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vault/revisions")
public class EbomRevisionController {
    private final EbomRevisionRepositoryPort repo;
    private final CreateEbomRevisionUseCase createUseCase;
    private final DiffEbomRevisionsUseCase diffUseCase;
    private final ApproveEbomRevisionUseCase approveUseCase;
    private final JwtService jwtService;

    public EbomRevisionController(EbomRevisionRepositoryPort repo, CreateEbomRevisionUseCase createUseCase,
                                  DiffEbomRevisionsUseCase diffUseCase, ApproveEbomRevisionUseCase approveUseCase,
                                  JwtService jwtService) {
        this.repo = repo; this.createUseCase = createUseCase; this.diffUseCase = diffUseCase;
        this.approveUseCase = approveUseCase; this.jwtService = jwtService;
    }

    private UUID callerId(String authorization) {
        String token = authorization.substring(7).trim();
        return jwtService.verify(token).orElseThrow(() -> new IllegalStateException("Invalid token")).userId();
    }

    @GetMapping("/assembly/{assemblyId}")
    public List<EbomRevision> list(@PathVariable UUID assemblyId) {
        return repo.findByAssemblyId(assemblyId);
    }

    @GetMapping("/{id}")
    public EbomRevision get(@PathVariable UUID id) {
        return repo.findById(id).orElseThrow();
    }

    @PostMapping("/assembly/{assemblyId}")
    public EbomRevision create(@RequestHeader("Authorization") String authorization,
                               @PathVariable UUID assemblyId,
                               @RequestParam(required = false) String reason,
                               @RequestParam(defaultValue = "false") boolean major) {
        // items from assembly children? empty for now
        return createUseCase.execute(assemblyId, List.of(), reason, callerId(authorization), major);
    }

    @PostMapping("/{id}/approve")
    public EbomRevision approve(@RequestHeader("Authorization") String authorization,
                                 @PathVariable UUID id,
                                 @RequestParam(defaultValue = "true") boolean approve,
                                 @RequestParam(required = false) String justification) {
        return approveUseCase.execute(id, callerId(authorization), approve, justification);
    }

    @GetMapping("/diff")
    public DiffEbomRevisionsUseCase.DiffResult diff(@RequestParam UUID a, @RequestParam UUID b) {
        EbomRevision ra = repo.findById(a).orElseThrow();
        EbomRevision rb = repo.findById(b).orElseThrow();
        return diffUseCase.execute(ra, rb);
    }
}
