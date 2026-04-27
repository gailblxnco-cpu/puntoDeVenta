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

    // Paneles y Campos
    @FXML private FlowPane flowProductos;
    @FXML private TextField txtBuscar;

    // Etiquetas de Totales
    @FXML private Label lblTotal, lblSubtotal, lblClienteActivo, lblDescuento;

    private ObservableList<Producto> listaPedido = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. Vincular columnas con las propiedades del modelo Producto
        colNombre.setCellValueFactory(d -> d.getValue().nombreProperty());
        colPrecio.setCellValueFactory(d -> d.getValue().precioProperty().asObject());
        tablaPedido.setItems(listaPedido);

        // 2. botones dinámicos de bebidas
        cargarInventarioEnPantalla();

        // 3. Refrescar estado de cliente y totales (por si venimos de otra ventana)
        actualizarTotales();
    }

    private void cargarInventarioEnPantalla() {
        flowProductos.getChildren().clear();
        for (Producto p : InventarioGlobal.getProductos()) {
            Button btnItem = new Button(p.getNombre() + "\n$" + p.getPrecio());

            // Usamos la clase de tu archivo estilos.css
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

        // Verificamos si hay alguien en sesión
        Cliente activo = InventarioGlobal.getClienteActivo();

        if (activo != null) {
            lblClienteActivo.setText("Cliente: " + activo.getNombre());

            if (InventarioGlobal.isUsarPuntosEnVenta()) {
                // Tasa de conversión: 1 punto = $0.10
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
        lblSubtotal.setText(String.format("$%.2f", subtotal));
        lblTotal.setText(String.format("$%.2f", totalFinal));
    }

    @FXML
    public void handlePago(ActionEvent event) {
        if (listaPedido.isEmpty()) {
            mostrarAlerta("Caja Vacía", "Agregue productos antes de cobrar.");
            return;
        }

        double subtotal = listaPedido.stream().mapToDouble(Producto::getPrecio).sum();
        double desc = 0;
        Cliente c = InventarioGlobal.getClienteActivo();
        StringBuilder ticket = new StringBuilder("GEN POS - VENTA FINALIZADA\n\n");

        if (c != null) {
            // Aplicar descuento si se canjearon puntos
            if (InventarioGlobal.isUsarPuntosEnVenta()) {
                desc = c.getPuntos() * 0.1;
                c.setPuntos(0); // El cliente gastó su saldo
                ticket.append("Descuento Puntos: -$").append(String.format("%.2f", desc)).append("\n");
            }

            // Ganar nuevos puntos (1 punto por cada $10 de compra)
            int nuevosPuntos = (int)(subtotal / 10);
            c.setPuntos(c.getPuntos() + nuevosPuntos);

            ticket.append("Cliente: ").append(c.getNombre()).append("\n");
            ticket.append("Nuevos puntos: +").append(nuevosPuntos).append("\n");
            ticket.append("Saldo total: ").append(c.getPuntos()).append(" pts\n\n");
        }

        double totalFinal = Math.max(0, subtotal - desc);
        ticket.append("TOTAL PAGADO: $").append(String.format("%.2f", totalFinal));

        mostrarAlerta("Ticket", ticket.toString());

        // Limpiar para la siguiente transacción
        listaPedido.clear();
        InventarioGlobal.setClienteActivo(null);
        InventarioGlobal.setUsarPuntosEnVenta(false);
        actualizarTotales();
    }

    // NAVEGACIÓN
    @FXML
    public void abrirVentanaCliente(ActionEvent e) {
        // Ahora abrimos primero la ventana de identificación
        cambiarVista(e, "ClienteIdentificarView.fxml", "Identificar Cliente");
    }

    @FXML
    public void abrirInventario(ActionEvent e) {
        cambiarVista(e, "InventarioView.fxml", "Inventario");
    }

    private void cambiarVista(ActionEvent event, String fxml, String titulo) {
        try {
            // Se usa la diagonal inicial para asegurar la ruta en el .jar o recursos
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/abd/puntodeventa/" + fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Mantener el tamaño de la ventana de Aby
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(scene);
            stage.setTitle(titulo);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error crítico al cargar: " + fxml);
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