package com.bankary.cuenta.infrastructure.adapter.in;

import com.bankary.cuenta.application.exception.SaldoInsuficienteException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleSaldoInsuficiente_ReturnsBadRequest() {
        SaldoInsuficienteException ex = new SaldoInsuficienteException("Saldo no disponible");
        MockHttpServletRequest request = new MockHttpServletRequest();
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleSaldoInsuficiente(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Saldo no disponible", response.getBody().getMessage());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    void handleGenericException_ReturnsInternalError() {
        Exception ex = new Exception("Critical failure");
        MockHttpServletRequest request = new MockHttpServletRequest();
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleGenericException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Ha ocurrido un error inesperado en el servidor", response.getBody().getMessage());
    }
}
