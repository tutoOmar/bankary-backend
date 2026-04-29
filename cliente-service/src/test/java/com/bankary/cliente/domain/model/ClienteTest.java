package com.bankary.cliente.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Cliente Domain Model Tests")
class ClienteTest {

    @Nested
    @DisplayName("Equivalence Partitioning & Boundary Value Analysis: Edad")
    class EdadTests {

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 60, 119, 120})
        @DisplayName("Valid ages (Boundary: 0, 120)")
        void shouldAcceptValidAges(int age) {
            Cliente cliente = Cliente.builder()
                    .nombre("Test User")
                    .edad(age)
                    .build();
            assertDoesNotThrow(cliente::validate);
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 121})
        @DisplayName("Invalid ages (Boundary: -1, 121)")
        void shouldRejectInvalidAges(int age) {
            Cliente cliente = Cliente.builder()
                    .nombre("Test User")
                    .edad(age)
                    .build();
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, cliente::validate);
            assertEquals("La edad debe estar entre 0 y 120", ex.getMessage());
        }

        @Test
        @DisplayName("Reject null age")
        void shouldRejectNullAge() {
            Cliente cliente = Cliente.builder()
                    .nombre("Test User")
                    .edad(null)
                    .build();
            assertThrows(IllegalArgumentException.class, cliente::validate);
        }
    }

    @Nested
    @DisplayName("Equivalence Partitioning: Nombre")
    class NombreTests {

        @Test
        @DisplayName("Accept valid name")
        void shouldAcceptValidName() {
            Cliente cliente = Cliente.builder()
                    .nombre("Juan Perez")
                    .edad(30)
                    .build();
            assertDoesNotThrow(cliente::validate);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "  "})
        @DisplayName("Reject empty or blank names")
        void shouldRejectEmptyNames(String name) {
            Cliente cliente = Cliente.builder()
                    .nombre(name)
                    .edad(30)
                    .build();
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, cliente::validate);
            assertEquals("El nombre no puede estar vacio", ex.getMessage());
        }

        @Test
        @DisplayName("Reject null name")
        void shouldRejectNullName() {
            Cliente cliente = Cliente.builder()
                    .nombre(null)
                    .edad(30)
                    .build();
            assertThrows(IllegalArgumentException.class, cliente::validate);
        }
    }
}
