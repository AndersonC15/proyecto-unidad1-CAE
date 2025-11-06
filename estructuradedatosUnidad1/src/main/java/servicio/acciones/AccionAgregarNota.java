package servicio.acciones;

import modelo.Accion;
import modelo.Nota;
import modelo.Ticket;

public class AccionAgregarNota extends Accion {

    private final Ticket ticket;
    private final Nota nota;

    public AccionAgregarNota(Ticket ticket, Nota nota) {
        // Tipo y descripción para el historial
        super("AGREGAR_NOTA", "Nota ID " + nota.id() + ": " + nota.texto());
        this.ticket = ticket;
        this.nota = nota; // La nota ya fue agregada en el Ticket, se guarda la referencia
    }

    // Ejecutar (para Redo): Reinserta la nota.
    @Override
    public void ejecutar() {
        ticket.getListaNotas().insertarInicio(nota);
    }

    // Deshacer (para Undo): Elimina la nota agregada
    @Override
    public void deshacer() {
        // Eliminación por primera coincidencia del ID de la nota
        ticket.getListaNotas().eliminar(nota.id());
    }

    @Override
    public String getResumenDetallado() {
        return String.format("AGREGAR_NOTA: Ticket #%d - Nota ID %d: \"%s\"",
                ticket.getId(), nota.id(), nota.texto());
    }
}
