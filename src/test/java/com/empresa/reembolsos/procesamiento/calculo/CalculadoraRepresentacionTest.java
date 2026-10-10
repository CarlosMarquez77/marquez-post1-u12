package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.empresa.reembolsos.Solicitudes.solicitud;
import static org.junit.jupiter.api.Assertions.*;

class CalculadoraRepresentacionTest {

    private final CalculadoraRepresentacion calculadora = new CalculadoraRepresentacion();

    @Test
    void getCategoria_esRepresentacion() {
        assertEquals(CategoriaGasto.REPRESENTACION, calculadora.getCategoria());
    }

    @Test
    void calcular_montoBajoElTopeMensual_reembolsaElMontoCompleto() {
        BigDecimal aprobado = calculadora.calcular(solicitud(CategoriaGasto.REPRESENTACION, "850000"));

        assertEquals(0, new BigDecimal("850000").compareTo(aprobado));
    }

    @Test
    void calcular_montoSobreElTopeMensual_reembolsaSoloElTope() {
        BigDecimal aprobado = calculadora.calcular(solicitud(CategoriaGasto.REPRESENTACION, "2600000"));

        assertEquals(0, new BigDecimal("2000000").compareTo(aprobado));
    }
}
