package abd.puntodeventa;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;

public class MenuPrincipalController {

    @FXML private Button btnUsuarios;

    /**
     * Se ejecuta al cargar la vista.
     * Gestiona los accesos: si no es ADMIN, el botón desaparece y
     * el FlowPane centra automáticamente los módulos restantes.
     */
    @FXML
    public void initialize() {
        Usuario logueado = InventarioGlobal.getUsuarioLogueado();

        if (logueado == null || !logueado.getRol().equals("ADMIN")) {
            if (btnUsuarios != null) {
                btnUsuarios.setVisible(false);
                btnUsuarios.setManaged(false);
            }
        }
    }

    // --- MÉTODOS DE NAVEGACIÓN ---

    @FXML
    public void irAVentas(ActionEvent event) {
        cambiarVista(event, "MainView.fxml", "Gen POS - Caja Principal");
    }

    @FXML
    public void irAInventario(ActionEvent event) {
        cambiarVista(event, "InventarioView.fxml", "Gen POS - Gestión de Inventario");
    }

    @FXML
    public void irAUsuarios(ActionEvent event) {
        cambiarVista(event, "GestionUsuariosView.fxml", "Gen POS - Gestión de Personal");
    }

    @FXML
    public void irAReportes(ActionEvent event) {
        mostrarAlerta("Módulo en Desarrollo", "El sistema de reportes y estadísticas estará disponible en la próxima versión.");
    }

    @FXML
    public void cerrarSesion(ActionEvent event) {
        InventarioGlobal.setUsuarioLogueado(null);
        cambiarVista(event, "LoginView.fxml", "Gen POS - Iniciar Sesión");
    }

    /**
     * Cambia la escena optimizando el tamaño para transiciones fluidas.
     */
    private void cambiarVista(ActionEvent event, String fxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/abd/puntodeventa/" + fxml));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Usamos las dimensiones de la escena actual para evitar deformaciones
            double ancho = stage.getScene().getWidth();
            double alto = stage.getScene().getHeight();

            Scene scene = new Scene(root, ancho, alto);

            stage.setScene(scene);
            stage.setTitle(titulo);
            stage.show();

        } catch (IOException e) {
            mostrarAlerta("Error de Sistema", "No se pudo cargar la vista: " + fxml);
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}