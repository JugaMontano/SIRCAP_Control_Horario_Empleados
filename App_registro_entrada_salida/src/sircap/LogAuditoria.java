package sircap;

import java.time.LocalDateTime;

/** Registro de auditoría de cambios hechos por usuarios con permisos (RNF-05). */
public class LogAuditoria {
    private LocalDateTime fechaHora;
    private String nombreUsuario;
    private String accion;

    public LogAuditoria(LocalDateTime fechaHora, String nombreUsuario, String accion) {
        this.fechaHora = fechaHora;
        this.nombreUsuario = nombreUsuario;
        this.accion = accion;
    }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getAccion() { return accion; }

    @Override
    public String toString() {
        return fechaHora + " | " + nombreUsuario + " | " + accion;
    }
}
