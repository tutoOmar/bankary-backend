package com.tupackage.clienteservice.exception;

import java.util.UUID;

public class ClienteNotFoundException extends RuntimeException {
    public ClienteNotFoundException(UUID id) {
        super("Cliente not found: " + id);
    }
}
