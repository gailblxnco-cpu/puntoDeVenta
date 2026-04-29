package abd.puntodeventa;

/**
 * Esta clase ahora solo actúa como la "Memoria de la Caja Registradora".
 * Mantiene los datos del cliente activo durante una venta.
 * El inventario, los usuarios y los registros ya se manejan 100% en MySQL.
 */
public class InventarioGlobal {

    // VARIABLES DE LA VENTA ACTUAL
    private static Cliente clienteActivo = null;

    // Bandera para saber si se aplicará el descuento de 0.1 por punto
    private static boolean usarPuntosEnVenta = false;

    // --- MÉTODOS DE ACCESO ---

    public static Cliente getClienteActivo() {
        return clienteActivo;
    }

    public static void setClienteActivo(Cliente cliente) {
        clienteActivo = cliente;
    }

    public static boolean isUsarPuntosEnVenta() {
        return usarPuntosEnVenta;
    }

    public static void setUsarPuntosEnVenta(boolean usar) {
        usarPuntosEnVenta = usar;
    }
}