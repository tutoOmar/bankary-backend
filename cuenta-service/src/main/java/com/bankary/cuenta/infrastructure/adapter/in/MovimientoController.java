package com.bankary.cuenta.infrastructure.adapter.in;

import com.bankary.cuenta.application.dto.MovimientoRequest;
import com.bankary.cuenta.domain.model.Movimiento;
import com.bankary.cuenta.domain.port.in.MovimientoUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoUseCase useCase;

    @PostMapping
    public ResponseEntity<Movimiento> create(@Valid @RequestBody MovimientoRequest request) {
        Movimiento movimiento = Movimiento.builder()
                .tipoMovimiento(request.getTipoMovimiento())
                .valor(request.getValor())
                .build();
        return new ResponseEntity<>(useCase.registrarMovimiento(request.getNumeroCuenta(), movimiento), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Movimiento>> list() {
        return ResponseEntity.ok(useCase.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Movimiento> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(useCase.getById(id));
    }
}
