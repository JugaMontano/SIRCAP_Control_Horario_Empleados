package sircap.ui;

import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import sircap.*;

/** RF-04: solicitudes de permisos, vacaciones y cambios de turno. RRHH las aprueba o rechaza. */
public class PanelSolicitudes extends PanelBase {
    private final JComboBox<TipoSolicitud> tipo = new JComboBox<>(TipoSolicitud.values());
    private final JSpinner desde = Estilo.selectorFecha(LocalDate.now());
    private final JSpinner hasta = Estilo.selectorFecha(LocalDate.now());
    private final JTextField motivo = new JTextField(28);
    private final DefaultTableModel modelo = Estilo.modelo("Código", "Empleado", "Tipo", "Desde", "Hasta", "Motivo", "Estado");
    private final JTable tabla = Estilo.tabla(modelo);
    private List<Solicitud> filas = new ArrayList<>();

    public PanelSolicitudes(SistemaSIRCAP sistema, Usuario usuario) {
        super(sistema, usuario);
        add(Estilo.titulo("Solicitudes de permisos, vacaciones y cambios de turno"), BorderLayout.NORTH);

        JPanel formulario = Estilo.caja();
        JPanel linea = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        linea.setOpaque(false);
        linea.add(new JLabel("Tipo:")); linea.add(tipo);
        linea.add(new JLabel("Desde:")); linea.add(desde);
        linea.add(new JLabel("Hasta:")); linea.add(hasta);
        linea.add(new JLabel("Motivo:")); linea.add(motivo);
        JButton enviar = Estilo.boton("Enviar solicitud", Estilo.AZUL_CLARO);
        linea.add(enviar);
        formulario.add(linea, BorderLayout.CENTER);
        enviar.addActionListener(e -> {
            boolean ok = ejecutar(() -> sistema.crearSolicitud(usuario, (TipoSolicitud) tipo.getSelectedItem(),
                    Estilo.fecha(desde), Estilo.fecha(hasta), motivo.getText()));
            if (ok) { motivo.setText(""); actualizar(); Estilo.info(this, "Solicitud enviada a Recursos Humanos."); }
        });

        JPanel centro = new JPanel(new BorderLayout(0, 10));
        centro.setOpaque(false);
        centro.add(formulario, BorderLayout.NORTH);
        Estilo.colorearEstado(tabla, 6);
        centro.add(Estilo.conScroll(tabla), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        if (sistema.getAutenticacion().tienePermiso(usuario, Modulo.AUSENCIAS)) {
            JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            acciones.setOpaque(false);
            JButton aprobar = Estilo.boton("Aprobar seleccionada", Estilo.VERDE);
            JButton rechazar = Estilo.boton("Rechazar seleccionada", Estilo.ROJO);
            acciones.add(aprobar); acciones.add(rechazar);
            add(acciones, BorderLayout.SOUTH);
            aprobar.addActionListener(e -> resolver(true));
            rechazar.addActionListener(e -> resolver(false));
        }
    }

    private void resolver(boolean aprobar) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { Estilo.error(this, "Selecciona una solicitud de la tabla."); return; }
        if (ejecutar(() -> sistema.resolverSolicitud(usuario, filas.get(fila), aprobar))) actualizar();
    }

    @Override
    public void actualizar() {
        filas = sistema.listarSolicitudes(usuario);
        modelo.setRowCount(0);
        for (Solicitud s : filas) {
            modelo.addRow(new Object[]{s.getIdSolicitud(), s.getSolicitante().getNombreCompleto(), s.getTipo(),
                    s.getFechaInicio().format(Estilo.FECHA), s.getFechaFin().format(Estilo.FECHA), s.getMotivo(), s.getEstado()});
        }
    }
}
