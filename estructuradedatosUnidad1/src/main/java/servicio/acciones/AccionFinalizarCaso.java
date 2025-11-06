package servicio.acciones;

import modelo.Accion;
import modelo.Estado;
import modelo.Ticket;
import servicio.GestorTickets;
import java.util.TreeMap;

// Acción para Finalizar/Re-encolar (Undo/Redo GLOBAL)
public class AccionFinalizarCaso extends Accion {

    private GestorTickets gestor;
    private Ticket ticket;
    private Estado estadoOriginalTicket; // El estado que tenía el ticket (COMPLETADO, PENDIENTE, etc.)

    public AccionFinalizarCaso(GestorTickets gestor, Ticket ticket) {
        super("FINALIZAR_CASO", "Ticket ID " + ticket.getId() + " -> " + ticket.getEstado());
        this.gestor = gestor;
        this.ticket = ticket;
        this.estadoOriginalTicket = ticket.getEstado(); // Captura el estado final
    }

    @Override
    public void ejecutar() {
        // REHACER: Mueve el ticket de 'enAtencion' a su destino final

        // Quitarlo de enAtencion
        gestor.setTicketEnAtencion(null);

        // Moverlo a su destino
        if (estadoOriginalTicket == Estado.COMPLETADO) {
            gestor.getTicketsFinalizados().put(ticket.getId(), ticket);
        } else {
            // Re-encolar (PENDIENTE_DOCS, EN_PROCESO)
            if (ticket.esUrgente()) {
                gestor.getColaUrgente().enqueque(ticket);
            } else {
                gestor.getColaNormal().enqueque(ticket);
            }
        }
        // El estado del ticket ya es el correcto
    }

    @Override
    public void deshacer() {
        // DESHACER: Revertir la finalización

        if (estadoOriginalTicket == Estado.COMPLETADO) {
            gestor.getTicketsFinalizados().remove(ticket.getId());
        } else {
            // Quitarlo de la cola donde fue re-encolado
            Ticket t;
            if (ticket.esUrgente()) {
                t = gestor.getColaUrgente().eliminarPorId(ticket.getId());
            } else {
                t = gestor.getColaNormal().eliminarPorId(ticket.getId());
            }
        }

        // Restaurarlo en 'ticketEnAtencion'
        gestor.setTicketEnAtencion(ticket);
        ticket.cambiarEstado(Estado.EN_ATENCION);
    }

    @Override
    public String getResumenDetallado() {
        return String.format("FINALIZAR/RE-ENCOLAR: Ticket #%d movido a %s",
                ticket.getId(), estadoOriginalTicket);
    }
}
