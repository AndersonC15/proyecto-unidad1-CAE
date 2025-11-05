package modelo;

import java.time.LocalDateTime;

// Clase abstracta para el patrón Command, esencial para Undo/Redo
public abstract class Accion {

    private final String tipo;
    private final String descripcion;
    private final LocalDateTime fechaHora;

    public Accion(String tipo, String descripcion) {
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fechaHora = LocalDateTime.now();
    }

    public abstract void ejecutar();
    public abstract void deshacer();

    public String getResumen() {
        return String.format("%s: %s", tipo, descripcion);
    }

    // NUEVO: permite un resumen más detallado que las acciones concretas pueden sobreescribir
    public String getResumenDetallado() {
        return getResumen(); // por defecto, mismo resumen
    }

    @Override
    public String toString() {
        return String.format("%s: %s (%s)", tipo, descripcion, fechaHora);
    }
}
