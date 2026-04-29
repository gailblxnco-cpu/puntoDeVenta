package abd.puntodeventa;

import atlantafx.base.theme.PrimerDark;
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

    @FXML private TableView<Empleado> tablaUsuarios;
    @FXML private TableColumn<Empleado, String> colUser;
    @FXML private TableColumn<Empleado, String> colRol;

    private ObservableList<Empleado> listaUsuariosBD = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colUser.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        colRol.setCellValueFactory(cellData -> cellData.getValue().rolProperty());

        // BLOQUEO DE LA TABLA (Sin setResizable para permitir que se estiren solas)
        tablaUsuarios.setEditable(false);
        colUser.setReorderable(false);
        colRol.setReorderable(false);

        // Esto fuerza a las columnas a repartirse el espacio sin dejar huecos vacíos al final
        tablaUsuarios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        cbRol.setItems(FXCollections.observableArrayList("gerente", "vendedor", "mesero"));
        cargarUsuariosDesdeBD();
    }

    private void cargarUsuariosDesdeBD() {
        listaUsuariosBD.clear();
        String sql = "{CALL SP_ObtenerEmpleados()}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                listaUsuariosBD.add(new Empleado(
                        rs.getInt("idEmpleado"),
                        rs.getString("nombre"),
                        rs.getString("rol"),
                        rs.getString("contrasena")
                ));
            }
            tablaUsuarios.setItems(listaUsuariosBD);

        } catch (SQLException e) {
            mostrarAlerta("Error de Carga", "No se pudieron cargar los empleados.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void seleccionarUsuario(MouseEvent event) {
        Empleado seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            txtNuevoUser.setText(seleccionado.getNombre());
            txtNuevoPass.setText(seleccionado.getContrasena());
            cbRol.setValue(seleccionado.getRol());
        }
    }

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
            mostrarAlerta("Éxito", "Usuario creado correctamente.", Alert.AlertType.INFORMATION);

            limpiarCampos(null);
            cargarUsuariosDesdeBD();

        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo crear el usuario: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void modificarUsuario(ActionEvent event) {
        Empleado seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Error", "Primero selecciona un usuario de la tabla.", Alert.AlertType.WARNING);
            return;
        }
        if (!validarCampos()) return;

        String sql = "{CALL SP_ActualizarEmpleado(?, ?, ?, ?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, seleccionado.getIdEmpleado());
            stmt.setString(2, txtNuevoUser.getText().trim());
            stmt.setString(3, txtNuevoPass.getText().trim());
            stmt.setString(4, cbRol.getValue());

            stmt.executeUpdate();
            mostrarAlerta("Éxito", "Usuario modificado correctamente.", Alert.AlertType.INFORMATION);

            limpiarCampos(null);
            cargarUsuariosDesdeBD();

        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudo modificar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void eliminarUsuario(ActionEvent event) {
        Empleado seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Error", "Selecciona un usuario de la tabla para eliminar.", Alert.AlertType.WARNING);
            return;
        }

        if (seleccionado.getIdEmpleado() == SesionActiva.getIdEmpleado()) {
            mostrarAlerta("Acción denegada", "No puedes eliminar tu propia cuenta en sesión.", Alert.AlertType.WARNING);
            return;
        }

        String sql = "{CALL SP_EliminarEmpleado(?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, seleccionado.getIdEmpleado());
            stmt.executeUpdate();

            mostrarAlerta("Éxito", "Usuario dado de baja exitosamente.", Alert.AlertType.INFORMATION);
            limpiarCampos(null);
            cargarUsuariosDesdeBD();

        } catch (SQLException e) {
            mostrarAlerta("Error", "Ocurrió un problema al dar de baja al usuario.", Alert.AlertType.ERROR);
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
            mostrarAlerta("Campos incompletos", "Por favor, completa todos los campos.", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.getDialogPane().getScene().setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
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