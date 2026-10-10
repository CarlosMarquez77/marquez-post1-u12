package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.empresa.reembolsos.Solicitudes.solicitud;
import static org.junit.jupiter.api.Assertions.*;

class CalculadoraViajeTest {

    private final CalculadoraViaje calculadora = new CalculadoraViaje();

    @Test
    void getCategoria_esViaje() {
        assertEquals(CategoriaGasto.VIAJE, calculadora.getCategoria());
    }

    @Test
    void calcular_montoBajoElTopeDiarioPorDias_reembolsaElMontoCompleto() {
        SolicitudReembolso solicitud = solicitud(CategoriaGasto.VIAJE, "250000");
        solicitud.setDiasViaje(2);

        assertEquals(0, new BigDecimal("250000").compareTo(calculadora.calcular(solicitud)));
    }

    @Test
    void calcular_montoSobreElTopeDiarioPorDias_reembolsaSoloElTope() {
        // tope = 150000 * 3 dias = 450000
        SolicitudReembolso solicitud = solicitud(CategoriaGasto.VIAJE, "500000");
        solicitud.setDiasViaje(3);

        assertEquals(0, new BigDecimal("450000").compareTo(calculadora.calcular(solicitud)));
    }
}
