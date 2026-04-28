package com.tupackage.clienteservice.application.usecase;

import com.tupackage.clienteservice.domain.event.ClienteEvent;
import com.tupackage.clienteservice.domain.model.Cliente;
import com.tupackage.clienteservice.domain.port.out.ClienteEventPublisher;
import com.tupackage.clienteservice.domain.port.out.ClienteRepository;
import com.tupackage.clienteservice.exception.ClienteNotFoundException;
import com.tupackage.clienteservice.exception.DuplicateIdentificacionException;
import com.tupackage.clienteservice.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteUseCaseImpl")
class ClienteUseCaseImplTest {

    @Mock ClienteRepository repository;
    @Mock ClienteEventPublisher publisher;
    @Mock BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    ClienteUseCaseImpl useCase;

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Cliente buildCliente(String nombre, String identificacion) {
        Cliente c = new Cliente();
        c.setNombre(nombre);
        c.setEdad(30);
        c.setIdentificacion(identificacion);
        c.setPassword("secret");
        return c;
    }

    private Cliente buildSavedCliente(Cliente source) {
        source.setId(UUID.randomUUID());
        return source;
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Happy path: persiste, hashea password y publica evento CLIENTE_CREATED")
        void shouldPersistAndPublishCreatedEvent() {
            // GIVEN
            Cliente input = buildCliente("Juan", "ID-001");
            when(repository.findByIdentificacion("ID-001")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("secret")).thenReturn("HASH_XYZ");
            when(repository.save(any())).thenAnswer(inv -> buildSavedCliente(inv.getArgument(0)));

            // WHEN
            Cliente result = useCase.create(input);

            // THEN
            assertThat(result.getId()).isNotNull();
            assertThat(result.getPasswordHash()).isEqualTo("HASH_XYZ");
            assertThat(result.getPassword()).isNull(); // password plano limpiado
            verify(repository).save(any());

            ArgumentCaptor<ClienteEvent> eventCaptor = ArgumentCaptor.forClass(ClienteEvent.class);
            verify(publisher).publish(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getType()).isEqualTo(ClienteEvent.Type.CLIENTE_CREATED);
            assertThat(eventCaptor.getValue().getNombre()).isEqualTo("Juan");
        }

        @Test
        @DisplayName("Error: nombre vacío lanza ValidationException")
        void shouldThrowWhenNombreIsBlank() {
            // GIVEN
            Cliente input = buildCliente("  ", "ID-002");

            // WHEN / THEN
            assertThatThrownBy(() -> useCase.create(input))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Nombre obligatorio");

            verifyNoInteractions(repository, publisher);
        }

        @Test
        @DisplayName("Error: identificacion vacía lanza ValidationException")
        void shouldThrowWhenIdentificacionIsBlank() {
            // GIVEN
            Cliente input = buildCliente("Juan", "");

            // WHEN / THEN
            assertThatThrownBy(() -> useCase.create(input))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Identificacion obligatoria");
        }

        @Test
        @DisplayName("Error: identificacion duplicada activa lanza DuplicateIdentificacionException")
        void shouldThrowWhenIdentificacionAlreadyExistsAndActive() {
            // GIVEN
            Cliente existing = buildCliente("Otro", "ID-DUP");
            existing.setEstado(true);
            when(repository.findByIdentificacion("ID-DUP")).thenReturn(Optional.of(existing));

            Cliente input = buildCliente("Nuevo", "ID-DUP");

            // WHEN / THEN
            assertThatThrownBy(() -> useCase.create(input))
                    .isInstanceOf(DuplicateIdentificacionException.class);

            verify(repository, never()).save(any());
            verifyNoInteractions(publisher);
        }

        @Test
        @DisplayName("Idempotencia: identificacion duplicada pero inactiva permite crear")
        void shouldAllowCreateWhenDuplicateIsInactive() {
            // GIVEN
            Cliente inactivo = buildCliente("Viejo", "ID-INA");
            inactivo.setEstado(false);
            when(repository.findByIdentificacion("ID-INA")).thenReturn(Optional.of(inactivo));
            when(passwordEncoder.encode(any())).thenReturn("HASH");
            when(repository.save(any())).thenAnswer(inv -> buildSavedCliente(inv.getArgument(0)));

            Cliente input = buildCliente("Nuevo", "ID-INA");

            // WHEN
            Cliente result = useCase.create(input);

            // THEN
            assertThat(result.getId()).isNotNull();
            verify(repository).save(any());
        }

        @Test
        @DisplayName("Resiliencia: fallo en publicación de evento no aborta la operación")
        void shouldNotFailWhenEventPublishingThrows() {
            // GIVEN
            Cliente input = buildCliente("Maria", "ID-003");
            when(repository.findByIdentificacion("ID-003")).thenReturn(Optional.empty());
            when(passwordEncoder.encode(any())).thenReturn("HASH");
            when(repository.save(any())).thenAnswer(inv -> buildSavedCliente(inv.getArgument(0)));
            doThrow(new RuntimeException("RabbitMQ down")).when(publisher).publish(any());

            // WHEN — no debe lanzar excepción
            Cliente result = useCase.create(input);

            // THEN
            assertThat(result.getId()).isNotNull();
        }
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("Happy path: actualiza campos y publica evento CLIENTE_UPDATED")
        void shouldUpdateAndPublishUpdatedEvent() {
            // GIVEN
            UUID id = UUID.randomUUID();
            Cliente existing = buildCliente("Viejo", "ID-UPD");
            existing.setId(id);
            existing.setEstado(true);

            when(repository.findById(id)).thenReturn(Optional.of(existing));
            when(repository.findByIdentificacion("ID-UPD")).thenReturn(Optional.of(existing));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Cliente updates = buildCliente("Nuevo Nombre", "ID-UPD");

            // WHEN
            Cliente result = useCase.update(id, updates);

            // THEN
            assertThat(result.getNombre()).isEqualTo("Nuevo Nombre");
            ArgumentCaptor<ClienteEvent> captor = ArgumentCaptor.forClass(ClienteEvent.class);
            verify(publisher).publish(captor.capture());
            assertThat(captor.getValue().getType()).isEqualTo(ClienteEvent.Type.CLIENTE_UPDATED);
        }

        @Test
        @DisplayName("Error: cliente no existe lanza ClienteNotFoundException")
        void shouldThrowWhenClienteNotFound() {
            // GIVEN
            UUID id = UUID.randomUUID();
            when(repository.findById(id)).thenReturn(Optional.empty());

            // WHEN / THEN
            assertThatThrownBy(() -> useCase.update(id, buildCliente("X", "Y")))
                    .isInstanceOf(ClienteNotFoundException.class);
        }
    }

    // ── deleteLogical ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteLogical()")
    class DeleteLogical {

        @Test
        @DisplayName("Happy path: desactiva cliente y publica evento CLIENTE_DELETED")
        void shouldDeactivateAndPublishDeletedEvent() {
            // GIVEN
            UUID id = UUID.randomUUID();
            Cliente existing = buildCliente("Pedro", "ID-DEL");
            existing.setId(id);
            existing.setEstado(true);

            when(repository.findById(id)).thenReturn(Optional.of(existing));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // WHEN
            Cliente result = useCase.deleteLogical(id);

            // THEN
            assertThat(result.isEstado()).isFalse();
            ArgumentCaptor<ClienteEvent> captor = ArgumentCaptor.forClass(ClienteEvent.class);
            verify(publisher).publish(captor.capture());
            assertThat(captor.getValue().getType()).isEqualTo(ClienteEvent.Type.CLIENTE_DELETED);
        }

        @Test
        @DisplayName("Error: cliente no existe lanza ClienteNotFoundException")
        void shouldThrowWhenClienteNotFound() {
            // GIVEN
            UUID id = UUID.randomUUID();
            when(repository.findById(id)).thenReturn(Optional.empty());

            // WHEN / THEN
            assertThatThrownBy(() -> useCase.deleteLogical(id))
                    .isInstanceOf(ClienteNotFoundException.class);
        }
    }

    // ── findAllActive ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAllActive() retorna solo clientes con estado=true")
    void shouldReturnOnlyActiveClientes() {
        // GIVEN
        Cliente activo = buildCliente("A", "ID-ACT");
        activo.setEstado(true);
        when(repository.findAllActive()).thenReturn(List.of(activo));

        // WHEN
        List<Cliente> result = useCase.findAllActive();

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isEstado()).isTrue();
    }
}
