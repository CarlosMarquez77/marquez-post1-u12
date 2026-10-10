package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;

import java.util.List;

/**
 * Eslabon de la cadena de validacion (Chain of Responsibility).
 * Cada eslabon ejecuta su propia verificacion y, si la solicitud la pasa,
 * la entrega al siguiente sin saber cuantos eslabones quedan.
 */
public abstract class ValidadorSolicitud {

    private ValidadorSolicitud siguiente;

    /**
     * Enlaza el eslabon que sigue a este.
     *
     * @return el eslabon recibido, para poder encadenar las llamadas
     */
    public ValidadorSolicitud siguiente(ValidadorSolicitud siguiente) {
        this.siguiente = siguiente;
        return siguiente;
    }

    /**
     * Valida la solicitud en este eslabon y en los siguientes.
     *
     * @param solicitud            solicitud que se quiere registrar
     * @param pendientesDelEmpleado solicitudes pendientes del mismo empleado, ya cargadas
     * @return el primer rechazo encontrado o un resultado valido si pasa toda la cadena
     */
    public ResultadoValidacion validar(SolicitudReembolso solicitud, List<SolicitudReembolso> pendientesDelEmpleado) {
        ResultadoValidacion propio = validarPropio(solicitud, pendientesDelEmpleado);
        if (!propio.valida() || siguiente == null) {
            return propio;
        }
        return siguiente.validar(solicitud, pendientesDelEmpleado);
    }

    protected abstract ResultadoValidacion validarPropio(
        SolicitudReembolso solicitud, List<SolicitudReembolso> pendientesDelEmpleado);
}
