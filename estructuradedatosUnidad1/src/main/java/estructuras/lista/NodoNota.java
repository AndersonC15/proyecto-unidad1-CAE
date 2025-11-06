package estructuras.lista;

import modelo.Nota;

public class NodoNota {
    private Nota dato;
    private NodoNota siguiente;

    public NodoNota(Nota dato) {
        this.dato = dato;
        this.siguiente = null;
    }

    // Getters y Setters
    public Nota getDato() { return dato; }
    public NodoNota getSiguiente() { return siguiente; }
    public void setSiguiente(NodoNota siguiente) { this.siguiente = siguiente; }
}
