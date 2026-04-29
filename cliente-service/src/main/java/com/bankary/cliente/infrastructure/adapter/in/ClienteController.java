package com.bankary.cliente.infrastructure.adapter.in;

import com.bankary.cliente.application.dto.ClienteRequest;
import com.bankary.cliente.application.dto.ClienteResponse;
import com.bankary.cliente.application.dto.CreateClienteCommand;
import com.bankary.cliente.application.dto.UpdateClienteCommand;
import com.bankary.cliente.domain.port.in.ClienteCommandUseCase;
import com.bankary.cliente.domain.port.in.ClienteQueryUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteCommandUseCase commandUseCase;
    private final ClienteQueryUseCase queryUseCase;

    @PostMapping
    public ResponseEntity<ClienteResponse> create(@Valid @RequestBody ClienteRequest request) {
        CreateClienteCommand command = CreateClienteCommand.builder()
                .nombre(request.getNombre())
                .genero(request.getGenero())
                .edad(request.getEdad())
                .identificacion(request.getIdentificacion())
                .direccion(request.getDireccion())
                .telefono(request.getTelefono())
                .contrasena(request.getContrasena())
                .build();

        return new ResponseEntity<>(commandUseCase.create(command), HttpStatus.CREATED);
    }

    @PutMapping("/{clienteId}")
    public ResponseEntity<ClienteResponse> update(@PathVariable UUID clienteId,
            @Valid @RequestBody ClienteRequest request) {
        UpdateClienteCommand command = UpdateClienteCommand.builder()
                .nombre(request.getNombre())
                .genero(request.getGenero())
                .edad(request.getEdad())
                .identificacion(request.getIdentificacion())
                .direccion(request.getDireccion())
                .telefono(request.getTelefono())
                .contrasena(request.getContrasena())
                .build();
        return ResponseEntity.ok(commandUseCase.update(clienteId, command));
    }

    @GetMapping("/{clienteId}")
    public ResponseEntity<ClienteResponse> getById(@PathVariable UUID clienteId) {
        return ResponseEntity.ok(queryUseCase.getById(clienteId));
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> list() {
        return ResponseEntity.ok(queryUseCase.list());
    }

    @DeleteMapping("/{clienteId}")
    public ResponseEntity<Void> delete(@PathVariable UUID clienteId) {
        commandUseCase.delete(clienteId);
        return ResponseEntity.ok().build();
    }
}
