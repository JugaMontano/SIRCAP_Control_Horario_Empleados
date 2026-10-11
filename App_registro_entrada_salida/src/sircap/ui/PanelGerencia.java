package sircap.ui;

import java.awt.*;
import java.util.Map;
import javax.swing.*;
import sircap.*;

/** RF-09: vista global de asistencia y ausentismo del mes en curso. */
public class PanelGerencia extends PanelBase {
    private final JLabel activos = new JLabel("0");
    private final JLabel presentes = new JLabel("0");
    private final JLabel tardanzas = new JLabel("0");
    private final JLabel ausencias = new JLabel("0");
    private final JLabel extras = new JLabel("0");
    private final JLabel pendientes = new JLabel("0");
    private final Grafico grafico = new Grafico();

    public PanelGerencia(SistemaSIRCAP sistema, Usuario usuario) {
        super(sistema, usuario);
        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        norte.add(Estilo.titulo("Panel de consulta estratégica (mes en curso)"), BorderLayout.WEST);
        JButton refrescar = Estilo.boton("Actualizar", Estilo.AZUL_CLARO);
        refrescar.addActionListener(e -> actualizar());
        norte.add(refrescar, BorderLayout.EAST);
        add(norte, BorderLayout.NORTH);

        JPanel tarjetas = new JPanel(new GridLayout(2, 3, 12, 12));
        tarjetas.setOpaque(false);
        tarjetas.add(Estilo.tarjeta("Empleados activos", activos, Estilo.AZUL));
        tarjetas.add(Estilo.tarjeta("Presentes ahora", presentes, Estilo.VERDE));
        tarjetas.add(Estilo.tarjeta("Llegadas tarde del mes", tardanzas, Estilo.AMBAR));
        tarjetas.add(Estilo.tarjeta("Ausencias reportadas del mes", ausencias, Estilo.ROJO));
        tarjetas.add(Estilo.tarjeta("Horas extras del mes", extras, Estilo.AZUL_CLARO));
        tarjetas.add(Estilo.tarjeta("Solicitudes pendientes", pendientes, Estilo.AMBAR));

        JPanel cuerpo = new JPanel(new BorderLayout(0, 12));
        cuerpo.setOpaque(false);
        cuerpo.add(tarjetas, BorderLayout.NORTH);
        JPanel caja = Estilo.caja();
        JLabel t = new JLabel("Horas trabajadas en el mes por empleado");
        t.setFont(Estilo.NEGRITA);
        caja.add(t, BorderLayout.NORTH);
        caja.add(new JScrollPane(grafico, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        ((JScrollPane) caja.getComponent(1)).setBorder(null);
        cuerpo.add(caja, BorderLayout.CENTER);
        add(cuerpo, BorderLayout.CENTER);
    }

    @Override
    public void actualizar() {
        Metricas m = sistema.obtenerMetricas(usuario);
        activos.setText(String.valueOf(m.empleadosActivos));
        presentes.setText(String.valueOf(m.presentesAhora));
        tardanzas.setText(String.valueOf(m.tardanzasMes));
        ausencias.setText(String.valueOf(m.ausenciasMes));
        extras.setText(String.format("%.1f h", m.horasExtrasMes));
        pendientes.setText(String.valueOf(m.solicitudesPendientes));
        grafico.datos(m.horasPorEmpleado);
    }

    /** Barras horizontales dibujadas a mano (sin librerías externas). */
    private static class Grafico extends JPanel {
        private Map<String, Double> datos = new java.util.LinkedHashMap<>();

        Grafico() { setBackground(Color.WHITE); }

        void datos(Map<String, Double> nuevos) {
            this.datos = nuevos;
            setPreferredSize(new Dimension(300, Math.max(60, nuevos.size() * 30 + 16)));
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double max = 1;
            for (double v : datos.values()) max = Math.max(max, v);
            int etiqueta = 200, margenDerecho = 80, y = 8;
            int ancho = Math.max(50, getWidth() - etiqueta - margenDerecho);
            g2.setFont(Estilo.FUENTE);
            FontMetrics fm = g2.getFontMetrics();
            if (datos.isEmpty()) {
                g2.setColor(Estilo.GRIS);
                g2.drawString("Aún no hay empleados registrados.", 8, 28);
            }
            for (Map.Entry<String, Double> e : datos.entrySet()) {
                String nombre = e.getKey();
                while (fm.stringWidth(nombre) > etiqueta - 12 && nombre.length() > 4) nombre = nombre.substring(0, nombre.length() - 2) + "…";
                g2.setColor(Color.DARK_GRAY);
                g2.drawString(nombre, 8, y + 17);
                int largo = (int) Math.round(ancho * e.getValue() / max);
                g2.setColor(Estilo.AZUL_CLARO);
                g2.fillRoundRect(etiqueta, y + 3, Math.max(largo, 2), 18, 6, 6);
                g2.setColor(Estilo.GRIS);
                g2.drawString(String.format("%.1f h", e.getValue()), etiqueta + Math.max(largo, 2) + 8, y + 17);
                y += 30;
            }
        }
    }
}
