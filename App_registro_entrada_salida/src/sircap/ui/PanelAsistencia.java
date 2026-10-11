package sircap.ui;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import javax.swing.*;
import sircap.*;

/** RF-01: marcar entrada y salida con un solo clic. */
public class PanelAsistencia extends PanelBase {
    private final JLabel reloj = new JLabel("", SwingConstants.CENTER);
    private final JLabel fecha = new JLabel("", SwingConstants.CENTER);
    private final JLabel estado = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel horario = new JLabel(" ", SwingConstants.CENTER);
    private final JButton bEntrada = Estilo.boton("Marcar ENTRADA", Estilo.VERDE);
    private final JButton bSalida = Estilo.boton("Marcar SALIDA", Estilo.ROJO);
    private final Timer timer;

    public PanelAsistencia(SistemaSIRCAP sistema, Usuario usuario) {
        super(sistema, usuario);
        add(Estilo.titulo("Registro de asistencia"), BorderLayout.NORTH);

        reloj.setFont(new Font("SansSerif", Font.BOLD, 64));
        reloj.setForeground(Estilo.AZUL);
        fecha.setFont(new Font("SansSerif", Font.PLAIN, 18));
        fecha.setForeground(Estilo.GRIS);
        estado.setFont(new Font("SansSerif", Font.BOLD, 17));
        horario.setForeground(Estilo.GRIS);
        bEntrada.setFont(new Font("SansSerif", Font.BOLD, 20));
        bSalida.setFont(new Font("SansSerif", Font.BOLD, 20));
        bEntrada.setPreferredSize(new Dimension(260, 64));
        bSalida.setPreferredSize(new Dimension(260, 64));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        botones.setOpaque(false);
        botones.add(bEntrada);
        botones.add(bSalida);

        JPanel centro = Estilo.caja();
        centro.setLayout(new GridLayout(0, 1, 0, 14));
        centro.setBorder(BorderFactory.createCompoundBorder(centro.getBorder(), BorderFactory.createEmptyBorder(24, 20, 24, 20)));
        centro.add(fecha);
        centro.add(reloj);
        centro.add(estado);
        centro.add(botones);
        centro.add(horario);

        JPanel envoltura = new JPanel(new GridBagLayout());
        envoltura.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; c.insets = new Insets(0, 80, 0, 80);
        envoltura.add(centro, c);
        add(envoltura, BorderLayout.CENTER);

        bEntrada.addActionListener(e -> { if (ejecutar(() -> sistema.marcarEntrada(usuario))) actualizar(); });
        bSalida.addActionListener(e -> { if (ejecutar(() -> sistema.marcarSalida(usuario))) actualizar(); });

        timer = new Timer(1000, e -> pintarReloj());
        timer.start();
        pintarReloj();
    }

    private void pintarReloj() {
        reloj.setText(LocalTime.now().format(Estilo.HORA_SEG));
        LocalDate hoy = LocalDate.now();
        String dia = hoy.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.forLanguageTag("es-CO"));
        fecha.setText(dia.substring(0, 1).toUpperCase() + dia.substring(1) + ", " + hoy.format(Estilo.FECHA));
    }

    @Override
    public void removeNotify() {
        timer.stop();
        super.removeNotify();
    }

    @Override
    public void actualizar() {
        Empleado emp = usuario.getEmpleado();
        horario.setText("Tu horario: " + emp.getHorarioAsignado().getHoraEntradaEsperada().format(Estilo.HORA)
                + " a " + emp.getHorarioAsignado().getHoraSalidaEsperada().format(Estilo.HORA));

        RegistroAsistencia abierto = sistema.buscarRegistroAbierto(emp.getIdEmpleado());
        List<RegistroAsistencia> historial = sistema.consultarHistorial(emp.getIdEmpleado());
        RegistroAsistencia ultimoHoy = null;
        for (RegistroAsistencia r : historial) {
            if (r.getMarcaEntrada().toLocalDate().equals(LocalDate.now())) { ultimoHoy = r; break; }
        }

        boolean turnoHoyAbierto = abierto != null && abierto.getMarcaEntrada().toLocalDate().equals(LocalDate.now());
        bEntrada.setEnabled(!turnoHoyAbierto);
        bSalida.setEnabled(turnoHoyAbierto);

        if (turnoHoyAbierto) {
            String extra = abierto.llegoTarde() ? "  (llegada tarde: " + abierto.getMinutosTarde() + " min)" : "";
            estado.setForeground(abierto.llegoTarde() ? Estilo.AMBAR : Estilo.VERDE);
            estado.setText("Entrada registrada a las " + abierto.getMarcaEntrada().format(Estilo.HORA) + extra + " - turno en curso");
        } else if (abierto != null) {
            estado.setForeground(Estilo.AMBAR);
            estado.setText("Quedó una salida sin registrar del " + abierto.getMarcaEntrada().format(Estilo.FECHA)
                    + ". Se cerrará sola al marcar tu entrada.");
        } else if (ultimoHoy != null) {
            estado.setForeground(Estilo.AZUL);
            String texto = "Jornada registrada: " + ultimoHoy.getMarcaEntrada().format(Estilo.HORA) + " - "
                    + ultimoHoy.getMarcaSalida().format(Estilo.HORA)
                    + " (" + String.format("%.2f", ultimoHoy.calcularHorasTrabajadas()) + " h)";
            if (ultimoHoy.getHorasExtrasCalculadas() > 0) {
                texto += ". Horas extras: " + String.format("%.2f", ultimoHoy.getHorasExtrasCalculadas())
                        + " h (" + ultimoHoy.getEstadoHorasExtras().toString().toLowerCase() + ")";
            }
            estado.setText(texto);
        } else {
            estado.setForeground(Estilo.GRIS);
            estado.setText("Aún no has registrado tu entrada hoy");
        }
    }
}
