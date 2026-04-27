package abd.puntodeventa;

import javafx.beans.property.*;

public class Producto {
    private final IntegerProperty id;
    private final StringProperty nombre;
    private final DoubleProperty precio;
    private final IntegerProperty stock;

    public Producto(int id, String nombre, double precio, int stock) {
        this.id = new SimpleIntegerProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.precio = new SimpleDoubleProperty(precio);
        this.stock = new SimpleIntegerProperty(stock);
    }

    // Property getters (para el TableView)
    public IntegerProperty idProperty() { return id; }
    public StringProperty nombreProperty() { return nombre; }
    public DoubleProperty precioProperty() { return precio; }
    public IntegerProperty stockProperty() { return stock; }

    // Getters normales (para la lógica)
    public int getId() { return id.get(); }
    public String getNombre() { return nombre.get(); }
    public double getPrecio() { return precio.get(); }
    public int getStock() { return stock.get(); }
}