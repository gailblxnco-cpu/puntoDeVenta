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
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClienteController {

    @FXML private TextField txtTelefonoBusqueda;
    @FXML private TextField txtNuevoNombre;
    @FXML private TextField txtNuevoTelefono;

    @FXML
    public void buscarCliente(ActionEvent event) {
        String telefono = txtTelefonoBusqueda.getText().trim();
        if (telefono.isEmpty()) {
            mostrarAlerta("Atención", "Ingrese un número de teléfono.", Alert.AlertType.WARNING);
            return;
        }

        String sql = "{CALL SP_BuscarClientePorTelefono(?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, telefono);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Cliente encontrado: Guardamos en memoria global
                Cliente encontrado = new Cliente(
                        rs.getInt("idCliente"),
                        rs.getString("nombreCompleto"),
                        rs.getString("telefono"),
                        rs.getInt("puntos")
                );
                SesionActiva.setClienteActivo(encontrado);

                cambiarVistaModal(event, "PerfilClienteView.fxml", "Perfil de Recompensas");
            } else {
                mostrarAlerta("No encontrado", "El cliente no existe. Proceda a registrarlo.", Alert.AlertType.ERROR);
            }
        } catch (SQLException e) {
            mostrarAlerta("Error BD", "Fallo al buscar cliente.", Alert.AlertType.ERROR);
            e.printStackTrace();
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

        String sql = "{CALL SP_InsertarCliente(?, ?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, nombre);
            stmt.setString(2, telefono);
            stmt.executeUpdate();

            mostrarAlerta("Éxito", "Cliente " + nombre + " registrado correctamente.", Alert.AlertType.INFORMATION);

            // Opcional: auto-buscarlo después de registrar para cargarlo a memoria
            txtTelefonoBusqueda = new TextField(telefono); // Truco rápido para la búsqueda
            buscarCliente(event);

        } catch (SQLException e) {
            mostrarAlerta("Error BD", "No se pudo registrar el cliente.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void irARegistro(ActionEvent event) { cambiarVistaModal(event, "ClienteRegistroView.fxml", "Nuevo Cliente"); }
    @FXML
    public void irAIdentificacion(ActionEvent event) { cambiarVistaModal(event, "ClienteIdentificarView.fxml", "Identificar Cliente"); }
    @FXML
    public void cerrarVentana(ActionEvent event) { ((Stage) ((Node) event.getSource()).getScene().getWindow()).close(); }

    private void cambiarVistaModal(ActionEvent event, String fxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/abd/puntodeventa/" + fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titulo);
        } catch (IOException e) { e.printStackTrace(); }
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