package sircap.ui;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import sircap.*;

/** RF-06: reporte consolidado de asistencia por rango de fechas, exportable a CSV (abre en Excel). */
public class PanelReportes extends PanelBase {
    private final JSpinner desde = Estilo.selectorFecha(LocalDate.now().withDayOfMonth(1));
    private final JSpinner hasta = Estilo.selectorFecha(LocalDate.now());
    private final DefaultTableModel modelo = Estilo.modelo("Empleado", "Fecha", "Entrada", "Salida", "Horas trabajadas", "Horas extras", "Observación");
    private final JLabel resumen = new JLabel(" ");
    private ReporteAsistencia reporte;

    public PanelReportes(SistemaSIRCAP sistema, Usuario usuario) {
        super(sistema, usuario);
        add(Estilo.titulo("Reportes de asistencia"), BorderLayout.NORTH);

        JPanel filtros = Estilo.caja();
        JPanel linea = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        linea.setOpaque(false);
        linea.add(new JLabel("Desde:")); linea.add(desde);
        linea.add(new JLabel("Hasta:")); linea.add(hasta);
        JButton generar = Estilo.boton("Generar reporte", Estilo.AZUL_CLARO);
        JButton exportar = Estilo.boton("Exportar a CSV", Estilo.VERDE);
        linea.add(generar); linea.add(exportar);
        filtros.add(linea, BorderLayout.CENTER);

        JPanel centro = new JPanel(new BorderLayout(0, 10));
        centro.setOpaque(false);
        centro.add(filtros, BorderLayout.NORTH);
        centro.add(Estilo.conScroll(Estilo.tabla(modelo)), BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);
        resumen.setFont(Estilo.NEGRITA);
        add(resumen, BorderLayout.SOUTH);

        generar.addActionListener(e -> generar());
        exportar.addActionListener(e -> exportar());
    }

    private void generar() {
        ejecutar(() -> {
            reporte = sistema.generarReporte(usuario, Estilo.fecha(desde), Estilo.fecha(hasta));
            modelo.setRowCount(0);
            for (RegistroAsistencia r : reporte.getRegistros()) {
                String obs = r.estaAbierto() ? "Salida pendiente" : (r.llegoTarde() ? "Llegó " + r.getMinutosTarde() + " min tarde" : "");
                modelo.addRow(new Object[]{r.getEmpleado().getNombreCompleto(), r.getMarcaEntrada().format(Estilo.FECHA),
                        r.getMarcaEntrada().format(Estilo.HORA), r.getMarcaSalida() == null ? "-" : r.getMarcaSalida().format(Estilo.HORA),
                        String.format("%.2f", r.calcularHorasTrabajadas()), String.format("%.2f", r.getHorasExtrasCalculadas()), obs});
            }
            resumen.setText("Horas trabajadas: " + String.format("%.2f", reporte.totalHorasTrabajadas())
                    + "   |   Horas extras: " + String.format("%.2f", reporte.totalHorasExtras())
                    + "   |   Ausencias reportadas: " + reporte.totalAusencias());
        });
    }

    private void exportar() {
        if (reporte == null) generar();
        if (reporte == null) return;
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("reporte_asistencia_" + reporte.getFechaInicio() + "_a_" + reporte.getFechaFin() + ".csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            // El BOM al inicio hace que Excel muestre bien las tildes
            String contenido = "﻿" + reporte.exportarCSV();
            Files.write(fc.getSelectedFile().toPath(), contenido.getBytes(StandardCharsets.UTF_8));
            Estilo.info(this, "Reporte guardado en:\n" + fc.getSelectedFile().getAbsolutePath());
        } catch (IOException ex) {
            Estilo.error(this, "No se pudo guardar el archivo: " + ex.getMessage());
        }
    }

    @Override
    public void actualizar() {
        generar();
    }
}
