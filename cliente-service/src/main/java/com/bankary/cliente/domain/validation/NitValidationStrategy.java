package com.bankary.cliente.domain.validation;

import com.bankary.cliente.domain.exception.DocumentoInvalidoException;
import java.util.regex.Pattern;

public class NitValidationStrategy implements DocumentoValidationStrategy {

    private static final Pattern PATTERN = Pattern.compile("^\\d{9}-\\d{1}$");

    @Override
    public void validateFormat(String numero) {
        if (numero == null || !PATTERN.matcher(numero).matches()) {
            throw new DocumentoInvalidoException(
                    "NIT debe tener 9 dígitos numéricos más dígito verificador separado por guion (XXXXXXXXX-D)");
        }
    }
}
