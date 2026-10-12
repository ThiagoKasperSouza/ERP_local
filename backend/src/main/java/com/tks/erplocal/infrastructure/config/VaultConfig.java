package com.tks.erplocal.infrastructure.config;

import com.tks.erplocal.app.usecases.vault.UploadAssemblyWithEbomUseCase;
import com.tks.erplocal.domain.vault.ports.EbomParserPort;
import com.tks.erplocal.domain.vault.ports.VaultFileRepositoryPort;
import com.tks.erplocal.infrastructure.adapters.vault.CsvEbomParserAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VaultConfig {

    @Bean
    public EbomParserPort ebomParserPort() {
        return new CsvEbomParserAdapter();
    }

    @Bean
    public UploadAssemblyWithEbomUseCase uploadAssemblyWithEbomUseCase(
            VaultFileRepositoryPort repository, EbomParserPort parser) {
        return new UploadAssemblyWithEbomUseCase(repository, parser);
    }
}
