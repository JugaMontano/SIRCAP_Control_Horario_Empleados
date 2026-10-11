package sircap.ui;

import java.awt.*;
import java.time.YearMonth;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import sircap.*;

/** RF-02: horario asignado e historial de asistencia propio. */
public class PanelMiHistorial extends PanelBase {
    private final JLabel horario = new JLabel();
    private final JLabel totales = new JLabel(" ");
    private final JComboBox<Object> meses = new JComboBox<>();
    private final DefaultTableModel modelo = Estilo.modelo("Fecha", "Entrada", "Salida", "Horas trabajadas", "Horas extras", "Estado horas extras", "Observación");
    private boolean cargando = false;

    public PanelMiHistorial(SistemaSIRCAP sistema, Usuario usuario) {
        super(sistema, usuario);
        JPanel norte = new JPanel(new BorderLayout(0, 4));
        norte.setOpaque(false);
        norte.add(Estilo.titulo("Mi historial de asistencia"), BorderLayout.NORTH);
        horario.setForeground(Estilo.GRIS);
        norte.add(horario, BorderLayout.CENTER);
        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtro.setOpaque(false);
        filtro.add(new JLabel("Mes:"));
        filtro.add(meses);
        norte.add(filtro, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        JTable tabla = Estilo.tabla(modelo);
        Estilo.colorearEstado(tabla, 5);
        add(Estilo.conScroll(tabla), BorderLayout.CENTER);
        totales.setFont(Estilo.NEGRITA);
        add(totales, BorderLayout.SOUTH);

        meses.addItem("Todos los meses");
        YearMonth m = YearMonth.now();
        for (int i = 0; i < 12; i++) meses.addItem(m.minusMonths(i));
        meses.addActionListener(e -> { if (!cargando) actualizar(); });
    }

    @Override
    public void actualizar() {
        cargando = true;
        Empleado emp = usuario.getEmpleado();
        horario.setText("Horario asignado: " + emp.getHorarioAsignado());
        modelo.setRowCount(0);
        double horas = 0, extras = 0;
        Object filtro = meses.getSelectedItem();
        for (RegistroAsistencia r : sistema.consultarHistorial(emp.getIdEmpleado())) {
            if (filtro instanceof YearMonth && !YearMonth.from(r.getMarcaEntrada()).equals(filtro)) continue;
            String obs = r.estaAbierto() ? "Salida pendiente" : (r.llegoTarde() ? "Llegó " + r.getMinutosTarde() + " min tarde" : "");
            modelo.addRow(new Object[]{
                    r.getMarcaEntrada().format(Estilo.FECHA), r.getMarcaEntrada().format(Estilo.HORA),
                    r.getMarcaSalida() == null ? "-" : r.getMarcaSalida().format(Estilo.HORA),
                    String.format("%.2f", r.calcularHorasTrabajadas()), String.format("%.2f", r.getHorasExtrasCalculadas()),
                    r.getHorasExtrasCalculadas() > 0 ? r.getEstadoHorasExtras().toString() : "-", obs});
            horas += r.calcularHorasTrabajadas();
            extras += r.getHorasExtrasCalculadas();
        }
        totales.setText("Total: " + String.format("%.2f", horas) + " horas trabajadas   |   " + String.format("%.2f", extras) + " horas extras");
        cargando = false;
    }
}
