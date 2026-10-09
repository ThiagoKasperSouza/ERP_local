package com.tks.erplocal.infrastructure.adapters.inbound.rest;

import com.tks.erplocal.app.usecases.vault.DeleteVaultFileUseCase;
import com.tks.erplocal.app.usecases.vault.DownloadVaultFileUseCase;
import com.tks.erplocal.app.usecases.vault.GetVaultFileUseCase;
import com.tks.erplocal.app.usecases.vault.ListProjectsUseCase;
import com.tks.erplocal.app.usecases.vault.ListVaultFilesUseCase;
import com.tks.erplocal.app.usecases.vault.UpdateVaultFileMetadataUseCase;
import com.tks.erplocal.app.usecases.vault.UploadVaultFileUseCase;
import com.tks.erplocal.domain.vault.model.FileStatus;
import com.tks.erplocal.domain.vault.model.VaultFile;
import com.tks.erplocal.infrastructure.security.JwtService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/vault/files")
public class VaultController {

    private final UploadVaultFileUseCase uploadVaultFileUseCase;
    private final ListVaultFilesUseCase listVaultFilesUseCase;
    private final ListProjectsUseCase listProjectsUseCase;
    private final GetVaultFileUseCase getVaultFileUseCase;
    private final DownloadVaultFileUseCase downloadVaultFileUseCase;
    private final UpdateVaultFileMetadataUseCase updateVaultFileMetadataUseCase;
    private final DeleteVaultFileUseCase deleteVaultFileUseCase;
    private final JwtService jwtService;

    public VaultController(UploadVaultFileUseCase uploadVaultFileUseCase,
                           ListVaultFilesUseCase listVaultFilesUseCase,
                           ListProjectsUseCase listProjectsUseCase,
                           GetVaultFileUseCase getVaultFileUseCase,
                           DownloadVaultFileUseCase downloadVaultFileUseCase,
                           UpdateVaultFileMetadataUseCase updateVaultFileMetadataUseCase,
                           DeleteVaultFileUseCase deleteVaultFileUseCase,
                           JwtService jwtService) {
        this.uploadVaultFileUseCase = uploadVaultFileUseCase;
        this.listVaultFilesUseCase = listVaultFilesUseCase;
        this.getVaultFileUseCase = getVaultFileUseCase;
        this.downloadVaultFileUseCase = downloadVaultFileUseCase;
        this.updateVaultFileMetadataUseCase = updateVaultFileMetadataUseCase;
        this.deleteVaultFileUseCase = deleteVaultFileUseCase;
        this.listProjectsUseCase = listProjectsUseCase;
        this.jwtService = jwtService;
    }

    private UUID callerId(String authorization) {
        String token = authorization.substring(7).trim();
        return jwtService.verify(token)
                .orElseThrow(() -> new IllegalStateException("Invalid token"))
                .userId();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission("CanUseVault")
    public VaultFileResponse upload(@RequestHeader("Authorization") String authorization,
                                    @RequestParam("file") MultipartFile file,
                                    @RequestParam(value = "externalReference", required = false) String externalReference,
                                    @RequestParam(value = "projectCode", required = false) String projectCode)
            throws IOException {
        VaultFile saved = uploadVaultFileUseCase.execute(
                callerId(authorization), file.getOriginalFilename(), file.getContentType(),
                file.getInputStream(), file.getSize(), externalReference, projectCode);
        return VaultFileResponse.from(saved);
    }

    @GetMapping
    @RequirePermission("CanUseVault")
    public Map<String, Object> list(@RequestHeader("Authorization") String authorization,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size,
                                    @RequestParam(value = "project", required = false) String project) {
        ListVaultFilesUseCase.Page result =
                listVaultFilesUseCase.execute(callerId(authorization), project, page, size);
        List<VaultFileResponse> items = result.items().stream().map(VaultFileResponse::from).toList();
        return Map.of("page", result.page(), "size", result.size(), "items", items);
    }

    @GetMapping("/projects")
    @RequirePermission("CanUseVault")
    public List<Map<String, Object>> projects(@RequestHeader("Authorization") String authorization) {
        return listProjectsUseCase.execute(callerId(authorization)).stream()
                .map(g -> Map.<String, Object>of("code", g.code(), "fileCount", g.fileCount()))
                .toList();
    }

    @GetMapping("/{id}")
    @RequirePermission("CanUseVault")
    public VaultFileResponse get(@RequestHeader("Authorization") String authorization,
                                 @PathVariable UUID id) {
        return VaultFileResponse.from(getVaultFileUseCase.execute(callerId(authorization), id));
    }

    @GetMapping("/{id}/download")
    @RequirePermission("CanUseVault")
    public ResponseEntity<InputStreamResource> download(@RequestHeader("Authorization") String authorization,
                                                        @PathVariable UUID id) throws IOException {
        DownloadVaultFileUseCase.Download download =
                downloadVaultFileUseCase.execute(callerId(authorization), id);
        VaultFile file = download.file();
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.getName(), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentLength(file.getSizeBytes())
                .contentType(MediaType.parseMediaType(file.getMimeType()))
                .body(new InputStreamResource(download.content()));
    }

    @PutMapping("/{id}")
    @RequirePermission(value = "CanUseVault", adminOnly = true)
    public VaultFileResponse update(@PathVariable UUID id, @RequestBody UpdateVaultFileRequest body) {
        FileStatus status = body.status() == null ? null : FileStatus.valueOf(body.status());
        return VaultFileResponse.from(updateVaultFileMetadataUseCase.execute(
                id, body.name(), status, body.approver(), body.externalReference(), body.projectCode()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequirePermission(value = "CanUseVault", adminOnly = true)
    public void delete(@PathVariable UUID id) {
        deleteVaultFileUseCase.execute(id);
    }
}
