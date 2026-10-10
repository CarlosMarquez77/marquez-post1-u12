package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SelectorCalculadoraTest {

    @ParameterizedTest
    @EnumSource(CategoriaGasto.class)
    void obtener_categoriaRegistrada_entregaLaCalculadoraDeEsaCategoria(CategoriaGasto categoria) {
        SelectorCalculadora selector = new SelectorCalculadora(List.of(
            new CalculadoraViaje(), new CalculadoraRepresentacion(), new CalculadoraCapacitacion()));

        assertEquals(categoria, selector.obtener(categoria).getCategoria());
    }

    @Test
    void obtener_categoriaSinCalculadora_lanzaIllegalArgumentException() {
        SelectorCalculadora selector = new SelectorCalculadora(List.of(new CalculadoraViaje()));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
            () -> selector.obtener(CategoriaGasto.CAPACITACION));
        assertEquals("Categoria no soportada: CAPACITACION", error.getMessage());
    }
}
