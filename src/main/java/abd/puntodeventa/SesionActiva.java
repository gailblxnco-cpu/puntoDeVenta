package abd.puntodeventa;

public class SesionActiva {
    private static int idEmpleadoLogueado;
    private static String nombreLogueado;
    private static String rolLogueado;

    // Métodos para establecer y obtener la sesión
    public static void setUsuario(int id, String nombre, String rol) {
        idEmpleadoLogueado = id;
        nombreLogueado = nombre;
        rolLogueado = rol;
    }

    public static int getIdEmpleado() { return idEmpleadoLogueado; }
    public static String getNombre() { return nombreLogueado; }
    public static String getRol() { return rolLogueado; }
}