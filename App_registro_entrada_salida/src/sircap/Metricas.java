package sircap;

import java.util.LinkedHashMap;
import java.util.Map;

/** Indicadores del panel de consulta estratégica (RF-09). Corresponden al mes en curso. */
public class Metricas {
    public int empleadosActivos;
    public int presentesAhora;
    public int tardanzasMes;
    public int ausenciasMes;
    public int solicitudesPendientes;
    public int horasExtrasPendientes;
    public double horasTrabajadasMes;
    public double horasExtrasMes;
    /** Horas trabajadas en el mes por empleado (nombre -> horas). */
    public Map<String, Double> horasPorEmpleado = new LinkedHashMap<>();
}
