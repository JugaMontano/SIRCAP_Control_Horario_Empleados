package sircap;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Reporte consolidado y exportable de asistencia en un rango de fechas (RF-06). */
public class ReporteAsistencia {
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private List<RegistroAsistencia> registros = new ArrayList<>();
    private List<Ausencia> ausencias = new ArrayList<>();

    public ReporteAsistencia(LocalDate fechaInicio, LocalDate fechaFin,
                             List<RegistroAsistencia> todosLosRegistros, List<Ausencia> todasLasAusencias) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        for (RegistroAsistencia r : todosLosRegistros) {
            LocalDate dia = r.getMarcaEntrada().toLocalDate();
            if (!dia.isBefore(fechaInicio) && !dia.isAfter(fechaFin)) {
                registros.add(r);
            }
        }
        for (Ausencia a : todasLasAusencias) {
            if (!a.getFecha().isBefore(fechaInicio) && !a.getFecha().isAfter(fechaFin)) {
                ausencias.add(a);
            }
        }
    }

    public double totalHorasTrabajadas() {
        double total = 0;
        for (RegistroAsistencia r : registros) {
            total += r.calcularHorasTrabajadas();
        }
        return total;
    }

    public double totalHorasExtras() {
        double total = 0;
        for (RegistroAsistencia r : registros) {
            total += r.getHorasExtrasCalculadas();
        }
        return total;
    }

    public int totalAusencias() { return ausencias.size(); }

    /** Exporta el reporte en formato CSV (texto separado por comas). */
    public String exportarCSV() {
        StringBuilder sb = new StringBuilder("empleado,entrada,salida,horas_trabajadas,horas_extras\n");
        for (RegistroAsistencia r : registros) {
            sb.append('"').append(r.getEmpleado().getNombreCompleto().replace("\"", "\"\"")).append('"').append(',')
              .append(r.getMarcaEntrada()).append(',')
              .append(r.getMarcaSalida() != null ? r.getMarcaSalida() : "").append(',')
              .append(String.format("%.2f", r.calcularHorasTrabajadas())).append(',')
              .append(String.format("%.2f", r.getHorasExtrasCalculadas())).append('\n');
        }
        return sb.toString();
    }

    public List<RegistroAsistencia> getRegistros() { return registros; }
    public List<Ausencia> getAusencias() { return ausencias; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
}
