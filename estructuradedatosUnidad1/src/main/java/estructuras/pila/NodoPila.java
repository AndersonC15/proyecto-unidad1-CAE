package estructuras.pila;

import modelo.Accion;

public class NodoPila {
    private Accion dato;
    private NodoPila siguiente; // Usar 'siguiente' para consistencia con otras estructuras

    // Constructor corregido para enlazar al crear (conveniente para el push)
    public NodoPila(Accion dato, NodoPila siguiente) {
        this.dato = dato;
        this.siguiente = siguiente;
    }

    public Accion getDato() { return dato; }
    public NodoPila getSiguiente() { return siguiente; }
}
