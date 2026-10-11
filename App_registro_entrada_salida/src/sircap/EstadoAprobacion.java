package sircap;

/** Estado de una solicitud, ausencia u hora extra que requiere aprobación de RRHH. */
public enum EstadoAprobacion {
    PENDIENTE("Pendiente"), APROBADA("Aprobada"), RECHAZADA("Rechazada");

    private final String etiqueta;

    EstadoAprobacion(String etiqueta) { this.etiqueta = etiqueta; }

    @Override
    public String toString() { return etiqueta; }
}
