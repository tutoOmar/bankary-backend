package com.bankary.cliente.domain.validation;

import com.bankary.cliente.domain.exception.DocumentoInvalidoException;
import java.util.regex.Pattern;

public class TiValidationStrategy implements DocumentoValidationStrategy {

    private static final Pattern PATTERN = Pattern.compile("^\\d{10,11}$");

    @Override
    public void validateFormat(String numero) {
        if (numero == null || !PATTERN.matcher(numero).matches()) {
            throw new DocumentoInvalidoException("TI debe tener entre 10 y 11 dígitos numéricos");
        }
    }

    @Override
    public void validateEdad(Integer edad) {
        if (edad != null && (edad < 7 || edad > 17)) {
            throw new DocumentoInvalidoException(
                    "Tarjeta de Identidad solo aplica para personas entre 7 y 17 años");
        }
    }
}
