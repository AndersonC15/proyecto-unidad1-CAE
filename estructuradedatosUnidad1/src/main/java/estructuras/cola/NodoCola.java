package estructuras.cola;

import modelo.Ticket;

public class NodoCola {
    private Ticket dato;
    private NodoCola siguiente;

    public NodoCola(Ticket dato) {
        this.dato = dato;
        this.siguiente = null;
    }

    // Getters para acceso en QuequeCAE
    public Ticket getDato() { return dato; }
    public NodoCola getSiguiente() { return siguiente; }
    public void setSiguiente(NodoCola siguiente) { this.siguiente = siguiente; }
}
