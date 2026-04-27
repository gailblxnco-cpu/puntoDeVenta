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

    /**
     * Procesa el intento de inicio de sesión comparando con la
     * lista de usuarios en InventarioGlobal.
     */
    @FXML
    public void handleLogin(ActionEvent event) {
        String userIn = txtUsuario.getText().trim();
        String passIn = txtPassword.getText().trim();

        lblError.setText(""); // Limpiamos errores previos

        if (userIn.isEmpty() || passIn.isEmpty()) {
            lblError.setText("Por favor, llena todos los campos.");
            return;
        }

        // Buscamos al usuario en nuestra "base de datos" temporal
        boolean encontrado = false;
        for (Usuario u : InventarioGlobal.getUsuarios()) {
            if (u.getUsername().equals(userIn) && u.getPassword().equals(passIn)) {
                // GUARDAMOS EL USUARIO LOGUEADO: Vital para el control de roles
                InventarioGlobal.setUsuarioLogueado(u);

                // Navegamos al Menú Principal
                cambiarVista(event, "MenuPrincipalView.fxml", "Gen POS - Panel de Control");
                encontrado = true;
                break;
            }
        }

        if (!encontrado) {
            lblError.setText("Usuario o contraseña incorrectos.");
        }
    }

    /**
     * Método de navegación corregido para mantener el tamaño de la escena
     * y evitar que la ventana crezca por los bordes del sistema operativo.
     */
    private void cambiarVista(ActionEvent event, String fxml, String titulo) {
        try {
            // Usamos la ruta absoluta del recurso para evitar errores en IntelliJ
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/abd/puntodeventa/" + fxml));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Obtenemos el tamaño del CONTENIDO (Scene), no de la ventana (Stage)
            double ancho = stage.getScene().getWidth();
            double alto = stage.getScene().getHeight();

            Scene scene = new Scene(root, ancho, alto);

            stage.setScene(scene);
            stage.setTitle(titulo);
            stage.centerOnScreen(); // Opcional: centra la ventana al cambiar de módulo
            stage.show();

        } catch (IOException e) {
            lblError.setText("Error crítico: No se pudo cargar " + fxml);
            e.printStackTrace();
        }
    }
}