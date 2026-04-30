package com.bankary.cliente.domain;

import com.bankary.cliente.domain.exception.DocumentoInvalidoException;
import com.bankary.cliente.domain.model.TipoDocumento;
import com.bankary.cliente.domain.validation.DocumentoValidationStrategy;
import com.bankary.cliente.domain.validation.DocumentoValidatorRegistry;

public class DocumentoValidator {

    public static void validate(TipoDocumento tipo, String numero, Integer edad) {
        if (tipo == null) {
            throw new DocumentoInvalidoException("El tipo de documento es obligatorio");
        }
        if (numero == null || numero.isBlank()) {
            throw new DocumentoInvalidoException("El numero de documento es obligatorio");
        }

        DocumentoValidationStrategy strategy = DocumentoValidatorRegistry.getStrategy(tipo);
        strategy.validateFormat(numero);
        strategy.validateEdad(edad);
    }
}
