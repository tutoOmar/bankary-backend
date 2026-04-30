package com.bankary.cuenta.application.usecase;

import com.bankary.cuenta.application.exception.ResourceNotFoundException;
import com.bankary.cuenta.application.exception.SaldoInsuficienteException;
import com.bankary.cuenta.domain.model.Cuenta;
import com.bankary.cuenta.domain.model.Movimiento;
import com.bankary.cuenta.domain.port.in.MovimientoUseCase;
import com.bankary.cuenta.domain.port.out.CuentaRepository;
import com.bankary.cuenta.domain.port.out.MovimientoRepository;
import com.bankary.cuenta.domain.model.strategy.CuentaStrategy;
import com.bankary.cuenta.domain.model.strategy.CuentaStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovimientoUseCaseImpl implements MovimientoUseCase {

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;

    @Override
    @Transactional
    public Movimiento registrarMovimiento(String numeroCuenta, Movimiento movimiento) {
        log.info("Registrando movimiento | numeroCuenta={} | tipo={} | valor={}", 
                numeroCuenta, movimiento.getTipoMovimiento(), movimiento.getValor());
        
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> {
                    log.error("Movimiento fallido: cuenta no encontrada | numeroCuenta={}", numeroCuenta);
                    return new ResourceNotFoundException("Cuenta no encontrada");
                });

        BigDecimal nuevoSaldo;
        if (movimiento.getTipoMovimiento() == Movimiento.TipoMovimiento.DEPOSITO) {
            nuevoSaldo = cuenta.getSaldoDisponible().add(movimiento.getValor());
            log.debug("Procesando depósito | saldoAnterior={} | nuevoSaldo={}", cuenta.getSaldoDisponible(), nuevoSaldo);
        } else {
            CuentaStrategy strategy = CuentaStrategyFactory.getStrategy(cuenta.getTipoCuenta());
            try {
                strategy.validarRetiro(cuenta.getSaldoDisponible(), movimiento.getValor());
            } catch (SaldoInsuficienteException e) {
                log.error("Retiro rechazado por saldo insuficiente | numeroCuenta={} | saldo={} | retiro={}", 
                        numeroCuenta, cuenta.getSaldoDisponible(), movimiento.getValor());
                throw e;
            }
            nuevoSaldo = cuenta.getSaldoDisponible().subtract(movimiento.getValor());
            log.debug("Procesando retiro | saldoAnterior={} | nuevoSaldo={}", cuenta.getSaldoDisponible(), nuevoSaldo);
        }

        cuenta.setSaldoDisponible(nuevoSaldo);
        cuentaRepository.save(cuenta);

        movimiento.setFecha(Instant.now());
        movimiento.setSaldo(nuevoSaldo);
        movimiento.setCuentaId(cuenta.getId());
        
        Movimiento saved = movimientoRepository.save(movimiento);
        log.info("Movimiento registrado exitosamente | id={} | nuevoSaldo={}", saved.getId(), nuevoSaldo);
        return saved;
    }

    @Override
    public List<Movimiento> list() {
        log.debug("Listando todos los movimientos");
        return movimientoRepository.findAll();
    }

    @Override
    public Movimiento getById(UUID id) {
        log.debug("Consultando movimiento | id={}", id);
        return movimientoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Movimiento no encontrado | id={}", id);
                    return new ResourceNotFoundException("Movimiento no encontrado");
                });
    }
}
