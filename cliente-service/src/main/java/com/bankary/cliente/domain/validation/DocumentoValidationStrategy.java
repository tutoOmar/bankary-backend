package com.bankary.cliente.domain.validation;

public interface DocumentoValidationStrategy {

    void validateFormat(String numero);

    default void validateEdad(Integer edad) {
    }
}
