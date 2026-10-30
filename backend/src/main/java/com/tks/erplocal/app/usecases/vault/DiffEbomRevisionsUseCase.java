package com.tks.erplocal.app.usecases.vault;

import com.tks.erplocal.domain.vault.model.EbomEntry;
import com.tks.erplocal.domain.vault.model.EbomRevision;
import java.util.*;

import org.springframework.stereotype.Component;
@Component
public class DiffEbomRevisionsUseCase {
    public record DiffResult(List<EbomEntry> added, List<EbomEntry> removed, List<String> changed) {}

    public DiffResult execute(EbomRevision a, EbomRevision b) {
        Map<String, EbomEntry> ma = new HashMap<>();
        for (EbomEntry e : a.getItems()) ma.put(key(e), e);
        Map<String, EbomEntry> mb = new HashMap<>();
        for (EbomEntry e : b.getItems()) mb.put(key(e), e);

        List<EbomEntry> added = new ArrayList<>();
        List<EbomEntry> removed = new ArrayList<>();
        List<String> changed = new ArrayList<>();

        for (var e : mb.entrySet()) {
            if (!ma.containsKey(e.getKey())) added.add(e.getValue());
        }
        for (var e : ma.entrySet()) {
            if (!mb.containsKey(e.getKey())) removed.add(e.getValue());
        }
        // changed: same key but different fileId
        for (var e : ma.entrySet()) {
            EbomEntry other = mb.get(e.getKey());
            if (other != null && !Objects.equals(e.getValue().getFileId(), other.getFileId())) {
                changed.add(e.getKey());
            }
        }
        return new DiffResult(added, removed, changed);
    }

    private String key(EbomEntry e) {
        return (e.getPartNumber() == null ? "" : e.getPartNumber()) + "|" + (e.getFileName() == null ? "" : e.getFileName());
    }
}
