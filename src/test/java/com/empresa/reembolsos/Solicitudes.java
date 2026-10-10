package com.empresa.reembolsos;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Construye solicitudes validas para las pruebas; cada prueba cambia
 * solo el dato que le interesa.
 */
public final class Solicitudes {

    private Solicitudes() {
    }

    public static SolicitudReembolso solicitud(CategoriaGasto categoria, String monto) {
        SolicitudReembolso solicitud = new SolicitudReembolso();
        solicitud.setEmpleadoId("E-100");
        solicitud.setCategoria(categoria);
        solicitud.setMontoSolicitado(new BigDecimal(monto));
        solicitud.setFechaGasto(LocalDate.of(2026, 10, 5));
        solicitud.setDiasViaje(1);
        solicitud.setDocumentosSoporte(true);
        return solicitud;
    }
}
