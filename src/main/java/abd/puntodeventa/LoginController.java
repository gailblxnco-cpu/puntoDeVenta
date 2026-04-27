package abd.puntodeventa;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    @FXML
    public void handleLogin(ActionEvent event) {
        String usuario = txtUsuario.getText();
        String password = txtPassword.getText();

        lblError.setText(""); // Limpiar mensaje anterior

        // usuario temporal
        if (usuario.equals("Aby") && password.equals("1234")) {
            iniciarSistema(event);
        } else {
            lblError.setText("Error: Usuario o contraseña incorrectos.");
        }
    }

    private void iniciarSistema(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // tamaño actual de la ventana de Login
            double anchoActual = stage.getWidth();
            double altoActual = stage.getHeight();

            Scene scene = new Scene(root, anchoActual, altoActual);

            stage.setScene(scene);
            stage.setTitle("Gen POS - Caja Principal");
            stage.show();

        } catch (IOException e) {
            lblError.setText("Error al cargar la interfaz del sistema.");
            e.printStackTrace();
        }
    }
}