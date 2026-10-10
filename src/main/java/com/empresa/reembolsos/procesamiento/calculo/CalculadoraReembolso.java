package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

import java.math.BigDecimal;

/**
 * Estrategia de calculo del monto aprobado (Strategy).
 * Hay una implementacion por categoria de gasto; una categoria nueva
 * se agrega creando otra implementacion, sin modificar las existentes.
 */
public interface CalculadoraReembolso {

    /**
     * @return la categoria de gasto que esta calculadora sabe resolver
     */
    CategoriaGasto getCategoria();

    /**
     * Calcula el monto aprobado segun la formula de la categoria.
     *
     * @param solicitud solicitud ya validada
     * @return monto a reembolsar
     */
    BigDecimal calcular(SolicitudReembolso solicitud);
}
