package sircap;

/** Categorías de ausencias y novedades del personal (RF-05). */
public enum TipoNovedad {
    ENFERMEDAD("Enfermedad / incapacidad"), LICENCIA("Licencia"), VACACIONES("Vacaciones"), LLEGADA_TARDE("Llegada tarde");

    private final String etiqueta;

    TipoNovedad(String etiqueta) { this.etiqueta = etiqueta; }

    @Override
    public String toString() { return etiqueta; }
}
