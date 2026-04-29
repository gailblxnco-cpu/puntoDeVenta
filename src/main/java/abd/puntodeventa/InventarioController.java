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

public class InventarioController {

    @FXML private TableView<Producto> tablaInventario;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;

    @FXML private TextField txtId, txtNombre, txtPrecio, txtStock, txtBuscar;
    @FXML private Label lblEstado;

    private ObservableList<Producto> listaInventarioBD = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(d -> d.getValue().idProperty().asObject());
        colNombre.setCellValueFactory(d -> d.getValue().nombreProperty());
        colPrecio.setCellValueFactory(d -> d.getValue().precioProperty().asObject());
        colStock.setCellValueFactory(d -> d.getValue().stockProperty().asObject());

        cargarInventarioDesdeBD();
    }

    // --- LEER (READ) ---
    private void cargarInventarioDesdeBD() {
        listaInventarioBD.clear();
        String sql = "{CALL SP_ObtenerInventario()}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                listaInventarioBD.add(new Producto(
                        rs.getInt("idInventario"),
                        rs.getString("nombreProducto"),
                        rs.getDouble("precioUnitario"),
                        rs.getInt("stockDisponible")
                ));
            }
            tablaInventario.setItems(listaInventarioBD);

        } catch (SQLException e) {
            lblEstado.setText("Error al cargar el inventario.");
            lblEstado.setStyle("-fx-text-fill: #e74c3c;"); // Rojo para error
            e.printStackTrace();
        }
    }

    // --- SELECCIONAR RENGLÓN ---
    @FXML
    public void seleccionarProducto(MouseEvent event) {
        Producto seleccionado = tablaInventario.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            txtId.setText(String.valueOf(seleccionado.getId()));
            txtNombre.setText(seleccionado.getNombre());
            txtPrecio.setText(String.valueOf(seleccionado.getPrecio()));
            txtStock.setText(String.valueOf(seleccionado.getStock()));
        }
    }

    // --- GUARDAR (INSERT) ---
    @FXML
    public void handleGuardar(ActionEvent event) {
        if (validarCampos()) {
            String sql = "{CALL SP_InsertarProducto(?, ?, ?, ?, ?)}";

            try (Connection conn = ConexionDB.getConnection();
                 CallableStatement stmt = conn.prepareCall(sql)) {

                stmt.setString(1, txtNombre.getText().trim());
                stmt.setDouble(2, Double.parseDouble(txtPrecio.getText().trim()));
                stmt.setInt(3, Integer.parseInt(txtStock.getText().trim()));
                stmt.setString(4, "Bebidas"); // Categoría por defecto

                // ¡Aquí usamos la sesión para registrar quién agregó el producto!
                stmt.setInt(5, SesionActiva.getIdEmpleado());

                stmt.executeUpdate();

                lblEstado.setStyle("-fx-text-fill: #2ecc71;"); // Verde para éxito
                lblEstado.setText("Producto guardado con éxito.");

                handleLimpiar(null);
                cargarInventarioDesdeBD();

            } catch (SQLException e) {
                lblEstado.setStyle("-fx-text-fill: #e74c3c;");
                lblEstado.setText("Error al guardar: " + e.getMessage());
            }
        }
    }

    // --- MODIFICAR (UPDATE) ---
    @FXML
    public void handleModificar(ActionEvent event) {
        Producto seleccionado = tablaInventario.getSelectionModel().getSelectedItem();
        if (seleccionado != null && validarCampos()) {
            String sql = "{CALL SP_ActualizarProducto(?, ?, ?, ?, ?, ?)}";

            try (Connection conn = ConexionDB.getConnection();
                 CallableStatement stmt = conn.prepareCall(sql)) {

                stmt.setInt(1, seleccionado.getId());
                stmt.setString(2, txtNombre.getText().trim());
                stmt.setDouble(3, Double.parseDouble(txtPrecio.getText().trim()));
                stmt.setInt(4, Integer.parseInt(txtStock.getText().trim()));
                stmt.setString(5, "Bebidas");
                stmt.setInt(6, SesionActiva.getIdEmpleado()); // Registra quién lo modificó

                stmt.executeUpdate();

                lblEstado.setStyle("-fx-text-fill: #2ecc71;");
                lblEstado.setText("Producto modificado correctamente.");

                handleLimpiar(null);
                cargarInventarioDesdeBD();

            } catch (SQLException e) {
                lblEstado.setStyle("-fx-text-fill: #e74c3c;");
                lblEstado.setText("Error al modificar: " + e.getMessage());
            }
        } else if (seleccionado == null) {
            lblEstado.setStyle("-fx-text-fill: #e74c3c;");
            lblEstado.setText("Selecciona un producto de la tabla primero.");
        }
    }

    // --- LIMPIAR FORMULARIO ---
    @FXML
    public void handleLimpiar(ActionEvent event) {
        txtId.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtStock.clear();
        tablaInventario.getSelectionModel().clearSelection();
        if (event != null) { // Solo limpia el label si fue presionado manualmente el botón
            lblEstado.setText("");
        }
    }

    private boolean validarCampos() {
        try {
            if (txtNombre.getText().trim().isEmpty()) {
                lblEstado.setStyle("-fx-text-fill: #e74c3c;");
                lblEstado.setText("El nombre no puede estar vacío.");
                return false;
            }
            Double.parseDouble(txtPrecio.getText().trim());
            Integer.parseInt(txtStock.getText().trim());
            return true;
        } catch (NumberFormatException e) {
            lblEstado.setStyle("-fx-text-fill: #e74c3c;");
            lblEstado.setText("Error: Precio y Stock deben ser numéricos.");
            return false;
        }
    }

    @FXML
    public void volverAlMenu(ActionEvent event) {
        cambiarVista(event, "MenuPrincipalView.fxml", "Gen POS - Menú Principal");
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