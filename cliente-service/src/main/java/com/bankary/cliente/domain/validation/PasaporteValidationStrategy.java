package com.bankary.cliente.domain.validation;

import com.bankary.cliente.domain.exception.DocumentoInvalidoException;
import java.util.regex.Pattern;

public class PasaporteValidationStrategy implements DocumentoValidationStrategy {

    private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9]{5,12}$");

    @Override
    public void validateFormat(String numero) {
        if (numero == null || !PATTERN.matcher(numero).matches()) {
            throw new DocumentoInvalidoException("PASAPORTE debe tener entre 5 y 12 caracteres alfanuméricos");
        }
    }
}
