package abd.puntodeventa;

public class SesionActiva {
    // Datos del cajero/gerente
    private static int idEmpleado = 0;
    private static String nombre = null;
    private static String rol = null;

    // Datos de la venta actual
    private static Cliente clienteActivo = null;
    private static boolean usarPuntosEnVenta = false;

    // --- MÉTODOS DE EMPLEADO ---
    public static void setUsuario(int id, String nom, String r) {
        idEmpleado = id;
        nombre = nom;
        rol = r;
    }
    public static int getIdEmpleado() { return idEmpleado; }
    public static String getNombre() { return nombre; }
    public static String getRol() { return rol; }

    // --- MÉTODOS DE CLIENTE Y PUNTOS ---
    public static Cliente getClienteActivo() { return clienteActivo; }
    public static void setClienteActivo(Cliente cliente) { clienteActivo = cliente; }
    public static boolean isUsarPuntosEnVenta() { return usarPuntosEnVenta; }
    public static void setUsarPuntosEnVenta(boolean usar) { usarPuntosEnVenta = usar; }
}