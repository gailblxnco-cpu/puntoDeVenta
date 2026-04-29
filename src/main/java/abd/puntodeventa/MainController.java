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
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class MainController {

    @FXML private TableView<Producto> tablaPedido;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private FlowPane flowProductos;
    @FXML private TextField txtBuscar;
    @FXML private Label lblTotal, lblSubtotal, lblClienteActivo, lblDescuento;

    private ObservableList<Producto> listaPedido = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(d -> d.getValue().nombreProperty());
        colPrecio.setCellValueFactory(d -> d.getValue().precioProperty().asObject());
        tablaPedido.setItems(listaPedido);

        // Llenar botones desde MySQL
        cargarInventarioEnPantalla();
        actualizarTotales();
    }

    private void cargarInventarioEnPantalla() {
        flowProductos.getChildren().clear();
        String sql = "{CALL SP_ObtenerProductosVenta()}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Producto p = new Producto(
                        rs.getInt("idInventario"),
                        rs.getString("nombreProducto"),
                        rs.getDouble("precioUnitario"),
                        rs.getInt("stockDisponible")
                );

                Button btnItem = new Button(p.getNombre() + "\n$" + p.getPrecio() + "\nStock: " + p.getStock());
                btnItem.getStyleClass().add("product-button");

                // Evento al dar clic en el botón
                btnItem.setOnAction(e -> agregarAlCarrito(p));
                flowProductos.getChildren().add(btnItem);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar el menú de bebidas.");
        }
    }

    private void agregarAlCarrito(Producto p) {
        // Validamos físicamente el stock antes de añadirlo a la tabla
        long cantidadEnCarrito = listaPedido.stream().filter(item -> item.getId() == p.getId()).count();
        if (cantidadEnCarrito < p.getStock()) {
            listaPedido.add(p);
            actualizarTotales();
        } else {
            mostrarAlerta("Stock insuficiente", "No quedan más unidades de " + p.getNombre() + " en inventario.");
        }
    }

    private void actualizarTotales() {
        double subtotal = listaPedido.stream().mapToDouble(Producto::getPrecio).sum();
        double descEfectivo = 0;
        Cliente activo = InventarioGlobal.getClienteActivo();

        if (activo != null) {
            lblClienteActivo.setText("Cliente: " + activo.getNombre());
            if (InventarioGlobal.isUsarPuntosEnVenta()) {
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

        // Calculamos el descuento si se activó el uso de puntos
        if (cliente != null && InventarioGlobal.isUsarPuntosEnVenta()) {
            descuento = cliente.getPuntos() * 0.1;
        }

        double totalFinal = Math.max(0, subtotal - descuento);

        // === INICIO DE TRANSACCIÓN ATÓMICA ===
        try (Connection conn = ConexionDB.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int idPedidoGenerado = 0;

                // 1. Crear el Pedido (Cabecera)
                try (CallableStatement stmtCabecera = conn.prepareCall("{CALL SP_CrearPedido(?, ?, ?, ?)}")) {
                    stmtCabecera.setDouble(1, totalFinal);
                    stmtCabecera.setInt(2, SesionActiva.getIdEmpleado());

                    if (cliente != null) {
                        stmtCabecera.setInt(3, cliente.getIdCliente());
                    } else {
                        stmtCabecera.setNull(3, java.sql.Types.INTEGER);
                    }

                    stmtCabecera.registerOutParameter(4, java.sql.Types.INTEGER);
                    stmtCabecera.execute();
                    idPedidoGenerado = stmtCabecera.getInt(4);
                }

                // 2. Registrar Detalles y Restar Stock
                try (CallableStatement stmtDetalle = conn.prepareCall("{CALL SP_RegistrarDetalle(?, ?, ?, ?)}")) {
                    for (Producto p : listaPedido) {
                        stmtDetalle.setInt(1, idPedidoGenerado);
                        stmtDetalle.setInt(2, p.getId());
                        stmtDetalle.setInt(3, 1);
                        stmtDetalle.setDouble(4, p.getPrecio());
                        stmtDetalle.addBatch();
                    }
                    stmtDetalle.executeBatch();
                }

                // 3. Gestión de Puntos de Fidelidad
                if (cliente != null) {
                    // Calculamos puntos ganados: 1 por cada $10 gastados (sobre el total final)
                    int puntosGanados = (int) (totalFinal / 10);

                    // Si usó puntos, su saldo inicial para esta cuenta es 0, si no, es lo que ya tenía
                    int saldoActual = InventarioGlobal.isUsarPuntosEnVenta() ? 0 : cliente.getPuntos();
                    int nuevoSaldoTotal = saldoActual + puntosGanados;

                    try (CallableStatement stmtPuntos = conn.prepareCall("{CALL SP_ActualizarPuntosCliente(?, ?)}")) {
                        stmtPuntos.setInt(1, cliente.getIdCliente());
                        stmtPuntos.setInt(2, nuevoSaldoTotal);
                        stmtPuntos.executeUpdate();
                    }

                    // Actualizamos el objeto en memoria para que la interfaz refleje el cambio
                    cliente.setPuntos(nuevoSaldoTotal);
                }

                // Si todo fue exitoso, guardamos en la BD
                conn.commit();

                mostrarAlerta("Venta Exitosa", "Ticket #" + idPedidoGenerado +
                        "\nTotal: $" + String.format("%.2f", totalFinal) +
                        (cliente != null ? "\nNuevo saldo de puntos: " + cliente.getPuntos() : ""));

                // Limpiar mesa de trabajo
                listaPedido.clear();
                InventarioGlobal.setClienteActivo(null);
                InventarioGlobal.setUsarPuntosEnVenta(false);
                actualizarTotales();
                cargarInventarioEnPantalla();

            } catch (SQLException ex) {
                conn.rollback(); // Si algo falla, no se registra nada ni se restan puntos
                throw ex;
            }

        } catch (SQLException e) {
            mostrarAlerta("Error de Venta", "Ocurrió un error al procesar el pago: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void volverAlMenu(ActionEvent event) {
        cambiarVista(event, "MenuPrincipalView.fxml", "Menú Principal");
    }

    @FXML
    public void abrirVentanaCliente(ActionEvent event) {
        cambiarVista(event, "ClienteIdentificarView.fxml", "Identificar Cliente");
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

    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}