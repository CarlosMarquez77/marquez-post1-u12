package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.empresa.reembolsos.Solicitudes.solicitud;
import static org.junit.jupiter.api.Assertions.*;

class CadenaValidadoresTest {

    private final CadenaValidadores cadena = new CadenaValidadores(
        new ValidadorDocumentosSoporte(), new ValidadorMontoMaximo(), new ValidadorDuplicados());

    @Test
    void validar_solicitudCorrecta_pasaLosTresEslabones() {
        assertTrue(cadena.validar(solicitud(CategoriaGasto.VIAJE, "100000"), List.of()).valida());
    }

    @Test
    void validar_fallanDocumentosYMonto_reportaSoloElPrimerEslabon() {
        SolicitudReembolso solicitud = solicitud(CategoriaGasto.VIAJE, "900000");
        solicitud.setDocumentosSoporte(false);

        ResultadoValidacion resultado = cadena.validar(solicitud, List.of());

        assertEquals("Faltan documentos soporte", resultado.motivoRechazo());
    }

    @Test
    void validar_fallanMontoYDuplicados_reportaElMontoPorqueVaAntes() {
        SolicitudReembolso pendiente = solicitud(CategoriaGasto.VIAJE, "50000");

        ResultadoValidacion resultado = cadena.validar(solicitud(CategoriaGasto.VIAJE, "900000"), List.of(pendiente));

        assertEquals("Monto excede el tope de la categoria VIAJE", resultado.motivoRechazo());
    }

    @Test
    void validar_soloHayDuplicado_llegaHastaElUltimoEslabon() {
        SolicitudReembolso pendiente = solicitud(CategoriaGasto.VIAJE, "50000");

        ResultadoValidacion resultado = cadena.validar(solicitud(CategoriaGasto.VIAJE, "100000"), List.of(pendiente));

        assertEquals("Ya existe una solicitud pendiente de la misma categoria en el periodo",
            resultado.motivoRechazo());
    }
}
