package servicio.acciones;

import modelo.Accion;
import modelo.Estado;
import modelo.Ticket;

//Cambio de estado segun el undo/redo(Accion)
public class AccionCambiarEstado extends Accion {

    private final Ticket ticket;
    private final Estado estadoAnterior;
    private final Estado estadoNuevo;

    public AccionCambiarEstado(Ticket ticket, Estado estadoAnterior, Estado estadoNuevo) {
        super("CAMBIO_ESTADO", "De " + estadoAnterior + " a " + estadoNuevo);
        this.ticket = ticket;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
    }

    // Aplica un nuevo estado(redo), reaplicando el cambio deshecho
    @Override
    public void ejecutar() {
        ticket.cambiarEstado(estadoNuevo);
    }

    // Elimina el ultimo cambio(undo), volviendo al estado anterior
    @Override
    public void deshacer() {
        ticket.cambiarEstado(estadoAnterior);
    }

    @Override
    public String getResumenDetallado() {
        return String.format("CAMBIO_ESTADO: Ticket #%d - De %s a %s",
                ticket.getId(), estadoAnterior, estadoNuevo);
    }

}
