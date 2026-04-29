package abd.puntodeventa;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    @FXML
    public void handleLogin(ActionEvent event) {
        String usuario = txtUsuario.getText();
        String password = txtPassword.getText();

        if (usuario.isEmpty() || password.isEmpty()) {
            lblError.setText("Por favor, llena ambos campos.");
            return;
        }

        // Llamada a tu procedimiento almacenado de MySQL
        String sql = "{CALL ValidarLogin(?, ?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, usuario);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Guardamos los datos del empleado en la clase global
                SesionActiva.setUsuario(
                        rs.getInt("idEmpleado"),
                        rs.getString("nombre"),
                        rs.getString("rol")
                );

                // Limpiamos el error si había alguno y abrimos el menú
                lblError.setText("");
                abrirMenuPrincipal(event);
            } else {
                lblError.setText("Usuario o contraseña incorrectos.");
            }

        } catch (SQLException e) {
            lblError.setText("Error al conectar con la base de datos.");
            e.printStackTrace();
        }
    }

    private void abrirMenuPrincipal(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/abd/puntodeventa/MenuPrincipalView.fxml"));
            Parent root = loader.load();

            // Usamos el 'event' para obtener la ventana actual y no abrir una ventana nueva encima
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Mantenemos el tamaño de la ventana para una transición limpia
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            stage.setScene(scene);

            // Personalizamos el título de la ventana
            stage.setTitle("Gen POS - Menú Principal (" + SesionActiva.getRol().toUpperCase() + ")");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            lblError.setText("Error al cargar la pantalla del menú.");
        }
    }
}