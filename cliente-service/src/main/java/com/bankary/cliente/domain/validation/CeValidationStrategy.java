package com.bankary.cliente.domain.validation;

import com.bankary.cliente.domain.exception.DocumentoInvalidoException;
import java.util.regex.Pattern;

public class CeValidationStrategy implements DocumentoValidationStrategy {

    private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9]{6,12}$");

    @Override
    public void validateFormat(String numero) {
        if (numero == null || !PATTERN.matcher(numero).matches()) {
            throw new DocumentoInvalidoException("CE debe tener entre 6 y 12 caracteres alfanuméricos");
        }
    }
}
