package sircap;

/** Tipos de solicitud que puede enviar un empleado (RF-04). */
public enum TipoSolicitud {
    PERMISO("Permiso"), VACACIONES("Vacaciones"), CAMBIO_TURNO("Cambio de turno");

    private final String etiqueta;

    TipoSolicitud(String etiqueta) { this.etiqueta = etiqueta; }

    @Override
    public String toString() { return etiqueta; }
}
