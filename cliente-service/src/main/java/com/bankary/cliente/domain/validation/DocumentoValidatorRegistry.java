package com.bankary.cliente.domain.validation;

import com.bankary.cliente.domain.exception.DocumentoInvalidoException;
import com.bankary.cliente.domain.model.TipoDocumento;

import java.util.EnumMap;
import java.util.Map;

public class DocumentoValidatorRegistry {

    private static final Map<TipoDocumento, DocumentoValidationStrategy> REGISTRY =
            new EnumMap<>(TipoDocumento.class);

    static {
        REGISTRY.put(TipoDocumento.CC,        new CcValidationStrategy());
        REGISTRY.put(TipoDocumento.TI,        new TiValidationStrategy());
        REGISTRY.put(TipoDocumento.CE,        new CeValidationStrategy());
        REGISTRY.put(TipoDocumento.NIT,       new NitValidationStrategy());
        REGISTRY.put(TipoDocumento.PASAPORTE, new PasaporteValidationStrategy());
    }

    public static DocumentoValidationStrategy getStrategy(TipoDocumento tipo) {
        DocumentoValidationStrategy strategy = REGISTRY.get(tipo);
        if (strategy == null) {
            throw new DocumentoInvalidoException(
                    "No existe validador registrado para el tipo de documento: " + tipo);
        }
        return strategy;
    }
}
