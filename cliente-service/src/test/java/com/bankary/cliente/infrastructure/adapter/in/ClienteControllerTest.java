package com.bankary.cliente.infrastructure.adapter.in;

import com.bankary.cliente.application.dto.ClienteRequest;
import com.bankary.cliente.application.dto.ClienteResponse;
import com.bankary.cliente.domain.port.in.ClienteCommandUseCase;
import com.bankary.cliente.domain.port.in.ClienteQueryUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteController.class)
@DisplayName("ClienteController Unit Tests")
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteCommandUseCase commandUseCase;

    @MockBean
    private ClienteQueryUseCase queryUseCase;

    @Test
    @DisplayName("POST /api/v1/clientes - Success")
    void create_Exitoso() throws Exception {
        ClienteRequest request = ClienteRequest.builder()
                .nombre("Juan Perez")
                .genero("M")
                .edad(30)
                .tipoDocumento("CC")
                .numeroDocumento("123456789")
                .direccion("Calle 123")
                .telefono("3001234567")
                .contrasena("pass123")
                .build();

        ClienteResponse response = ClienteResponse.builder()
                .clienteId(UUID.randomUUID())
                .nombre("Juan Perez")
                .build();

        when(commandUseCase.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Juan Perez"));
    }

    @Test
    @DisplayName("GET /api/v1/clientes/{id} - Success")
    void getById_Exitoso() throws Exception {
        UUID id = UUID.randomUUID();
        ClienteResponse response = ClienteResponse.builder()
                .clienteId(id)
                .nombre("Juan Perez")
                .build();

        when(queryUseCase.getById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/clientes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan Perez"));
    }

    @Test
    @DisplayName("GET /api/v1/clientes - Success")
    void list_Exitoso() throws Exception {
        ClienteResponse response = ClienteResponse.builder().nombre("Juan").build();
        when(queryUseCase.list()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Juan"));
    }

    @Test
    @DisplayName("PUT /api/v1/clientes/{id} - Success")
    void update_Exitoso() throws Exception {
        UUID id = UUID.randomUUID();
        ClienteRequest request = ClienteRequest.builder()
                .nombre("Juan Updated")
                .tipoDocumento("CC")
                .numeroDocumento("123")
                .genero("M")
                .direccion("Calle 123")
                .telefono("300")
                .edad(25)
                .build();

        ClienteResponse response = ClienteResponse.builder().nombre("Juan Updated").build();
        when(commandUseCase.update(any(), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/clientes/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/clientes/{id} - Success")
    void delete_Exitoso() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/clientes/{id}", id))
                .andExpect(status().isOk());
    }
}
