package sircap.ui;

import java.awt.*;
import javax.swing.table.DefaultTableModel;
import sircap.*;

/** RNF-05: registro de auditoría de acciones (inicios de sesión, aprobaciones, cambios de horario...). */
public class PanelAuditoria extends PanelBase {
    private final DefaultTableModel modelo = Estilo.modelo("Fecha y hora", "Usuario", "Acción");

    public PanelAuditoria(SistemaSIRCAP sistema, Usuario usuario) {
        super(sistema, usuario);
        add(Estilo.titulo("Registro de auditoría"), BorderLayout.NORTH);
        add(Estilo.conScroll(Estilo.tabla(modelo)), BorderLayout.CENTER);
    }

    @Override
    public void actualizar() {
        modelo.setRowCount(0);
        java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        for (LogAuditoria l : sistema.listarAuditoria(usuario)) {
            modelo.addRow(new Object[]{l.getFechaHora().format(f), l.getNombreUsuario(), l.getAccion()});
        }
    }
}
