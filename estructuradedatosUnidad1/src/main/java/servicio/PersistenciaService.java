package servicio;

import estructuras.cola.NodoCola;
import estructuras.cola.QuequeCAE;
import estructuras.lista.NodoNota;
import modelo.Estado;
import modelo.Nota;
import modelo.Ticket;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.TreeMap;
import java.nio.charset.StandardCharsets;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;

public class PersistenciaService {

    private static final String RUTA_TICKETS = "data_tickets.csv";
    private static final String RUTA_NOTAS = "data_notas.csv";
    private static final String RUTA_CONTADORES = "data_contadores.csv";
    private static final String DELIMITADOR = ";";

    public void guardarDatos(GestorTickets gestor) {
        System.out.println("Guardando datos en archivos CSV (UTF-8)...");

        try (PrintWriter contWriter = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(RUTA_CONTADORES), StandardCharsets.UTF_8))) {

            contWriter.println("NextTicketId");
            contWriter.println(Ticket.getNextGeneradorId());
        } catch (IOException e) {
            System.err.println("Error fatal al guardar contadores: " + e.getMessage());
        }

        try (PrintWriter ticketWriter = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(RUTA_TICKETS), StandardCharsets.UTF_8));
             PrintWriter notaWriter = new PrintWriter(new OutputStreamWriter(
                     new FileOutputStream(RUTA_NOTAS), StandardCharsets.UTF_8))) {

            // Escribir cabeceras para las columnas
            ticketWriter.println("ID" + DELIMITADOR + "NombreCliente" + DELIMITADOR + "Estado" + DELIMITADOR + "EsUrgente" + DELIMITADOR + "NextNotaId" + DELIMITADOR + "FechaFinalizacion");
            notaWriter.println("TicketID" + DELIMITADOR + "NotaID" + DELIMITADOR + "FechaHora" + DELIMITADOR + "Texto");

            QuequeCAE[] colas = {gestor.getColaNormal(), gestor.getColaUrgente()};

            for (QuequeCAE cola : colas) {
                NodoCola actual = cola.getFrente();
                while (actual != null) {
                    escribirTicketYNotas(actual.getDato(), ticketWriter, notaWriter);
                    actual = actual.getSiguiente();
                }
            }
            for (Ticket t : gestor.getTicketsFinalizados().values()) {
                escribirTicketYNotas(t, ticketWriter, notaWriter);
            }
            if (gestor.getTicketEnAtencion() != null) {
                escribirTicketYNotas(gestor.getTicketEnAtencion(), ticketWriter, notaWriter);
            }

            System.out.println("Datos guardados exitosamente.");

        } catch (IOException e) {
            System.err.println("Error al guardar datos en CSV: " + e.getMessage());
        }
    }

    private void escribirTicketYNotas(Ticket t, PrintWriter ticketWriter, PrintWriter notaWriter) {
        String fechaFinalStr = "null";
        if (t.getFechaFinalizacion() != null) {
            fechaFinalStr = t.getFechaFinalizacion().toString();
        }

        ticketWriter.println(
                t.getId() + DELIMITADOR +
                        limpiarTexto(t.getNombreCliente()) + DELIMITADOR +
                        t.getEstado().name() + DELIMITADOR +
                        t.esUrgente() + DELIMITADOR +
                        t.getNextNotaId() + DELIMITADOR +
                        fechaFinalStr
        );

        NodoNota notaActual = t.getListaNotas().getCabeza();
        while (notaActual != null) {
            Nota n = notaActual.getDato();
            notaWriter.println(
                    t.getId() + DELIMITADOR +
                            n.id() + DELIMITADOR +
                            n.fechaHora().toString() + DELIMITADOR +
                            limpiarTexto(n.texto())
            );
            notaActual = notaActual.getSiguiente();
        }
    }

    private String limpiarTexto(String texto) {
        if (texto == null) return "";
        return texto.replace(DELIMITADOR, "").replace("\n", " ").replace("\r", " ");
    }

    public void cargarDatos(GestorTickets gestor) {
        System.out.println("Cargando datos desde archivos CSV (UTF-8)...");
        gestor.limpiarTodo();

        String linea;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(RUTA_CONTADORES), StandardCharsets.UTF_8))) {

            br.readLine(); // Omitir las columnas
            linea = br.readLine();
            if (linea != null) {
                Ticket.setGeneradorId(Integer.parseInt(linea.trim()));
                System.out.println("Contador de Tickets reiniciado a: " + linea);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Archivo de contadores no encontrado. Se iniciará en 1.");
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error al cargar contadores: " + e.getMessage());
        }

        TreeMap<Integer, Ticket> ticketsCargados = new TreeMap<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(RUTA_TICKETS), StandardCharsets.UTF_8))) {

            String cabecera = br.readLine();
            boolean tieneFechaFinal = cabecera != null && cabecera.contains("FechaFinalizacion");
            if (!tieneFechaFinal) {
                System.out.println("Advertencia: Archivo 'data_tickets.csv' antiguo detectado. Fechas de finalización no se cargarán.");
            }

            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(DELIMITADOR);
                if (datos.length < 5) continue;
                try {
                    int id = Integer.parseInt(datos[0]);
                    String nombre = datos[1];
                    Estado estado = Estado.fromString(datos[2]);
                    boolean esUrgente = Boolean.parseBoolean(datos[3]);
                    int nextNotaId = Integer.parseInt(datos[4]);
                    LocalDateTime fechaFinal = null;
                    if (tieneFechaFinal && datos.length > 5 && !datos[5].equals("null")) {
                        try {
                            fechaFinal = LocalDateTime.parse(datos[5]);
                        } catch (DateTimeParseException e) {
                            // Ignorar fecha malformada
                        }
                    }
                    Ticket t = new Ticket(id, nombre, estado, esUrgente, nextNotaId, fechaFinal);
                    ticketsCargados.put(id, t);
                } catch (NumberFormatException e) {
                    System.err.println("Error al parsear línea de ticket (se omite): " + linea);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Archivo de tickets no encontrado. Iniciando sistema vacío.");
            return;
        } catch (IOException e) {
            System.err.println("Error al leer archivo de tickets: " + e.getMessage());
            return;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(RUTA_NOTAS), StandardCharsets.UTF_8))) {

            br.readLine(); // Omitir las columnas
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(DELIMITADOR);
                if (datos.length < 4) continue;
                try {
                    int ticketId = Integer.parseInt(datos[0]);
                    int notaId = Integer.parseInt(datos[1]);
                    LocalDateTime fecha = LocalDateTime.parse(datos[2]);
                    String texto = datos[3];
                    Ticket ticketPadre = ticketsCargados.get(ticketId);
                    if (ticketPadre != null) {
                        ticketPadre.agregarNota(notaId, texto, fecha);
                    }
                } catch (NumberFormatException | DateTimeParseException e) {
                    System.err.println("Error al parsear línea de nota (se omite): " + linea);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Archivo de notas no encontrado.");
        } catch (IOException e) {
            System.err.println("Error al leer archivo de notas: " + e.getMessage());
        }

        // Traer los cambios
        int ticketsEnAtencion = 0;
        for (Ticket t : ticketsCargados.values()) {
            switch (t.getEstado()) {
                case EN_COLA: gestor.getColaNormal().enqueque(t); break;
                case URGENTE: gestor.getColaUrgente().enqueque(t); break;
                case PENDIENTE_DOCS: case EN_PROCESO:
                    if (t.esUrgente()) gestor.getColaUrgente().enqueque(t);
                    else gestor.getColaNormal().enqueque(t);
                    break;
                case COMPLETADO: gestor.getTicketsFinalizados().put(t.getId(), t); break;
                case EN_ATENCION:
                    if (ticketsEnAtencion == 0) {
                        gestor.setTicketEnAtencion(t);
                        ticketsEnAtencion++;
                    } else {
                        System.err.println("Conflicto: Múltiples tickets 'EN_ATENCION'. Re-encolando Ticket #" + t.getId());
                        if (t.esUrgente()) gestor.getColaUrgente().enqueque(t);
                        else gestor.getColaNormal().enqueque(t);
                    }
                    break;
            }
        }
        System.out.println("Carga de datos completada. " + ticketsCargados.size() + " tickets cargados.");
    }
}
