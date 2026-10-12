package com.tks.erplocal.infrastructure.adapters.vault;

import com.tks.erplocal.domain.vault.ports.FileStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** File server local (dev): guarda os bytes num diretório configurável. */
@Component
public class LocalFileStorageAdapter implements FileStoragePort {

    private final Path baseDir;

    public LocalFileStorageAdapter(@Value("${app.vault.dir:./data/vault}") String baseDir) throws IOException {
        this.baseDir = Path.of(baseDir).toAbsolutePath().normalize();
        Files.createDirectories(this.baseDir);
    }

    @Override
    public void store(String key, InputStream content, long sizeBytes) throws IOException {
        Path target = resolve(key);
        Files.createDirectories(target.getParent());
        Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public InputStream load(String key) throws IOException {
        return Files.newInputStream(resolve(key));
    }

    @Override
    public void delete(String key) throws IOException {
        Files.deleteIfExists(resolve(key));
    }

    @Override
    public boolean exists(String key) {
        try {
            return Files.isRegularFile(resolve(key));
        } catch (IOException e) {
            return false;
        }
    }

    private Path resolve(String key) throws IOException {
        Path target = baseDir.resolve(key).normalize();
        if (!target.startsWith(baseDir)) {
            throw new IOException("Invalid storage key (path traversal): " + key);
        }
        return target;
    }
}
