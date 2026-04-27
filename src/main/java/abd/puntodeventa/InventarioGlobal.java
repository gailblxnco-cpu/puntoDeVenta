package abd.puntodeventa;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class InventarioGlobal {


    // 1. VARIABLES DE PRODUCTOS
    private static final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private static int contadorId = 8;


    // 2. VARIABLES DE CLIENTES
    private static final ObservableList<Cliente> clientes = FXCollections.observableArrayList();
    private static Cliente clienteActivo = null;

    // Bandera para saber si se aplicará el descuento de 0.1 por punto
    private static boolean usarPuntosEnVenta = false;


    // 3. INICIALIZACIÓN DE DATOS
    static {
        // Cargar Catálogo Inicial de Café
        productos.add(new Producto(1, "Espresso", 30.0, 50));
        productos.add(new Producto(2, "Americano", 35.0, 60));
        productos.add(new Producto(3, "Latte", 45.0, 40));
        productos.add(new Producto(4, "Cappuccino", 50.0, 35));
        productos.add(new Producto(5, "Mocha", 55.0, 25));
        productos.add(new Producto(6, "Cold Brew", 60.0, 20));
        productos.add(new Producto(7, "Frappé", 65.0, 15));

        // Cargar Clientes de Prueba
        clientes.add(new Cliente("Juan Pérez", "5551234", 150));
        clientes.add(new Cliente("Aby", "8331234567", 500));
    }


    // 4. MÉTODOS DE ACCESO (Getters & Setters)

    // --- Productos ---
    public static ObservableList<Producto> getProductos() {
        return productos;
    }

    public static int getNextId() {
        return contadorId++;
    }

    // --- Clientes ---
    public static ObservableList<Cliente> getClientes() {
        return clientes;
    }

    public static Cliente getClienteActivo() {
        return clienteActivo;
    }

    public static void setClienteActivo(Cliente cliente) {
        clienteActivo = cliente;
    }

    // --- Lógica de Descuento ---
    public static boolean isUsarPuntosEnVenta() {
        return usarPuntosEnVenta;
    }

    public static void setUsarPuntosEnVenta(boolean usar) {
        usarPuntosEnVenta = usar;
    }
}