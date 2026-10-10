package com.empresa.reembolsos.procesamiento.notificacion;

import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

/**
 * Interesado en los cambios de estado de una solicitud (Observer).
 * El servicio de aplicacion avisa a todas las implementaciones registradas
 * sin conocer cuantas son ni que hace cada una.
 */
public interface ObservadorCambioEstado {

    /**
     * Se invoca despues de guardar el nuevo estado de la solicitud.
     *
     * @param solicitud      solicitud con su estado ya actualizado
     * @param estadoAnterior estado que tenia antes del cambio
     */
    void onCambioEstado(SolicitudReembolso solicitud, EstadoSolicitud estadoAnterior);
}
