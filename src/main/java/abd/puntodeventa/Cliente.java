package abd.puntodeventa;

import javafx.beans.property.*;

public class Cliente {
    private final IntegerProperty idCliente;
    private final StringProperty nombre;
    private final StringProperty telefono;
    private final IntegerProperty puntos;

    public Cliente(int id, String nombre, String telefono, int puntos) {
        this.idCliente = new SimpleIntegerProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.telefono = new SimpleStringProperty(telefono);
        this.puntos = new SimpleIntegerProperty(puntos);
    }

    // --- Getters normales ---
    public int getIdCliente() { return idCliente.get(); }
    public String getNombre() { return nombre.get(); }
    public String getTelefono() { return telefono.get(); }
    public int getPuntos() { return puntos.get(); }

    // --- Setters ---
    public void setPuntos(int nuevosPuntos) { this.puntos.set(nuevosPuntos); }

    // --- Properties para JavaFX ---
    public IntegerProperty idClienteProperty() { return idCliente; }
    public StringProperty nombreProperty() { return nombre; }
    public StringProperty telefonoProperty() { return telefono; }
    public IntegerProperty puntosProperty() { return puntos; }
}