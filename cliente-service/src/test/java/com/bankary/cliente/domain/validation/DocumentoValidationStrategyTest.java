package com.bankary.cliente.domain.validation;

import com.bankary.cliente.domain.exception.DocumentoInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocumentoValidationStrategyTest {

    @Nested
    @DisplayName("CcValidationStrategy")
    class CcStrategyTests {
        private final CcValidationStrategy strategy = new CcValidationStrategy();

        @Test
        @DisplayName("CC válida con 10 dígitos")
        void ccValida() {
            assertDoesNotThrow(() -> strategy.validateFormat("1023456789"));
        }

        @Test
        @DisplayName("CC inválida con menos de 8 dígitos")
        void ccInvalidaCorta() {
            DocumentoInvalidoException ex = assertThrows(DocumentoInvalidoException.class,
                    () -> strategy.validateFormat("123"));
            assertTrue(ex.getMessage().contains("8 y 10 dígitos"));
        }

        @Test
        @DisplayName("CC rechaza menor de edad")
        void ccRechazaMenorEdad() {
            assertThrows(DocumentoInvalidoException.class, () -> strategy.validateEdad(17));
        }

        @Test
        @DisplayName("CC acepta exactamente 18 años")
        void ccAcepta18Anios() {
            assertDoesNotThrow(() -> strategy.validateEdad(18));
        }

        @Test
        @DisplayName("CC acepta edad nula")
        void ccAceptaEdadNula() {
            assertDoesNotThrow(() -> strategy.validateEdad(null));
        }
    }

    @Nested
    @DisplayName("TiValidationStrategy")
    class TiStrategyTests {
        private final TiValidationStrategy strategy = new TiValidationStrategy();

        @Test
        @DisplayName("TI válida para menor de 18")
        void tiValida() {
            assertDoesNotThrow(() -> {
                strategy.validateFormat("10234567890");
                strategy.validateEdad(15);
            });
        }

        @Test
        @DisplayName("TI rechaza mayor de 17 años")
        void tiRechazaMayorEdad() {
            assertThrows(DocumentoInvalidoException.class, () -> strategy.validateEdad(18));
        }

        @Test
        @DisplayName("TI rechaza menor de 7 años")
        void tiRechazaMenorDeSiete() {
            assertThrows(DocumentoInvalidoException.class, () -> strategy.validateEdad(6));
        }
    }

    @Nested
    @DisplayName("NitValidationStrategy")
    class NitStrategyTests {
        private final NitValidationStrategy strategy = new NitValidationStrategy();

        @Test
        @DisplayName("NIT válido con formato correcto")
        void nitValido() {
            assertDoesNotThrow(() -> strategy.validateFormat("123456789-0"));
        }

        @Test
        @DisplayName("NIT inválido sin dígito verificador")
        void nitInvalido() {
            assertThrows(DocumentoInvalidoException.class, () -> strategy.validateFormat("123456789"));
        }
    }

    @Nested
    @DisplayName("DocumentoValidatorRegistry")
    class RegistryTests {

        @Test
        @DisplayName("Registry retorna estrategia para cada tipo conocido")
        void retornaEstrategiaParaTodosLosTipos() {
            com.bankary.cliente.domain.model.TipoDocumento[] tipos =
                    com.bankary.cliente.domain.model.TipoDocumento.values();
            for (var tipo : tipos) {
                assertNotNull(DocumentoValidatorRegistry.getStrategy(tipo),
                        "Falta estrategia para tipo: " + tipo);
            }
        }
    }
}
