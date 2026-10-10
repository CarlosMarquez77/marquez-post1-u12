package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.empresa.reembolsos.Solicitudes.solicitud;
import static org.junit.jupiter.api.Assertions.*;

class ValidadorDuplicadosTest {

    private final ValidadorDuplicados validador = new ValidadorDuplicados();

    @Test
    void validar_sinPendientes_esValida() {
        assertTrue(validador.validar(solicitud(CategoriaGasto.VIAJE, "100000"), List.of()).valida());
    }

    @Test
    void validar_pendienteDeLaMismaCategoriaYMes_rechaza() {
        SolicitudReembolso pendiente = solicitud(CategoriaGasto.VIAJE, "50000");
        pendiente.setFechaGasto(LocalDate.of(2026, 10, 28));

        ResultadoValidacion resultado = validador.validar(solicitud(CategoriaGasto.VIAJE, "100000"), List.of(pendiente));

        assertFalse(resultado.valida());
        assertEquals("Ya existe una solicitud pendiente de la misma categoria en el periodo",
            resultado.motivoRechazo());
    }

    @Test
    void validar_pendienteDeOtraCategoriaEnElMismoMes_esValida() {
        SolicitudReembolso pendiente = solicitud(CategoriaGasto.CAPACITACION, "50000");

        assertTrue(validador.validar(solicitud(CategoriaGasto.VIAJE, "100000"), List.of(pendiente)).valida());
    }

    @Test
    void validar_pendienteDeLaMismaCategoriaEnOtroMes_esValida() {
        SolicitudReembolso pendiente = solicitud(CategoriaGasto.VIAJE, "50000");
        pendiente.setFechaGasto(LocalDate.of(2026, 9, 30));

        assertTrue(validador.validar(solicitud(CategoriaGasto.VIAJE, "100000"), List.of(pendiente)).valida());
    }
}
