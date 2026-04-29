module abd.puntodeventa {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires atlantafx.base;

    opens abd.puntodeventa to javafx.fxml;
    exports abd.puntodeventa;
}