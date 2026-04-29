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

// Imports para la base de datos
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

    // Lista observable para actualizar la tabla en tiempo real
    private ObservableList<Producto> listaInventarioBD = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Enlazar columnas con las properties del modelo Producto
        colId.setCellValueFactory(d -> d.getValue().idProperty().asObject());
        colNombre.setCellValueFactory(d -> d.getValue().nombreProperty());
        colPrecio.setCellValueFactory(d -> d.getValue().precioProperty().asObject());
        colStock.setCellValueFactory(d -> d.getValue().stockProperty().asObject());

        cargarInventarioDesdeBD();
    }

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
            mostrarMensajeEstado("Error crítico al cargar BD.", true);
            e.printStackTrace();
        }
    }

    @FXML
    public void seleccionarProducto(MouseEvent event) {
        Producto seleccionado = tablaInventario.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            txtId.setText(String.valueOf(seleccionado.getId()));
            txtNombre.setText(seleccionado.getNombre());
            txtPrecio.setText(String.valueOf(seleccionado.getPrecio()));
            txtStock.setText(String.valueOf(seleccionado.getStock()));
            limpiarEstado();
        }
    }

    @FXML
    public void handleGuardar(ActionEvent event) {
        if (validarCampos()) {
            String nombre = txtNombre.getText().trim();
            double precio = Double.parseDouble(txtPrecio.getText().trim());
            int stock = Integer.parseInt(txtStock.getText().trim());

            // Obtenemos el ID del gerente/cajero logueado para la BD
            int idEmpleado = SesionActiva.getIdEmpleado();

            String sql = "{CALL SP_InsertarProducto(?, ?, ?, ?, ?)}";

            try (Connection conn = ConexionDB.getConnection();
                 CallableStatement stmt = conn.prepareCall(sql)) {

                stmt.setString(1, nombre);
                stmt.setDouble(2, precio);
                stmt.setInt(3, stock);
                stmt.setString(4, "General"); // Categoría por defecto
                stmt.setInt(5, idEmpleado);

                stmt.executeUpdate(); // Ejecutar guardado en MySQL

                mostrarMensajeEstado("Producto guardado con éxito.", false);
                handleLimpiar(null);

                // Refrescamos la tabla consultando nuevamente la BD
                cargarInventarioDesdeBD();

            } catch (SQLException e) {
                mostrarMensajeEstado("Error al guardar en la BD.", true);
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleModificar(ActionEvent event) {
        Producto seleccionado = tablaInventario.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            if (validarCampos()) {
                int id = seleccionado.getId();
                String nombre = txtNombre.getText().trim();
                double precio = Double.parseDouble(txtPrecio.getText().trim());
                int stock = Integer.parseInt(txtStock.getText().trim());
                int idEmpleado = SesionActiva.getIdEmpleado();

                String sql = "{CALL SP_ActualizarProducto(?, ?, ?, ?, ?, ?)}";

                try (Connection conn = ConexionDB.getConnection();
                     CallableStatement stmt = conn.prepareCall(sql)) {

                    stmt.setInt(1, id);
                    stmt.setString(2, nombre);
                    stmt.setDouble(3, precio);
                    stmt.setInt(4, stock);
                    stmt.setString(5, "General"); // Categoría
                    stmt.setInt(6, idEmpleado);

                    stmt.executeUpdate();

                    mostrarMensajeEstado("Producto modificado correctamente.", false);
                    handleLimpiar(null);
                    cargarInventarioDesdeBD(); // Actualizar vista

                } catch (SQLException e) {
                    mostrarMensajeEstado("Error al actualizar en la BD.", true);
                    e.printStackTrace();
                }
            }
        } else {
            mostrarMensajeEstado("Selecciona un producto de la tabla.", true);
        }
    }

    @FXML
    public void handleLimpiar(ActionEvent event) {
        txtId.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtStock.clear();
        tablaInventario.getSelectionModel().clearSelection();
        limpiarEstado();
    }

    private boolean validarCampos() {
        try {
            if (txtNombre.getText().trim().isEmpty()) {
                mostrarMensajeEstado("Error: El nombre no puede estar vacío.", true);
                return false;
            }
            Double.parseDouble(txtPrecio.getText().trim());
            Integer.parseInt(txtStock.getText().trim());
            return true;
        } catch (NumberFormatException e) {
            mostrarMensajeEstado("Error: Precio y Stock deben ser numéricos.", true);
            return false;
        }
    }

    @FXML
    public void volverAlMenu(ActionEvent event) {
        cambiarVista(event, "MenuPrincipalView.fxml", "Gen POS - Menú Principal");
    }

    // --- UX COMPATIBLE CON ATLANTAFX ---
    private void mostrarMensajeEstado(String mensaje, boolean esError) {
        if (lblEstado != null) {
            lblEstado.setText(mensaje);
            lblEstado.getStyleClass().removeAll("text-danger", "text-success");
            if (esError) {
                lblEstado.getStyleClass().add("text-danger");
            } else {
                lblEstado.getStyleClass().add("text-success");
            }
        }
    }

    private void limpiarEstado() {
        if (lblEstado != null) {
            lblEstado.setText("");
            lblEstado.getStyleClass().removeAll("text-danger", "text-success");
        }
    }

    private void cambiarVista(ActionEvent event, String fxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/abd/puntodeventa/" + fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            double ancho = stage.getScene().getWidth();
            double alto = stage.getScene().getHeight();

            Scene scene = new Scene(root, ancho, alto);
            stage.setScene(scene);
            stage.setTitle(titulo);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar: " + fxml);
            e.printStackTrace();
        }
    }
}