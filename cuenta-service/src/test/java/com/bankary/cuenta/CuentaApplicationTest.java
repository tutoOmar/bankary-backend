package com.bankary.cuenta;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("Application Entry Point Test")
class CuentaApplicationTest {

    @Test
    @DisplayName("Should load application context (Smoke Test)")
    void main() {
        assertDoesNotThrow(() -> {
            new CuentaApplication();
        });
    }
}
