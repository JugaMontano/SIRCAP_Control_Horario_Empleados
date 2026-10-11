package sircap;

/** Empleado de la organización (RF-08). */
public class Empleado {
    private String idEmpleado;
    private String nombreCompleto;
    private Rol rol;
    private boolean activo;
    private HorarioLaboral horarioAsignado;

    public Empleado(String idEmpleado, String nombreCompleto, Rol rol, HorarioLaboral horarioAsignado) {
        this.idEmpleado = idEmpleado;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.horarioAsignado = horarioAsignado;
        this.activo = true;
    }

    @Override
    public String toString() { return nombreCompleto + " (" + idEmpleado + ")"; }

    public String getIdEmpleado() { return idEmpleado; }
    public String getNombreCompleto() { return nombreCompleto; }
    public Rol getRol() { return rol; }
    public boolean isActivo() { return activo; }
    public HorarioLaboral getHorarioAsignado() { return horarioAsignado; }

    public void setHorarioAsignado(HorarioLaboral horarioAsignado) { this.horarioAsignado = horarioAsignado; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
