package estructuras.cola;

import modelo.Ticket;

public class NodoCola {
    private Ticket dato; // almacena un objeto Ticket, o el estudiante en la cola
    private NodoCola siguiente; // almacena una referencia al siguiente nodo en la cola

    public NodoCola(Ticket dato) {
        this.dato = dato;
        this.siguiente = null; // El nodo siguiente es null por defecto
    }

    // Getters para acceso en QuequeCAE
    public Ticket getDato() { return dato; }
    public NodoCola getSiguiente() { return siguiente; }
    public void setSiguiente(NodoCola siguiente) { this.siguiente = siguiente; }
}