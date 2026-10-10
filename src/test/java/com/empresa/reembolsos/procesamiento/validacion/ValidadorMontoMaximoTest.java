package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static com.empresa.reembolsos.Solicitudes.solicitud;
import static org.junit.jupiter.api.Assertions.*;

class ValidadorMontoMaximoTest {

    private final ValidadorMontoMaximo validador = new ValidadorMontoMaximo();

    @ParameterizedTest
    @CsvSource({
        "VIAJE, 150000",
        "REPRESENTACION, 2000000",
        "CAPACITACION, 3000000"
    })
    void validar_montoIgualAlTopeDeLaCategoria_esValida(CategoriaGasto categoria, String tope) {
        assertTrue(validador.validar(solicitud(categoria, tope), List.of()).valida());
    }

    @ParameterizedTest
    @CsvSource({
        "VIAJE, 150001",
        "REPRESENTACION, 2000001",
        "CAPACITACION, 3000001"
    })
    void validar_montoSobreElTopeDeLaCategoria_rechazaNombrandoLaCategoria(CategoriaGasto categoria, String monto) {
        ResultadoValidacion resultado = validador.validar(solicitud(categoria, monto), List.of());

        assertFalse(resultado.valida());
        assertEquals("Monto excede el tope de la categoria " + categoria, resultado.motivoRechazo());
    }

    @Test
    void validar_montoBajoElTope_esValida() {
        assertTrue(validador.validar(solicitud(CategoriaGasto.VIAJE, "80000"), List.of()).valida());
    }
}
