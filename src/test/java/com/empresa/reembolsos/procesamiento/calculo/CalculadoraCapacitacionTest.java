package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.empresa.reembolsos.Solicitudes.solicitud;
import static org.junit.jupiter.api.Assertions.*;

class CalculadoraCapacitacionTest {

    private final CalculadoraCapacitacion calculadora = new CalculadoraCapacitacion();

    @Test
    void getCategoria_esCapacitacion() {
        assertEquals(CategoriaGasto.CAPACITACION, calculadora.getCategoria());
    }

    @Test
    void calcular_montoNormal_reembolsaElOchentaPorCiento() {
        BigDecimal aprobado = calculadora.calcular(solicitud(CategoriaGasto.CAPACITACION, "1000000"));

        assertEquals(0, new BigDecimal("800000").compareTo(aprobado));
    }

    @Test
    void calcular_ochentaPorCientoSobreElTopeAnual_reembolsaSoloElTope() {
        // 80 % de 4.000.000 = 3.200.000, mayor que el tope anual de 3.000.000
        BigDecimal aprobado = calculadora.calcular(solicitud(CategoriaGasto.CAPACITACION, "4000000"));

        assertEquals(0, new BigDecimal("3000000").compareTo(aprobado));
    }
}
