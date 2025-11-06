package servicio.acciones;

import modelo.Accion;
import modelo.Nota;
import estructuras.lista.ListaNotas;

// Acción concreta para eliminar una nota (Undo/Redo de TICKET)
public class AccionEliminarNota extends Accion {

    private final ListaNotas listaNotas;
    private final Nota notaEliminada; // Guardamos la Nota que se eliminó

    public AccionEliminarNota(ListaNotas listaNotas, Nota notaEliminada) {
        super("ELIMINAR_NOTA", "Nota ID " + notaEliminada.id() + " eliminada.");
        this.listaNotas = listaNotas;
        this.notaEliminada = notaEliminada;
    }

    // Ejecutar (para Redo): Re-elimina la nota
    @Override
    public void ejecutar() {
        listaNotas.eliminar(notaEliminada.id());
    }

    // Deshacer (para Undo): Re-inserta la nota eliminada al inicio
    @Override
    public void deshacer() {
        listaNotas.insertarInicio(notaEliminada);
    }

    @Override
    public String getResumenDetallado() {
        return String.format("ELIMINAR_NOTA: Nota ID %d: \"%s\"",
                notaEliminada.id(), notaEliminada.texto());
    }
}