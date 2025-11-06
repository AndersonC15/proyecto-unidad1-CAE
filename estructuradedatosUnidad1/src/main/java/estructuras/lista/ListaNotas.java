package estructuras.lista;

import modelo.Nota;

public class ListaNotas {

    private NodoNota cabeza;

    public ListaNotas() {
        this.cabeza = null;
    }

    public boolean estaVacia() {
        return cabeza == null;
    }

    // Insertar al inicio
    public void insertarInicio(Nota n) {
        NodoNota nuevo = new NodoNota(n);
        nuevo.setSiguiente(cabeza);
        cabeza = nuevo;
    }

    // Eliminar la primera coincidencia
    public Nota eliminar(int id) {
        if (estaVacia()) return null;

        // Eliminar la cabeza
        if (cabeza.getDato().id() == id) {
            Nota notaEliminada = cabeza.getDato();
            cabeza = cabeza.getSiguiente();
            return notaEliminada;
        }

        NodoNota actual = cabeza;
        // buscar el nodo anterior al que se va a eliminar
        while (actual.getSiguiente() != null && actual.getSiguiente().getDato().id() != id) {
            actual = actual.getSiguiente();
        }

        if (actual.getSiguiente() == null) return null; // No se encontró

        // Eliminar nodo intermedio/final
        Nota notaEliminada = actual.getSiguiente().getDato();
        actual.setSiguiente(actual.getSiguiente().getSiguiente());
        return notaEliminada;
    }

    // Recorrer para listar
    public void mostrar() {
        if (estaVacia()) {
            System.out.println("  (No hay notas registradas para este ticket)");
            return;
        }

        NodoNota aux = cabeza;
        while (aux != null) {
            System.out.println("    " + aux.getDato());
            aux = aux.getSiguiente();
        }
    }

    /**
     * NUEVO: Para reporte Top-K [cite: 49, 62]
     */
    public int getTamanio() {
        int contador = 0;
        NodoNota actual = cabeza;
        while (actual != null) {
            contador++;
            actual = actual.getSiguiente();
        }
        return contador;
    }

    /**
     * NUEVO: Para persistencia
     */
    public NodoNota getCabeza() {
        return cabeza;
    }
}
