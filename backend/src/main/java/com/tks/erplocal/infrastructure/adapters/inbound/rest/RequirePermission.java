package com.tks.erplocal.infrastructure.adapters.inbound.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Exige a permissão canônica (US1) no endpoint. A checagem é feita no banco
 * a cada request (grants sempre frescos); ADMIN passa direto.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    String value();
    boolean adminOnly() default false;
}
