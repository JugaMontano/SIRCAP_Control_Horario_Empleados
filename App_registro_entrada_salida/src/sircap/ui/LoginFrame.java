package sircap.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import sircap.*;

/** Pantalla de inicio de sesión (RF-10). */
public class LoginFrame extends JFrame {
    private final SistemaSIRCAP sistema;
    private final JTextField usuario = new JTextField(18);
    private final JPasswordField contrasena = new JPasswordField(18);

    public LoginFrame(SistemaSIRCAP sistema) {
        super("SIRCAP - Iniciar sesión");
        this.sistema = sistema;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel fondo = new JPanel(new GridBagLayout());
        fondo.setBackground(Estilo.AZUL);

        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel marca = new JLabel("SIRCAP");
        marca.setFont(new Font("SansSerif", Font.BOLD, 34));
        marca.setForeground(Estilo.AZUL);
        JLabel descripcion = Estilo.subtitulo("<html>Sistema Integrado para Registro y<br>Control de Asistencia del Personal</html>");
        marca.setAlignmentX(Component.LEFT_ALIGNMENT);
        descripcion.setAlignmentX(Component.LEFT_ALIGNMENT);
        tarjeta.add(marca);
        tarjeta.add(descripcion);
        tarjeta.add(Box.createVerticalStrut(22));

        Estilo.Formulario f = new Estilo.Formulario();
        f.agregar("Usuario", usuario);
        f.agregar("Contraseña", contrasena);
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        tarjeta.add(f);

        JButton entrar = Estilo.boton("Ingresar", Estilo.AZUL_CLARO);
        entrar.setAlignmentX(Component.LEFT_ALIGNMENT);
        tarjeta.add(Box.createVerticalStrut(14));
        tarjeta.add(entrar);

        if (adminSinCambiarClave()) {
            JLabel pista = new JLabel("<html><br>Primer ingreso: usuario <b>" + SistemaSIRCAP.USUARIO_INICIAL
                    + "</b>, contraseña <b>" + SistemaSIRCAP.CLAVE_INICIAL + "</b>.<br>Cámbiala después de entrar.</html>");
            pista.setForeground(Estilo.AMBAR);
            pista.setAlignmentX(Component.LEFT_ALIGNMENT);
            tarjeta.add(pista);
        }

        fondo.add(tarjeta);
        setContentPane(fondo);
        entrar.addActionListener(e -> ingresar());
        contrasena.addActionListener(e -> ingresar());
        getRootPane().setDefaultButton(entrar);

        pack();
        setMinimumSize(new Dimension(520, 440));
        setSize(560, 480);
        setLocationRelativeTo(null);
    }

    private boolean adminSinCambiarClave() {
        for (Usuario u : sistema.getAutenticacion().getUsuarios()) {
            if (u.getNombreUsuario().equals(SistemaSIRCAP.USUARIO_INICIAL) && u.validarContrasena(SistemaSIRCAP.CLAVE_INICIAL)) return true;
        }
        return false;
    }

    private void ingresar() {
        try {
            Usuario u = sistema.iniciarSesion(usuario.getText(), new String(contrasena.getPassword()));
            new VentanaPrincipal(sistema, u).setVisible(true);
            dispose();
        } catch (SircapException ex) {
            Estilo.error(this, ex.getMessage());
            contrasena.setText("");
            contrasena.requestFocus();
        }
    }
}
