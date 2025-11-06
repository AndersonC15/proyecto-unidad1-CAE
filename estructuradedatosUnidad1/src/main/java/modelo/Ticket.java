package modelo;

import estructuras.lista.ListaNotas;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

public class Ticket {
    // Generador de ID para el ticket
    private static final AtomicInteger GENERADOR_ID = new AtomicInteger(1);
    private int id;
    private String nombreCliente;
    private Estado estado;
    private ListaNotas listaNotas;
    private AtomicInteger generadorIdNota = new AtomicInteger(1);
    private boolean esUrgente;

    private LocalDateTime fechaFinalizacion;

    public Ticket(String nombreCliente, Estado estadoInicial, boolean esUrgente) {
        this.id = GENERADOR_ID.getAndIncrement();
        this.nombreCliente = nombreCliente;
        this.estado = estadoInicial;
        this.esUrgente = esUrgente;
        this.listaNotas = new ListaNotas();
        this.fechaFinalizacion = null; // Inicia nulo
    }

    // Constructor para usar en persistencia
    public Ticket(int id, String nombreCliente, Estado estado, boolean esUrgente, int nextNotaId, LocalDateTime fechaFinalizacion) {
        this.id = id;
        this.nombreCliente = nombreCliente;
        this.estado = estado;
        this.esUrgente = esUrgente;
        this.listaNotas = new ListaNotas();
        this.generadorIdNota = new AtomicInteger(nextNotaId);
        this.fechaFinalizacion = fechaFinalizacion;

        GENERADOR_ID.set(Math.max(GENERADOR_ID.get(), id + 1));
    }

    public Nota agregarNota(String texto) {
        int idNota = generadorIdNota.getAndIncrement();
        Nota nueva = new Nota(idNota, texto);
        listaNotas.insertarInicio(nueva);
        return nueva;
    }

    public Nota agregarNota(int idNota, String texto, LocalDateTime fechaHora){
        Nota nueva = new Nota(idNota, texto, fechaHora);
        listaNotas.insertarInicio(nueva);
        return nueva;
    }

    public Nota eliminarNota(int idNota) {
        return listaNotas.eliminar(idNota);
    }

    public void cambiarEstado(Estado nuevoEstado) {
        this.estado = nuevoEstado;
    }

    @Override
    public String toString() {
        return String.format("Ticket ID: %-3d | Cliente: %-20s | Estado: %-15s | Prioridad: %s",
                id, nombreCliente, estado, esUrgente ? "URGENTE" : "NORMAL");
    }


    public int getId() { return id; }
    public String getNombreCliente() { return nombreCliente; }
    public Estado getEstado() { return estado; }
    public ListaNotas getListaNotas() { return listaNotas; }
    public boolean esUrgente() { return esUrgente; }
    public void setEsUrgente(boolean esUrgente) { this.esUrgente = esUrgente; }


    public static void setGeneradorId(int id) { GENERADOR_ID.set(id); }
    public static int getNextGeneradorId() { return GENERADOR_ID.get(); }
    public int getNextNotaId() { return generadorIdNota.get(); }


    public LocalDateTime getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(LocalDateTime fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }
}