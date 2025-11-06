package modelo;

import java.time.LocalDateTime;

// Clase abstracta para el patrón Command (Undo/Redo)
public abstract class Accion {

    private final String tipo;
    private final String descripcion;
    private final LocalDateTime fechaHora;

    public Accion(String tipo, String descripcion) {
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fechaHora = LocalDateTime.now();
    }

    /**
     * Ejecuta (o rehace) la acción.
     */
    public abstract void ejecutar();

    /**
     * Deshace la acción.
     */
    public abstract void deshacer();

    public String getResumen() {
        return String.format("%s: %s", tipo, descripcion);
    }

    public String getResumenDetallado() {
        return getResumen(); // Las clases hijas deben sobreescribir esto
    }

    @Override
    public String toString() {
        return String.format("%s: %s (%s)", tipo, descripcion, fechaHora);
    }
}ket management methods
