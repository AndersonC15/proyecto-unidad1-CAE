package estructuras.pila;

import modelo.Accion;

public class PilaAcciones {
    private NodoPila tope;
    private int size = 0;

    // Push: insertar en la cima
    public void push(Accion accion) {
        tope = new NodoPila(accion, tope);
        size++;
    }

    // Pop: sacar de la cima
    public Accion pop() {
        if (estaVacia()) return null;

        Accion a = tope.dato();
        tope = tope.siguiente();
        size--;
        return a;
    }


    public boolean estaVacia() {
        return tope == null;
    }

    public int getSize() {
        return size;
    }

    public void limpiar() {
        tope = null;
        size = 0;
    }
}
