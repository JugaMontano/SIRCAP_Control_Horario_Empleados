package sircap;

import java.time.Duration;
import java.time.LocalTime;

/** Horario laboral esperado de un empleado (RF-03). */
public class HorarioLaboral {
    private String nombre;
    private LocalTime horaEntradaEsperada;
    private LocalTime horaSalidaEsperada;

    public HorarioLaboral(String nombre, LocalTime horaEntradaEsperada, LocalTime horaSalidaEsperada) {
        this.nombre = nombre;
        this.horaEntradaEsperada = horaEntradaEsperada;
        this.horaSalidaEsperada = horaSalidaEsperada;
    }

    public String getNombre() { return nombre; }
    public LocalTime getHoraEntradaEsperada() { return horaEntradaEsperada; }
    public LocalTime getHoraSalidaEsperada() { return horaSalidaEsperada; }

    public void modificar(LocalTime nuevaEntrada, LocalTime nuevaSalida) {
        this.horaEntradaEsperada = nuevaEntrada;
        this.horaSalidaEsperada = nuevaSalida;
    }

    public double horasPorJornada() {
        return Duration.between(horaEntradaEsperada, horaSalidaEsperada).toMinutes() / 60.0;
    }

    @Override
    public String toString() {
        return nombre + " (" + horaEntradaEsperada + " - " + horaSalidaEsperada + ")";
    }
}
