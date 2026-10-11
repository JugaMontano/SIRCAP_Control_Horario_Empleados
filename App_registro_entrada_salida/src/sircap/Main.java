package sircap;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import sircap.ui.Estilo;
import sircap.ui.LoginFrame;

/** Punto de entrada de la aplicación SIRCAP. Ejecuta esta clase. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Estilo.aplicarTema();
            try {
                SistemaSIRCAP sistema = new SistemaSIRCAP(new Almacen("datos"));
                new LoginFrame(sistema).setVisible(true);
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(null, "No se pudo iniciar SIRCAP:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
