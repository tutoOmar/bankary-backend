package com.bankary.cuenta.infrastructure.adapter.in;

import com.bankary.cuenta.application.dto.CuentaRequest;
import com.bankary.cuenta.domain.model.Cuenta;
import com.bankary.cuenta.domain.model.TipoCuenta;
import com.bankary.cuenta.domain.port.in.CuentaUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CuentaController.class)
@DisplayName("CuentaController Unit Tests")
class CuentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CuentaUseCase useCase;

    @Test
    @DisplayName("POST /api/v1/cuentas - Success")
    void create_Exitoso() throws Exception {
        CuentaRequest request = CuentaRequest.builder()
                .numeroCuenta("123456")
                .tipoCuenta(TipoCuenta.AHORRO)
                .saldoInicial(new BigDecimal("1000"))
                .clienteId(UUID.randomUUID())
                .build();

        Cuenta response = Cuenta.builder().numeroCuenta("123456").build();
        when(useCase.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/cuentas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroCuenta").value("123456"));
    }

    @Test
    @DisplayName("GET /api/v1/cuentas - Success")
    void list_Exitoso() throws Exception {
        Cuenta response = Cuenta.builder().numeroCuenta("123").build();
        when(useCase.list()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/cuentas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroCuenta").value("123"));
    }

    @Test
    @DisplayName("GET /api/v1/cuentas/{numero} - Success")
    void getByNumero_Exitoso() throws Exception {
        Cuenta response = Cuenta.builder().numeroCuenta("123").build();
        when(useCase.getByNumeroCuenta("123")).thenReturn(response);

        mockMvc.perform(get("/api/v1/cuentas/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroCuenta").value("123"));
    }

    @Test
    @DisplayName("PUT /api/v1/cuentas/{numero} - Success")
    void update_Exitoso() throws Exception {
        Cuenta response = Cuenta.builder().numeroCuenta("123").build();
        when(useCase.update(anyString(), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/cuentas/123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(response)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/cuentas/{numero} - Success")
    void delete_Exitoso() throws Exception {
        mockMvc.perform(delete("/api/v1/cuentas/123"))
                .andExpect(status().isOk());
    }
}
