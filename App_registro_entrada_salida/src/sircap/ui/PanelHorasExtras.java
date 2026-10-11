package sircap.ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import sircap.*;

/** RF-07: horas extras calculadas automáticamente, pendientes de autorización por RRHH. */
public class PanelHorasExtras extends PanelBase {
    private final DefaultTableModel modelo = Estilo.modelo("Empleado", "Fecha", "Entrada", "Salida", "Horas extras", "Estado");
    private final JTable tabla = Estilo.tabla(modelo);
    private List<RegistroAsistencia> filas = new ArrayList<>();

    public PanelHorasExtras(SistemaSIRCAP sistema, Usuario usuario) {
        super(sistema, usuario);
        JPanel norte = new JPanel(new BorderLayout(0, 4));
        norte.setOpaque(false);
        norte.add(Estilo.titulo("Horas extras"), BorderLayout.NORTH);
        norte.add(Estilo.subtitulo("Se calculan solas al registrar la salida (desde 15 minutos después de la hora de salida del horario)."), BorderLayout.CENTER);
        add(norte, BorderLayout.NORTH);

        Estilo.colorearEstado(tabla, 5);
        add(Estilo.conScroll(tabla), BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        acciones.setOpaque(false);
        JButton aprobar = Estilo.boton("Aprobar", Estilo.VERDE);
        JButton rechazar = Estilo.boton("Rechazar", Estilo.ROJO);
        acciones.add(aprobar); acciones.add(rechazar);
        add(acciones, BorderLayout.SOUTH);
        aprobar.addActionListener(e -> resolver(true));
        rechazar.addActionListener(e -> resolver(false));
    }

    private void resolver(boolean aprobar) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { Estilo.error(this, "Selecciona un registro de la tabla."); return; }
        if (ejecutar(() -> sistema.resolverHorasExtras(usuario, filas.get(fila), aprobar))) actualizar();
    }

    @Override
    public void actualizar() {
        filas = sistema.listarRegistrosConHorasExtras(usuario);
        modelo.setRowCount(0);
        for (RegistroAsistencia r : filas) {
            modelo.addRow(new Object[]{r.getEmpleado().getNombreCompleto(), r.getMarcaEntrada().format(Estilo.FECHA),
                    r.getMarcaEntrada().format(Estilo.HORA), r.getMarcaSalida().format(Estilo.HORA),
                    String.format("%.2f h", r.getHorasExtrasCalculadas()), r.getEstadoHorasExtras()});
        }
    }
}
