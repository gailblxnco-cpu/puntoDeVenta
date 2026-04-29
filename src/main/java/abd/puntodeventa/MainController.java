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
    @FXML private Label lblSubtotal;
    @FXML private Label lblDescuento;
    @FXML private Label lblTotal;
    @FXML private TextField txtBuscar;
    @FXML private FlowPane flowProductos;
    @FXML private Label lblCajero;

    private ObservableList<Producto> listaPedido = FXCollections.observableArrayList();
    private double totalActual = 0.0;
    private double descuentoActual = 0.0;

    @FXML
    public void initialize() {
        // Mostrar cajero real de la sesión
        if (SesionActiva.getNombre() != null) {
            lblCajero.setText("Sesión: " + SesionActiva.getNombre());
        }

        colNombre.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        colPrecio.setCellValueFactory(cellData -> cellData.getValue().precioProperty().asObject());

        // BLOQUEOS DE SEGURIDAD
        tablaPedido.setEditable(false);
        colNombre.setReorderable(false);
        colPrecio.setReorderable(false);
        colNombre.setResizable(false);
        colPrecio.setResizable(false);

        // AMARRE DE ANCHO: 70% y 30%, restando 2px para evitar barras horizontales
        colNombre.prefWidthProperty().bind(tablaPedido.widthProperty().multiply(0.70).subtract(2));
        colPrecio.prefWidthProperty().bind(tablaPedido.widthProperty().multiply(0.30).subtract(2));

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
                btnItem.getStyleClass().addAll("button", "accent", "elevated-1");
                btnItem.setPrefSize(130, 80);

                btnItem.setOnAction(e -> agregarAlPedido(p));
                flowProductos.getChildren().add(btnItem);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void agregarAlPedido(Producto p) {
        listaPedido.add(p);
        actualizarTotales();
    }

    @FXML
    public void quitarArticulo(ActionEvent event) {
        Producto seleccionado = tablaPedido.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            listaPedido.remove(seleccionado);
            actualizarTotales();
            tablaPedido.getSelectionModel().clearSelection();
        } else {
            mostrarAlerta("Atención", "Selecciona un artículo del ticket para quitarlo.", Alert.AlertType.WARNING);
        }
    }

    private void actualizarTotales() {
        totalActual = listaPedido.stream().mapToDouble(Producto::getPrecio).sum();

        double descuentoReal = 0.0;
        Cliente cActivo = SesionActiva.getClienteActivo();

        if (cActivo != null && SesionActiva.isUsarPuntosEnVenta()) {
            double descuentoMaximo = cActivo.getPuntos() * 0.10;
            descuentoReal = Math.min(totalActual, descuentoMaximo);
        }

        descuentoActual = descuentoReal;
        double totalConDescuento = totalActual - descuentoReal;

        lblSubtotal.setText(String.format("$%.2f", totalActual));
        lblDescuento.setText(String.format("🎁 Descuento: -$%.2f", descuentoActual));
        lblTotal.setText(String.format("$%.2f", totalConDescuento));
    }

    @FXML
    public void handlePago(ActionEvent event) {
        if (listaPedido.isEmpty()) {
            mostrarAlerta("Error", "El pedido está vacío.", Alert.AlertType.WARNING);
            return;
        }

        double totalFinal = totalActual - descuentoActual;

        String sqlPedido = "{CALL SP_CrearPedido(?, ?, ?, ?)}";
        String sqlDetalle = "{CALL SP_RegistrarDetalle(?, ?, ?, ?)}";
        String sqlPuntos = "{CALL SP_ActualizarPuntosCliente(?, ?)}";

        try (Connection conn = ConexionDB.getConnection()) {
            conn.setAutoCommit(false);

            int idPedidoGenerado = 0;
            try (CallableStatement stmtPedido = conn.prepareCall(sqlPedido)) {
                stmtPedido.setDouble(1, totalFinal);
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

            try (CallableStatement stmtDetalle = conn.prepareCall(sqlDetalle)) {
                for (Producto p : listaPedido) {
                    stmtDetalle.setInt(1, idPedidoGenerado);
                    stmtDetalle.setInt(2, p.getId());
                    stmtDetalle.setInt(3, 1);
                    stmtDetalle.setDouble(4, p.getPrecio());
                    stmtDetalle.executeUpdate();
                }
            }

            Cliente cActivo = SesionActiva.getClienteActivo();
            int puntosGanadosHoy = 0;

            if (cActivo != null) {
                int puntosActuales = cActivo.getPuntos();
                int puntosGastados = SesionActiva.isUsarPuntosEnVenta() ? (int)(descuentoActual / 0.10) : 0;
                puntosGanadosHoy = (int)(totalFinal / 10.0);

                int nuevosPuntos = puntosActuales - puntosGastados + puntosGanadosHoy;

                try (CallableStatement stmtPuntos = conn.prepareCall(sqlPuntos)) {
                    stmtPuntos.setInt(1, cActivo.getIdCliente());
                    stmtPuntos.setInt(2, nuevosPuntos);
                    stmtPuntos.executeUpdate();
                }
            }

            conn.commit();

            String msj = "Venta #" + idPedidoGenerado + " procesada correctamente.";
            if(cActivo != null) msj += "\nEl cliente acumuló " + puntosGanadosHoy + " puntos nuevos.";

            mostrarAlerta("Éxito", msj, Alert.AlertType.INFORMATION);

            listaPedido.clear();
            SesionActiva.setClienteActivo(null);
            SesionActiva.setUsarPuntosEnVenta(false);
            descuentoActual = 0.0;
            lblClienteActivo.setText("👤 Público General");
            actualizarTotales();
            cargarInventarioEnPantalla();

        } catch (SQLException e) {
            mostrarAlerta("Error crítico", "Fallo al procesar la venta.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void abrirVentanaCliente(ActionEvent event) {
        abrirModalAndWait("ClienteIdentificarView.fxml", "Identificar Cliente");

        Cliente c = SesionActiva.getClienteActivo();
        if (c != null) {
            lblClienteActivo.setText("👤 Cliente: " + c.getNombre());
        } else {
            lblClienteActivo.setText("👤 Público General");
        }

        actualizarTotales();
    }

    @FXML
    public void volverAlMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/abd/puntodeventa/MenuPrincipalView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void abrirModalAndWait(String fxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/abd/puntodeventa/" + fxml));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(titulo);
            stage.setResizable(false);
            stage.showAndWait();
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