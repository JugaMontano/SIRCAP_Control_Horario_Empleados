package sircap;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Marca de entrada y salida de un empleado (RF-01) con cálculo automático de horas extras (RF-07).
 * Las marcas no tienen setters: una vez registradas no se pueden alterar (RNF-05).
 */
public class RegistroAsistencia {
    private static final long MINUTOS_MINIMOS_HORA_EXTRA = 15;
    private static final long TOLERANCIA_TARDANZA_MINUTOS = 5;

    private Empleado empleado;
    private LocalDateTime marcaEntrada;
    private LocalDateTime marcaSalida;
    private double horasExtrasCalculadas;
    private EstadoAprobacion estadoHorasExtras;
    private long minutosTarde;

    public RegistroAsistencia(Empleado empleado, LocalDateTime marcaEntrada) {
        this.empleado = empleado;
        this.marcaEntrada = marcaEntrada;
        this.horasExtrasCalculadas = 0.0;
        this.estadoHorasExtras = EstadoAprobacion.PENDIENTE;
        LocalTime esperada = empleado.getHorarioAsignado().getHoraEntradaEsperada();
        LocalTime real = marcaEntrada.toLocalTime();
        this.minutosTarde = real.isAfter(esperada) ? Duration.between(esperada, real).toMinutes() : 0;
    }

    /** Constructor para reconstruir un registro guardado. */
    public RegistroAsistencia(Empleado empleado, LocalDateTime marcaEntrada, LocalDateTime marcaSalida,
                              double horasExtras, EstadoAprobacion estadoHorasExtras, long minutosTarde) {
        this.empleado = empleado;
        this.marcaEntrada = marcaEntrada;
        this.marcaSalida = marcaSalida;
        this.horasExtrasCalculadas = horasExtras;
        this.estadoHorasExtras = estadoHorasExtras;
        this.minutosTarde = minutosTarde;
    }

    public long getMinutosTarde() { return minutosTarde; }

    public boolean llegoTarde() { return minutosTarde > TOLERANCIA_TARDANZA_MINUTOS; }

    public Empleado getEmpleado() { return empleado; }
    public LocalDateTime getMarcaEntrada() { return marcaEntrada; }
    public LocalDateTime getMarcaSalida() { return marcaSalida; }
    public double getHorasExtrasCalculadas() { return horasExtrasCalculadas; }
    public EstadoAprobacion getEstadoHorasExtras() { return estadoHorasExtras; }

    public boolean estaAbierto() { return marcaSalida == null; }

    public void registrarSalida(LocalDateTime marcaSalida) {
        if (this.marcaSalida != null) {
            throw new IllegalStateException("La salida ya fue registrada y no puede modificarse.");
        }
        if (!marcaSalida.isAfter(marcaEntrada)) {
            throw new IllegalArgumentException("La salida debe ser posterior a la entrada.");
        }
        this.marcaSalida = marcaSalida;
        calcularHorasExtras();
    }

    // RF-07: diferencia entre la salida esperada del horario y la salida real
    private void calcularHorasExtras() {
        LocalTime salidaEsperada = empleado.getHorarioAsignado().getHoraSalidaEsperada();
        LocalTime salidaReal = marcaSalida.toLocalTime();
        if (salidaReal.isAfter(salidaEsperada)) {
            long minutosExcedidos = Duration.between(salidaEsperada, salidaReal).toMinutes();
            if (minutosExcedidos >= MINUTOS_MINIMOS_HORA_EXTRA) {
                this.horasExtrasCalculadas = minutosExcedidos / 60.0;
            }
        }
    }

    public double calcularHorasTrabajadas() {
        if (marcaSalida == null) {
            return 0.0;
        }
        return Duration.between(marcaEntrada, marcaSalida).toMinutes() / 60.0;
    }

    public void resolverHorasExtras(EstadoAprobacion decision) {
        this.estadoHorasExtras = decision;
    }

    public void mostrarDetalleAsistencia() {
        System.out.println("--- Reporte de Asistencia ---");
        System.out.println("Empleado: " + empleado.getNombreCompleto());
        System.out.println("Entrada: " + marcaEntrada);
        System.out.println("Salida: " + (marcaSalida != null ? marcaSalida : "En turno / No registrada"));
        System.out.println("Horas extras calculadas: " + String.format("%.2f", horasExtrasCalculadas) + " hrs");
    }
}
