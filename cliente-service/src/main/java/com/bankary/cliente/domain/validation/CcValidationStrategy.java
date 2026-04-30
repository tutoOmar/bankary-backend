package com.bankary.cliente.domain.validation;

import com.bankary.cliente.domain.exception.DocumentoInvalidoException;
import java.util.regex.Pattern;

public class CcValidationStrategy implements DocumentoValidationStrategy {

    private static final Pattern PATTERN = Pattern.compile("^\\d{8,10}$");

    @Override
    public void validateFormat(String numero) {
        if (numero == null || !PATTERN.matcher(numero).matches()) {
            throw new DocumentoInvalidoException("CC debe tener entre 8 y 10 dígitos numéricos");
        }
    }

    @Override
    public void validateEdad(Integer edad) {
        if (edad != null && edad < 18) {
            throw new DocumentoInvalidoException("Cédula de Ciudadanía requiere edad mínima de 18 años");
        }
    }
}
