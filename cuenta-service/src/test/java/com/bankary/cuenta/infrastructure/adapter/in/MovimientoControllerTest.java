package com.bankary.cuenta.infrastructure.adapter.in;

import com.bankary.cuenta.application.dto.MovimientoRequest;
import com.bankary.cuenta.domain.model.Movimiento;
import com.bankary.cuenta.domain.port.in.MovimientoUseCase;
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

@WebMvcTest(MovimientoController.class)
@DisplayName("MovimientoController Unit Tests")
class MovimientoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MovimientoUseCase useCase;

    @Test
    @DisplayName("POST /api/v1/movimientos - Success")
    void create_Exitoso() throws Exception {
        MovimientoRequest request = MovimientoRequest.builder()
                .numeroCuenta("123456")
                .tipoMovimiento(Movimiento.TipoMovimiento.DEPOSITO)
                .valor(new BigDecimal("500"))
                .build();

        Movimiento response = Movimiento.builder().id(UUID.randomUUID()).build();
        when(useCase.registrarMovimiento(anyString(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/movimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("GET /api/v1/movimientos - Success")
    void list_Exitoso() throws Exception {
        when(useCase.list()).thenReturn(List.of(new Movimiento()));

        mockMvc.perform(get("/api/v1/movimientos"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/movimientos/{id} - Success")
    void getById_Exitoso() throws Exception {
        UUID id = UUID.randomUUID();
        when(useCase.getById(id)).thenReturn(new Movimiento());

        mockMvc.perform(get("/api/v1/movimientos/{id}", id))
                .andExpect(status().isOk());
    }
}
