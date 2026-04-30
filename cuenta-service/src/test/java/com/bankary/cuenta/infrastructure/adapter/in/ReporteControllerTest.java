package com.bankary.cuenta.infrastructure.adapter.in;

import com.bankary.cuenta.domain.port.in.ReporteUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReporteController.class)
@DisplayName("ReporteController Unit Tests")
class ReporteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReporteUseCase useCase;

    @Test
    @DisplayName("GET /api/v1/reportes - Success")
    void generarReporte_Exitoso() throws Exception {
        UUID clienteId = UUID.randomUUID();
        when(useCase.generarReporte(any(), any(), any())).thenReturn(List.of(Map.of("test", "data")));

        mockMvc.perform(get("/api/v1/reportes")
                .param("fechaInicio", "2024-01-01")
                .param("fechaFin", "2024-01-31")
                .param("clienteId", clienteId.toString()))
                .andExpect(status().isOk());
    }
}
