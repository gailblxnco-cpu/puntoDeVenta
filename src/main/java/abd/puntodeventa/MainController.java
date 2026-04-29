package abd.puntodeventa;

// Imports de JavaFX y AtlantaFX
import atlantafx.base.theme.PrimerDark;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

// Imports para MySQL
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

                Button btnItem = new Button(p.getNombre() + "\n$" + p.getPrecio());
                btnItem.getStyleClass().addAll("button", "accent", "outlined");
                btnItem.setPrefSize(130, 80);

                btnItem.setOnAction(e -> agregarAlPedido(p));

                flowProductos.getChildren().add(btnItem);
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar productos para venta.");
            e.printStackTrace();
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

        double totalConDescuento = totalActual - descuentoActual;
        if(totalConDescuento < 0) totalConDescuento = 0;

        String sqlPedido = "{CALL SP_CrearPedido(?, ?, ?, ?)}";
        String sqlDetalle = "{CALL SP_RegistrarDetalle(?, ?, ?, ?)}";

        try (Connection conn = ConexionDB.getConnection()) {
            // Transacción manual
            conn.setAutoCommit(false);

            // 1. Crear el Pedido
            int idPedidoGenerado = 0;
            try (CallableStatement stmtPedido = conn.prepareCall(sqlPedido)) {
                stmtPedido.setDouble(1, totalConDescuento);

                // Obtenemos el empleado de la sesión global (Asegúrate de tener la clase SesionActiva.java)
                stmtPedido.setInt(2, SesionActiva.getIdEmpleado());

                Cliente cActivo = SesionActiva.getClienteActivo();
                if (cActivo != null) {
                    stmtPedido.setInt(3, cActivo.getIdCliente());
                } else {
                    stmtPedido.setNull(3, Types.INTEGER);
                }

                stmtPedido.registerOutParameter(4, Types.INTEGER);
                stmtPedido.execute();
                idPedidoGenerado = stmtPedido.getInt(4);
            }

            // 2. Insertar los detalles
            try (CallableStatement stmtDetalle = conn.prepareCall(sqlDetalle)) {
                for (Producto p : listaPedido) {
                    stmtDetalle.setInt(1, idPedidoGenerado);
                    stmtDetalle.setInt(2, p.getId());
                    stmtDetalle.setInt(3, 1);
                    stmtDetalle.setDouble(4, p.getPrecio());
                    stmtDetalle.executeUpdate();
                }
            }

            conn.commit();
            mostrarAlerta("Éxito", "Venta #" + idPedidoGenerado + " procesada correctamente.", Alert.AlertType.INFORMATION);

            // Limpieza
            listaPedido.clear();
            SesionActiva.setClienteActivo(null);
            SesionActiva.setUsarPuntosEnVenta(false);
            descuentoActual = 0.0;
            lblClienteActivo.setText("Cliente: Ninguno");
            lblDescuento.setText("Descuento: $0.00");
            actualizarTotales();
            cargarInventarioEnPantalla();

        } catch (SQLException e) {
            mostrarAlerta("Error crítico", "Fallo al procesar la venta. Transacción cancelada.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
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