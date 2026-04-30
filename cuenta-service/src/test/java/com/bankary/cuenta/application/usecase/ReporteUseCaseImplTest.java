package com.bankary.cuenta.application.usecase;

import com.bankary.cuenta.application.exception.ResourceNotFoundException;
import com.bankary.cuenta.domain.model.ClienteSnapshot;
import com.bankary.cuenta.domain.model.Cuenta;
import com.bankary.cuenta.domain.model.Movimiento;
import com.bankary.cuenta.domain.model.TipoCuenta;
import com.bankary.cuenta.domain.port.out.ClienteSnapshotRepository;
import com.bankary.cuenta.domain.port.out.CuentaRepository;
import com.bankary.cuenta.domain.port.out.MovimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReporteUseCaseImpl Unit Tests")
class ReporteUseCaseImplTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private ClienteSnapshotRepository clienteSnapshotRepository;

    @InjectMocks
    private ReporteUseCaseImpl useCase;

    private UUID clienteId;
    private ClienteSnapshot cliente;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        cliente = ClienteSnapshot.builder()
                .clienteId(clienteId)
                .nombre("Juan Perez")
                .build();
    }

    @Test
    @DisplayName("generarReporte: Happy Path")
    void generarReporte_Exitoso() {
        // Arrange
        LocalDate inicio = LocalDate.now().minusDays(1);
        LocalDate fin = LocalDate.now();

        Cuenta cuenta = Cuenta.builder()
                .id(UUID.randomUUID())
                .clienteId(clienteId)
                .numeroCuenta("123456")
                .tipoCuenta(TipoCuenta.AHORRO)
                .estado(true)
                .build();

        Movimiento mov = Movimiento.builder()
                .tipoMovimiento(Movimiento.TipoMovimiento.DEPOSITO)
                .valor(new BigDecimal("100"))
                .saldo(new BigDecimal("500"))
                .fecha(Instant.now())
                .build();

        when(clienteSnapshotRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(cuentaRepository.findAll()).thenReturn(List.of(cuenta));
        when(movimientoRepository.findByCuentaIdAndFechaBetween(any(), any(), any())).thenReturn(List.of(mov));

        // Act
        List<Map<String, Object>> result = useCase.generarReporte(inicio, fin, clienteId);

        // Assert
        assertFalse(result.isEmpty());
        Map<String, Object> item = result.get(0);
        assertEquals("Juan Perez", item.get("cliente"));
        assertEquals("123456", item.get("numeroCuenta"));
        assertEquals(new BigDecimal("400"), item.get("saldoInicial")); // 500 - 100
        assertEquals(new BigDecimal("500"), item.get("saldoDisponible"));
    }

    @Test
    @DisplayName("generarReporte: With Retiro")
    void generarReporte_Retiro() {
        // Arrange
        LocalDate inicio = LocalDate.now().minusDays(1);
        LocalDate fin = LocalDate.now();

        Cuenta cuenta = Cuenta.builder()
                .id(UUID.randomUUID())
                .clienteId(clienteId)
                .numeroCuenta("123456")
                .tipoCuenta(TipoCuenta.AHORRO)
                .build();

        Movimiento mov = Movimiento.builder()
                .tipoMovimiento(Movimiento.TipoMovimiento.RETIRO)
                .valor(new BigDecimal("100"))
                .saldo(new BigDecimal("400"))
                .fecha(Instant.now())
                .build();

        when(clienteSnapshotRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(cuentaRepository.findAll()).thenReturn(List.of(cuenta));
        when(movimientoRepository.findByCuentaIdAndFechaBetween(any(), any(), any())).thenReturn(List.of(mov));

        // Act
        List<Map<String, Object>> result = useCase.generarReporte(inicio, fin, clienteId);

        // Assert
        Map<String, Object> item = result.get(0);
        assertEquals(new BigDecimal("500"), item.get("saldoInicial")); // 400 + 100
        assertEquals(new BigDecimal("-100"), item.get("movimiento"));
    }

    @Test
    @DisplayName("generarReporte: Client not found throws exception")
    void generarReporte_ClienteNoEncontrado() {
        // Arrange
        when(clienteSnapshotRepository.findById(clienteId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> useCase.generarReporte(LocalDate.now(), LocalDate.now(), clienteId));
    }

    @Test
    @DisplayName("generarReporte: No accounts for client returns empty list")
    void generarReporte_SinCuentas() {
        // Arrange
        when(clienteSnapshotRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(cuentaRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Map<String, Object>> result = useCase.generarReporte(LocalDate.now(), LocalDate.now(), clienteId);

        // Assert
        assertTrue(result.isEmpty());
    }
}
