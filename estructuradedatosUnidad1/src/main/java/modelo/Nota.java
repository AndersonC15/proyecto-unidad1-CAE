package modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record Nota (int id, String texto, LocalDateTime fechaHora){
    public Nota(int id, String texto) {
        this(id, texto, LocalDateTime.now());
    }

    //usamos record ya que es una clase basica e inmmutable

    @Override
    public String toString() {
        return String.format("Nota ID: %-3d | Texto: %-20s | Fecha: %s",
                id, texto, fechaHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }
}