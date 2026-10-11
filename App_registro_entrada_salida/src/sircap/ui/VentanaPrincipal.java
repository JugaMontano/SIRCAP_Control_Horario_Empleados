package sircap.ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import sircap.*;

/** Ventana principal: muestra solo las pestañas a las que el rol del usuario tiene acceso (RF-10). */
public class VentanaPrincipal extends JFrame {
    private final SistemaSIRCAP sistema;
    private final Usuario usuario;
    private final JTabbedPane pestanas = new JTabbedPane();
    private final List<PanelBase> paneles = new ArrayList<>();

    public VentanaPrincipal(SistemaSIRCAP sistema, Usuario usuario) {
        super("SIRCAP - Control de asistencia del personal");
        this.sistema = sistema;
        this.usuario = usuario;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.add(crearEncabezado(), BorderLayout.NORTH);
        raiz.add(pestanas, BorderLayout.CENTER);
        setContentPane(raiz);

        agregar("Asistencia", Modulo.REGISTRO_ASISTENCIA, new PanelAsistencia(sistema, usuario));
        agregar("Mi historial", Modulo.PANEL_EMPLEADO, new PanelMiHistorial(sistema, usuario));
        agregar("Solicitudes", Modulo.SOLICITUDES, new PanelSolicitudes(sistema, usuario));
        agregar("Ausencias", Modulo.AUSENCIAS, new PanelAusencias(sistema, usuario));
        agregar("Horas extras", Modulo.HORAS_EXTRAS, new PanelHorasExtras(sistema, usuario));
        agregar("Empleados", Modulo.REGISTRO_EMPLEADOS, new PanelEmpleados(sistema, usuario));
        agregar("Horarios", Modulo.GESTION_HORARIOS, new PanelHorarios(sistema, usuario));
        agregar("Reportes", Modulo.REPORTES, new PanelReportes(sistema, usuario));
        agregar("Panel gerencial", Modulo.PANEL_ESTRATEGICO, new PanelGerencia(sistema, usuario));
        agregar("Auditoría", Modulo.AUDITORIA, new PanelAuditoria(sistema, usuario));

        for (PanelBase p : paneles) p.actualizar();
        pestanas.addChangeListener(e -> {
            Component c = pestanas.getSelectedComponent();
            if (c instanceof PanelBase) ((PanelBase) c).actualizar();
        });

        setSize(1180, 740);
        setMinimumSize(new Dimension(980, 620));
        setLocationRelativeTo(null);
    }

    private void agregar(String titulo, Modulo modulo, PanelBase panel) {
        if (sistema.getAutenticacion().tienePermiso(usuario, modulo)) {
            pestanas.addTab(titulo, panel);
            paneles.add(panel);
        }
    }

    private JPanel crearEncabezado() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Estilo.AZUL);
        p.setBorder(new EmptyBorder(10, 18, 10, 18));

        JLabel marca = new JLabel("SIRCAP");
        marca.setForeground(Color.WHITE);
        marca.setFont(new Font("SansSerif", Font.BOLD, 22));
        p.add(marca, BorderLayout.WEST);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        derecha.setOpaque(false);
        JLabel quien = new JLabel(usuario.getEmpleado().getNombreCompleto() + "  ·  " + usuario.getRol());
        quien.setForeground(Color.WHITE);
        JButton clave = Estilo.boton("Cambiar contraseña", Estilo.AZUL_CLARO);
        JButton salir = Estilo.boton("Cerrar sesión", Estilo.ROJO);
        derecha.add(quien);
        derecha.add(clave);
        derecha.add(salir);
        p.add(derecha, BorderLayout.EAST);

        clave.addActionListener(e -> cambiarContrasena());
        salir.addActionListener(e -> {
            new LoginFrame(sistema).setVisible(true);
            dispose();
        });
        return p;
    }

    private void cambiarContrasena() {
        JPasswordField actual = new JPasswordField(16);
        JPasswordField nueva = new JPasswordField(16);
        JPasswordField repetir = new JPasswordField(16);
        Estilo.Formulario f = new Estilo.Formulario();
        f.agregar("Contraseña actual", actual);
        f.agregar("Contraseña nueva", nueva);
        f.agregar("Repetir la nueva", repetir);
        int r = JOptionPane.showConfirmDialog(this, f, "Cambiar contraseña", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;
        if (!new String(nueva.getPassword()).equals(new String(repetir.getPassword()))) {
            Estilo.error(this, "Las contraseñas nuevas no coinciden.");
            return;
        }
        try {
            sistema.cambiarContrasena(usuario, new String(actual.getPassword()), new String(nueva.getPassword()));
            Estilo.info(this, "Contraseña actualizada.");
        } catch (SircapException ex) {
            Estilo.error(this, ex.getMessage());
        }
    }
}
