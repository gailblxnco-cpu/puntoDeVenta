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
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import java.io.IOException;

public class MainController {
    // Componentes de la Tabla de Pedido
    @FXML private TableView<Producto> tablaPedido;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Double> colPrecio;

    // Paneles y Buscador
    @FXML private FlowPane flowProductos;
    @FXML private TextField txtBuscar;

    // Etiquetas de Totales y Fidelización
    @FXML private Label lblTotal;
    @FXML private Label lblSubtotal;
    @FXML private Label lblClienteActivo;
    @FXML private Label lblDescuento;

    private ObservableList<Producto> listaPedido = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Configurar columnas de la tabla
        colNombre.setCellValueFactory(d -> d.getValue().nombreProperty());
        colPrecio.setCellValueFactory(d -> d.getValue().precioProperty().asObject());
        tablaPedido.setItems(listaPedido);

        // 2. Cargar botones de productos con estilo CSS
        cargarInventarioEnPantalla();

        // 3. Refrescar totales y datos del cliente activo
        actualizarTotales();
    }

    private void cargarInventarioEnPantalla() {
        flowProductos.getChildren().clear();
        for (Producto p : InventarioGlobal.getProductos()) {
            Button btnItem = new Button(p.getNombre() + "\n$" + p.getPrecio());

            // Asignamos la clase del archivo CSS
            btnItem.getStyleClass().add("product-button");

            btnItem.setOnAction(e -> {
                listaPedido.add(p);
                actualizarTotales();
            });
            flowProductos.getChildren().add(btnItem);
        }
    }

    private void actualizarTotales() {
        double subtotal = listaPedido.stream().mapToDouble(Producto::getPrecio).sum();
        double descEfectivo = 0;

        Cliente activo = InventarioGlobal.getClienteActivo();

        if (activo != null) {
            lblClienteActivo.setText("Cliente: " + activo.getNombre());

            if (InventarioGlobal.isUsarPuntosEnVenta()) {
                // Cada punto acumulado equivale a $0.10 de descuento
                descEfectivo = activo.getPuntos() * 0.1;
                lblDescuento.setText(String.format("Descuento: -$%.2f (%d pts)", descEfectivo, activo.getPuntos()));
            } else {
                lblDescuento.setText("Descuento: $0.00");
            }
        } else {
            lblClienteActivo.setText("Cliente: Ninguno");
            lblDescuento.setText("Descuento: $0.00");
        }

        double totalFinal = Math.max(0, subtotal - descEfectivo);
        lblSubtotal.setText(String.format("%.2f", subtotal));
        lblTotal.setText(String.format("%.2f", totalFinal));
    }

    @FXML
    public void handlePago(ActionEvent event) {
        if (listaPedido.isEmpty()) {
            mostrarAlerta("Carrito vacío", "Por favor, agrega productos al pedido.");
            return;
        }

        double subtotal = listaPedido.stream().mapToDouble(Producto::getPrecio).sum();
        double descuento = 0;
        Cliente cliente = InventarioGlobal.getClienteActivo();
        StringBuilder ticket = new StringBuilder("DETALLE DE VENTA\n------------------\n");

        if (cliente != null) {
            if (InventarioGlobal.isUsarPuntosEnVenta()) {
                descuento = cliente.getPuntos() * 0.1;
                cliente.setPuntos(0); // Se consumen los puntos
                ticket.append("Puntos canjeados: -$").append(String.format("%.2f", descuento)).append("\n");
            }

            // El cliente gana 1 punto por cada $10 gastados (sobre el subtotal)
            int nuevosPuntos = (int) (subtotal / 10);
            cliente.setPuntos(cliente.getPuntos() + nuevosPuntos);

            ticket.append("Cliente: ").append(cliente.getNombre()).append("\n");
            ticket.append("Puntos ganados: +").append(nuevosPuntos).append("\n");
            ticket.append("Nuevo saldo: ").append(cliente.getPuntos()).append(" pts\n");
        }

        double totalFinal = Math.max(0, subtotal - descuento);
        ticket.append("------------------\nTOTAL: $").append(String.format("%.2f", totalFinal));

        mostrarAlerta("Venta Exitosa", ticket.toString());

        // Limpiar sesión de venta actual
        listaPedido.clear();
        InventarioGlobal.setClienteActivo(null);
        InventarioGlobal.setUsarPuntosEnVenta(false);
        actualizarTotales();
    }


    @FXML
    public void volverAlMenu(ActionEvent event) {
        cambiarVista(event, "MenuPrincipalView.fxml", "Menú Principal");
    }

    @FXML
    public void abrirVentanaCliente(ActionEvent event) {
        cambiarVista(event, "ClienteIdentificarView.fxml", "Identificar Cliente");
    }

    @FXML
    public void abrirInventario(ActionEvent event) {
        cambiarVista(event, "InventarioView.fxml", "Gestión de Inventario");
    }

    private void cambiarVista(ActionEvent event, String fxml, String titulo) {
        try {
            // Usar la ruta absoluta del recurso para mayor estabilidad
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
            System.err.println("Error al cargar la vista: " + fxml);
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}