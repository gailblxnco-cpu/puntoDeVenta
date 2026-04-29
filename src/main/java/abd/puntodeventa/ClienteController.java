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
import java.sql.*;

public class ClienteController {

    // Campos para la ventana de Identificación
    @FXML private TextField txtTelefonoBusqueda;

    // Campos para la ventana de Registro
    @FXML private TextField txtNuevoNombre;
    @FXML private TextField txtNuevoTelefono;

    // --- LÓGICA DE IDENTIFICACIÓN (Búsqueda) ---
    @FXML
    public void buscarCliente(ActionEvent event) {
        String telefono = txtTelefonoBusqueda.getText();

        if (telefono == null || telefono.trim().isEmpty()) {
            mostrarAlerta("Campo requerido", "Por favor, ingrese un número de teléfono.");
            return;
        }

        buscarYActivarCliente(event, telefono.trim());
    }

    // --- LÓGICA DE REGISTRO ---
    @FXML
    public void registrarCliente(ActionEvent event) {
        String nombre = txtNuevoNombre.getText().trim();
        String tel = txtNuevoTelefono.getText().trim();

        if (nombre.isEmpty() || tel.isEmpty()) {
            mostrarAlerta("Campos incompletos", "Debe llenar el nombre y el teléfono para registrar.");
            return;
        }

        String sql = "{CALL SP_InsertarCliente(?, ?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, nombre);
            stmt.setString(2, tel);
            stmt.executeUpdate();

            mostrarAlerta("Registro Exitoso", "El cliente ha sido registrado.");

            // Auto-logueamos al cliente recién creado para obtener su ID
            buscarYActivarCliente(event, tel);

        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo registrar. Verifique que el teléfono no esté duplicado.");
            e.printStackTrace();
        }
    }

    // --- MÉTODO AUXILIAR DE BASE DE DATOS ---
    private void buscarYActivarCliente(ActionEvent event, String telefono) {
        String sql = "{CALL SP_BuscarClientePorTelefono(?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, telefono);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Cliente encontrado = new Cliente(
                        rs.getInt("idCliente"),
                        rs.getString("nombreCompleto"),
                        rs.getString("telefono"),
                        rs.getInt("puntos")
                );

                InventarioGlobal.setClienteActivo(encontrado);
                cambiarVista(event, "PerfilClienteView.fxml", "Perfil del Cliente");
            } else {
                mostrarAlerta("No encontrado", "No existe un cliente con ese número. Proceda al registro.");
            }
        } catch (SQLException e) {
            mostrarAlerta("Error de Conexión", "Hubo un problema al buscar en la base de datos.");
            e.printStackTrace();
        }
    }

    // --- NAVEGACIÓN ENTRE VISTAS ---
    @FXML
    public void irARegistro(ActionEvent event) {
        cambiarVista(event, "ClienteRegistroView.fxml", "Registrar Nuevo Cliente");
    }

    @FXML
    public void irAIdentificacion(ActionEvent event) {
        cambiarVista(event, "ClienteIdentificarView.fxml", "Identificar Cliente");
    }

    @FXML
    public void cerrarVentana(ActionEvent event) {
        InventarioGlobal.setClienteActivo(null);
        cambiarVista(event, "MainView.fxml", "Gen POS - Caja Principal");
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void cambiarVista(ActionEvent event, String fxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/abd/puntodeventa/" + fxml));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            double anchoContenido = stage.getScene().getWidth();
            double altoContenido = stage.getScene().getHeight();

            Scene scene = new Scene(root, anchoContenido, altoContenido);
            stage.setScene(scene);
            stage.setTitle(titulo);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}