package servicio;

import estructuras.cola.QuequeCAE;
import estructuras.pila.UndoRedoManager;
import modelo.Ticket;
import modelo.Estado;
import modelo.Nota;
import servicio.acciones.*;
import estructuras.cola.NodoCola;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class GestorTickets {

    private final QuequeCAE colaNormal;
    private final QuequeCAE colaUrgente;
    private Ticket ticketEnAtencion;
    private final UndoRedoManager undoRedoTicket;
    private final UndoRedoManager undoRedoGlobal;
    private final TreeMap<Integer, Ticket> ticketsFinalizados;

    public GestorTickets() {
        this.colaNormal = new QuequeCAE();
        this.colaUrgente = new QuequeCAE();
        this.undoRedoTicket = new UndoRedoManager();
        this.undoRedoGlobal = new UndoRedoManager();
        this.ticketsFinalizados = new TreeMap<>();
    }

    // Método para gestionar la entrada de tickets en cola normal o urgente
    public void recibirNuevoCaso(String nombreCliente, boolean esUrgente) {
        Estado estadoInicial = esUrgente ? Estado.URGENTE : Estado.EN_COLA;
        Ticket nuevoTicket = new Ticket(nombreCliente, estadoInicial, esUrgente);

        if (esUrgente) {
            colaUrgente.enqueque(nuevoTicket);
        } else {
            colaNormal.enqueque(nuevoTicket);
        }

        undoRedoGlobal.registrarAccion(new AccionRecibirTicket(this, nuevoTicket));

        System.out.println("Nuevo ticket recibido y encolado: ");
        System.out.println(nuevoTicket.toString());
    }

    // Método para gestionar la entrada de tickets en cola normal o urgente
    public boolean iniciarAtencion() {
        if (ticketEnAtencion != null) {
            System.err.println("Error: Ya hay un ticket en atención (#" + ticketEnAtencion.getId() + ").");
            return false;
        }

        Ticket siguiente;
        boolean eraUrgente = false;

        if (!colaUrgente.estaVacia()) {
            siguiente = colaUrgente.dequeue();
            eraUrgente = true;
            System.out.println("Atendiendo siguiente caso URGENTE...");
        }
        else if (!colaNormal.estaVacia()) {
            siguiente = colaNormal.dequeue();
            System.out.println("La cola urgente está vacía. Atendiendo siguiente caso NORMAL...");
        }
        else {
            System.err.println("Las colas (Urgente y Normal) están vacías. No hay casos para atender.");
            return false;
        }

        ticketEnAtencion = siguiente;

        undoRedoGlobal.registrarAccion(new AccionIniciarAtencion(this, ticketEnAtencion));

        ticketEnAtencion.cambiarEstado(Estado.EN_ATENCION);

        if (eraUrgente) {
            ticketEnAtencion.setEsUrgente(false);
            System.out.println("Info: La prioridad URGENTE del Ticket #" + ticketEnAtencion.getId() + " ha sido consumida.");
        }

        // Limpia el historial del ticket ANTERIOR al iniciar uno nuevo
        undoRedoTicket.limpiar();

        System.out.println("\n--- Iniciando Atención ---");
        System.out.println(ticketEnAtencion.toString());
        System.out.println("--------------------------");
        return true;
    }

    // método que gestiona lo sucedio al finalizar
    public boolean finalizarCaso() {
        if (ticketEnAtencion == null) {
            System.err.println("Error: No hay caso en atención para finalizar.");
            return false;
        }

        Estado estadoFinal = ticketEnAtencion.getEstado();

        if (estadoFinal == Estado.EN_ATENCION) {
            System.err.println("Error: No se puede finalizar el caso.");
            System.err.println("Debe cambiar el estado a EN_PROCESO, PENDIENTE_DOCS o COMPLETADO primero.");
            return false;
        }

        if (estadoFinal == Estado.COMPLETADO) {
            ticketEnAtencion.setFechaFinalizacion(LocalDateTime.now());
        }


        undoRedoTicket.registrarAccion(new AccionFinalizarCaso(this, ticketEnAtencion));

        // Acción secundaria a hacer: limpia historial principal
        undoRedoGlobal.limpiar();

        Ticket ticketFinalizado = ticketEnAtencion;
        ticketEnAtencion = null;

        if (estadoFinal == Estado.COMPLETADO) {
            ticketsFinalizados.put(ticketFinalizado.getId(), ticketFinalizado);
            System.out.println("\n--- Caso Finalizado (COMPLETADO) ---");
            System.out.println("Ticket #" + ticketFinalizado.getId() + " movido al historial.");
        }
        else {
            if (ticketFinalizado.esUrgente()) {
                colaUrgente.enqueque(ticketFinalizado);
                System.out.println("\n--- Caso Re-encolado (URGENTE) ---");
            } else {
                colaNormal.enqueque(ticketFinalizado);
                System.out.println("\n--- Caso Re-encolado (NORMAL) ---");
            }
            System.out.println("Ticket #" + ticketFinalizado.getId() + " devuelto a la cola con estado " + estadoFinal);
        }

        return true;
    }

    public boolean registrarNota(String texto) {
        if (ticketEnAtencion == null) {
            System.err.println("Error: No hay caso en atención. Inicie uno primero.");
            return false;
        }
        if (texto == null || texto.trim().isEmpty()) {
            System.err.println("Error: El texto de la nota no puede estar vacío.");
            return false;
        }
        Nota nuevaNota = ticketEnAtencion.agregarNota(texto);
        AccionAgregarNota accion = new AccionAgregarNota(ticketEnAtencion, nuevaNota);

        undoRedoTicket.registrarAccion(accion);

        // Acción secundaria para hacer: limpia historial principal
        undoRedoGlobal.limpiar();

        System.out.println("Nota agregada y acción registrada para Undo (Ticket).");
        return true;
    }


    public boolean eliminarNota(int idNota) {
        if (ticketEnAtencion == null) {
            System.err.println("Error: No hay caso en atención. Inicie uno primero.");
            return false;
        }
        Nota notaEliminada = ticketEnAtencion.eliminarNota(idNota);
        if (notaEliminada != null) {
            AccionEliminarNota accion = new AccionEliminarNota(ticketEnAtencion.getListaNotas(), notaEliminada);
            undoRedoTicket.registrarAccion(accion);
            undoRedoGlobal.limpiar();

            System.out.println("Nota ID " + idNota + " eliminada y acción registrada para Undo (Ticket).");
            return true;
        } else {
            System.err.println("Error: No se encontró la nota con el ID #" + idNota + " en este ticket.");
            return false;
        }
    }

    public boolean cambiarEstadoInterno(Estado nuevoEstado) {
        if (ticketEnAtencion == null) {
            System.err.println("Error: No hay caso en atención.");
            return false;
        }
        Estado estadoAnterior = ticketEnAtencion.getEstado();
        if (estadoAnterior == nuevoEstado) {
            System.out.println("El ticket ya se encuentra en estado: " + nuevoEstado);
            return true;
        }
        if (estadoAnterior == Estado.COMPLETADO) {
            System.err.println("Error: Un ticket COMPLETADO no puede cambiar de estado.");
            return false;
        }
        ticketEnAtencion.cambiarEstado(nuevoEstado);
        AccionCambiarEstado accion = new AccionCambiarEstado(ticketEnAtencion, estadoAnterior, nuevoEstado);

        undoRedoTicket.registrarAccion(accion);
        undoRedoGlobal.limpiar();

        System.out.println("Estado del Ticket #" + ticketEnAtencion.getId() + " cambiado a " + nuevoEstado + ".");
        System.out.println("Acción registrada para Undo (Ticket).");
        return true;
    }

    public boolean deshacerAccionTicket() {
        if (ticketEnAtencion == null && undoRedoTicket.getSize() == 0) {
            System.err.println("Error: No hay ticket en atención ni acciones pendientes para deshacer.");
            return false;
        }
        return undoRedoTicket.deshacer();
    }

    public boolean rehacerAccionTicket() {
        return undoRedoTicket.rehacer();
    }

    public boolean deshacerAccionGlobal() {
        return undoRedoGlobal.deshacer();
    }

    public boolean rehacerAccionGlobal() {
        return undoRedoGlobal.rehacer();
    }


    // Método importante para limpiar los undo o acciones realizados en el ticket actual, y llevarlo a 0
    public void limpiarHistorialTicket() {
        undoRedoTicket.limpiar();
    }

    public void listarCasosEnEspera() {
        System.out.println("\n--- Casos en Espera ---");
        System.out.println("\n== EN ATENCIÓN ==");
        if (ticketEnAtencion != null) {
            System.out.println("  -> " + ticketEnAtencion.toString());
        } else {
            System.out.println("  (Nadie en atención)");
        }
        System.out.println("\n== COLA URGENTE (" + colaUrgente.getTamanio() + ") ==");
        colaUrgente.listar();
        System.out.println("\n== COLA NORMAL (" + colaNormal.getTamanio() + ") ==");
        colaNormal.listar();
        System.out.println("---------------------------------");
    }

    public void listarTicketsFinalizados() {
        System.out.println("\n--- Historial de Casos Finalizados (COMPLETADO) ---");
        if (ticketsFinalizados.isEmpty()) {
            System.out.println("  (No hay casos completados en el historial)");
            return;
        }
        for (Ticket t : ticketsFinalizados.values()) {
            String fechaStr = (t.getFechaFinalizacion() != null) ?
                    t.getFechaFinalizacion().toLocalDate().toString() : "N/A";
            System.out.printf("  ID: %-3d | Cliente: %-20s | Fecha Fin: %-10s | Estado: %s\n",
                    t.getId(), t.getNombreCliente(), fechaStr, t.getEstado());
        }
        System.out.println("-----------------------------------------------------------------");
    }

    public boolean listarTicketsFinalizados(LocalDate fechaDesde, LocalDate fechaHasta) {
        if (ticketsFinalizados.isEmpty()) {
            System.out.println("  (No hay casos completados en el historial)");
            return false;
        }
        LocalDateTime inicioDelDia = fechaDesde.atStartOfDay();
        LocalDateTime finDelDia = fechaHasta.atTime(LocalTime.MAX);
        int contador = 0;
        for (Ticket t : ticketsFinalizados.values()) {
            LocalDateTime fechaFin = t.getFechaFinalizacion();
            if (fechaFin != null &&
                    !fechaFin.isBefore(inicioDelDia) &&
                    !fechaFin.isAfter(finDelDia)) {
                if (contador == 0) {
                    System.out.println("\n--- Casos Finalizados Encontrados ---");
                }
                String fechaStr = fechaFin.toLocalDate().toString();
                System.out.printf("  ID: %-3d | Cliente: %-20s | Fecha Fin: %-10s | Estado: %s\n",
                        t.getId(), t.getNombreCliente(), fechaStr, t.getEstado());
                contador++;
            }
        }
        System.out.println("-----------------------------------------------------------------");
        if (contador == 0) {
            System.out.println("  (No se encontraron tickets en el rango de fechas seleccionado)");
            return false;
        }
        return true;
    }

    public void consultarHistorial(int idTicket) {
        Ticket ticket = ticketsFinalizados.get(idTicket);
        if (ticket == null) {
            System.err.println("Error: El ticket #" + idTicket + " no se encuentra en el historial de FINALIZADOS.");
            return;
        }
        System.out.println("\n--- Historial Detallado del Ticket #" + idTicket + " ---");
        System.out.println(ticket.toString());
        if (ticket.getFechaFinalizacion() != null) {
            System.out.println("Fecha Finalización: " + ticket.getFechaFinalizacion().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
        System.out.println("\n  Notas Registradas:");
        ticket.getListaNotas().mostrar();
        System.out.println("----------------------------------------");
    }

    public void ejecutarReporteTopK(int k) {
        if (k <= 0) {
            System.err.println("Error: K debe ser un número positivo.");
            return;
        }
        List<Ticket> todosLosTickets = new ArrayList<>();
        if (ticketEnAtencion != null) todosLosTickets.add(ticketEnAtencion);
        for (NodoCola n = colaUrgente.getFrente(); n != null; n = n.getSiguiente()) todosLosTickets.add(n.getDato());
        for (NodoCola n = colaNormal.getFrente(); n != null; n = n.getSiguiente()) todosLosTickets.add(n.getDato());
        todosLosTickets.addAll(ticketsFinalizados.values());
        if (todosLosTickets.isEmpty()) {
            System.out.println("No hay tickets en el sistema para generar un reporte.");
            return;
        }
        todosLosTickets.sort(Comparator.comparingInt(t -> ((Ticket)t).getListaNotas().getTamanio()).reversed());
        System.out.printf("\n--- Reporte Top-%d Tickets por N° de Notas ---\n", k);
        System.out.printf("%-5s | %-8s | %-20s | %-15s\n", "Rank", "N° Notas", "Cliente", "Estado Final");
        System.out.println("----------------------------------------------------------");
        int count = 0;
        for (Ticket t : todosLosTickets) {
            if (count >= k) break;
            count++;
            System.out.printf("%-5d | %-8d | %-20s | %-15s\n",
                    count, t.getListaNotas().getTamanio(), t.getNombreCliente(), t.getEstado());
        }
        System.out.println("----------------------------------------------------------");
    }

    public Ticket getTicketEnAtencion() { return ticketEnAtencion; }
    public void setTicketEnAtencion(Ticket ticket) { this.ticketEnAtencion = ticket; }
    public boolean hayTicketsFinalizados() { return !ticketsFinalizados.isEmpty(); }
    public int undoTicketCount() { return undoRedoTicket.getSize(); }
    public int redoTicketCount() { return undoRedoTicket.getRedoSize(); }
    public int undoGlobalCount() { return undoRedoGlobal.getSize(); }
    public int redoGlobalCount() { return undoRedoGlobal.getRedoSize(); }
    public QuequeCAE getColaNormal() { return colaNormal; }
    public QuequeCAE getColaUrgente() { return colaUrgente; }
    public TreeMap<Integer, Ticket> getTicketsFinalizados() { return ticketsFinalizados; }

    public void limpiarTodo() {
        colaNormal.limpiar();
        colaUrgente.limpiar();
        ticketsFinalizados.clear();
        ticketEnAtencion = null;
        undoRedoGlobal.limpiar();
        undoRedoTicket.limpiar();
        Ticket.setGeneradorId(1);
        System.out.println("Estado interno del gestor limpiado.");
    }
}