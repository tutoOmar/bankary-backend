package com.bankary.cuenta.application.usecase;

import com.bankary.cuenta.application.exception.ConflictException;
import com.bankary.cuenta.application.exception.ResourceNotFoundException;
import com.bankary.cuenta.domain.exception.CuentaDuplicadaException;
import com.bankary.cuenta.domain.model.ClienteSnapshot;
import com.bankary.cuenta.domain.model.Cuenta;
import com.bankary.cuenta.domain.model.TipoCuenta;
import com.bankary.cuenta.domain.port.out.ClienteSnapshotRepository;
import com.bankary.cuenta.domain.port.out.CuentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CuentaUseCaseImpl Unit Tests")
class CuentaUseCaseImplTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private ClienteSnapshotRepository clienteSnapshotRepository;

    @Mock
    private com.bankary.cuenta.domain.port.out.ClienteExternalServicePort clienteExternalServicePort;

    @InjectMocks
    private CuentaUseCaseImpl useCase;

    private Cuenta cuenta;
    private UUID clienteId;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        cuenta = Cuenta.builder()
                .numeroCuenta("123456")
                .clienteId(clienteId)
                .tipoCuenta(TipoCuenta.AHORRO)
                .saldoInicial(new BigDecimal("1000"))
                .estado(true)
                .build();
    }

    @Test
    @DisplayName("create: Happy Path")
    void create_Exitoso() {
        // Arrange
        when(cuentaRepository.findByNumeroCuenta(anyString())).thenReturn(Optional.empty());
        when(clienteSnapshotRepository.findById(any())).thenReturn(Optional.of(ClienteSnapshot.builder().build()));
        when(cuentaRepository.findByClienteIdAndEstadoTrue(any())).thenReturn(Collections.emptyList());
        when(cuentaRepository.save(any(Cuenta.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Cuenta result = useCase.create(cuenta);

        // Assert
        assertNotNull(result.getId());
        assertEquals(cuenta.getNumeroCuenta(), result.getNumeroCuenta());
        assertEquals(new BigDecimal("1000"), result.getSaldoDisponible());
        assertTrue(result.isEstado());
        verify(cuentaRepository).save(any(Cuenta.class));
    }

    @Test
    @DisplayName("create: Duplicate account throws ConflictException")
    void create_CuentaDuplicada_LanzaConflictException() {
        // Arrange
        when(cuentaRepository.findByNumeroCuenta("123456")).thenReturn(Optional.of(cuenta));

        // Act & Assert
        assertThrows(ConflictException.class, () -> useCase.create(cuenta));
        verify(cuentaRepository, never()).save(any());
    }

    @Test
    @DisplayName("create: Client not found throws ResourceNotFoundException")
    void create_ClienteNoEncontrado_LanzaResourceNotFoundException() {
        // Arrange
        when(cuentaRepository.findByNumeroCuenta(anyString())).thenReturn(Optional.empty());
        when(clienteSnapshotRepository.findById(clienteId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> useCase.create(cuenta));
    }

    @Test
    @DisplayName("create: Limit exceeded throws CuentaDuplicadaException")
    void create_LimiteExcedido_LanzaCuentaDuplicadaException() {
        // Arrange
        when(cuentaRepository.findByNumeroCuenta(anyString())).thenReturn(Optional.empty());
        when(clienteSnapshotRepository.findById(any())).thenReturn(Optional.of(ClienteSnapshot.builder().build()));
        when(cuentaRepository.findByClienteIdAndEstadoTrue(clienteId)).thenReturn(List.of(cuenta));

        // Act & Assert
        assertThrows(CuentaDuplicadaException.class, () -> useCase.create(cuenta));
    }

    @Test
    @DisplayName("update: Happy Path")
    void update_Exitoso() {
        // Arrange
        when(cuentaRepository.findByNumeroCuenta("123456")).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.save(any(Cuenta.class))).thenAnswer(i -> i.getArgument(0));

        Cuenta updateInfo = Cuenta.builder()
                .tipoCuenta(TipoCuenta.CORRIENTE)
                .estado(false)
                .build();

        // Act
        Cuenta result = useCase.update("123456", updateInfo);

        // Assert
        assertEquals(TipoCuenta.CORRIENTE, result.getTipoCuenta());
        assertFalse(result.isEstado());
        verify(cuentaRepository).save(any());
    }

    @Test
    @DisplayName("update: Not found throws ResourceNotFoundException")
    void update_NoEncontrado_LanzaResourceNotFoundException() {
        // Arrange
        when(cuentaRepository.findByNumeroCuenta("999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> useCase.update("999", cuenta));
    }

    @Test
    @DisplayName("delete: Happy Path")
    void delete_Exitoso() {
        // Arrange
        when(cuentaRepository.findByNumeroCuenta("123456")).thenReturn(Optional.of(cuenta));

        // Act
        useCase.delete("123456");

        // Assert
        verify(cuentaRepository).deleteByNumeroCuenta("123456");
    }

    @Test
    @DisplayName("delete: Not found throws ResourceNotFoundException")
    void delete_NoEncontrado_LanzaResourceNotFoundException() {
        // Arrange
        when(cuentaRepository.findByNumeroCuenta("999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> useCase.delete("999"));
    }

    @Test
    @DisplayName("getByNumeroCuenta: Happy Path")
    void getByNumeroCuenta_Exitoso() {
        // Arrange
        when(cuentaRepository.findByNumeroCuenta("123456")).thenReturn(Optional.of(cuenta));

        // Act
        Cuenta result = useCase.getByNumeroCuenta("123456");

        // Assert
        assertEquals("123456", result.getNumeroCuenta());
    }

    @Test
    @DisplayName("getByNumeroCuenta: Not found throws ResourceNotFoundException")
    void getByNumeroCuenta_NoEncontrado_LanzaResourceNotFoundException() {
        // Arrange
        when(cuentaRepository.findByNumeroCuenta("999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> useCase.getByNumeroCuenta("999"));
    }

    @Test
    @DisplayName("list: Happy Path")
    void list_Exitoso() {
        // Arrange
        when(cuentaRepository.findAll()).thenReturn(List.of(cuenta));

        // Act
        List<Cuenta> result = useCase.list();

        // Assert
        assertEquals(1, result.size());
        verify(cuentaRepository).findAll();
    }
}
