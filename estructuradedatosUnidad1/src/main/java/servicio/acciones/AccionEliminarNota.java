package servicio.acciones;

import modelo.Accion;
import modelo.Nota;
import estructuras.lista.ListaNotas;

// Accion para eliminar notas especificas
public class AccionEliminarNota extends Accion {

    private final ListaNotas listaNotas;
    private final Nota notaEliminada; // Guardamos la nota eliminada

    public AccionEliminarNota(ListaNotas listaNotas, Nota notaEliminada) {
        super("ELIMINAR_NOTA", "Nota ID " + notaEliminada.id() + " eliminada.");
        this.listaNotas = listaNotas;
        this.notaEliminada = notaEliminada;
    }

    // Reeliminacion de notas
    @Override
    public void ejecutar() {
        listaNotas.eliminar(notaEliminada.id());
    }

    //Reinserta la nota eliminada
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
