package com.bankary.cuenta.domain.port.in;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.Map;

public interface ReporteUseCase {
    List<Map<String, Object>> generarReporte(LocalDate fechaInicio, LocalDate fechaFin, UUID clienteId);
}
