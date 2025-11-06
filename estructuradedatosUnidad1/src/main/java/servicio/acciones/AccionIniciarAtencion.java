package servicio.acciones;

import modelo.Accion;
import modelo.Estado;
import modelo.Ticket;
import servicio.GestorTickets;

public class AccionIniciarAtencion extends Accion {

    private GestorTickets gestor;
    private Ticket ticketAtendido;
    private boolean eraUrgente; // Esta bandera es la clave

    public AccionIniciarAtencion(GestorTickets gestor, Ticket ticketAtendido) {
        super("INICIAR_ATENCION", "Ticket ID " + ticketAtendido.getId());
        this.gestor = gestor;
        this.ticketAtendido = ticketAtendido;
        // Capturamos el estado ANTES de que GestorTickets lo mutee
        this.eraUrgente = ticketAtendido.esUrgente();
    }

    @Override
    public void ejecutar() {
        // Vuelve a poner el ticket en 'ticketEnAtencion'

        Ticket t;
        if (eraUrgente) {
            t = gestor.getColaUrgente().eliminarPorId(ticketAtendido.getId());
        } else {
            t = gestor.getColaNormal().eliminarPorId(ticketAtendido.getId());
        }

        gestor.setTicketEnAtencion(ticketAtendido);
        ticketAtendido.cambiarEstado(Estado.EN_ATENCION);

        // Re-consumir la urgencia, tal como lo hace la acción original
        if (eraUrgente) {
            ticketAtendido.setEsUrgente(false);
        }
    }

    @Override
    public void deshacer() {
        // Devuelve el ticket a su cola de salida

        gestor.setTicketEnAtencion(null);

        // Restaura la bandera 'esUrgente' ANTES de encolarla
        ticketAtendido.setEsUrgente(this.eraUrgente);

        if (eraUrgente) {
            ticketAtendido.cambiarEstado(Estado.URGENTE);
            gestor.getColaUrgente().enquequeAlFrente(ticketAtendido);
        } else {
            ticketAtendido.cambiarEstado(Estado.EN_COLA);
            gestor.getColaNormal().enquequeAlFrente(ticketAtendido);
        }
    }

    @Override
    public String getResumenDetallado() {
        // (El resumen no cambia)
        return String.format("INICIAR ATENCIÓN: Ticket #%d (%s) movido a EN ATENCIÓN",
                ticketAtendido.getId(), eraUrgente ? "Urgente" : "Normal");
    }
}

