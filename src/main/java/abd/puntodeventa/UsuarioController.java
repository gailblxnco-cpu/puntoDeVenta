package abd.puntodeventa;

import javafx.collections.FXCollections;
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

public class UsuarioController {

    @FXML private TextField txtNuevoUser;
    @FXML private PasswordField txtNuevoPass;
    @FXML private ComboBox<String> cbRol;

    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, String> colUser;
    @FXML private TableColumn<Usuario, String> colRol;

    @FXML
    public void initialize() {
        colUser.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());
        colRol.setCellValueFactory(cellData -> cellData.getValue().rolProperty());
        tablaUsuarios.setItems(InventarioGlobal.getUsuarios());
        cbRol.setItems(FXCollections.observableArrayList("ADMIN", "CAJERO"));
    }

    /**
     * Llena el formulario cuando haces clic en un renglón de la tabla.
     */
    @FXML
    public void seleccionarUsuario(MouseEvent event) {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            txtNuevoUser.setText(seleccionado.getUsername());
            txtNuevoPass.setText(seleccionado.getPassword());
            cbRol.setValue(seleccionado.getRol());
        }
    }

    @FXML
    public void crearUsuario(ActionEvent event) {
        if (!validarCampos()) return;

        Usuario nuevo = new Usuario(txtNuevoUser.getText().trim(), txtNuevoPass.getText().trim(), cbRol.getValue());
        InventarioGlobal.getUsuarios().add(nuevo);

        mostrarAlerta("Éxito", "Usuario creado correctamente.");
        limpiarCampos(null);
    }

    @FXML
    public void modificarUsuario(ActionEvent event) {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Error", "Primero selecciona un usuario de la tabla.");
            return;
        }
        if (!validarCampos()) return;

        // Actualizamos los datos del usuario seleccionado
        seleccionado.usernameProperty().set(txtNuevoUser.getText().trim());
        // Nota: Si quieres actualizar la contraseña, deberás agregar un passwordProperty() en tu clase Usuario
        // seleccionado.passwordProperty().set(txtNuevoPass.getText().trim());
        seleccionado.rolProperty().set(cbRol.getValue());

        tablaUsuarios.refresh(); // Refresca la tabla para ver los cambios
        mostrarAlerta("Éxito", "Usuario modificado correctamente.");
        limpiarCampos(null);
    }

    @FXML
    public void eliminarUsuario(ActionEvent event) {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Error", "Primero selecciona un usuario de la tabla para eliminar.");
            return;
        }

        // Protección para no eliminar al propio admin logueado
        if (seleccionado.getUsername().equals(InventarioGlobal.getUsuarioLogueado().getUsername())) {
            mostrarAlerta("Acción denegada", "No puedes eliminar tu propio usuario mientras estás en sesión.");
            return;
        }

        InventarioGlobal.getUsuarios().remove(seleccionado);
        mostrarAlerta("Éxito", "Usuario eliminado correctamente.");
        limpiarCampos(null);
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