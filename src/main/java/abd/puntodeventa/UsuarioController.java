package abd.puntodeventa;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioController {

    @FXML private TextField txtNuevoUser;
    @FXML private PasswordField txtNuevoPass;
    @FXML private ComboBox<String> cbRol;

    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, String> colUser;
    @FXML private TableColumn<Usuario, String> colRol;

    private ObservableList<Usuario> listaUsuariosBD = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colUser.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());
        colRol.setCellValueFactory(cellData -> cellData.getValue().rolProperty());

        cbRol.setItems(FXCollections.observableArrayList("gerente", "vendedor", "mesero"));
        cargarUsuariosDesdeBD();
    }

    // --- LEER (READ) ---
    private void cargarUsuariosDesdeBD() {
        listaUsuariosBD.clear();
        String sql = "{CALL SP_ObtenerEmpleados()}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                listaUsuariosBD.add(new Usuario(
                        rs.getInt("idEmpleado"),
                        rs.getString("nombre"),
                        rs.getString("contrasena"),
                        rs.getString("rol"),
                        true // Todos los que devuelve el SP son activos
                ));
            }
            tablaUsuarios.setItems(listaUsuariosBD);

        } catch (SQLException e) {
            mostrarAlerta("Error de Carga", "No se pudieron cargar los empleados.");
            e.printStackTrace();
        }
    }

    // --- SELECCIONAR RENGLÓN ---
    @FXML
    public void seleccionarUsuario(MouseEvent event) {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            txtNuevoUser.setText(seleccionado.getUsername());
            txtNuevoPass.setText(seleccionado.getPassword());
            cbRol.setValue(seleccionado.getRol());
        }
    }

    // --- CREAR (CREATE) ---
    @FXML
    public void crearUsuario(ActionEvent event) {
        if (!validarCampos()) return;
        String sql = "{CALL SP_InsertarEmpleado(?, ?, ?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, txtNuevoUser.getText().trim());
            stmt.setString(2, txtNuevoPass.getText().trim());
            stmt.setString(3, cbRol.getValue());

            stmt.executeUpdate();
            mostrarAlerta("Éxito", "Usuario creado correctamente.");

            limpiarCampos(null);
            cargarUsuariosDesdeBD();

        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo crear el usuario: " + e.getMessage());
        }
    }

    // --- MODIFICAR (UPDATE) ---
    @FXML
    public void modificarUsuario(ActionEvent event) {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Error", "Primero selecciona un usuario de la tabla.");
            return;
        }
        if (!validarCampos()) return;

        String sql = "{CALL SP_ActualizarEmpleado(?, ?, ?, ?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, seleccionado.getId());
            stmt.setString(2, txtNuevoUser.getText().trim());
            stmt.setString(3, txtNuevoPass.getText().trim());
            stmt.setString(4, cbRol.getValue());

            stmt.executeUpdate();
            mostrarAlerta("Éxito", "Usuario modificado correctamente.");

            limpiarCampos(null);
            cargarUsuariosDesdeBD();

        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo modificar: " + e.getMessage());
        }
    }

    // --- ELIMINAR (SOFT DELETE) ---
    @FXML
    public void eliminarUsuario(ActionEvent event) {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Error", "Selecciona un usuario de la tabla para eliminar.");
            return;
        }

        if (seleccionado.getId() == SesionActiva.getIdEmpleado()) {
            mostrarAlerta("Acción denegada", "No puedes eliminar tu propia cuenta en sesión.");
            return;
        }

        String sql = "{CALL SP_EliminarEmpleado(?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, seleccionado.getId());
            stmt.executeUpdate();

            mostrarAlerta("Éxito", "Usuario dado de baja exitosamente.");
            limpiarCampos(null);
            cargarUsuariosDesdeBD(); // Al refrescar, ya no aparecerá en la tabla

        } catch (SQLException e) {
            mostrarAlerta("Error", "Ocurrió un problema al dar de baja al usuario.");
        }
    }

    @FXML
    public void limpiarCampos(ActionEvent event) {
        txtNuevoUser.clear();
        txtNuevoPass.clear();
        cbRol.getSelectionModel().clearSelection();
        tablaUsuarios.getSelectionModel().clearSelection();
    }

    @FXML
    public void volverMenu(ActionEvent event) {
        cambiarVista(event, "MenuPrincipalView.fxml", "Gen POS - Menú Principal");
    }

    private boolean validarCampos() {
        if (txtNuevoUser.getText().trim().isEmpty() || txtNuevoPass.getText().trim().isEmpty() || cbRol.getValue() == null) {
            mostrarAlerta("Campos incompletos", "Por favor, completa todos los campos.");
            return false;
        }
        return true;
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
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            stage.setScene(scene);
            stage.setTitle(titulo);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}