package estructuras.cola;

import modelo.Ticket;

public class QuequeCAE {
    private NodoCola frente;
    private NodoCola fin;
    private int tamanio;

    // Insertar un nuevo ticket al final de la cola
    public void enqueque(Ticket ticket) {
        NodoCola nuevo = new NodoCola(ticket);
        if (estaVacia()) {
            frente = nuevo;
        } else {
            fin.setSiguiente(nuevo);
        }
        fin = nuevo;
        tamanio++;
        // La lógica de cambiar estado se mueve a GestorTickets
    }

    /**
     * NUEVO: Inserta al frente. Necesario para Undo de 'iniciarAtencion'.
     * Rompe FIFO intencionalmente.
     */
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

    // Sacar el ticket del frente de la cola (FIFO)
    public Ticket dequeue() {
        if (estaVacia()) {
            return null;
        }
        Ticket ticketAtendido = frente.getDato();
        frente = frente.getSiguiente();
        if (frente == null) {
            fin = null; // Si la cola queda vacía
        }
        tamanio--;
        return ticketAtendido;
    }

    /**
     * NUEVO: Elimina un ticket por ID. Necesario para Undo de 'recibirNuevoCaso'.
     * @return El ticket eliminado, o null si no se encuentra.
     */
    public Ticket eliminarPorId(int id) {
        if (estaVacia()) return null;

        // Caso 1: Es la cabeza
        if (frente.getDato().getId() == id) {
            return dequeue(); // Dequeue maneja la lógica de re-asignar frente y fin
        }

        // Caso 2: Es un nodo intermedio o el final
        NodoCola actual = frente;
        while (actual.getSiguiente() != null && actual.getSiguiente().getDato().getId() != id) {
            actual = actual.getSiguiente();
        }

        // Si no se encontró
        if (actual.getSiguiente() == null) return null;

        // Si se encontró
        NodoCola nodoAEliminar = actual.getSiguiente();
        Ticket ticketEliminado = nodoAEliminar.getDato();

        actual.setSiguiente(nodoAEliminar.getSiguiente());

        // Si el nodo eliminado era el fin, actualizar el fin
        if (actual.getSiguiente() == null) {
            fin = actual;
        }

        tamanio--;
        return ticketEliminado;
    }

    // Recorrer y listar los tickets en espera (Request #6)
    public void listar() {
        if (estaVacia()) {
            System.out.println("  (Vacía)");
            return;
        }
        NodoCola actual = frente;
        int i = 1;
        while (actual != null) {
            // Se usa el toString de Ticket (mejorado)
            System.out.println("  " + (i++) + ". " + actual.getDato().toString());
            actual = actual.getSiguiente();
        }
    }

    /**
     * NUEVO: Getter para persistencia y reportes
     */
    public NodoCola getFrente() {
        return frente;
    }

    /**
     * NUEVO: Para cargar datos
     */
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
