package sircap.ui;

import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import sircap.*;

/** RF-05: registro, categorización y aprobación de ausencias e incapacidades. */
public class PanelAusencias extends PanelBase {
    private final JComboBox<Empleado> empleado = new JComboBox<>();
    private final JComboBox<TipoNovedad> tipo = new JComboBox<>(TipoNovedad.values());
    private final JSpinner fecha = Estilo.selectorFecha(LocalDate.now());
    private final JTextField justificacion = new JTextField(26);
    private final DefaultTableModel modelo = Estilo.modelo("Empleado", "Tipo", "Fecha", "Justificación", "Estado");
    private final JTable tabla = Estilo.tabla(modelo);
    private List<Ausencia> filas = new ArrayList<>();

    public PanelAusencias(SistemaSIRCAP sistema, Usuario usuario) {
        super(sistema, usuario);
        add(Estilo.titulo("Ausencias e incapacidades"), BorderLayout.NORTH);

        JPanel formulario = Estilo.caja();
        JPanel linea = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        linea.setOpaque(false);
        linea.add(new JLabel("Empleado:")); linea.add(empleado);
        linea.add(new JLabel("Tipo:")); linea.add(tipo);
        linea.add(new JLabel("Fecha:")); linea.add(fecha);
        linea.add(new JLabel("Justificación:")); linea.add(justificacion);
        JButton registrar = Estilo.boton("Registrar", Estilo.AZUL_CLARO);
        linea.add(registrar);
        formulario.add(linea, BorderLayout.CENTER);
        registrar.addActionListener(e -> {
            if (empleado.getSelectedItem() == null) { Estilo.error(this, "No hay empleados para seleccionar."); return; }
            boolean ok = ejecutar(() -> sistema.registrarAusencia(usuario, (Empleado) empleado.getSelectedItem(),
                    (TipoNovedad) tipo.getSelectedItem(), Estilo.fecha(fecha), justificacion.getText()));
            if (ok) { justificacion.setText(""); actualizar(); }
        });

        JPanel centro = new JPanel(new BorderLayout(0, 10));
        centro.setOpaque(false);
        centro.add(formulario, BorderLayout.NORTH);
        Estilo.colorearEstado(tabla, 4);
        centro.add(Estilo.conScroll(tabla), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        acciones.setOpaque(false);
        JButton aprobar = Estilo.boton("Aprobar seleccionada", Estilo.VERDE);
        JButton rechazar = Estilo.boton("Rechazar seleccionada", Estilo.ROJO);
        acciones.add(aprobar); acciones.add(rechazar);
        add(acciones, BorderLayout.SOUTH);
        aprobar.addActionListener(e -> resolver(true));
        rechazar.addActionListener(e -> resolver(false));
    }

    private void resolver(boolean aprobar) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { Estilo.error(this, "Selecciona una ausencia de la tabla."); return; }
        if (ejecutar(() -> sistema.resolverAusencia(usuario, filas.get(fila), aprobar))) actualizar();
    }

    @Override
    public void actualizar() {
        Object seleccionado = empleado.getSelectedItem();
        empleado.removeAllItems();
        for (Empleado e : sistema.getEmpleados()) {
            if (e.isActivo()) empleado.addItem(e);
        }
        if (seleccionado != null) empleado.setSelectedItem(seleccionado);

        filas = sistema.listarAusencias(usuario);
        modelo.setRowCount(0);
        for (Ausencia a : filas) {
            modelo.addRow(new Object[]{a.getEmpleado().getNombreCompleto(), a.getTipo(), a.getFecha().format(Estilo.FECHA),
                    a.getJustificacion(), a.getEstado()});
        }
    }
}
