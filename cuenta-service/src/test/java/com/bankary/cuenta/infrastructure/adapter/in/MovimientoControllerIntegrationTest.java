package com.bankary.cuenta.infrastructure.adapter.in;

import com.bankary.cuenta.application.dto.CuentaRequest;
import com.bankary.cuenta.application.dto.MovimientoRequest;
import com.bankary.cuenta.domain.model.Cuenta;
import com.bankary.cuenta.domain.model.Movimiento;
import com.bankary.cuenta.domain.model.TipoCuenta;
import com.bankary.cuenta.domain.port.out.CuentaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class MovimientoControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Container
    static RabbitMQContainer rabbitMQ = new RabbitMQContainer("rabbitmq:3-management-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQ::getAmqpPort);
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CuentaRepository cuentaRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void shouldRegisterMovementAndUpdateBalance() throws Exception {
        // 1. Crear una cuenta primero
        Cuenta account = Cuenta.builder()
                .id(UUID.randomUUID())
                .numeroCuenta("478758")
                .tipoCuenta(TipoCuenta.AHORRO)
                .saldoInicial(new BigDecimal("2000"))
                .saldoDisponible(new BigDecimal("2000"))
                .clienteId(UUID.randomUUID())
                .estado(true)
                .build();
        cuentaRepository.save(account);

        // 2. Realizar un retiro
        MovimientoRequest request = new MovimientoRequest();
        request.setNumeroCuenta("478758");
        request.setTipoMovimiento(Movimiento.TipoMovimiento.RETIRO);
        request.setValor(new BigDecimal("575"));

        mockMvc.perform(post("/api/v1/movimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldo").value(1425))
                .andExpect(jsonPath("$.tipoMovimiento").value("RETIRO"));
    }

    @Test
    void shouldReturnBadRequestWhenBalanceIsInsufficient() throws Exception {
        // 1. Crear una cuenta con poco saldo
        Cuenta account = Cuenta.builder()
                .id(UUID.randomUUID())
                .numeroCuenta("123")
                .tipoCuenta(TipoCuenta.CORRIENTE)
                .saldoInicial(new BigDecimal("100"))
                .saldoDisponible(new BigDecimal("100"))
                .clienteId(UUID.randomUUID())
                .estado(true)
                .build();
        cuentaRepository.save(account);

        // 2. Intentar retirar más de lo que hay
        MovimientoRequest request = new MovimientoRequest();
        request.setNumeroCuenta("123");
        request.setTipoMovimiento(Movimiento.TipoMovimiento.RETIRO);
        request.setValor(new BigDecimal("500"));

        mockMvc.perform(post("/api/v1/movimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Saldo no disponible"));
    }
}
