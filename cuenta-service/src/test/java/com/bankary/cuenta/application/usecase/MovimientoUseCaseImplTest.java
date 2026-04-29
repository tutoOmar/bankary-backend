package com.bankary.cuenta.application.usecase;

import com.bankary.cuenta.application.exception.ResourceNotFoundException;
import com.bankary.cuenta.application.exception.SaldoInsuficienteException;
import com.bankary.cuenta.domain.model.Cuenta;
import com.bankary.cuenta.domain.model.Movimiento;
import com.bankary.cuenta.domain.port.out.CuentaRepository;
import com.bankary.cuenta.domain.port.out.MovimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimientoUseCaseImplTest {

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @InjectMocks
    private MovimientoUseCaseImpl useCase;

    private Cuenta cuenta;

    @BeforeEach
    void setUp() {
        cuenta = Cuenta.builder()
                .id(UUID.randomUUID())
                .numeroCuenta("123456")
                .saldoDisponible(new BigDecimal("1000"))
                .estado(true)
                .build();
    }

    @Test
    void registrarMovimiento_DepositoExitoso() {
        // Arrange
        Movimiento movimiento = Movimiento.builder()
                .tipoMovimiento(Movimiento.TipoMovimiento.DEPOSITO)
                .valor(new BigDecimal("500"))
                .build();

        when(cuentaRepository.findByNumeroCuenta("123456")).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Movimiento result = useCase.registrarMovimiento("123456", movimiento);

        // Assert
        assertEquals(new BigDecimal("1500"), result.getSaldo());
        assertEquals(new BigDecimal("1500"), cuenta.getSaldoDisponible());
        verify(cuentaRepository, times(1)).save(cuenta);
        verify(movimientoRepository, times(1)).save(any(Movimiento.class));
    }

    @Test
    void registrarMovimiento_RetiroExitoso() {
        // Arrange
        Movimiento movimiento = Movimiento.builder()
                .tipoMovimiento(Movimiento.TipoMovimiento.RETIRO)
                .valor(new BigDecimal("500"))
                .build();

        when(cuentaRepository.findByNumeroCuenta("123456")).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Movimiento result = useCase.registrarMovimiento("123456", movimiento);

        // Assert
        assertEquals(new BigDecimal("500"), result.getSaldo());
        assertEquals(new BigDecimal("500"), cuenta.getSaldoDisponible());
        verify(cuentaRepository, times(1)).save(cuenta);
    }

    @Test
    void registrarMovimiento_SaldoInsuficiente_LanzaExcepcion() {
        // Arrange
        Movimiento movimiento = Movimiento.builder()
                .tipoMovimiento(Movimiento.TipoMovimiento.RETIRO)
                .valor(new BigDecimal("1500"))
                .build();

        when(cuentaRepository.findByNumeroCuenta("123456")).thenReturn(Optional.of(cuenta));

        // Act & Assert
        SaldoInsuficienteException exception = assertThrows(SaldoInsuficienteException.class, 
            () -> useCase.registrarMovimiento("123456", movimiento));
        
        assertEquals("Saldo no disponible", exception.getMessage());
        verify(cuentaRepository, never()).save(any());
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void registrarMovimiento_CuentaNoExiste_LanzaExcepcion() {
        // Arrange
        Movimiento movimiento = Movimiento.builder().build();
        when(cuentaRepository.findByNumeroCuenta("999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
            () -> useCase.registrarMovimiento("999", movimiento));
    }
}
