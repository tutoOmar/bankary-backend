package com.bankary.cuenta.application.usecase;

import com.bankary.cuenta.application.exception.ResourceNotFoundException;
import com.bankary.cuenta.domain.model.ClienteSnapshot;
import com.bankary.cuenta.domain.model.Cuenta;
import com.bankary.cuenta.domain.model.Movimiento;
import com.bankary.cuenta.domain.port.in.ReporteUseCase;
import com.bankary.cuenta.domain.port.out.ClienteSnapshotRepository;
import com.bankary.cuenta.domain.port.out.CuentaRepository;
import com.bankary.cuenta.domain.port.out.MovimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteUseCaseImpl implements ReporteUseCase {

    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;
    private final ClienteSnapshotRepository clienteSnapshotRepository;

    @Override
    public List<Map<String, Object>> generarReporte(LocalDate fechaInicio, LocalDate fechaFin, UUID clienteId) {
        ClienteSnapshot cliente = clienteSnapshotRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        List<Cuenta> cuentas = cuentaRepository.findAll().stream()
                .filter(c -> c.getClienteId().equals(clienteId))
                .collect(Collectors.toList());

        List<Map<String, Object>> reporte = new ArrayList<>();

        for (Cuenta cuenta : cuentas) {
            List<Movimiento> movimientos = movimientoRepository.findByCuentaIdAndFechaBetween(
                    cuenta.getId(),
                    fechaInicio.atStartOfDay().toInstant(ZoneOffset.UTC),
                    fechaFin.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC)
            );

            for (Movimiento mov : movimientos) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("fecha", mov.getFecha().atZone(ZoneOffset.UTC).toLocalDate());
                item.put("cliente", cliente.getNombre());
                item.put("numeroCuenta", cuenta.getNumeroCuenta());
                item.put("tipo", cuenta.getTipoCuenta());
                
                // Cálculo de saldo inicial para ese movimiento específico
                BigDecimal saldoAnterior;
                if (mov.getTipoMovimiento() == Movimiento.TipoMovimiento.DEPOSITO) {
                    saldoAnterior = mov.getSaldo().subtract(mov.getValor());
                } else {
                    saldoAnterior = mov.getSaldo().add(mov.getValor());
                }
                
                item.put("saldoInicial", saldoAnterior);
                item.put("estado", cuenta.isEstado());
                item.put("movimiento", mov.getTipoMovimiento() == Movimiento.TipoMovimiento.RETIRO ? mov.getValor().negate() : mov.getValor());
                item.put("saldoDisponible", mov.getSaldo());
                
                reporte.add(item);
            }
        }

        return reporte;
    }
}
