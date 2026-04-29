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
        // Datos simulados (Aquí jalarías los datos del cliente real)
        lblNombreCliente.setText("Aby");
        lblTelefonoCliente.setText("833-123-4567");
        lblPuntos.setText("150");
    }

    @FXML
    public void aplicarPuntos(ActionEvent event) {
        // Lógica para enviar descuento a MainController...
        mostrarAlerta("Descuento Aplicado", "Se han descontado 150 puntos ($15.00 MXN) de la venta actual.", Alert.AlertType.INFORMATION);
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