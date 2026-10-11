package sircap.ui;

import java.awt.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import sircap.*;

/** RF-03: crear, modificar y asignar horarios laborales. */
public class PanelHorarios extends PanelBase {
    private final JTextField nombre = new JTextField(16);
    private final JSpinner entrada = Estilo.selectorHora(LocalTime.of(8, 0));
    private final JSpinner salida = Estilo.selectorHora(LocalTime.of(17, 0));
    private final JComboBox<Empleado> empleado = new JComboBox<>();
    private final JComboBox<HorarioLaboral> horarioAsignar = new JComboBox<>();
    private final DefaultTableModel modelo = Estilo.modelo("Horario", "Entrada", "Salida", "Horas por jornada", "Empleados asignados");
    private final JTable tabla = Estilo.tabla(modelo);
    private List<HorarioLaboral> filas = new ArrayList<>();

    public PanelHorarios(SistemaSIRCAP sistema, Usuario usuario) {
        super(sistema, usuario);
        add(Estilo.titulo("Horarios laborales"), BorderLayout.NORTH);

        // Crear / modificar
        JPanel crear = Estilo.caja();
        JPanel linea1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        linea1.setOpaque(false);
        linea1.add(new JLabel("Nombre:")); linea1.add(nombre);
        linea1.add(new JLabel("Entrada:")); linea1.add(entrada);
        linea1.add(new JLabel("Salida:")); linea1.add(salida);
        JButton bCrear = Estilo.boton("Crear horario", Estilo.AZUL_CLARO);
        JButton bModificar = Estilo.boton("Cambiar horas del seleccionado", Estilo.AZUL);
        linea1.add(bCrear); linea1.add(bModificar);
        crear.add(linea1, BorderLayout.CENTER);

        // Asignar
        JPanel asignar = Estilo.caja();
        JPanel linea2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        linea2.setOpaque(false);
        linea2.add(new JLabel("Asignar a:")); linea2.add(empleado);
        linea2.add(new JLabel("el horario:")); linea2.add(horarioAsignar);
        JButton bAsignar = Estilo.boton("Asignar", Estilo.VERDE);
        linea2.add(bAsignar);
        asignar.add(linea2, BorderLayout.CENTER);

        JPanel formularios = new JPanel(new GridLayout(0, 1, 0, 10));
        formularios.setOpaque(false);
        formularios.add(crear);
        formularios.add(asignar);

        JPanel centro = new JPanel(new BorderLayout(0, 10));
        centro.setOpaque(false);
        centro.add(formularios, BorderLayout.NORTH);
        centro.add(Estilo.conScroll(tabla), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (!e.getValueIsAdjusting() && fila >= 0 && fila < filas.size()) {
                HorarioLaboral h = filas.get(fila);
                nombre.setText(h.getNombre());
                entrada.setValue(((SpinnerDateModel) Estilo.selectorHora(h.getHoraEntradaEsperada()).getModel()).getDate());
                salida.setValue(((SpinnerDateModel) Estilo.selectorHora(h.getHoraSalidaEsperada()).getModel()).getDate());
            }
        });
        bCrear.addActionListener(e -> {
            if (ejecutar(() -> sistema.crearHorario(usuario, nombre.getText(), Estilo.hora(entrada), Estilo.hora(salida)))) {
                nombre.setText("");
                actualizar();
            }
        });
        bModificar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila < 0) { Estilo.error(this, "Selecciona un horario de la tabla."); return; }
            if (ejecutar(() -> sistema.modificarHorario(usuario, filas.get(fila), Estilo.hora(entrada), Estilo.hora(salida)))) actualizar();
        });
        bAsignar.addActionListener(e -> {
            if (empleado.getSelectedItem() == null || horarioAsignar.getSelectedItem() == null) {
                Estilo.error(this, "Selecciona un empleado y un horario.");
                return;
            }
            if (ejecutar(() -> sistema.asignarHorario(usuario, (Empleado) empleado.getSelectedItem(),
                    (HorarioLaboral) horarioAsignar.getSelectedItem()))) {
                actualizar();
                Estilo.info(this, "Horario asignado.");
            }
        });
    }

    @Override
    public void actualizar() {
        Object e0 = empleado.getSelectedItem();
        Object h0 = horarioAsignar.getSelectedItem();
        empleado.removeAllItems();
        horarioAsignar.removeAllItems();
        for (Empleado e : sistema.getEmpleados()) {
            if (e.isActivo()) empleado.addItem(e);
        }
        for (HorarioLaboral h : sistema.getHorarios()) horarioAsignar.addItem(h);
        if (e0 != null) empleado.setSelectedItem(e0);
        if (h0 != null) horarioAsignar.setSelectedItem(h0);

        filas = new ArrayList<>(sistema.getHorarios());
        modelo.setRowCount(0);
        for (HorarioLaboral h : filas) {
            int cuantos = 0;
            for (Empleado e : sistema.getEmpleados()) {
                if (e.getHorarioAsignado() == h) cuantos++;
            }
            modelo.addRow(new Object[]{h.getNombre(), h.getHoraEntradaEsperada().format(Estilo.HORA),
                    h.getHoraSalidaEsperada().format(Estilo.HORA), String.format("%.1f h", h.horasPorJornada()), cuantos});
        }
    }
}
