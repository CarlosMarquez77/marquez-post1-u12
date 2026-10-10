package com.empresa.reembolsos;

import com.empresa.reembolsos.infraestructura.RepositorioSolicitudesJpa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class FlujoSolicitudesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepositorioSolicitudesJpa repositorio;

    @BeforeEach
    void limpiarSolicitudes() {
        repositorio.deleteAll();
    }

    @ParameterizedTest
    @CsvSource({
        "VIAJE, 120000, 2, 120000",
        "REPRESENTACION, 850000, 0, 850000",
        "CAPACITACION, 1000000, 0, 800000"
    })
    void crear_solicitudValidaDeCadaCategoria_quedaPendienteConElMontoAprobadoDeSuFormula(
            String categoria, String monto, int diasViaje, double aprobadoEsperado) throws Exception {
        mockMvc.perform(post("/api/solicitudes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(solicitudJson(categoria, monto, diasViaje, true)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.estado").value("PENDIENTE"))
            .andExpect(jsonPath("$.montoAprobado").value(aprobadoEsperado));
    }

    @Test
    void crear_sinDocumentosSoporte_respondeConElMotivoYNoGuardaNada() throws Exception {
        mockMvc.perform(post("/api/solicitudes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(solicitudJson("VIAJE", "90000", 1, false)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.error").value("Faltan documentos soporte"));

        assertEquals(0, repositorio.count());
    }

    @Test
    void crear_segundaSolicitudDeLaMismaCategoriaYMes_seRechazaPorDuplicada() throws Exception {
        crearSolicitud("VIAJE", "120000", 2);

        mockMvc.perform(post("/api/solicitudes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(solicitudJson("VIAJE", "50000", 1, true)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.error")
                .value("Ya existe una solicitud pendiente de la misma categoria en el periodo"));
    }

    @Test
    void cambiarEstado_aAprobada_notificaAlEmpleadoAContabilidadYAAuditoria(CapturedOutput salida) throws Exception {
        long id = crearSolicitud("VIAJE", "120000", 2);

        mockMvc.perform(put("/api/solicitudes/" + id + "/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"APROBADA\""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("APROBADA"));

        assertTrue(salida.getOut().contains("Notificando a E-100: su solicitud paso de PENDIENTE a APROBADA"));
        assertTrue(salida.getOut().contains("Contabilidad: programar pago de 120000.00 para E-100"));
        assertTrue(salida.getOut().contains("Auditoria: solicitud " + id + " cambio de PENDIENTE a APROBADA"));
    }

    @Test
    void cambiarEstado_aRechazada_noAvisaAContabilidad(CapturedOutput salida) throws Exception {
        long id = crearSolicitud("REPRESENTACION", "850000", 0);

        mockMvc.perform(put("/api/solicitudes/" + id + "/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"RECHAZADA\""))
            .andExpect(status().isOk());

        assertTrue(salida.getOut().contains("Notificando a E-100: su solicitud paso de PENDIENTE a RECHAZADA"));
        assertTrue(salida.getOut().contains("Auditoria: solicitud " + id + " cambio de PENDIENTE a RECHAZADA"));
        assertFalse(salida.getOut().contains("Contabilidad: programar pago"));
    }

    @Test
    void cambiarEstado_solicitudInexistente_respondeConError() throws Exception {
        mockMvc.perform(put("/api/solicitudes/999/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"APROBADA\""))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Solicitud no encontrada: 999"));
    }

    private long crearSolicitud(String categoria, String monto, int diasViaje) throws Exception {
        mockMvc.perform(post("/api/solicitudes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(solicitudJson(categoria, monto, diasViaje, true)))
            .andExpect(status().isOk());
        return repositorio.findAll().get(0).getId();
    }

    private String solicitudJson(String categoria, String monto, int diasViaje, boolean documentos) {
        return """
            {"empleadoId":"E-100","categoria":"%s","montoSolicitado":%s,
             "fechaGasto":"2026-10-05","diasViaje":%d,"documentosSoporte":%b}
            """.formatted(categoria, monto, diasViaje, documentos);
    }
}
