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
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import java.io.IOException;

public class MainController {

    @FXML private TableView<Producto> tablaPedido;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private Label lblClienteActivo;
    @FXML private Label lblDescuento;
    @FXML private Label lblTotal;
    @FXML private TextField txtBuscar;
    @FXML private FlowPane flowProductos;

    private ObservableList<Producto> listaPedido = FXCollections.observableArrayList();
    private double totalActual = 0.0;
    private double descuentoActual = 0.0;

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        colPrecio.setCellValueFactory(cellData -> cellData.getValue().precioProperty().asObject());
        tablaPedido.setItems(listaPedido);

        cargarInventarioEnPantalla();
    }

    private void cargarInventarioEnPantalla() {
        flowProductos.getChildren().clear();
        for (Producto p : InventarioGlobal.getProductos()) {
            Button btnItem = new Button(p.getNombre() + "\n$" + p.getPrecio());

            // Integración nativa con AtlantaFX
            btnItem.getStyleClass().addAll("button", "accent", "outlined");
            btnItem.setPrefSize(130, 80);

            btnItem.setOnAction(e -> agregarAlPedido(p));
            flowProductos.getChildren().add(btnItem);
        }
    }

    private void agregarAlPedido(Producto p) {
        listaPedido.add(p);
        actualizarTotales();
    }

    private void actualizarTotales() {
        totalActual = listaPedido.stream().mapToDouble(Producto::getPrecio).sum();
        double totalConDescuento = totalActual - descuentoActual;
        if(totalConDescuento < 0) totalConDescuento = 0;

        lblTotal.setText(String.format("$%.2f", totalConDescuento));
    }

    @FXML
    public void handlePago(ActionEvent event) {
        if (listaPedido.isEmpty()) {
            mostrarAlerta("Error", "El pedido está vacío.", Alert.AlertType.WARNING);
            return;
        }
        mostrarAlerta("Éxito", "Pago procesado correctamente.", Alert.AlertType.INFORMATION);
        listaPedido.clear();
        descuentoActual = 0.0;
        lblClienteActivo.setText("Cliente: Ninguno");
        lblDescuento.setText("Descuento: $0.00");
        actualizarTotales();
    }

    @FXML
    public void abrirVentanaCliente(ActionEvent event) {
        abrirModal("ClienteIdentificarView.fxml", "Identificar Cliente");
    }

    @FXML
    public void abrirInventario(ActionEvent event) {
        cambiarVista(event, "InventarioView.fxml", "Gen POS - Inventario");
    }

    @FXML
    public void volverAlMenu(ActionEvent event) {
        cambiarVista(event, "MenuPrincipalView.fxml", "Gen POS - Menú");
    }

    private void cambiarVista(ActionEvent event, String fxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/abd/puntodeventa/" + fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight()));
            stage.setTitle(titulo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void abrirModal(String fxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/abd/puntodeventa/" + fxml));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(titulo);
            stage.setResizable(false);
            stage.show();
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