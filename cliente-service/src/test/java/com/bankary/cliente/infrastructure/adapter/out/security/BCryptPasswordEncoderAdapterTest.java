package com.bankary.cliente.infrastructure.adapter.out.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BCryptPasswordEncoderAdapter Unit Tests")
class BCryptPasswordEncoderAdapterTest {

    private final BCryptPasswordEncoderAdapter adapter = new BCryptPasswordEncoderAdapter();

    @Test
    @DisplayName("encode - Should return hashed password")
    void encode_Exitoso() {
        String pass = "secret";
        String encoded = adapter.encode(pass);
        assertNotNull(encoded);
        assertNotEquals(pass, encoded);
    }

    @Test
    @DisplayName("matches - Should return true for correct password")
    void matches_Exitoso() {
        String pass = "secret";
        String encoded = adapter.encode(pass);
        assertTrue(adapter.matches(pass, encoded));
    }

    @Test
    @DisplayName("matches - Should return false for incorrect password")
    void matches_Fallido() {
        String pass = "secret";
        String encoded = adapter.encode(pass);
        assertFalse(adapter.matches("wrong", encoded));
    }
}
