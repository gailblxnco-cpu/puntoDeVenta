package abd.puntodeventa;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    // Datos de tu esquema proporcionado
    private static final String URL = "jdbc:mysql://localhost:3306/cafeteria_is";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}