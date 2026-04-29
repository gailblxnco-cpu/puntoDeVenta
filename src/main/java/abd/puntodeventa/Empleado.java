package abd.puntodeventa;

import javafx.beans.property.*;

public class Empleado {
    private final IntegerProperty idEmpleado;
    private final StringProperty nombre;
    private final StringProperty rol;
    private final StringProperty contrasena;

    public Empleado(int idEmpleado, String nombre, String rol, String contrasena) {
        this.idEmpleado = new SimpleIntegerProperty(idEmpleado);
        this.nombre = new SimpleStringProperty(nombre);
        this.rol = new SimpleStringProperty(rol);
        this.contrasena = new SimpleStringProperty(contrasena);
    }

    // --- Getters tradicionales ---
    public int getIdEmpleado() { return idEmpleado.get(); }
    public String getNombre() { return nombre.get(); }
    public String getRol() { return rol.get(); }
    public String getContrasena() { return contrasena.get(); }

    // --- Setters ---
    public void setNombre(String nombre) { this.nombre.set(nombre); }
    public void setRol(String rol) { this.rol.set(rol); }
    public void setContrasena(String contrasena) { this.contrasena.set(contrasena); }

    // --- Properties para JavaFX (Para el TableView) ---
    public IntegerProperty idEmpleadoProperty() { return idEmpleado; }
    public StringProperty nombreProperty() { return nombre; }
    public StringProperty rolProperty() { return rol; }
    public StringProperty contrasenaProperty() { return contrasena; }
}