package abd.puntodeventa;

import atlantafx.base.theme.PrimerDark;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class PerfilClienteController {

    @FXML private Label lblNombreCliente;
    @FXML private Label lblTelefonoCliente;
    @FXML private Label lblPuntos;

    @FXML
    public void initialize() {
        Cliente c = SesionActiva.getClienteActivo();
        if (c != null) {
            lblNombreCliente.setText(c.getNombre());
            lblTelefonoCliente.setText(c.getTelefono());
            lblPuntos.setText(String.valueOf(c.getPuntos()));
        }
    }

    @FXML
    public void aplicarPuntos(ActionEvent event) {
        SesionActiva.setUsarPuntosEnVenta(true);

        Cliente c = SesionActiva.getClienteActivo();
        double descuentoAproximado = c.getPuntos() * 0.10;

        mostrarAlerta("Descuento Activado", "Se aplicará un descuento de $" + String.format("%.2f", descuentoAproximado) + " MXN en la venta actual.", Alert.AlertType.INFORMATION);
        regresarACaja(event);
    }

    @FXML
    public void continuarSinPuntos(ActionEvent event) {
        SesionActiva.setUsarPuntosEnVenta(false);
        regresarACaja(event);
    }

    @FXML
    public void regresarACaja(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
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