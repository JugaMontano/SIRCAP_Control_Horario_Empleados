package sircap.ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import sircap.*;

/** RF-08: registro de nuevos empleados con su usuario de acceso y horario. */
public class PanelEmpleados extends PanelBase {
    private final JTextField nombre = new JTextField(20);
    private final JComboBox<Rol> rol = new JComboBox<>();
    private final JComboBox<HorarioLaboral> horario = new JComboBox<>();
    private final JTextField nombreUsuario = new JTextField(20);
    private final JPasswordField contrasena = new JPasswordField(20);
    private final DefaultTableModel modelo = Estilo.modelo("Código", "Nombre", "Rol", "Horario", "Usuario", "Estado");
    private final JTable tabla = Estilo.tabla(modelo);
    private List<Empleado> filas = new ArrayList<>();

    public PanelEmpleados(SistemaSIRCAP sistema, Usuario usuario) {
        super(sistema, usuario);
        add(Estilo.titulo("Empleados"), BorderLayout.NORTH);

        for (Rol r : Rol.values()) {
            if (r != Rol.ADMINISTRADOR || usuario.getRol() == Rol.ADMINISTRADOR) rol.addItem(r);
        }
        rol.setSelectedItem(Rol.EMPLEADO);

        Estilo.Formulario f = new Estilo.Formulario();
        f.agregar("Nombre completo", nombre);
        f.agregar("Rol", rol);
        f.agregar("Horario", horario);
        f.agregar("Usuario de acceso", nombreUsuario);
        f.agregar("Contraseña inicial", contrasena);
        JButton registrar = Estilo.boton("Registrar empleado", Estilo.AZUL_CLARO);
        f.agregarAncho(registrar);

        JPanel izquierda = Estilo.caja();
        JLabel t = new JLabel("Nuevo empleado");
        t.setFont(Estilo.NEGRITA);
        JPanel contenido = new JPanel(new BorderLayout(0, 8));
        contenido.setOpaque(false);
        contenido.add(t, BorderLayout.NORTH);
        contenido.add(f, BorderLayout.CENTER);
        izquierda.add(contenido, BorderLayout.NORTH);
        izquierda.setPreferredSize(new Dimension(360, 10));

        Estilo.colorearEstado(tabla, 5);
        JPanel derecha = new JPanel(new BorderLayout(0, 10));
        derecha.setOpaque(false);
        derecha.add(Estilo.conScroll(tabla), BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        acciones.setOpaque(false);
        JButton activar = Estilo.boton("Activar", Estilo.VERDE);
        JButton desactivar = Estilo.boton("Desactivar", Estilo.ROJO);
        acciones.add(activar); acciones.add(desactivar);
        derecha.add(acciones, BorderLayout.SOUTH);

        JPanel centro = new JPanel(new BorderLayout(14, 0));
        centro.setOpaque(false);
        centro.add(izquierda, BorderLayout.WEST);
        centro.add(derecha, BorderLayout.CENTER);
        add(centro, BorderLayout.CENTER);

        registrar.addActionListener(e -> {
            boolean ok = ejecutar(() -> sistema.registrarEmpleado(usuario, nombre.getText(), (Rol) rol.getSelectedItem(),
                    (HorarioLaboral) horario.getSelectedItem(), nombreUsuario.getText(), new String(contrasena.getPassword())));
            if (ok) {
                nombre.setText(""); nombreUsuario.setText(""); contrasena.setText("");
                actualizar();
                Estilo.info(this, "Empleado registrado. Ya puede iniciar sesión con su usuario.");
            }
        });
        activar.addActionListener(e -> cambiar(true));
        desactivar.addActionListener(e -> cambiar(false));
    }

    private void cambiar(boolean activo) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { Estilo.error(this, "Selecciona un empleado de la tabla."); return; }
        if (ejecutar(() -> sistema.cambiarEstadoEmpleado(usuario, filas.get(fila), activo))) actualizar();
    }

    @Override
    public void actualizar() {
        Object seleccionado = horario.getSelectedItem();
        horario.removeAllItems();
        for (HorarioLaboral h : sistema.getHorarios()) horario.addItem(h);
        if (seleccionado != null) horario.setSelectedItem(seleccionado);

        filas = new ArrayList<>(sistema.getEmpleados());
        modelo.setRowCount(0);
        for (Empleado e : filas) {
            String user = "";
            for (Usuario u : sistema.getAutenticacion().getUsuarios()) {
                if (u.getEmpleado() == e) user = u.getNombreUsuario();
            }
            modelo.addRow(new Object[]{e.getIdEmpleado(), e.getNombreCompleto(), e.getRol(), e.getHorarioAsignado().getNombre(),
                    user, e.isActivo() ? "Activo" : "Inactivo"});
        }
    }
}
