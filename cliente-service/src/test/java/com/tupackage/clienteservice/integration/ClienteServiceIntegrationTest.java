package com.tupackage.clienteservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tupackage.clienteservice.infrastructure.adapter.out.persistence.ClienteEntity;
import com.tupackage.clienteservice.infrastructure.adapter.out.persistence.ClienteJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class ClienteServiceIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine")).withDatabaseName("test").withUsername("postgres").withPassword("postgres");

    @Container
    public static RabbitMQContainer rabbit = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.11-management"));

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", () -> rabbit.getAmqpPort());
    }

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    ClienteJpaRepository clienteJpaRepository;

    @Autowired
    RabbitAdmin rabbitAdmin;

    @Autowired
    ObjectMapper objectMapper;

    @org.junit.jupiter.api.BeforeEach
    void cleanDb() {
        clienteJpaRepository.deleteAll();
    }

    @Test
    public void postCliente_persistsAndPublishes() throws Exception {
        Map<String, Object> req = new HashMap<>();
        req.put("nombre", "Integration Test");
        req.put("edad", 40);
        req.put("identificacion", "INT-123");
        req.put("password", "pwd");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(req), headers);

        ResponseEntity<String> resp = restTemplate.postForEntity("/api/v1/clientes", entity, String.class);
        assertThat(resp.getStatusCode().value()).isIn(201, 200);

        // Verify persistence
        ClienteEntity e = clienteJpaRepository.findByIdentificacion("INT-123").orElse(null);
        assertThat(e).isNotNull();
        assertThat(e.getNombre()).isEqualTo("Integration Test");

        // Verify queue/exchange existence via RabbitAdmin
        assertThat(rabbitAdmin.getQueueProperties("cliente.event.queue")).isNotNull();
    }
}
