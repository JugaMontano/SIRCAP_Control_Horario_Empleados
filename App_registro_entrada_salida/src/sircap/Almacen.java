package sircap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Guarda y carga los datos del sistema en archivos CSV dentro de una carpeta (por defecto "datos").
 * Es la única clase que sabe CÓMO se guardan los datos: si más adelante se cambia a SQLite u otra
 * base de datos, solo hay que reemplazar esta clase.
 */
public class Almacen {
    private final Path carpeta;

    public Almacen(String nombreCarpeta) {
        this.carpeta = Paths.get(nombreCarpeta);
        try {
            Files.createDirectories(carpeta);
        } catch (IOException e) {
            throw new SircapException("No se pudo crear la carpeta de datos: " + e.getMessage());
        }
    }

    public Path getCarpeta() { return carpeta; }

    // ====================== HORARIOS ======================
    public List<HorarioLaboral> cargarHorarios() {
        List<HorarioLaboral> lista = new ArrayList<>();
        for (String[] f : leer("horarios.csv")) {
            lista.add(new HorarioLaboral(f[0], LocalTime.parse(f[1]), LocalTime.parse(f[2])));
        }
        return lista;
    }

    public void guardarHorarios(List<HorarioLaboral> horarios) {
        List<String[]> filas = new ArrayList<>();
        for (HorarioLaboral h : horarios) {
            filas.add(new String[]{h.getNombre(), h.getHoraEntradaEsperada().toString(), h.getHoraSalidaEsperada().toString()});
        }
        escribir("horarios.csv", filas);
    }

    // ====================== EMPLEADOS ======================
    public List<Empleado> cargarEmpleados(List<HorarioLaboral> horarios) {
        List<Empleado> lista = new ArrayList<>();
        for (String[] f : leer("empleados.csv")) {
            HorarioLaboral horario = null;
            for (HorarioLaboral h : horarios) {
                if (h.getNombre().equals(f[4])) horario = h;
            }
            Empleado e = new Empleado(f[0], f[1], Rol.valueOf(f[2]), horario);
            e.setActivo(Boolean.parseBoolean(f[3]));
            lista.add(e);
        }
        return lista;
    }

    public void guardarEmpleados(List<Empleado> empleados) {
        List<String[]> filas = new ArrayList<>();
        for (Empleado e : empleados) {
            filas.add(new String[]{e.getIdEmpleado(), e.getNombreCompleto(), e.getRol().name(),
                    String.valueOf(e.isActivo()), e.getHorarioAsignado().getNombre()});
        }
        escribir("empleados.csv", filas);
    }

    // ====================== USUARIOS ======================
    public List<Usuario> cargarUsuarios(List<Empleado> empleados) {
        List<Usuario> lista = new ArrayList<>();
        for (String[] f : leer("usuarios.csv")) {
            Empleado e = buscar(empleados, f[2]);
            if (e != null) lista.add(Usuario.desdeHash(f[0], f[1], e));
        }
        return lista;
    }

    public void guardarUsuarios(List<Usuario> usuarios) {
        List<String[]> filas = new ArrayList<>();
        for (Usuario u : usuarios) {
            filas.add(new String[]{u.getNombreUsuario(), u.getContrasenaHash(), u.getEmpleado().getIdEmpleado()});
        }
        escribir("usuarios.csv", filas);
    }

    // ====================== ASISTENCIA ======================
    public List<RegistroAsistencia> cargarRegistros(List<Empleado> empleados) {
        List<RegistroAsistencia> lista = new ArrayList<>();
        for (String[] f : leer("asistencia.csv")) {
            Empleado e = buscar(empleados, f[0]);
            if (e == null) continue;
            LocalDateTime salida = f[2].isEmpty() ? null : LocalDateTime.parse(f[2]);
            lista.add(new RegistroAsistencia(e, LocalDateTime.parse(f[1]), salida,
                    Double.parseDouble(f[3]), EstadoAprobacion.valueOf(f[4]), Long.parseLong(f[5])));
        }
        return lista;
    }

    public void guardarRegistros(List<RegistroAsistencia> registros) {
        List<String[]> filas = new ArrayList<>();
        for (RegistroAsistencia r : registros) {
            filas.add(new String[]{r.getEmpleado().getIdEmpleado(), r.getMarcaEntrada().toString(),
                    r.getMarcaSalida() == null ? "" : r.getMarcaSalida().toString(),
                    String.valueOf(r.getHorasExtrasCalculadas()), r.getEstadoHorasExtras().name(),
                    String.valueOf(r.getMinutosTarde())});
        }
        escribir("asistencia.csv", filas);
    }

    // ====================== SOLICITUDES ======================
    public List<Solicitud> cargarSolicitudes(List<Empleado> empleados) {
        List<Solicitud> lista = new ArrayList<>();
        for (String[] f : leer("solicitudes.csv")) {
            Empleado e = buscar(empleados, f[1]);
            if (e == null) continue;
            Solicitud s = new Solicitud(f[0], e, TipoSolicitud.valueOf(f[2]), LocalDate.parse(f[3]),
                    LocalDate.parse(f[4]), f[5]);
            EstadoAprobacion estado = EstadoAprobacion.valueOf(f[6]);
            if (estado == EstadoAprobacion.APROBADA) s.aprobar();
            if (estado == EstadoAprobacion.RECHAZADA) s.rechazar();
            lista.add(s);
        }
        return lista;
    }

    public void guardarSolicitudes(List<Solicitud> solicitudes) {
        List<String[]> filas = new ArrayList<>();
        for (Solicitud s : solicitudes) {
            filas.add(new String[]{s.getIdSolicitud(), s.getSolicitante().getIdEmpleado(), s.getTipo().name(),
                    s.getFechaInicio().toString(), s.getFechaFin().toString(), s.getMotivo(), s.getEstado().name()});
        }
        escribir("solicitudes.csv", filas);
    }

    // ====================== AUSENCIAS ======================
    public List<Ausencia> cargarAusencias(List<Empleado> empleados) {
        List<Ausencia> lista = new ArrayList<>();
        for (String[] f : leer("ausencias.csv")) {
            Empleado e = buscar(empleados, f[0]);
            if (e == null) continue;
            Ausencia a = new Ausencia(e, TipoNovedad.valueOf(f[1]), LocalDate.parse(f[2]), f[3]);
            EstadoAprobacion estado = EstadoAprobacion.valueOf(f[4]);
            if (estado == EstadoAprobacion.APROBADA) a.aprobar();
            if (estado == EstadoAprobacion.RECHAZADA) a.rechazar();
            lista.add(a);
        }
        return lista;
    }

    public void guardarAusencias(List<Ausencia> ausencias) {
        List<String[]> filas = new ArrayList<>();
        for (Ausencia a : ausencias) {
            filas.add(new String[]{a.getEmpleado().getIdEmpleado(), a.getTipo().name(), a.getFecha().toString(),
                    a.getJustificacion(), a.getEstado().name()});
        }
        escribir("ausencias.csv", filas);
    }

    // ====================== AUDITORÍA ======================
    public List<LogAuditoria> cargarAuditoria() {
        List<LogAuditoria> lista = new ArrayList<>();
        for (String[] f : leer("auditoria.csv")) {
            lista.add(new LogAuditoria(LocalDateTime.parse(f[0]), f[1], f[2]));
        }
        return lista;
    }

    public void guardarAuditoria(List<LogAuditoria> logs) {
        List<String[]> filas = new ArrayList<>();
        for (LogAuditoria l : logs) {
            filas.add(new String[]{l.getFechaHora().toString(), l.getNombreUsuario(), l.getAccion()});
        }
        escribir("auditoria.csv", filas);
    }

    // ====================== utilidades ======================
    private Empleado buscar(List<Empleado> empleados, String id) {
        for (Empleado e : empleados) {
            if (e.getIdEmpleado().equals(id)) return e;
        }
        return null;
    }

    private List<String[]> leer(String archivo) {
        Path ruta = carpeta.resolve(archivo);
        List<String[]> filas = new ArrayList<>();
        if (!Files.exists(ruta)) return filas;
        try {
            for (String linea : Files.readAllLines(ruta, StandardCharsets.UTF_8)) {
                if (!linea.trim().isEmpty()) filas.add(parsearLinea(linea));
            }
        } catch (IOException e) {
            throw new SircapException("No se pudo leer " + archivo + ": " + e.getMessage());
        }
        return filas;
    }

    private void escribir(String archivo, List<String[]> filas) {
        List<String> lineas = new ArrayList<>();
        for (String[] fila : filas) lineas.add(formatearLinea(fila));
        Path destino = carpeta.resolve(archivo);
        Path temporal = carpeta.resolve(archivo + ".tmp");
        try {
            Files.write(temporal, lineas, StandardCharsets.UTF_8);
            Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new SircapException("No se pudo guardar " + archivo + ": " + e.getMessage());
        }
    }

    /** Une los campos con comas; los campos se encierran en comillas y las comillas internas se duplican. */
    static String formatearLinea(String[] campos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) sb.append(',');
            String c = campos[i] == null ? "" : campos[i].replace("\r", " ").replace("\n", " ");
            sb.append('"').append(c.replace("\"", "\"\"")).append('"');
        }
        return sb.toString();
    }

    static String[] parsearLinea(String linea) {
        List<String> campos = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean entreComillas = false;
        for (int i = 0; i < linea.length(); i++) {
            char c = linea.charAt(i);
            if (entreComillas) {
                if (c == '"' && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                    actual.append('"');
                    i++;
                } else if (c == '"') {
                    entreComillas = false;
                } else {
                    actual.append(c);
                }
            } else if (c == '"') {
                entreComillas = true;
            } else if (c == ',') {
                campos.add(actual.toString());
                actual.setLength(0);
            } else {
                actual.append(c);
            }
        }
        campos.add(actual.toString());
        return campos.toArray(new String[0]);
    }
}
