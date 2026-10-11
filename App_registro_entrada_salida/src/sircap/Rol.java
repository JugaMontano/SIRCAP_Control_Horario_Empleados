package sircap;

import java.util.EnumSet;
import java.util.Set;

/** Roles del sistema (RF-10). Cada rol define a qué módulos puede acceder. */
public enum Rol {
    ADMINISTRADOR("Administrador", EnumSet.allOf(Modulo.class)),
    RRHH("Recursos Humanos", EnumSet.of(Modulo.REGISTRO_ASISTENCIA, Modulo.PANEL_EMPLEADO, Modulo.SOLICITUDES,
            Modulo.GESTION_HORARIOS, Modulo.AUSENCIAS, Modulo.REPORTES,
            Modulo.HORAS_EXTRAS, Modulo.REGISTRO_EMPLEADOS)),
    GERENCIA("Gerencia", EnumSet.of(Modulo.REGISTRO_ASISTENCIA, Modulo.PANEL_EMPLEADO, Modulo.SOLICITUDES,
            Modulo.GESTION_HORARIOS, Modulo.REPORTES, Modulo.PANEL_ESTRATEGICO)),
    EMPLEADO("Empleado", EnumSet.of(Modulo.REGISTRO_ASISTENCIA, Modulo.PANEL_EMPLEADO, Modulo.SOLICITUDES));

    private final String etiqueta;
    private final Set<Modulo> modulosPermitidos;

    Rol(String etiqueta, Set<Modulo> modulosPermitidos) {
        this.etiqueta = etiqueta;
        this.modulosPermitidos = modulosPermitidos;
    }

    @Override
    public String toString() { return etiqueta; }

    public boolean puedeAcceder(Modulo modulo) {
        return modulosPermitidos.contains(modulo);
    }
}
