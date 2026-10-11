package sircap;

import java.util.ArrayList;
import java.util.List;

/** Login y control de acceso por rol (RF-10, RNF-01). */
public class ServicioAutenticacion {
    private List<Usuario> usuarios = new ArrayList<>();

    public void registrarUsuario(Usuario usuario) {
        usuarios.add(usuario);
    }

    /** Devuelve el usuario si las credenciales son válidas y el empleado está activo; si no, null. */
    public Usuario iniciarSesion(String nombreUsuario, String contrasena) {
        for (Usuario u : usuarios) {
            if (u.getNombreUsuario().equals(nombreUsuario)
                    && u.validarContrasena(contrasena)
                    && u.getEmpleado().isActivo()) {
                return u;
            }
        }
        return null;
    }

    public List<Usuario> getUsuarios() { return usuarios; }

    public boolean existeUsuario(String nombreUsuario) {
        for (Usuario u : usuarios) {
            if (u.getNombreUsuario().equalsIgnoreCase(nombreUsuario)) return true;
        }
        return false;
    }

    public boolean tienePermiso(Usuario usuario, Modulo modulo) {
        return usuario != null && usuario.getRol().puedeAcceder(modulo);
    }
}
