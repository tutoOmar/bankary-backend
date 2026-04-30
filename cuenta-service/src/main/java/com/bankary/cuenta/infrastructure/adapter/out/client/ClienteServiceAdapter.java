package com.bankary.cuenta.infrastructure.adapter.out.client;

import com.bankary.cuenta.domain.model.ClienteSnapshot;
import com.bankary.cuenta.domain.port.out.ClienteExternalServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClienteServiceAdapter implements ClienteExternalServicePort {

    private final RestTemplate restTemplate;

    @Value("${CLIENTE_SERVICE_URL:http://localhost:8080}")
    private String clienteServiceUrl;

    @Override
    public Optional<ClienteSnapshot> findClienteById(UUID clienteId) {
        String url = String.format("%s/api/v1/clientes/%s", clienteServiceUrl, clienteId);
        log.debug("Consultando cliente externo | id={} | url={}", clienteId, url);
        
        try {
            ClienteExternalResponse response = restTemplate.getForObject(url, ClienteExternalResponse.class);
            if (response != null) {
                return Optional.of(ClienteSnapshot.builder()
                        .clienteId(response.getClienteId())
                        .nombre(response.getNombre())
                        .lastUpdated(Instant.now())
                        .build());
            }
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Cliente no encontrado en servicio externo | id={}", clienteId);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error consultando cliente externo | id={} | error={}", clienteId, e.getMessage());
        }
        
        return Optional.empty();
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class ClienteExternalResponse {
        private UUID clienteId;
        private String nombre;
    }
}
