package com.bankary.cliente.domain;

import com.bankary.cliente.domain.exception.DocumentoInvalidoException;
import com.bankary.cliente.domain.model.TipoDocumento;

public class DocumentoValidator {

    public static void validate(TipoDocumento tipo, String numero, Integer edad) {
        if (tipo == null) {
            throw new DocumentoInvalidoException("El tipo de documento es obligatorio");
        }
        if (numero == null || numero.isBlank()) {
            throw new DocumentoInvalidoException("El numero de documento es obligatorio");
        }

        // Delegación basada en estrategia (SOLID: OCP)
        tipo.validateFormat(numero);
        tipo.validateEdad(edad);
    }
}
