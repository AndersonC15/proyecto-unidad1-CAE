package servicio;

import estructuras.cola.NodoCola;
import estructuras.cola.QuequeCAE;
import estructuras.lista.ListaNotas;
import estructuras.lista.NodoNota;
import modelo.Estado;
import modelo.Nota;
import modelo.Ticket;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.TreeMap;

/**
 * Gestiona la persistencia de datos en archivos CSV (Request #8) [cite: 46]
 */
public class PersistenciaService {

    // Rutas de los archivos CSV
    private static final String RUTA_TICKETS = "data_tickets.csv";
    private static final String RUTA_NOTAS = "data_notas.csv";
    private static final String RUTA_CONTADORES = "data_contadores.csv";
    private static final String DELIMITADOR = ";"; // Usar ; para evitar problemas con comas en el texto

    public void guardarDatos(GestorTickets gestor) {
        System.out.println("Guardando datos en archivos CSV...");

        // Guardar Contadores
        try (PrintWriter contWriter = new PrintWriter(new FileWriter(RUTA_CONTADORES))) {
            contWriter.println("NextTicketId");
            contWriter.println(Ticket.getNextGeneradorId());
        } catch (IOException e) {
            System.err.println("Error fatal al guardar contadores: " + e.getMessage());
            // Continuar para intentar guardar el resto...
        }

        // Guardar Tickets y Notas
        try (PrintWriter ticketWriter = new PrintWriter(new FileWriter(RUTA_TICKETS));
             PrintWriter notaWriter = new PrintWriter(new FileWriter(RUTA_NOTAS))) {

            // Escribir cabeceras
            ticketWriter.println("ID" + DELIMITADOR + "NombreCliente" + DELIMITADOR + "Estado" + DELIMITADOR + "EsUrgente" + DELIMITADOR + "NextNotaId");
            notaWriter.println("TicketID" + DELIMITADOR + "NotaID" + DELIMITADOR + "FechaHora" + DELIMITADOR + "Texto");

            // Crear una lista temporal de todas las colas para no duplicar código
            QuequeCAE[] colas = {gestor.getColaNormal(), gestor.getColaUrgente()};

            // 1. Guardar tickets en colas
            for (QuequeCAE cola : colas) {
                NodoCola actual = cola.getFrente();
                while (actual != null) {
                    escribirTicketYNotas(actual.getDato(), ticketWriter, notaWriter);
                    actual = actual.getSiguiente();
                }
            }

            // 2. Guardar tickets finalizados
            for (Ticket t : gestor.getTicketsFinalizados().values()) {
                escribirTicketYNotas(t, ticketWriter, notaWriter);
            }

            // 3. Guardar ticket en atención (si existe)
            if (gestor.getTicketEnAtencion() != null) {
                escribirTicketYNotas(gestor.getTicketEnAtencion(), ticketWriter, notaWriter);
            }

            System.out.println("Datos guardados exitosamente.");

        } catch (IOException e) {
            System.err.println("Error al guardar datos en CSV: " + e.getMessage());
        }
    }

    private void escribirTicketYNotas(Ticket t, PrintWriter ticketWriter, PrintWriter notaWriter) {
        // Guardar Ticket
        ticketWriter.println(
                t.getId() + DELIMITADOR +
                        limpiarTexto(t.getNombreCliente()) + DELIMITADOR +
                        t.getEstado().name() + DELIMITADOR + // Guardar el nombre del Enum
                        t.esUrgente() + DELIMITADOR +
                        t.getNextNotaId()
        );

        // Guardar Notas (recorriendo la SLL)
        NodoNota notaActual = t.getListaNotas().getCabeza();
        while (notaActual != null) {
            Nota n = notaActual.getDato();
            notaWriter.println(
                    t.getId() + DELIMITADOR +
                            n.id() + DELIMITADOR +
                            n.fechaHora().toString() + DELIMITADOR + // ISO DateTime
                            limpiarTexto(n.texto())
            );
            notaActual = notaActual.getSiguiente();
        }
    }

    // Limpia el texto para CSV (quita el delimitador y saltos de línea)
    private String limpiarTexto(String texto) {
        if (texto == null) return "";
        return texto.replace(DELIMITADOR, "").replace("\n", " ").replace("\r", " ");
    }

    public void cargarDatos(GestorTickets gestor) {
        System.out.println("Cargando datos desde archivos CSV...");

        // 1. Limpiar estado actual
        gestor.limpiarTodo();

        String linea;

        // 2. Cargar Contadores
        try (BufferedReader br = new BufferedReader(new FileReader(RUTA_CONTADORES))) {
            br.readLine(); // Omitir cabecera
            linea = br.readLine();
            if (linea != null) {
                Ticket.setGeneradorId(Integer.parseInt(linea.trim()));
                System.out.println("Contador de Tickets reiniciado a: " + linea);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Archivo de contadores no encontrado. Se iniciará en 1.");
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error al cargar contadores, se usará el valor por defecto: " + e.getMessage());
        }

        // Mapa temporal para almacenar tickets mientras se leen las notas
        TreeMap<Integer, Ticket> ticketsCargados = new TreeMap<>();

        // 3. Cargar Tickets
        try (BufferedReader br = new BufferedReader(new FileReader(RUTA_TICKETS))) {
            br.readLine(); // Omitir cabecera

            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(DELIMITADOR);
                if (datos.length < 5) continue; // Línea malformada

                try {
                    int id = Integer.parseInt(datos[0]);
                    String nombre = datos[1];
                    Estado estado = Estado.fromString(datos[2]);
                    boolean esUrgente = Boolean.parseBoolean(datos[3]);
                    int nextNotaId = Integer.parseInt(datos[4]);

                    Ticket t = new Ticket(id, nombre, estado, esUrgente, nextNotaId);
                    ticketsCargados.put(id, t);
                } catch (NumberFormatException e) {
                    System.err.println("Error al parsear línea de ticket (se omite): " + linea);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Archivo de tickets no encontrado. Iniciando sistema vacío.");
            return; // No hay nada que cargar
        } catch (IOException e) {
            System.err.println("Error al leer archivo de tickets: " + e.getMessage());
            return;
        }

        // 4. Cargar Notas
        try (BufferedReader br = new BufferedReader(new FileReader(RUTA_NOTAS))) {
            br.readLine(); // Omitir cabecera

            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(DELIMITADOR);
                if (datos.length < 4) continue;

                try {
                    int ticketId = Integer.parseInt(datos[0]);
                    int notaId = Integer.parseInt(datos[1]);
                    LocalDateTime fecha = LocalDateTime.parse(datos[2]); // ISO DateTime
                    String texto = datos[3];

                    Ticket ticketPadre = ticketsCargados.get(ticketId);
                    if (ticketPadre != null) {
                        // Añadir la nota al ticket
                        ticketPadre.agregarNota(notaId, texto, fecha);
                    } else {
                        System.err.println("Nota huérfana encontrada (Ticket ID " + ticketId + "), se omite.");
                    }
                } catch (NumberFormatException | DateTimeParseException e) {
                    System.err.println("Error al parsear línea de nota (se omite): " + linea);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Archivo de notas no encontrado. Se cargarán tickets sin notas.");
        } catch (IOException e) {
            System.err.println("Error al leer archivo de notas: " + e.getMessage());
        }

        // 5. Repoblar el Gestor
        int ticketsEnAtencion = 0;
        for (Ticket t : ticketsCargados.values()) {
            switch (t.getEstado()) {
                case EN_COLA:
                    gestor.getColaNormal().enqueque(t);
                    break;
                case URGENTE:
                    gestor.getColaUrgente().enqueque(t);
                    break;
                case PENDIENTE_DOCS:
                case EN_PROCESO:
                    // Si el estado es pendiente o en proceso, se re-encola (Request #4)
                    if (t.esUrgente()) gestor.getColaUrgente().enqueque(t);
                    else gestor.getColaNormal().enqueque(t);
                    break;
                case COMPLETADO:
                    gestor.getTicketsFinalizados().put(t.getId(), t);
                    break;
                case EN_ATENCION:
                    // Solo puede haber un ticket en atención
                    if (ticketsEnAtencion == 0) {
                        gestor.setTicketEnAtencion(t);
                        ticketsEnAtencion++;
                    } else {
                        // Si se guardaron varios en atención (error), se re-encolan
                        System.err.println("Conflicto: Múltiples tickets guardados como 'EN_ATENCION'. Re-encolando Ticket #" + t.getId());
                        if (t.esUrgente()) gestor.getColaUrgente().enqueque(t);
                        else gestor.getColaNormal().enqueque(t);
                    }
                    break;
            }
        }

        System.out.println("Carga de datos completada. " + ticketsCargados.size() + " tickets cargados.");
    }
}