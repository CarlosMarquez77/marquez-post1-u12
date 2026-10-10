package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.empresa.reembolsos.Solicitudes.solicitud;
import static org.junit.jupiter.api.Assertions.*;

class ValidadorDocumentosSoporteTest {

    private final ValidadorDocumentosSoporte validador = new ValidadorDocumentosSoporte();

    @Test
    void validar_conDocumentosSoporte_esValida() {
        SolicitudReembolso solicitud = solicitud(CategoriaGasto.VIAJE, "100000");

        ResultadoValidacion resultado = validador.validar(solicitud, List.of());

        assertTrue(resultado.valida());
        assertNull(resultado.motivoRechazo());
    }

    @Test
    void validar_sinDocumentosSoporte_rechazaConMotivo() {
        SolicitudReembolso solicitud = solicitud(CategoriaGasto.VIAJE, "100000");
        solicitud.setDocumentosSoporte(false);

        ResultadoValidacion resultado = validador.validar(solicitud, List.of());

        assertFalse(resultado.valida());
        assertEquals("Faltan documentos soporte", resultado.motivoRechazo());
    }
}
