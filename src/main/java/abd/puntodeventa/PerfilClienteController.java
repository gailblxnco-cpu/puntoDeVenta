package abd.puntodeventa;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class PerfilClienteController {

    @FXML private Label lblNombreCliente;
    @FXML private Label lblTelefonoCliente;
    @FXML private Label lblPuntos;

    @FXML
    public void initialize() {
        // Obtenemos al cliente que se identificó en la pantalla anterior
        Cliente clienteActual = InventarioGlobal.getClienteActivo();

        if (clienteActual != null) {
            lblNombreCliente.setText(clienteActual.getNombre());
            lblTelefonoCliente.setText(clienteActual.getTelefono());
            lblPuntos.setText(String.valueOf(clienteActual.getPuntos()));
        } else {
            lblNombreCliente.setText("Sin cliente seleccionado");
            lblPuntos.setText("0");
        }
    }

    @FXML
    public void aplicarPuntos(ActionEvent event) {
        Cliente clienteActual = InventarioGlobal.getClienteActivo();

        if (clienteActual != null && clienteActual.getPuntos() > 0) {
            // 1. Calculamos el valor del descuento para informar al usuario
            double valorDescuento = clienteActual.getPuntos() * 0.1;

            // 2. Activamos la bandera global para que MainController sepa que debe restar
            InventarioGlobal.setUsarPuntosEnVenta(true);

            // 3. Informamos con una alerta
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Puntos Aplicados");
            alert.setHeaderText("Descuento de $" + String.format("%.2f", valorDescuento) + " activado");
            alert.setContentText("El descuento se aplicará automáticamente al total de la venta.");
            alert.showAndWait();

            // 4. Volvemos a la caja
            regresarACaja(event);

        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Saldo Insuficiente");
            alert.setHeaderText(null);
            alert.setContentText("El cliente no tiene puntos acumulados para canjear.");
            alert.showAndWait();
        }
    }

    @FXML
    public void regresarACaja(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            double anchoContenido = stage.getScene().getWidth();
            double altoContenido = stage.getScene().getHeight();

            Scene scene = new Scene(root, anchoContenido, altoContenido);

            stage.setScene(scene);
            stage.setTitle("GEN POS - caja");
            stage.show();

        } catch (IOException e) {
            System.err.println("Error al regresar a la Caja Principal.");
            e.printStackTrace();
        }
    }
}