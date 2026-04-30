package com.bankary.cliente.domain.model;

import com.bankary.cliente.domain.exception.DocumentoInvalidoException;
import java.util.regex.Pattern;

public enum TipoDocumento {
    CC("^\\d{8,10}$", "CC debe tener entre 8 y 10 dígitos numéricos") {
        @Override
        public void validateEdad(Integer edad) {
            if (edad != null && edad < 18) {
                throw new DocumentoInvalidoException("Cédula de Ciudadanía requiere edad mínima de 18 años");
            }
        }
    },
    TI("^\\d{10,11}$", "TI debe tener entre 10 y 11 dígitos numéricos") {
        @Override
        public void validateEdad(Integer edad) {
            if (edad != null && (edad < 7 || edad > 17)) {
                throw new DocumentoInvalidoException(
                        "Tarjeta de Identidad solo aplica para personas entre 7 y 17 años");
            }
        }
    },
    CE("^[a-zA-Z0-9]{6,12}$", "CE debe tener entre 6 y 12 caracteres alfanuméricos"),
    NIT("^\\d{9}-\\d{1}$",
            "NIT debe tener 9 dígitos numéricos más dígito verificador separado por guion (XXXXXXXXX-D)"),
    PASAPORTE("^[a-zA-Z0-9]{5,12}$", "PASAPORTE debe tener entre 5 y 12 caracteres alfanuméricos");

    private final Pattern pattern;
    private final String errorMessage;

    TipoDocumento(String regex, String errorMessage) {
        this.pattern = Pattern.compile(regex);
        this.errorMessage = errorMessage;
    }

    public void validateFormat(String numero) {
        if (numero == null || !pattern.matcher(numero).matches()) {
            throw new DocumentoInvalidoException(errorMessage);
        }
    }

    public void validateEdad(Integer edad) {
    }
}
