package abd.puntodeventa;

import javafx.beans.property.*;

public class Usuario {
    private final IntegerProperty id;
    private final StringProperty username;
    private final StringProperty password;
    private final StringProperty rol;
    private final BooleanProperty activo;

    public Usuario(int id, String username, String password, String rol, boolean activo) {
        this.id = new SimpleIntegerProperty(id);
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
        this.rol = new SimpleStringProperty(rol);
        this.activo = new SimpleBooleanProperty(activo);
    }

    // --- Getters normales ---
    public int getId() { return id.get(); }
    public String getUsername() { return username.get(); }
    public String getPassword() { return password.get(); }
    public String getRol() { return rol.get(); }
    public boolean isActivo() { return activo.get(); }

    // --- Getters para las columnas de la tabla JavaFX ---
    public StringProperty usernameProperty() { return username; }
    public StringProperty rolProperty() { return rol; }
}