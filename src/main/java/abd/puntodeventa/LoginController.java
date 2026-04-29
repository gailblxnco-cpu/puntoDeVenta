package abd.puntodeventa;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    // Asegúrate de que estos fx:id coincidan con tu LoginView.fxml
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    @FXML
    public void validarIngreso() {
        String usuario = txtUsuario.getText();
        String password = txtPassword.getText();

        if (usuario.isEmpty() || password.isEmpty()) {
            lblError.setText("Por favor, llena ambos campos.");
            return;
        }

        // Sintaxis para llamar al procedimiento almacenado
        String sql = "{CALL ValidarLogin(?, ?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            // Pasar los parámetros al procedimiento
            stmt.setString(1, usuario);
            stmt.setString(2, password);

            // Ejecutar y obtener el resultado
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Si entra aquí, las credenciales son correctas
                int id = rs.getInt("idEmpleado");
                String nombre = rs.getString("nombre");
                String rol = rs.getString("rol");

                // Guardamos los datos del usuario en la sesión
                SesionActiva.setUsuario(id, nombre, rol);
                lblError.setText("¡Bienvenido, " + nombre + "!");

                // AQUÍ VA EL CÓDIGO PARA ABRIR EL MainView o MenuPrincipalView
                abrirMenuPrincipal();

            } else {
                // Si no hay resultados, el usuario o password están mal
                lblError.setText("Usuario o contraseña incorrectos.");
            }

        } catch (SQLException e) {
            lblError.setText("Error al conectar con la base de datos.");
            e.printStackTrace();
        }
    }

    private void abrirMenuPrincipal() {
        try {
            // 1. Cargar el archivo FXML del menú principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MenuPrincipalView.fxml"));
            Parent root = loader.load();

            // 2. Obtener la ventana (Stage) actual usando cualquier elemento de la interfaz (ej. txtUsuario)
            Stage stage = (Stage) txtUsuario.getScene().getWindow();

            // 3. Crear la nueva escena y establecerla en la ventana
            Scene scene = new Scene(root);
            stage.setScene(scene);

            // 4. (Opcional) Personalizar el título de la ventana con el nombre del usuario
            stage.setTitle("Punto de Venta - Sesión de: " + SesionActiva.getNombre() + " (" + SesionActiva.getRol() + ")");
            stage.centerOnScreen(); // Centrar la ventana
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            lblError.setText("Error al cargar la pantalla del menú.");
        }
    }
}