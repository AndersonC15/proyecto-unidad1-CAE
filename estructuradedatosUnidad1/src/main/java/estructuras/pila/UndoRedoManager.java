package estructuras.pila;

import modelo.Accion;

public class UndoRedoManager {
    private final PilaAcciones pilaUndo = new PilaAcciones();
    private final PilaAcciones pilaRedo = new PilaAcciones();

    public void registrarAccion(Accion accion) {
        if (accion == null) return;
        pilaUndo.push(accion);
        pilaRedo.limpiar(); // Al registrar una nueva acción, el historial de "rehacer" se pierde
    }

    public boolean deshacer() {
        if (pilaUndo.estaVacia()) {
            System.err.println("No hay acciones para deshacer.");
            return false;
        }
        Accion accion = pilaUndo.pop();
        if (accion == null) return false; // Seguridad

        accion.deshacer(); // Ejecuta la lógica de reversión
        pilaRedo.push(accion);

        System.out.println("Se deshizo: " + accion.getResumenDetallado());
        return true;
    }

    public boolean rehacer() {
        if (pilaRedo.estaVacia()) {
            System.err.println("No hay acciones para rehacer.");
            return false;
        }

        Accion accion = pilaRedo.pop();
        if (accion == null) return false;

        accion.ejecutar(); // Vuelve a ejecutar la lógica original
        pilaUndo.push(accion);

        System.out.println("Se rehizo: " + accion.getResumenDetallado());
        return true;
    }

    public int getSize() { return pilaUndo.getSize(); } // Cambiado a getSize() para consistencia
    public int getRedoSize() { return pilaRedo.getSize(); }

    public void limpiar() {
        pilaUndo.limpiar();
        pilaRedo.limpiar();
    }
}