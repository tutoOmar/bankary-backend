package com.tupackage.clienteservice.infrastructure.adapter.in.rest;

import com.tupackage.clienteservice.domain.model.Cliente;
import com.tupackage.clienteservice.domain.port.in.ClienteUseCase;
import com.tupackage.clienteservice.exception.ClienteNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/clientes")
@Validated
public class ClienteController {
    private static final Logger log = LoggerFactory.getLogger(ClienteController.class);

    private final ClienteUseCase clienteUseCase;

    public ClienteController(ClienteUseCase clienteUseCase) {
        this.clienteUseCase = clienteUseCase;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> create(@Valid @RequestBody ClienteRequest req) {
        Cliente c = new Cliente();
        c.setNombre(req.getNombre());
        c.setEdad(req.getEdad());
        c.setIdentificacion(req.getIdentificacion());
        c.setPassword(req.getPassword());

        Cliente saved = clienteUseCase.create(c);
        ClienteResponse resp = ClienteResponse.fromDomain(saved);
        return ResponseEntity.created(URI.create("/api/v1/clientes/" + saved.getId())).body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> getById(@PathVariable("id") UUID id) {
        Cliente c = clienteUseCase.findById(id);
        return ResponseEntity.ok(ClienteResponse.fromDomain(c));
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listActive() {
        List<ClienteResponse> list = clienteUseCase.findAllActive().stream().map(ClienteResponse::fromDomain).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody ClienteRequest req) {
        Cliente c = new Cliente();
        c.setNombre(req.getNombre());
        c.setEdad(req.getEdad());
        c.setIdentificacion(req.getIdentificacion());
        c.setPassword(req.getPassword());
        Cliente updated = clienteUseCase.update(id, c);
        return ResponseEntity.ok(ClienteResponse.fromDomain(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ClienteResponse> delete(@PathVariable("id") UUID id) {
        Cliente deleted = clienteUseCase.deleteLogical(id);
        return ResponseEntity.ok(ClienteResponse.fromDomain(deleted));
    }
}
