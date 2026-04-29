package com.bankary.cuenta.domain.exception;

public class CuentaDuplicadaException extends RuntimeException {
    public CuentaDuplicadaException(String message) {
        super(message);
    }
}
