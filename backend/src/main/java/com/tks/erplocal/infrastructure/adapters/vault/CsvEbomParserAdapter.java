package com.tks.erplocal.infrastructure.adapters.vault;

import com.tks.erplocal.domain.vault.model.EbomEntry;
import com.tks.erplocal.domain.vault.ports.EbomParserPort;

import java.util.ArrayList;
import java.util.List;

public class CsvEbomParserAdapter implements EbomParserPort {

    @Override
    public List<EbomEntry> parse(byte[] content, String fileName) {
        List<EbomEntry> entries = new ArrayList<>();
        if (content == null || content.length == 0) return entries;

        String text = new String(content);
        String[] lines = text.split("\\r?\\n");
        boolean first = true;
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            if (first) { first = false; continue; } // pula header
            String[] cols = line.split(",");
            if (cols.length >= 2) {
                entries.add(new EbomEntry(cols[0].trim(), cols[1].trim()));
            }
        }
        return entries;
    }
}
