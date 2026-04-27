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

public class ClienteController {

    @FXML private TextField txtTelefonoBusqueda;
    @FXML private TextField txtNuevoNombre;
    @FXML private TextField txtNuevoTelefono;

    // --- LÓGICA DE BÚSQUEDA ---
    @FXML
    public void buscarCliente(ActionEvent event) {
        String telefono = txtTelefonoBusqueda.getText();
        if (telefono == null || telefono.trim().isEmpty()) {
            mostrarAlerta("Campo vacío", "Por favor ingresa un teléfono.");
            return;
        }

        for (Cliente c : InventarioGlobal.getClientes()) {
            if (c.getTelefono().equals(telefono)) {
                InventarioGlobal.setClienteActivo(c);
                cambiarVista(event, "PerfilClienteView.fxml", "Perfil de Cliente");
                return;
            }
        }
        mostrarAlerta("No encontrado", "El cliente no existe. ¿Deseas registrarlo?");
    }

    // --- LÓGICA DE REGISTRO ---
    @FXML
    public void registrarCliente(ActionEvent event) {
        String nombre = txtNuevoNombre.getText();
        String telefono = txtNuevoTelefono.getText();

        if (nombre.isEmpty() || telefono.isEmpty()) {
            mostrarAlerta("Error", "Debes llenar todos los campos.");
            return;
        }

        Cliente nuevo = new Cliente(nombre, telefono, 0);
        InventarioGlobal.getClientes().add(nuevo);
        InventarioGlobal.setClienteActivo(nuevo);

        mostrarAlerta("Éxito", "Cliente registrado y seleccionado.");
        cambiarVista(event, "PerfilClienteView.fxml", "Perfil de Cliente");
    }

    // --- NAVEGACIÓN ENTRE VENTANAS ---
    @FXML
    public void irARegistro(ActionEvent event) {
        cambiarVista(event, "ClienteRegistroView.fxml", "Registrar Cliente");
    }

    @FXML
    public void irAIdentificacion(ActionEvent event) {
        cambiarVista(event, "ClienteIdentificarView.fxml", "Identificar Cliente");
    }

    @FXML
    public void cerrarVentana(ActionEvent event) {
        cambiarVista(event, "MainView.fxml", "Gen POS - Caja");
    }

    // --- UTILIDADES ---
    private void cambiarVista(ActionEvent event, String fxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(scene);
            stage.setTitle(titulo);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}