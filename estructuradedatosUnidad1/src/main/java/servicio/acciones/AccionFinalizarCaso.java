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

        // 1. Quitarlo de 'enAtencion'
        gestor.setTicketEnAtencion(null);

        // 2. Moverlo a su destino
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

        // 1. Quitar el ticket de su destino final
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
            // Si t es null, hubo un error, pero continuamos...
        }

        // 2. Restaurarlo en 'ticketEnAtencion'
        //    (Nota: esto pisa cualquier ticket que esté en atención.
        //     El Undo Global debe usarse con cuidado)
        gestor.setTicketEnAtencion(ticket);

        // 3. Restaurar el estado que tenía ANTES de finalizar (EN_ATENCION)
        //    (El estado guardado es el estado *final*. El estado *previo*
        //     se perdió, asumimos que era EN_ATENCION o PENDIENTE_DOCS/EN_PROCESO)
        //    Para simplificar, lo ponemos en EN_ATENCION.
        ticket.cambiarEstado(Estado.EN_ATENCION);
    }

    @Override
    public String getResumenDetallado() {
        return String.format("FINALIZAR/RE-ENCOLAR: Ticket #%d movido a %s",
                ticket.getId(), estadoOriginalTicket);
    }
}