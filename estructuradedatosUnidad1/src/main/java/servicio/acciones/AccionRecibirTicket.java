package servicio.acciones;

import modelo.Accion;
import modelo.Ticket;
import servicio.GestorTickets;

public class AccionRecibirTicket extends Accion {

    private GestorTickets gestor;
    private Ticket ticket;
    private boolean esUrgente;

    public AccionRecibirTicket(GestorTickets gestor, Ticket ticket) {
        super("RECIBIR_TICKET", "Ticket ID " + ticket.getId());
        this.gestor = gestor;
        this.ticket = ticket;
        this.esUrgente = ticket.esUrgente();
    }

    @Override
    public void ejecutar() {
        if (esUrgente) {
            gestor.getColaUrgente().enqueque(ticket);
        } else {
            gestor.getColaNormal().enqueque(ticket);
        }
    }

    @Override
    public void deshacer() {
        Ticket t;
        if (esUrgente) {
            t = gestor.getColaUrgente().eliminarPorId(ticket.getId());
        } else {
            t = gestor.getColaNormal().eliminarPorId(ticket.getId());
        }

        if (t == null) {
            System.err.println("Error al deshacer 'Recibir Ticket': No se encontró el ticket " + ticket.getId());
        }
    }

    @Override
    public String getResumenDetallado() {
        return String.format("RECIBIR TICKET: Nuevo ticket #%d (%s) agregado a la cola",
                ticket.getId(), esUrgente ? "Urgente" : "Normal");
    }
}

