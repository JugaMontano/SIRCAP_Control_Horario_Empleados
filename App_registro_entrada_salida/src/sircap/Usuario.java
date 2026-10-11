package sircap;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Credenciales de acceso de un empleado (RF-10). La contraseña se guarda cifrada (RNF-01). */
public class Usuario {
    private String nombreUsuario;
    private String contrasenaHash;
    private Empleado empleado;

    public Usuario(String nombreUsuario, String contrasenaPlana, Empleado empleado) {
        this.nombreUsuario = nombreUsuario;
        this.contrasenaHash = cifrar(contrasenaPlana);
        this.empleado = empleado;
    }

    /** Constructor para reconstruir un usuario guardado (la contraseña ya viene cifrada). */
    private Usuario(String nombreUsuario, Empleado empleado, String contrasenaHash) {
        this.nombreUsuario = nombreUsuario;
        this.empleado = empleado;
        this.contrasenaHash = contrasenaHash;
    }

    public static Usuario desdeHash(String nombreUsuario, String contrasenaHash, Empleado empleado) {
        return new Usuario(nombreUsuario, empleado, contrasenaHash);
    }

    public String getContrasenaHash() { return contrasenaHash; }

    public void cambiarContrasena(String nuevaContrasena) {
        this.contrasenaHash = cifrar(nuevaContrasena);
    }

    public String getNombreUsuario() { return nombreUsuario; }
    public Empleado getEmpleado() { return empleado; }
    public Rol getRol() { return empleado.getRol(); }

    public boolean validarContrasena(String contrasenaPlana) {
        return contrasenaHash.equals(cifrar(contrasenaPlana));
    }

    private static String cifrar(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
