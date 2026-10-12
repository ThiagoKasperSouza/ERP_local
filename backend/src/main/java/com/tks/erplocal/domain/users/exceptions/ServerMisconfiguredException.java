package com.tks.erplocal.domain.users.exceptions;

public class ServerMisconfiguredException extends RuntimeException {
    public ServerMisconfiguredException(String message) {
        super(message);
    }
}
