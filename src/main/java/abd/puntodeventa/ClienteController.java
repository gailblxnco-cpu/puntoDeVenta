package abd.puntodeventa;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class ClienteController {

    // Campos para la ventana de Identificación
    @FXML private TextField txtTelefonoBusqueda;

    // Campos para la ventana de Registro
    @FXML private TextField txtNuevoNombre;
    @FXML private TextField txtNuevoTelefono;

    // --- LÓGICA DE IDENTIFICACIÓN (Búsqueda) ---
    @FXML
    public void buscarCliente(ActionEvent event) {
        String telefono = txtTelefonoBusqueda.getText();

        if (telefono == null || telefono.trim().isEmpty()) {
            mostrarAlerta("Campo requerido", "Por favor, ingrese un número de teléfono.");
            return;
        }

        // Buscar en la lista global de clientes
        for (Cliente c : InventarioGlobal.getClientes()) {
            if (c.getTelefono().equals(telefono)) {
                // Cliente encontrado: lo activamos para la venta actual
                InventarioGlobal.setClienteActivo(c);
                cambiarVista(event, "PerfilClienteView.fxml", "Perfil del Cliente");
                return;
            }
        }

        mostrarAlerta("No encontrado", "No existe un cliente con ese número. Proceda al registro.");
    }

    // --- LÓGICA DE REGISTRO ---
    @FXML
    public void registrarCliente(ActionEvent event) {
        String nombre = txtNuevoNombre.getText();
        String telefono = txtNuevoTelefono.getText();

        if (nombre.isEmpty() || telefono.isEmpty()) {
            mostrarAlerta("Campos incompletos", "Debe llenar el nombre y el teléfono para registrar.");
            return;
        }

        // Crear y añadir el nuevo cliente
        Cliente nuevoCliente = new Cliente(nombre, telefono, 0);
        InventarioGlobal.getClientes().add(nuevoCliente);

        // Seleccionarlo automáticamente para la venta
        InventarioGlobal.setClienteActivo(nuevoCliente);

        mostrarAlerta("Registro Exitoso", "El cliente ha sido registrado y seleccionado.");
        cambiarVista(event, "PerfilClienteView.fxml", "Perfil del Cliente");
    }

    // --- NAVEGACIÓN ENTRE VISTAS ---

    @FXML
    public void irARegistro(ActionEvent event) {
        cambiarVista(event, "ClienteRegistroView.fxml", "Registrar Nuevo Cliente");
    }

    @FXML
    public void irAIdentificacion(ActionEvent event) {
        cambiarVista(event, "ClienteIdentificarView.fxml", "Identificar Cliente");
    }

    @FXML
    public void cerrarVentana(ActionEvent event) {
        // Al cancelar, nos aseguramos de limpiar cualquier cliente a medias
        InventarioGlobal.setClienteActivo(null);
        cambiarVista(event, "MainView.fxml", "Gen POS - Caja Principal");
    }

    // --- MÉTODOS AUXILIARES (UX Mejorado) ---

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Cambia la vista manteniendo el tamaño actual de la ventana
     * para que la transición sea fluida y profesional.
     */
    private void cambiarVista(ActionEvent event, String archivoFXML, String titulo) {
        try {
            // Usamos la ruta absoluta desde la raíz de resources para evitar errores
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/abd/puntodeventa/" + archivoFXML));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Obtenemos dimensiones actuales para que la ventana no "salte"
            double ancho = stage.getWidth();
            double alto = stage.getHeight();

            Scene scene = new Scene(root, ancho, alto);

            // Si tienes estilos globales, puedes volver a cargarlos aquí si es necesario
            // scene.getStylesheets().add(getClass().getResource("estilos.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle(titulo);
            stage.show();

        } catch (IOException e) {
            System.err.println("Error crítico al cargar la vista: " + archivoFXML);
            e.printStackTrace();
        }
    }
}