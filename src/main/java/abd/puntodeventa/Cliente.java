package abd.puntodeventa;

import javafx.beans.property.*;

public class Cliente {
    private final StringProperty nombre;
    private final StringProperty telefono;
    private final IntegerProperty puntos;

    public Cliente(String nombre, String telefono, int puntos) {
        this.nombre = new SimpleStringProperty(nombre);
        this.telefono = new SimpleStringProperty(telefono);
        this.puntos = new SimpleIntegerProperty(puntos);
    }

    public StringProperty nombreProperty() { return nombre; }
    public StringProperty telefonoProperty() { return telefono; }
    public IntegerProperty puntosProperty() { return puntos; }

    public String getNombre() { return nombre.get(); }
    public String getTelefono() { return telefono.get(); }
    public int getPuntos() { return puntos.get(); }
    public void setPuntos(int nuevosPuntos) { puntos.set(nuevosPuntos); }
}