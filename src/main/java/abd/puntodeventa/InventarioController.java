package abd.puntodeventa;

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

public class InventarioController {

    @FXML private TableView<Producto> tablaInventario;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;

    @FXML private TextField txtId, txtNombre, txtPrecio, txtStock, txtBuscar;
    @FXML private Label lblEstado;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(d -> d.getValue().idProperty().asObject());
        colNombre.setCellValueFactory(d -> d.getValue().nombreProperty());
        colPrecio.setCellValueFactory(d -> d.getValue().precioProperty().asObject());
        colStock.setCellValueFactory(d -> d.getValue().stockProperty().asObject());

        tablaInventario.setItems(InventarioGlobal.getProductos());
    }

    @FXML
    public void seleccionarProducto(MouseEvent event) {
        Producto seleccionado = tablaInventario.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            txtId.setText(String.valueOf(seleccionado.getId()));
            txtNombre.setText(seleccionado.getNombre());
            txtPrecio.setText(String.valueOf(seleccionado.getPrecio()));
            txtStock.setText(String.valueOf(seleccionado.getStock()));
            lblEstado.setText("Listo para modificar.");
        }
    }

    @FXML
    public void handleGuardar(ActionEvent event) {
        if (validarCampos()) {
            String nombre = txtNombre.getText();
            double precio = Double.parseDouble(txtPrecio.getText());
            int stock = Integer.parseInt(txtStock.getText());

            int nuevoId = InventarioGlobal.getNextId();
            Producto nuevoProducto = new Producto(nuevoId, nombre, precio, stock);
            InventarioGlobal.getProductos().add(nuevoProducto);

            lblEstado.setText("¡Bebida guardada exitosamente!");
            handleLimpiar(null);
        }
    }

    @FXML
    public void handleModificar(ActionEvent event) {
        Producto seleccionado = tablaInventario.getSelectionModel().getSelectedItem();
        if (seleccionado != null && validarCampos()) {
            seleccionado.nombreProperty().set(txtNombre.getText());
            seleccionado.precioProperty().set(Double.parseDouble(txtPrecio.getText()));
            seleccionado.stockProperty().set(Integer.parseInt(txtStock.getText()));

            tablaInventario.refresh();
            lblEstado.setText("¡Bebida actualizada exitosamente!");
        } else if (seleccionado == null) {
            lblEstado.setText("Error: Selecciona una bebida de la tabla.");
        }
    }

    @FXML
    public void handleLimpiar(ActionEvent event) {
        txtId.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtStock.clear();
        tablaInventario.getSelectionModel().clearSelection();
    }

    private boolean validarCampos() {
        try {
            if (txtNombre.getText().isEmpty()) return false;
            Double.parseDouble(txtPrecio.getText());
            Integer.parseInt(txtStock.getText());
            return true;
        } catch (NumberFormatException e) {
            lblEstado.setText("Error: Precio y Stock deben ser números.");
            return false;
        }
    }

    @FXML
    public void abrirCaja(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Mantener tamaño de ventana
            double anchoActual = stage.getWidth();
            double altoActual = stage.getHeight();

            Scene scene = new Scene(root, anchoActual, altoActual);
            stage.setScene(scene);
            stage.setTitle("Gen POS - Caja Principal");
            stage.show();

        } catch (IOException e) {
            System.err.println("Error al regresar a la Caja.");
            e.printStackTrace();
        }
    }
}