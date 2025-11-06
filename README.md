# proyecto-unidad1-CAE

### Aplicación Java basada en estructuras de datos para la administración, seguimiento y control de tickets mediante acciones reversibles.

## Integrantes del grupo

- Santiago Villamagua
- Pedro Jimenez
- Steven Jumbo
- Yandri Piscocama
- Anderson Coello

**Materia:** Estructura de Datos 
**Docente:** Andrés Roberto Navas Castellanos
**Ciclo:** 3A 
  
---

## Descripción general
El proyecto implementa un **sistema de gestión de tickets** que permite registrar, atender y dar seguimiento a solicitudes clasificadas por prioridad.  
El sistema hace uso de estructuras de datos clásicas (listas enlazadas, colas FIFO y pilas) y demuestra operaciones de **undo/redo**, **persistencia** y **consultas dinámicas**.

Su diseño se basa en un enfoque **iterativo** que avanza desde la creación del núcleo técnico hasta la integración de reglas, persistencia y reportes.

---

## Desarrollo por iteraciones

### Iteración 1 – Núcleo técnico
**Objetivo:** implementar el modelo de dominio y las estructuras fundamentales.

- Modelo de dominio:
  - `Ticket`, `Nota` (lista enlazada simple), `Accion` (undo/redo)  
  - Catálogo de estados
- Lista enlazada simple (SLL):
  - Insertar al inicio  
  - Eliminar primera coincidencia  
  - Recorrer
- Colas FIFO:
  - Cola **normal**  
  - Cola **urgente**
- Pilas de **undo/redo** con acciones:
  - `ADD_NOTA`  
  - `DEL_NOTA`  
  - `SET_STATUS`
- Pruebas unitarias para cada estructura.
- Flujo mínimo de integración:  
  *llegar → atender → notas/estados → undo/redo → terminar.*

---

###  Iteración 2 – Reglas y persistencia
**Objetivo:** aplicar reglas de negocio y asegurar la persistencia de datos.

- Validación de transiciones de estado:
- Persistencia en archivos de texto UTF-8:
- Guardado automático de colas, histórico y notas.
- Carga de datos al iniciar el programa.
- Pruebas de integración reproducibles con datos persistidos.

---

### 📊 Iteración 3 – Consultas, reportes y prioridad
**Objetivo:** ampliar las funcionalidades del sistema con consultas y reportes.

- Consultas:
- Tickets pendientes (por tipo) y Tickets atendidos (por estado final)
- Filtracion segun estado
- Reportes:
- **Top-k** por número de notas (recorrido SLL)
- Exportación de resultados a `.txt` y `.csv`
- Validación de prioridad:
- Si hay tickets **urgentes**, se atienden antes que los **normales**
- Prueba de integración final con guion reproducible para autoverificación.

---

# Sistema CAE - Centro de Atención al Estudiante

## 💡 ¿Qué es el Sistema CAE?
El **Sistema CAE** es una herramienta de consola para gestionar los casos y trámites de los estudiantes.  
Te permite **registrar, procesar y finalizar tickets**, llevando un historial completo de todas las acciones realizadas.  

Incluye la posibilidad de **deshacer** o **rehacer** acciones, para que nunca pierdas información importante o puedas corregir errores fácilmente.

---

## ⚙️ Cómo funciona Undo/Redo
Cada acción que realizas (agregar nota, cambiar estado, finalizar un caso) se guarda automáticamente.  
- 🔄 **Deshacer (Undo):** Revierte la última acción.  
- 🔁 **Rehacer (Redo):** Vuelve a aplicar una acción que habías deshecho.  

Así puedes probar cambios o corregir errores sin riesgo.

---

## 📝 Estados de los tickets
Cada ticket pasa por diferentes etapas según su progreso:

| Estado | Qué significa |
|--------|---------------|
| EN_COLA | El ticket acaba de llegar y espera su turno. |
| URGENTE | Caso prioritario que debe atenderse primero. |
| EN_ATENCIÓN | El operador está atendiendo el caso. |
| EN_PROCESO | Se están revisando documentos o completando formularios. |
| PENDIENTE_DOCS | Se espera información o documentos del estudiante. |
| COMPLETADO | El caso está cerrado y registrado en el historial. |

> ⚠️ Una vez finalizado (COMPLETADO o PENDIENTE_DOCS), el ticket **no se puede modificar**.

---

## 🚨 Casos especiales
- Intentar atender un ticket cuando no hay casos → se mostrará un mensaje.
- No se puede atender más de un ticket a la vez.
- Intentar deshacer una acción cuando no hay acciones anteriores → se avisará.
- Eliminar una nota o consultar un ticket inexistente → muestra un mensaje de error.

---

## 🚀 Cómo usarlo
Ejecuta `SistemaCAE` y verás el menú principal:

### 1. Recepción de Nuevo Caso
- Ingresa el nombre del estudiante o la descripción del trámite.
- Selecciona prioridad: Normal o Urgente.
- El ticket se añade automáticamente a la cola.

### 2. Consultar Casos en Espera
- Muestra todos los tickets pendientes con ID, nombre y estado.

### 3. Iniciar Atención
- Toma el primer ticket en la cola según la prioridad.
- Cambia su estado a **EN_ATENCIÓN**.
- Desde aquí puedes registrar notas, cambiar el estado o finalizar el caso.

### 4. Gestionar Caso en Atención
Submenú para el ticket actual:
- **Registrar Nota:** Añade observaciones del caso.
- **Eliminar Nota:** Borra notas existentes.
- **Cambiar Estado:** Actualiza el progreso del ticket.
- **Deshacer / Rehacer:** Revierte o reaplica la última acción.
- **Finalizar Caso:** Cierra el ticket y lo archiva en el historial.
- **Volver al menú principal.**

### 5. Consultar Historial
- Busca un ticket finalizado por su ID.
- Muestra toda la información y notas registradas durante el proceso.

### 6. Reporte Top-K
- Muestra los **tickets con más notas** registradas.

### 7. Deshacer Acción Global (Undo)
- Revierte la última acción realizada en todo el sistema.

### 8. Rehacer Acción Global (Redo)
- Reaplica una acción que habías deshecho.

### 9. Cargar Datos desde Archivo
- Recupera tickets y notas desde archivos guardados previamente.
- ⚠️ Advertencia: los cambios actuales se perderán si confirmas la operación.

### 0. Salir
- Pregunta si quieres guardar los cambios antes de cerrar.
- Guarda todos los tickets y notas en archivos para mantener el historial.

---

## 💾 Guardado y persistencia
El sistema guarda los tickets y sus notas en archivos CSV:
- `data_tickets.csv` → Tickets pendientes y finalizados.
- `data_notas.csv` → Notas de cada ticket.
- `data_contadores.csv` → IDs de tickets y notas.

Esto permite **cerrar el sistema y retomarlo luego** sin perder información.

---

## 🎯 Resumen
El Sistema CAE te ayuda a:
- Gestionar los tickets de manera organizada.
- Registrar notas y estados de cada caso.
- Deshacer o rehacer acciones cuando lo necesites.
- Consultar historial y generar reportes rápidos.
- Guardar y cargar datos fácilmente para continuar tu trabajo.

¡Todo desde la comodidad de la consola!


## 🚀 Instalación y ejecución

1. Clona este repositorio:
   ```bash
   git clone https://github.com/AndersonC15/proyecto-unidad1-CAE.git
