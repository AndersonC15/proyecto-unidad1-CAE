package modelo;

public enum Estado {
    EN_COLA("EN COLA"),
    URGENTE("URGENTE"), // <-- NUEVO ESTADO
    EN_ATENCION("EN ATENCIÓN"),
    EN_PROCESO("EN PROCESO"),
    PENDIENTE_DOCS("PENDIENTE DOCS"),
    COMPLETADO("COMPLETADO");

    private final String descripcion;

    Estado(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}