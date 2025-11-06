package consola;

import modelo.Estado;
import modelo.Ticket;
import servicio.GestorTickets;
import servicio.PersistenciaService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class SistemaCAE {

    private static final GestorTickets gestor = new GestorTickets();
    private static final PersistenciaService persistencia = new PersistenciaService();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Iniciando Sistema CAE...");
        persistencia.cargarDatos(gestor);
        ejecutarMenuPrincipal();
    }


    private static void ejecutarMenuPrincipal() {
        int opcion;
        boolean salir = false;

        while (!salir) {
            mostrarMenu();
            try {
                opcion = scanner.nextInt();
                scanner.nextLine();

                switch (opcion) {
                    case 1: recibirNuevoCaso(); break;
                    case 2: gestor.listarCasosEnEspera(); break;
                    case 3: iniciarAtencion(); break;
                    case 4: gestorAtencion(); break; // Llama al gestor modificado
                    case 5: gestionarConsultaHistorial(); break;
                    case 6: ejecutarReportes(); break;
                    case 7: deshacerGlobal(); break;
                    case 8: rehacerGlobal(); break;
                    case 9: cargarDatosSistema(); break;
                    case 0:
                        promptGuardarDatos();
                        salir = true;
                        break;
                    default:
                        System.err.println("Opción no válida. Intente de nuevo.");
                }
            } catch (InputMismatchException e) {
                System.err.println("Entrada inválida. Por favor, ingrese un número.");
                scanner.nextLine();
            }
        }
        scanner.close();
        System.out.println("\nSistema CAE finalizado. ¡Adiós!");
    }

    private static void mostrarMenu() {
        System.out.println("\n===== CENTRO DE ATENCIÓN AL ESTUDIANTE (CAE) =====");
        System.out.println("--- Gestión de Casos ---");
        System.out.println("1. Recepción de Nuevo Caso (Encolar)");
        System.out.println("2. Consultar Casos en Espera");
        System.out.println("3. Iniciar Atención del Siguiente Caso (Prioridad Urgente)");
        System.out.println("4. Gestionar Caso EN ATENCIÓN");
        System.out.println("5. Consultar Historial de Casos Finalizados");
        System.out.println("--- Administración y Reportes ---");
        System.out.println("6. Ejecutar Reporte Top-K (por N° de Notas)");
        System.out.println("7. Deshacer (Crear/Atender Ticket) (" + gestor.undoGlobalCount() + ")");
        System.out.println("8. Rehacer (Crear/Atender Ticket) (" + gestor.redoGlobalCount() + ")");
        System.out.println("9. [!] Cargar Datos (Sobrescribe cambios actuales)");
        System.out.println("0. Salir (y Guardar Cambios)");
        System.out.print("Seleccione una opción: ");
    }

    /**
     * MODIFICADO (Paso 8 de tu flujo)
     * Añade la llamada a limpiarHistorialTicket() al final.
     */
    private static void gestorAtencion() {
        // 1. Validación de entrada
        if (gestor.getTicketEnAtencion() == null && gestor.undoTicketCount() == 0 && gestor.redoTicketCount() == 0) {
            System.err.println("\nError: No hay ningún ticket en atención activa.");
            System.err.println("Use la Opción 3 para iniciar la atención del siguiente caso.");
            return;
        }

        boolean volverAlMenuPrincipal = false;

        // 2. Bucle de estado (permanece aquí)
        while (!volverAlMenuPrincipal) {

            try {
                if (gestor.getTicketEnAtencion() != null) {
                    // --- MODO 1: Ticket Activo ---
                    volverAlMenuPrincipal = manejarMenuConTicket();
                } else {
                    // --- MODO 2: Sin Ticket (Pero con historial) ---
                    volverAlMenuPrincipal = manejarMenuSinTicket();
                }
            } catch (InputMismatchException e) {
                System.err.println("Entrada inválida. Ingrese un número.");
                scanner.nextLine(); // Limpiar buffer
            }

        } // fin while

        // --- INICIO DE CORRECCIÓN (Paso 8) ---
        // Al salir del bucle (Opción 0), limpiamos el historial del ticket.
        gestor.limpiarHistorialTicket();
        // --- FIN DE CORRECCIÓN ---

        System.out.println("Volviendo al Menú Principal...");
    }

    /**
     * Helper (Sin cambios)
     * Muestra y maneja el menú cuando HAY un ticket activo.
     */
    private static boolean manejarMenuConTicket() throws InputMismatchException {
        Ticket actual = gestor.getTicketEnAtencion();
        System.out.println("\n--- GESTIÓN DEL TICKET #" + actual.getId() + " (" + actual.getEstado() + ") ---");
        System.out.println("Cliente: " + actual.getNombreCliente());
        System.out.println("Notas actuales:");
        actual.getListaNotas().mostrar();
        System.out.println("------------------------------------------");
        System.out.println("1. Registrar Nota/Observación");
        System.out.println("2. Eliminar Nota (por ID de nota)");
        System.out.println("3. Cambiar Estado del Ticket");
        System.out.println("4. Deshacer (Nota/Estado/Finalizar) (" + gestor.undoTicketCount() + ")");
        System.out.println("5. Rehacer (Nota/Estado/Finalizar) (" + gestor.redoTicketCount() + ")");
        System.out.println("6. [!] FINALIZAR/RE-ENCOLAR Caso");
        System.out.println("0. Volver al Menú Principal");
        System.out.print("Seleccione una opción: ");

        int opcion = scanner.nextInt();
        scanner.nextLine();

        switch (opcion) {
            case 1: registrarNotaEnAtencion(); break;
            case 2: eliminarNotaEnAtencion(); break;
            case 3: cambiarEstadoEnAtencion(); break;
            case 4: gestor.deshacerAccionTicket(); break;
            case 5: gestor.rehacerAccionTicket(); break;
            case 6:
                finalizarCasoEnAtencion();
                // Ya no cambia 'volverAlMenuPrincipal', se queda en el bucle
                break;
            case 0:
                return true; // Salir al menú principal
            default:
                System.err.println("Opción no válida.");
        }
        return false; // Permanecer en el menú de gestión
    }

    /**
     * Helper (Sin cambios)
     * Muestra y maneja el menú cuando NO hay ticket activo (post-finalización).
     */
    private static boolean manejarMenuSinTicket() throws InputMismatchException {
        System.out.println("\n--- GESTIÓN (Ticket No Activo) ---");
        System.out.println("El ticket no está en atención, pero hay acciones del último");
        System.out.println("ticket atendido que se pueden deshacer o rehacer (ej. Finalizar).");

        System.out.println("\nOpciones disponibles:");
        System.out.println("4. Deshacer última acción (Ticket) (" + gestor.undoTicketCount() + ")");
        System.out.println("5. Rehacer última acción (Ticket) (" + gestor.redoTicketCount() + ")");
        System.out.println("0. Volver al Menú Principal");
        System.out.print("Seleccione una opción: ");

        int opcion = scanner.nextInt();
        scanner.nextLine();

        switch (opcion) {
            case 4: // Deshacer
                if (gestor.deshacerAccionTicket()) {
                    if (gestor.getTicketEnAtencion() != null) {
                        System.out.println("Ticket #" + gestor.getTicketEnAtencion().getId() + " restaurado a 'EN ATENCIÓN'.");
                    }
                }
                break;
            case 5: // Rehacer
                if (gestor.rehacerAccionTicket()) {
                    if (gestor.getTicketEnAtencion() == null) {
                        System.out.println("Acción 'Finalizar/Re-encolar' rehecha.");
                    }
                }
                break;
            case 0:
                return true; // Salir al menú principal
            default:
                System.err.println("Opción no válida (Solo 4, 5 o 0).");
        }
        return false; // Permanecer en el menú de gestión
    }

    // --- Métodos de gestión de ticket (SIN CAMBIOS) ---

    private static void registrarNotaEnAtencion() {
        System.out.print("Escriba la observación (nota): ");
        String nota = scanner.nextLine();
        gestor.registrarNota(nota);
    }

    private static void eliminarNotaEnAtencion() {
        System.out.print("Ingrese ID de la Nota a eliminar: ");
        try {
            int id = scanner.nextInt();
            scanner.nextLine();
            gestor.eliminarNota(id);
        } catch (InputMismatchException e) {
            System.err.println("ID inválido. Debe ser un número.");
            scanner.nextLine();
        }
    }

    private static void cambiarEstadoEnAtencion() {
        System.out.println("\n--- Cambiar Estado del Ticket ---");
        Estado[] estados = {Estado.EN_PROCESO, Estado.PENDIENTE_DOCS, Estado.COMPLETADO};
        for (int i = 0; i < estados.length; i++) {
            System.out.println((i+1) + ". " + estados[i].toString());
        }
        System.out.print("Seleccione el nuevo estado: ");
        try {
            int indice = scanner.nextInt();
            scanner.nextLine();
            if (indice >= 1 && indice <= estados.length) {
                gestor.cambiarEstadoInterno(estados[indice - 1]);
            } else {
                System.err.println("Selección fuera de rango.");
            }
        } catch (InputMismatchException e) {
            System.err.println("Entrada inválida. Debe ser un número.");
            scanner.nextLine();
        }
    }

    private static boolean finalizarCasoEnAtencion() {
        System.out.println("\n--- Finalizando Gestión del Ticket ---");
        return gestor.finalizarCaso();
    }

    // --- Métodos de gestión de historial y reportes (SIN CAMBIOS) ---

    private static void gestionarConsultaHistorial() {
        if (!gestor.hayTicketsFinalizados()) {
            System.out.println("\nNo existen aún casos finalizados (COMPLETADO) en el historial.");
            return;
        }

        System.out.println("\n--- Consulta de Casos Finalizados ---");
        System.out.print("¿Desea filtrar por fecha? (S/N, N = Mostrar todos): ");
        String resp = scanner.nextLine();

        boolean resultadosEncontrados = false;

        if (resp.equalsIgnoreCase("S")) {
            LocalDate fechaDesde = pedirFecha("Ingrese fecha DESDE (dd/mm/aaaa): ");
            if (fechaDesde == null) {
                System.out.println("Operación cancelada.");
                return;
            }
            LocalDate fechaHasta = pedirFecha("Ingrese fecha HASTA (dd/mm/aaaa): ");
            if (fechaHasta == null) {
                System.out.println("Operación cancelada.");
                return;
            }
            if (fechaDesde.isAfter(fechaHasta)) {
                System.err.println("Error: La fecha 'DESDE' no puede ser posterior a la fecha 'HASTA'.");
                return;
            }
            System.out.println("\n--- Mostrando tickets finalizados entre " +
                    fechaDesde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " y " +
                    fechaHasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " ---");
            resultadosEncontrados = gestor.listarTicketsFinalizados(fechaDesde, fechaHasta);
        } else {
            gestor.listarTicketsFinalizados();
            resultadosEncontrados = gestor.hayTicketsFinalizados();
        }

        if (resultadosEncontrados) {
            pedirIdParaConsultar();
        }
    }

    private static void pedirIdParaConsultar() {
        System.out.print("\nIngrese ID del Ticket a consultar (de la lista anterior) (0 para volver): ");
        try {
            int id = scanner.nextInt();
            scanner.nextLine();
            if (id == 0) {
                System.out.println("Volviendo al menú principal.");
                return;
            }
            gestor.consultarHistorial(id);
        } catch (InputMismatchException e) {
            System.err.println("ID inválido. Debe ser un número.");
            scanner.nextLine();
        }
    }

    private static LocalDate pedirFecha(String prompt) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("[d/M/yyyy][dd/MM/yyyy]");
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            if (input == null || input.trim().isEmpty()) {
                System.err.println("La entrada no puede estar vacía.");
                continue;
            }
            try {
                LocalDate fecha = LocalDate.parse(input.trim(), dtf);
                return fecha;
            } catch (DateTimeParseException e) {
                System.err.println("Formato de fecha inválido. Use dd/mm/aaaa (ej. 05/11/2025). Intente de nuevo.");
            }
        }
    }

    private static void ejecutarReportes() {
        System.out.println("\n--- Reporte Top-K por N° de Notas ---");
        System.out.print("Ingrese el valor de 'K' (ej. 5 para Top-5): ");
        try {
            int k = scanner.nextInt();
            scanner.nextLine();
            gestor.ejecutarReporteTopK(k);
        } catch (InputMismatchException e) {
            System.err.println("Entrada inválida. Debe ser un número.");
            scanner.nextLine();
        }
    }

    private static void deshacerGlobal() {
        System.out.println("\n--- Deshacer Acción Global (Crear/Atender) ---");
        gestor.deshacerAccionGlobal();
    }

    private static void rehacerGlobal() {
        System.out.println("\n--- Rehacer Acción Global (Crear/Atender) ---");
        gestor.rehacerAccionGlobal();
    }

    private static void cargarDatosSistema() {
        System.err.println("\n--- [!] Cargar Datos desde Archivo ---");
        System.err.println("ADVERTENCIA: Esta acción borrará todo el progreso actual");
        System.err.println("que no haya sido guardado y lo reemplazará con los datos del archivo.");
        System.out.print("¿Está seguro que desea continuar? (S/N): ");
        String resp = scanner.nextLine();
        if (resp.equalsIgnoreCase("S")) {
            persistencia.cargarDatos(gestor);
        } else {
            System.out.println("Carga de datos cancelada.");
        }
    }

    private static void promptGuardarDatos() {
        System.out.print("\n¿Desea guardar los cambios realizados en el sistema? (S/N): ");
        String resp = scanner.nextLine();
        if (resp.equalsIgnoreCase("S")) {
            persistencia.guardarDatos(gestor);
        } else {
            System.out.println("Saliendo sin guardar cambios.");
        }
    }

    // --- Métodos 'helper' (que copiaste de la vez anterior)

    private static void recibirNuevoCaso() {
        System.out.println("\n--- Recepción de Nuevo Caso ---");
        System.out.print("Ingrese nombre o descripción del cliente/trámite: ");
        String cliente = scanner.nextLine();
        if (cliente.trim().isEmpty()) {
            System.err.println("Error: El nombre no puede estar vacío.");
            return;
        }
        boolean esUrgente = false;
        while (true) {
            System.out.print("¿Es un caso URGENTE? (S/N): ");
            String resp = scanner.nextLine();
            if (resp.equalsIgnoreCase("S")) {
                esUrgente = true;
                break;
            } else if (resp.equalsIgnoreCase("N")) {
                esUrgente = false;
                break;
            } else {
                System.err.println("Respuesta inválida. Ingrese 'S' o 'N'.");
            }
        }
        gestor.recibirNuevoCaso(cliente, esUrgente);
    }

    private static void iniciarAtencion() {
        gestor.iniciarAtencion();
    }
}