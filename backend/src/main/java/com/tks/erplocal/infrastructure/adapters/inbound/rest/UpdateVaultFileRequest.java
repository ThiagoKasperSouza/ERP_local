package com.tks.erplocal.infrastructure.adapters.inbound.rest;

import java.util.UUID;

public record UpdateVaultFileRequest(String name, String status, UUID approver, String externalReference,
                                       String projectCode) {
}
