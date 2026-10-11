package sircap;

/** Error de negocio (permiso denegado, dato inválido, etc.) con un mensaje que se muestra al usuario. */
public class SircapException extends RuntimeException {
    public SircapException(String mensaje) {
        super(mensaje);
    }
}
