package servicio.acciones;

import modelo.Accion;
import modelo.Nota;
import modelo.Ticket;

// Acción concreta para agregar una nota
public class AccionAgregarNota extends Accion {

    private final Ticket ticket;
    private final Nota nota;

    public AccionAgregarNota(Ticket ticket, Nota nota) {
        // Tipo y descripción para el historial
        super("AGREGAR_NOTA", "Nota ID " + nota.getId() + ": " + nota.getTexto());
        this.ticket = ticket;
        this.nota = nota; // La nota ya fue agregada en el Ticket, se guarda la referencia
    }

    // Ejecutar (para Redo): Reinserta la nota.
    @Override
    public void ejecutar() {
        // Se asume que la Nota ya está en el Ticket si la acción viene de Undo
        // Si no está, se inserta. Para simplificar, si se rehace, se re-ejecuta el agregar
        // Nota: para un undo/redo perfecto, se requeriría una SLL que permita reinserción por valor
        // o que el objeto Nota contenga la lógica de re-inserción.
        // En este caso, simplemente volvemos a llamar a agregarNota para que funcione como Redo
        // y se registra el nuevo objeto Nota en la SLL (al inicio)
        ticket.getListaNotas().insertarInicio(nota);
    }

    // Deshacer (para Undo): Elimina la nota agregada
    @Override
    public void deshacer() {
        // Eliminación por primera coincidencia del ID de la nota
        ticket.getListaNotas().eliminar(nota.getId());
    }
    @Override
    public String getResumenDetallado() {
        return String.format("AGREGAR_NOTA: Ticket #%d - Nota ID %d: \"%s\"",
                ticket.getId(), nota.getId(), nota.getTexto());
    }



}
