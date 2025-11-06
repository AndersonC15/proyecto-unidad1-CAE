package servicio.acciones;

import modelo.Accion;
import modelo.Nota;
import modelo.Ticket;

// Acción concreta para agregar una nota (Undo/Redo de TICKET)
public class AccionAgregarNota extends Accion {

    private final Ticket ticket;
    private final Nota nota; // La nota que se agregó

    public AccionAgregarNota(Ticket ticket, Nota nota) {
        super("AGREGAR_NOTA", "Nota ID " + nota.id());
        this.ticket = ticket;
        this.nota = nota;
    }

    // Ejecutar (para Redo): Reinserta la nota.
    @Override
    public void ejecutar() {
        // Vuelve a insertar la nota al inicio de la lista
        ticket.getListaNotas().insertarInicio(nota);
    }

    // Deshacer (para Undo): Elimina la nota agregada
    @Override
    public void deshacer() {
        // Elimina por ID
        ticket.getListaNotas().eliminar(nota.id());
    }

    @Override
    public String getResumenDetallado() {
        return String.format("AGREGAR_NOTA: Ticket #%d - Nota ID %d: \"%s\"",
                ticket.getId(), nota.id(), nota.texto());
    }
}