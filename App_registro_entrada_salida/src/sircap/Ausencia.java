package sircap;

import java.time.LocalDate;

/** Ausencia, incapacidad o novedad justificada de un empleado (RF-05). */
public class Ausencia {
    private Empleado empleado;
    private TipoNovedad tipo;
    private LocalDate fecha;
    private String justificacion;
    private EstadoAprobacion estado;

    public Ausencia(Empleado empleado, TipoNovedad tipo, LocalDate fecha, String justificacion) {
        this.empleado = empleado;
        this.tipo = tipo;
        this.fecha = fecha;
        this.justificacion = justificacion;
        this.estado = EstadoAprobacion.PENDIENTE;
    }

    public Empleado getEmpleado() { return empleado; }
    public TipoNovedad getTipo() { return tipo; }
    public LocalDate getFecha() { return fecha; }
    public String getJustificacion() { return justificacion; }
    public EstadoAprobacion getEstado() { return estado; }

    public void aprobar() { this.estado = EstadoAprobacion.APROBADA; }
    public void rechazar() { this.estado = EstadoAprobacion.RECHAZADA; }
}
