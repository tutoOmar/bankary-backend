package com.bankary.cliente.domain;

import com.bankary.cliente.domain.exception.DocumentoInvalidoException;
import com.bankary.cliente.domain.model.TipoDocumento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class DocumentoValidatorTest {

    @Test
    @DisplayName("CC válida con 10 dígitos y edad 18")
    void ccValida() {
        assertDoesNotThrow(() -> DocumentoValidator.validate(TipoDocumento.CC, "1023456789", 18));
    }

    @Test
    @DisplayName("CC inválida por pocos dígitos")
    void ccInvalidaPocosDigitos() {
        DocumentoInvalidoException ex = assertThrows(DocumentoInvalidoException.class, 
            () -> DocumentoValidator.validate(TipoDocumento.CC, "123", 20));
        assertEquals("CC debe tener entre 8 y 10 dígitos numéricos", ex.getMessage());
    }

    @Test
    @DisplayName("CC inválida por ser menor de edad")
    void ccInvalidaMenorEdad() {
        DocumentoInvalidoException ex = assertThrows(DocumentoInvalidoException.class, 
            () -> DocumentoValidator.validate(TipoDocumento.CC, "1023456789", 15));
        assertEquals("Cédula de Ciudadanía requiere edad mínima de 18 años", ex.getMessage());
    }

    @Test
    @DisplayName("TI válida para menor de edad")
    void tiValida() {
        assertDoesNotThrow(() -> DocumentoValidator.validate(TipoDocumento.TI, "10234567890", 15));
    }

    @Test
    @DisplayName("TI inválida para mayor de edad")
    void tiInvalidaMayorEdad() {
        DocumentoInvalidoException ex = assertThrows(DocumentoInvalidoException.class, 
            () -> DocumentoValidator.validate(TipoDocumento.TI, "10234567890", 20));
        assertEquals("Tarjeta de Identidad solo aplica para personas entre 7 y 17 años", ex.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
        "NIT, 123456789-0, true",
        "NIT, 12345, false",
        "CE, ABC123456, true",
        "PASAPORTE, PASS123, true"
    })
    @DisplayName("Validación de otros tipos de documento")
    void otrosDocumentos(TipoDocumento tipo, String numero, boolean valido) {
        if (valido) {
            assertDoesNotThrow(() -> DocumentoValidator.validate(tipo, numero, 25));
        } else {
            assertThrows(DocumentoInvalidoException.class, () -> DocumentoValidator.validate(tipo, numero, 25));
        }
    }
}
