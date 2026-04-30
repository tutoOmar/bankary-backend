package com.bankary.cuenta.infrastructure.adapter.in;

import com.bankary.cuenta.application.dto.CuentaRequest;
import com.bankary.cuenta.domain.model.Cuenta;
import com.bankary.cuenta.domain.port.in.CuentaUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaUseCase useCase;

    @PostMapping
    public ResponseEntity<Cuenta> create(@Valid @RequestBody CuentaRequest request) {
        Cuenta cuenta = Cuenta.builder()
                .numeroCuenta(request.getNumeroCuenta())
                .tipoCuenta(request.getTipoCuenta())
                .saldoInicial(request.getSaldoInicial())
                .clienteId(request.getClienteId())
                .build();
        return new ResponseEntity<>(useCase.create(cuenta), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Cuenta>> list() {
        return ResponseEntity.ok(useCase.list());
    }

    @GetMapping("/{numeroCuenta}")
    public ResponseEntity<Cuenta> getByNumeroCuenta(@PathVariable String numeroCuenta) {
        return ResponseEntity.ok(useCase.getByNumeroCuenta(numeroCuenta));
    }

    @PutMapping("/{numeroCuenta}")
    public ResponseEntity<Cuenta> update(@PathVariable String numeroCuenta, @RequestBody Cuenta cuenta) {
        return ResponseEntity.ok(useCase.update(numeroCuenta, cuenta));
    }

    @DeleteMapping("/{numeroCuenta}")
    public ResponseEntity<Void> delete(@PathVariable String numeroCuenta) {
        useCase.delete(numeroCuenta);
        return ResponseEntity.ok().build();
    }
}
