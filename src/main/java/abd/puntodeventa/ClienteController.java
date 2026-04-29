package abd.puntodeventa;

import atlantafx.base.theme.PrimerDark;
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

    // Componentes de Identificar View
    @FXML private TextField txtTelefonoBusqueda;

    // Componentes de Registro View
    @FXML private TextField txtNuevoNombre;
    @FXML private TextField txtNuevoTelefono;

    @FXML
    public void buscarCliente(ActionEvent event) {
        String telefono = txtTelefonoBusqueda.getText().trim();
        if (telefono.isEmpty()) {
            mostrarAlerta("Atención", "Ingrese un número de teléfono.", Alert.AlertType.WARNING);
            return;
        }

        // Simulación de búsqueda (Aquí iría tu lógica de base de datos o listas)
        if (telefono.equals("8331234567")) {
            // Si lo encuentra, abrimos su perfil
            cambiarVistaModal(event, "PerfilClienteView.fxml", "Perfil de Recompensas");
        } else {
            mostrarAlerta("No encontrado", "El cliente no existe. Regístrelo.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void registrarCliente(ActionEvent event) {
        String nombre = txtNuevoNombre.getText().trim();
        String telefono = txtNuevoTelefono.getText().trim();

        if (nombre.isEmpty() || telefono.isEmpty()) {
            mostrarAlerta("Error", "Debe llenar todos los campos.", Alert.AlertType.WARNING);
            return;
        }

        // Lógica de guardado...
        mostrarAlerta("Éxito", "Cliente " + nombre + " registrado.", Alert.AlertType.INFORMATION);
        cambiarVistaModal(event, "PerfilClienteView.fxml", "Perfil de Recompensas");
    }

    @FXML
    public void irARegistro(ActionEvent event) {
        cambiarVistaModal(event, "ClienteRegistroView.fxml", "Nuevo Cliente");
    }

    @FXML
    public void irAIdentificacion(ActionEvent event) {
        cambiarVistaModal(event, "ClienteIdentificarView.fxml", "Identificar Cliente");
    }

    @FXML
    public void cerrarVentana(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void cambiarVistaModal(ActionEvent event, String fxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/abd/puntodeventa/" + fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titulo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.getDialogPane().getScene().setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        alert.showAndWait();
    }
}