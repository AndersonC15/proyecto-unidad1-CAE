package modelo;

public enum Estado {
    EN_COLA("EN COLA"),
    URGENTE("URGENTE"), // Estado para la cola de prioridad
    EN_ATENCION("EN ATENCIÓN"),
    EN_PROCESO("EN PROCESO"),
    PENDIENTE_DOCS("PENDIENTE DOCS"),
    COMPLETADO("COMPLETADO"); // Estado final

    private final String descripcion;

    Estado(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }

    // Helper para persistencia
    public static Estado fromString(String text) {
        for (Estado e : Estado.values()) {
            if (e.name().equalsIgnoreCase(text) || e.descripcion.equalsIgnoreCase(text)) {
                return e;
            }
        }
        // Devuelve EN_COLA como default seguro si no se encuentra
        return EN_COLA;
    }
}
