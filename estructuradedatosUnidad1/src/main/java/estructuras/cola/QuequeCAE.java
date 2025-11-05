package estructuras.cola;

import modelo.Ticket;
import modelo.Estado;

public class QuequeCAE {
    private NodoCola frente;
    private NodoCola fin;
    private int tamanio;

    public void enqueque(Ticket ticket) {
        NodoCola nuevo = new NodoCola(ticket);
        if (estaVacia()) {
            frente = nuevo;
        } else {
            fin.setSiguiente(nuevo);
        }
        fin = nuevo;
        tamanio++;
        // Se elimina: ticket.cambiarEstado(Estado.EN_COLA);
        // Esta lógica ahora la maneja GestorTickets
    }

    public void enquequeAlFrente(Ticket ticket) {
        NodoCola nuevo = new NodoCola(ticket);
        if (estaVacia()) {
            frente = nuevo;
            fin = nuevo;
        } else {
            nuevo.setSiguiente(frente);
            frente = nuevo;
        }
        tamanio++;
    }

    public Ticket dequeue() {
        if (estaVacia()) {
            return null;
        }
        Ticket ticketAtendido = frente.getDato();
        frente = frente.getSiguiente();
        if (frente == null) {
            fin = null;
        }
        tamanio--;
        return ticketAtendido;
    }

    public Ticket eliminarPorId(int id) {
        if (estaVacia()) {
            System.out.println("No se puede eliminar porque esta vacia");
            return null;
        }
        // CASO 2: ELIMINAR EL FRENTE
        if (frente.getDato().getId() == id) {
            // 1. Guardas el ticket
            Ticket ticketAtendido = frente.getDato();
            // 2. Mueves el 'frente' al siguiente
            frente = frente.getSiguiente();
            // 3. Verificas si la cola quedó vacía
            if (frente == null) {
                fin = null;
            }

            tamanio--;
            return ticketAtendido;
        }

        NodoCola actual = frente;
        while (actual.getSiguiente() != null && actual.getSiguiente().getDato().getId() != id) {
            actual = actual.getSiguiente();
        }
        if (actual.getSiguiente() == null) {
            return null;
        }
        Ticket ticketAtendido = actual.getSiguiente().getDato();
        if (actual.getSiguiente()== fin) {
            fin = actual;
        }
        actual.setSiguiente(actual.getSiguiente().getSiguiente());
        tamanio--;
        return ticketAtendido;
    }
    // Interactúa con: AccionRecibirTicket.deshacer()// Devuelve el ticket eliminado o null si no se encuentra
    public void listar() {
        if (estaVacia()) {
            System.out.println(" (Vacía)");
            return;
        }
        NodoCola actual = frente;
        while (actual != null) {
            // Se usa el toString() mejorado de Ticket
            System.out.println("  -> " + actual.getDato().toString()); // <-- toString() de Ticket
            actual = actual.getSiguiente();
        }
    }

    public NodoCola getFrente() {
        // Permite a PersistenciaService recorrer la cola sin modificarla.
        return frente;
    }

    public void limpiar() {
        frente = null;
        fin = null;
        tamanio = 0;
    }

    public boolean estaVacia() {
        return frente == null;
    }

    public int getTamanio() {
        return tamanio;
    }
}