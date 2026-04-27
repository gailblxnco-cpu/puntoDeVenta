package abd.puntodeventa;

import javafx.beans.property.*;

public class Usuario {
    private final StringProperty username;
    private final StringProperty password;
    private final StringProperty rol; // "ADMIN" o "CAJERO"

    public Usuario(String username, String password, String rol) {
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
        this.rol = new SimpleStringProperty(rol);
    }

    public String getUsername() { return username.get(); }
    public String getPassword() { return password.get(); }
    public String getRol() { return rol.get(); }

    // Getters para Properties (útiles para tablas)
    public StringProperty usernameProperty() { return username; }
    public StringProperty rolProperty() { return rol; }
}