package sircap;

import java.time.LocalDate;

/** Solicitud de permiso, vacaciones o cambio de turno enviada a RRHH (RF-04). */
public class Solicitud {
    private String idSolicitud;
    private Empleado solicitante;
    private TipoSolicitud tipo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String motivo;
    private EstadoAprobacion estado;

    public Solicitud(String idSolicitud, Empleado solicitante, TipoSolicitud tipo,
                     LocalDate fechaInicio, LocalDate fechaFin, String motivo) {
        this.idSolicitud = idSolicitud;
        this.solicitante = solicitante;
        this.tipo = tipo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.motivo = motivo;
        this.estado = EstadoAprobacion.PENDIENTE;
    }

    public String getIdSolicitud() { return idSolicitud; }
    public Empleado getSolicitante() { return solicitante; }
    public TipoSolicitud getTipo() { return tipo; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public String getMotivo() { return motivo; }
    public EstadoAprobacion getEstado() { return estado; }

    public void aprobar() { this.estado = EstadoAprobacion.APROBADA; }
    public void rechazar() { this.estado = EstadoAprobacion.RECHAZADA; }
}
