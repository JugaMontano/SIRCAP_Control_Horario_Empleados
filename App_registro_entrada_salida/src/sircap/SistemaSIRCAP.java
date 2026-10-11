package sircap;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Clase controladora principal de SIRCAP. Aplica las reglas del negocio (permisos por rol, validaciones,
 * auditoría) y guarda los cambios a través del {@link Almacen}.
 * Los errores de negocio se lanzan como {@link SircapException} con un mensaje listo para mostrar.
 */
public class SistemaSIRCAP {
    public static final String USUARIO_INICIAL = "admin";
    public static final String CLAVE_INICIAL = "admin123";

    private final Almacen almacen;
    private final ServicioAutenticacion autenticacion = new ServicioAutenticacion();
    private final List<HorarioLaboral> horarios;
    private final List<Empleado> empleados;
    private final List<RegistroAsistencia> registros;
    private final List<Solicitud> solicitudes;
    private final List<Ausencia> ausencias;
    private final List<LogAuditoria> logsAuditoria;

    public SistemaSIRCAP(Almacen almacen) {
        this.almacen = almacen;
        this.horarios = almacen.cargarHorarios();
        this.empleados = almacen.cargarEmpleados(horarios);
        for (Usuario u : almacen.cargarUsuarios(empleados)) autenticacion.registrarUsuario(u);
        this.registros = almacen.cargarRegistros(empleados);
        this.solicitudes = almacen.cargarSolicitudes(empleados);
        this.ausencias = almacen.cargarAusencias(empleados);
        this.logsAuditoria = almacen.cargarAuditoria();
        if (empleados.isEmpty()) sembrarDatosIniciales();
    }

    /** Primer uso: crea un horario de oficina y el administrador inicial. */
    private void sembrarDatosIniciales() {
        HorarioLaboral oficina = new HorarioLaboral("Oficina", LocalTime.of(8, 0), LocalTime.of(17, 0));
        horarios.add(oficina);
        Empleado admin = new Empleado("EMP-001", "Administrador del sistema", Rol.ADMINISTRADOR, oficina);
        empleados.add(admin);
        autenticacion.registrarUsuario(new Usuario(USUARIO_INICIAL, CLAVE_INICIAL, admin));
        almacen.guardarHorarios(horarios);
        almacen.guardarEmpleados(empleados);
        almacen.guardarUsuarios(autenticacion.getUsuarios());
        registrarAuditoria("sistema", "Datos iniciales creados");
    }

    // ================= Consultas (listas de solo lectura) =================
    public List<HorarioLaboral> getHorarios() { return Collections.unmodifiableList(horarios); }
    public List<Empleado> getEmpleados() { return Collections.unmodifiableList(empleados); }
    public List<LogAuditoria> getLogsAuditoria() { return Collections.unmodifiableList(logsAuditoria); }
    public ServicioAutenticacion getAutenticacion() { return autenticacion; }

    public Empleado buscarEmpleado(String idEmpleado) {
        for (Empleado e : empleados) {
            if (e.getIdEmpleado().equals(idEmpleado)) return e;
        }
        return null;
    }

    // ================= RF-10: sesión y permisos =================
    public Usuario iniciarSesion(String nombreUsuario, String contrasena) {
        Usuario u = autenticacion.iniciarSesion(nombreUsuario.trim(), contrasena);
        if (u == null) throw new SircapException("Usuario o contraseña incorrectos, o el empleado está inactivo.");
        registrarAuditoria(u.getNombreUsuario(), "Inició sesión");
        return u;
    }

    public void cambiarContrasena(Usuario actor, String actual, String nueva) {
        if (!actor.validarContrasena(actual)) throw new SircapException("La contraseña actual no es correcta.");
        validarContrasena(nueva);
        actor.cambiarContrasena(nueva);
        almacen.guardarUsuarios(autenticacion.getUsuarios());
        registrarAuditoria(actor.getNombreUsuario(), "Cambió su contraseña");
    }

    private void validarContrasena(String clave) {
        if (clave == null || clave.length() < 6) throw new SircapException("La contraseña debe tener al menos 6 caracteres.");
    }

    private void exigir(Usuario actor, Modulo modulo) {
        if (!autenticacion.tienePermiso(actor, modulo)) {
            throw new SircapException("No tienes permiso para realizar esta acción.");
        }
    }

    // ================= RF-01: registro de asistencia =================
    public RegistroAsistencia marcarEntrada(Usuario actor) {
        exigir(actor, Modulo.REGISTRO_ASISTENCIA);
        return realizarCheckIn(actor.getEmpleado().getIdEmpleado(), LocalDateTime.now());
    }

    public RegistroAsistencia marcarSalida(Usuario actor) {
        exigir(actor, Modulo.REGISTRO_ASISTENCIA);
        return realizarCheckOut(actor.getEmpleado().getIdEmpleado(), LocalDateTime.now());
    }

    public RegistroAsistencia realizarCheckIn(String idEmpleado, LocalDateTime tiempoEntrada) {
        Empleado emp = buscarEmpleado(idEmpleado);
        if (emp == null || !emp.isActivo()) throw new SircapException("Empleado no encontrado o inactivo.");
        RegistroAsistencia abierto = buscarRegistroAbierto(idEmpleado);
        if (abierto != null) {
            if (abierto.getMarcaEntrada().toLocalDate().isBefore(tiempoEntrada.toLocalDate())) {
                cerrarRegistroOlvidado(abierto);
            } else {
                throw new SircapException("Ya registraste tu entrada hoy a las "
                        + abierto.getMarcaEntrada().toLocalTime().withNano(0) + ". Falta registrar la salida.");
            }
        }
        RegistroAsistencia nuevo = new RegistroAsistencia(emp, tiempoEntrada);
        registros.add(nuevo);
        almacen.guardarRegistros(registros);
        return nuevo;
    }

    public RegistroAsistencia realizarCheckOut(String idEmpleado, LocalDateTime tiempoSalida) {
        RegistroAsistencia abierto = buscarRegistroAbierto(idEmpleado);
        if (abierto == null) throw new SircapException("No tienes una entrada abierta. Registra primero tu entrada.");
        try {
            abierto.registrarSalida(tiempoSalida);
        } catch (IllegalArgumentException ex) {
            throw new SircapException(ex.getMessage());
        }
        almacen.guardarRegistros(registros);
        return abierto;
    }

    /** Si alguien olvidó marcar la salida de un día anterior, se cierra con su hora de salida esperada. */
    private void cerrarRegistroOlvidado(RegistroAsistencia r) {
        LocalDateTime salida = r.getMarcaEntrada().toLocalDate().atTime(r.getEmpleado().getHorarioAsignado().getHoraSalidaEsperada());
        if (!salida.isAfter(r.getMarcaEntrada())) salida = r.getMarcaEntrada().plusMinutes(1);
        r.registrarSalida(salida);
        registrarAuditoria("sistema", "Cierre automático por salida olvidada de " + r.getEmpleado().getIdEmpleado()
                + " (" + r.getMarcaEntrada().toLocalDate() + ")");
    }

    public RegistroAsistencia buscarRegistroAbierto(String idEmpleado) {
        for (RegistroAsistencia r : registros) {
            if (r.getEmpleado().getIdEmpleado().equals(idEmpleado) && r.estaAbierto()) return r;
        }
        return null;
    }

    // ================= RF-02: panel del empleado =================
    public List<RegistroAsistencia> consultarHistorial(String idEmpleado) {
        List<RegistroAsistencia> historial = new ArrayList<>();
        for (RegistroAsistencia r : registros) {
            if (r.getEmpleado().getIdEmpleado().equals(idEmpleado)) historial.add(r);
        }
        Collections.reverse(historial);
        return historial;
    }

    // ================= RF-03: gestión de horarios =================
    public HorarioLaboral crearHorario(Usuario actor, String nombre, LocalTime entrada, LocalTime salida) {
        exigir(actor, Modulo.GESTION_HORARIOS);
        if (nombre == null || nombre.trim().isEmpty()) throw new SircapException("Escribe un nombre para el horario.");
        if (!salida.isAfter(entrada)) throw new SircapException("La hora de salida debe ser posterior a la de entrada.");
        for (HorarioLaboral h : horarios) {
            if (h.getNombre().equalsIgnoreCase(nombre.trim())) throw new SircapException("Ya existe un horario con ese nombre.");
        }
        HorarioLaboral nuevo = new HorarioLaboral(nombre.trim(), entrada, salida);
        horarios.add(nuevo);
        almacen.guardarHorarios(horarios);
        registrarAuditoria(actor.getNombreUsuario(), "Creó el horario " + nuevo);
        return nuevo;
    }

    public void modificarHorario(Usuario actor, HorarioLaboral horario, LocalTime entrada, LocalTime salida) {
        exigir(actor, Modulo.GESTION_HORARIOS);
        if (!salida.isAfter(entrada)) throw new SircapException("La hora de salida debe ser posterior a la de entrada.");
        horario.modificar(entrada, salida);
        almacen.guardarHorarios(horarios);
        registrarAuditoria(actor.getNombreUsuario(), "Modificó el horario " + horario);
    }

    public void asignarHorario(Usuario actor, Empleado empleado, HorarioLaboral horario) {
        exigir(actor, Modulo.GESTION_HORARIOS);
        empleado.setHorarioAsignado(horario);
        almacen.guardarEmpleados(empleados);
        registrarAuditoria(actor.getNombreUsuario(), "Asignó el horario " + horario + " a " + empleado.getIdEmpleado());
    }

    // ================= RF-04: solicitudes =================
    public Solicitud crearSolicitud(Usuario actor, TipoSolicitud tipo, LocalDate inicio, LocalDate fin, String motivo) {
        exigir(actor, Modulo.SOLICITUDES);
        if (fin.isBefore(inicio)) throw new SircapException("La fecha final no puede ser anterior a la inicial.");
        if (motivo == null || motivo.trim().isEmpty()) throw new SircapException("Escribe el motivo de la solicitud.");
        Solicitud s = new Solicitud("SOL-" + String.format("%04d", solicitudes.size() + 1),
                actor.getEmpleado(), tipo, inicio, fin, motivo.trim());
        solicitudes.add(s);
        almacen.guardarSolicitudes(solicitudes);
        return s;
    }

    /** RRHH y administración ven todas; los demás solo las propias. */
    public List<Solicitud> listarSolicitudes(Usuario actor) {
        List<Solicitud> lista = new ArrayList<>();
        boolean veTodas = autenticacion.tienePermiso(actor, Modulo.AUSENCIAS);
        for (Solicitud s : solicitudes) {
            if (veTodas || s.getSolicitante() == actor.getEmpleado()) lista.add(s);
        }
        Collections.reverse(lista);
        return lista;
    }

    public void resolverSolicitud(Usuario actor, Solicitud solicitud, boolean aprobar) {
        exigir(actor, Modulo.AUSENCIAS);
        if (solicitud.getEstado() != EstadoAprobacion.PENDIENTE) throw new SircapException("Esa solicitud ya fue resuelta.");
        if (aprobar) solicitud.aprobar(); else solicitud.rechazar();
        almacen.guardarSolicitudes(solicitudes);
        registrarAuditoria(actor.getNombreUsuario(), (aprobar ? "Aprobó" : "Rechazó") + " la solicitud " + solicitud.getIdSolicitud());
    }

    // ================= RF-05: ausencias e incapacidades =================
    public Ausencia registrarAusencia(Usuario actor, Empleado empleado, TipoNovedad tipo, LocalDate fecha, String justificacion) {
        exigir(actor, Modulo.AUSENCIAS);
        if (justificacion == null || justificacion.trim().isEmpty()) throw new SircapException("Escribe la justificación.");
        Ausencia a = new Ausencia(empleado, tipo, fecha, justificacion.trim());
        ausencias.add(a);
        almacen.guardarAusencias(ausencias);
        registrarAuditoria(actor.getNombreUsuario(), "Registró ausencia (" + tipo + ") de " + empleado.getIdEmpleado());
        return a;
    }

    public List<Ausencia> listarAusencias(Usuario actor) {
        exigir(actor, Modulo.AUSENCIAS);
        List<Ausencia> lista = new ArrayList<>(ausencias);
        Collections.reverse(lista);
        return lista;
    }

    public void resolverAusencia(Usuario actor, Ausencia ausencia, boolean aprobar) {
        exigir(actor, Modulo.AUSENCIAS);
        if (ausencia.getEstado() != EstadoAprobacion.PENDIENTE) throw new SircapException("Esa ausencia ya fue resuelta.");
        if (aprobar) ausencia.aprobar(); else ausencia.rechazar();
        almacen.guardarAusencias(ausencias);
        registrarAuditoria(actor.getNombreUsuario(), (aprobar ? "Aprobó" : "Rechazó") + " ausencia de "
                + ausencia.getEmpleado().getIdEmpleado() + " (" + ausencia.getFecha() + ")");
    }

    // ================= RF-06: reportes =================
    public ReporteAsistencia generarReporte(Usuario actor, LocalDate inicio, LocalDate fin) {
        exigir(actor, Modulo.REPORTES);
        if (fin.isBefore(inicio)) throw new SircapException("La fecha final no puede ser anterior a la inicial.");
        return new ReporteAsistencia(inicio, fin, registros, ausencias);
    }

    // ================= RF-07: horas extras =================
    public List<RegistroAsistencia> listarRegistrosConHorasExtras(Usuario actor) {
        exigir(actor, Modulo.HORAS_EXTRAS);
        List<RegistroAsistencia> lista = new ArrayList<>();
        for (RegistroAsistencia r : registros) {
            if (r.getHorasExtrasCalculadas() > 0) lista.add(r);
        }
        Collections.reverse(lista);
        return lista;
    }

    public void resolverHorasExtras(Usuario actor, RegistroAsistencia registro, boolean aprobar) {
        exigir(actor, Modulo.HORAS_EXTRAS);
        if (registro.getEstadoHorasExtras() != EstadoAprobacion.PENDIENTE) throw new SircapException("Esas horas extras ya fueron resueltas.");
        registro.resolverHorasExtras(aprobar ? EstadoAprobacion.APROBADA : EstadoAprobacion.RECHAZADA);
        almacen.guardarRegistros(registros);
        registrarAuditoria(actor.getNombreUsuario(), (aprobar ? "Aprobó" : "Rechazó") + " horas extras de "
                + registro.getEmpleado().getIdEmpleado() + " (" + registro.getMarcaEntrada().toLocalDate() + ")");
    }

    // ================= RF-08: registro de nuevos empleados =================
    public Empleado registrarEmpleado(Usuario actor, String nombreCompleto, Rol rol, HorarioLaboral horario,
                                      String nombreUsuario, String contrasena) {
        exigir(actor, Modulo.REGISTRO_EMPLEADOS);
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) throw new SircapException("Escribe el nombre completo.");
        if (horario == null) throw new SircapException("Selecciona un horario. Si no hay ninguno, créalo en la pestaña Horarios.");
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) throw new SircapException("Escribe un nombre de usuario.");
        if (autenticacion.existeUsuario(nombreUsuario.trim())) throw new SircapException("Ese nombre de usuario ya existe.");
        if (rol == Rol.ADMINISTRADOR && actor.getRol() != Rol.ADMINISTRADOR) {
            throw new SircapException("Solo un administrador puede crear otro administrador.");
        }
        validarContrasena(contrasena);

        String id = "EMP-" + String.format("%03d", empleados.size() + 1);
        Empleado nuevo = new Empleado(id, nombreCompleto.trim(), rol, horario);
        empleados.add(nuevo);
        autenticacion.registrarUsuario(new Usuario(nombreUsuario.trim(), contrasena, nuevo));
        almacen.guardarEmpleados(empleados);
        almacen.guardarUsuarios(autenticacion.getUsuarios());
        registrarAuditoria(actor.getNombreUsuario(), "Registró al empleado " + id + " - " + nuevo.getNombreCompleto());
        return nuevo;
    }

    public void cambiarEstadoEmpleado(Usuario actor, Empleado empleado, boolean activo) {
        exigir(actor, Modulo.REGISTRO_EMPLEADOS);
        if (empleado == actor.getEmpleado() && !activo) throw new SircapException("No puedes desactivar tu propio usuario.");
        empleado.setActivo(activo);
        almacen.guardarEmpleados(empleados);
        registrarAuditoria(actor.getNombreUsuario(), (activo ? "Activó" : "Desactivó") + " al empleado " + empleado.getIdEmpleado());
    }

    // ================= RF-09: panel de consulta estratégica =================
    public Metricas obtenerMetricas(Usuario actor) {
        exigir(actor, Modulo.PANEL_ESTRATEGICO);
        Metricas m = new Metricas();
        YearMonth mes = YearMonth.now();
        for (Empleado e : empleados) {
            if (e.isActivo()) {
                m.empleadosActivos++;
                m.horasPorEmpleado.put(e.getNombreCompleto(), 0.0);
            }
        }
        for (RegistroAsistencia r : registros) {
            if (r.estaAbierto() && r.getMarcaEntrada().toLocalDate().equals(LocalDate.now())) m.presentesAhora++;
            if (YearMonth.from(r.getMarcaEntrada()).equals(mes)) {
                m.horasTrabajadasMes += r.calcularHorasTrabajadas();
                m.horasExtrasMes += r.getHorasExtrasCalculadas();
                if (r.llegoTarde()) m.tardanzasMes++;
                String nombre = r.getEmpleado().getNombreCompleto();
                if (m.horasPorEmpleado.containsKey(nombre)) {
                    m.horasPorEmpleado.put(nombre, m.horasPorEmpleado.get(nombre) + r.calcularHorasTrabajadas());
                }
            }
            if (r.getHorasExtrasCalculadas() > 0 && r.getEstadoHorasExtras() == EstadoAprobacion.PENDIENTE) m.horasExtrasPendientes++;
        }
        for (Ausencia a : ausencias) {
            if (YearMonth.from(a.getFecha()).equals(mes)) m.ausenciasMes++;
        }
        for (Solicitud s : solicitudes) {
            if (s.getEstado() == EstadoAprobacion.PENDIENTE) m.solicitudesPendientes++;
        }
        return m;
    }

    // ================= RNF-05: auditoría =================
    public List<LogAuditoria> listarAuditoria(Usuario actor) {
        exigir(actor, Modulo.AUDITORIA);
        List<LogAuditoria> lista = new ArrayList<>(logsAuditoria);
        Collections.reverse(lista);
        return lista;
    }

    private void registrarAuditoria(String nombreUsuario, String accion) {
        logsAuditoria.add(new LogAuditoria(LocalDateTime.now().withNano(0), nombreUsuario, accion));
        almacen.guardarAuditoria(logsAuditoria);
    }
}
