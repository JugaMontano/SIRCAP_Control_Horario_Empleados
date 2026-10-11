package sircap.ui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import sircap.SircapException;
import sircap.SistemaSIRCAP;
import sircap.Usuario;

/** Base de todas las pestañas: guarda el sistema y el usuario en sesión. */
public abstract class PanelBase extends JPanel {
    protected final SistemaSIRCAP sistema;
    protected final Usuario usuario;

    protected PanelBase(SistemaSIRCAP sistema, Usuario usuario) {
        super(new BorderLayout(0, 12));
        this.sistema = sistema;
        this.usuario = usuario;
        setBackground(Estilo.FONDO);
        setBorder(new EmptyBorder(16, 18, 16, 18));
    }

    /** Vuelve a leer los datos y refresca lo que se ve en pantalla. */
    public abstract void actualizar();

    /** Ejecuta una acción y muestra en un aviso los errores de negocio. */
    protected boolean ejecutar(Runnable accion) {
        try {
            accion.run();
            return true;
        } catch (SircapException ex) {
            Estilo.error(this, ex.getMessage());
            return false;
        }
    }
}
