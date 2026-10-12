package com.tks.erplocal.domain.vault.ports;

import com.tks.erplocal.domain.vault.model.EbomEntry;
import java.util.List;

public interface EbomParserPort {
    List<EbomEntry> parse(byte[] content, String fileName);
}
