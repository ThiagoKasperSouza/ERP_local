package com.tks.erplocal.domain.vault.ports;

import java.io.IOException;
import java.io.InputStream;

/**
 * Abstração do armazenamento de bytes. Hoje: diretório local (dev).
 * Produção futura: adapter S3/Blob com AES-256 em repouso, TLS 1.3 em
 * trânsito e backup diário com retenção de 30 dias (requisito US3) —
 * tudo atrás deste port, sem tocar nos usecases.
 */
public interface FileStoragePort {
    void store(String key, InputStream content, long sizeBytes) throws IOException;
    InputStream load(String key) throws IOException;
    void delete(String key) throws IOException;
    boolean exists(String key);
}
