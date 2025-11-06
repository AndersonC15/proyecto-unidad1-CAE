package servicio.acciones;

import modelo.Accion;
import modelo.Estado;
import modelo.Ticket;

// Acción concreta para cambiar el estado (Undo/Redo de TICKET)
public class AccionCambiarEstado extends Accion {

    private final Ticket ticket;
    private final Estado estadoAnterior;
    private final Estado estadoNuevo;

    public AccionCambiarEstado(Ticket ticket, Estado estadoAnterior, Estado estadoNuevo) {
        super("CAMBIO_ESTADO", "De " + estadoAnterior + " a " + estadoNuevo);
        this.ticket = ticket;
        this.estadoA
