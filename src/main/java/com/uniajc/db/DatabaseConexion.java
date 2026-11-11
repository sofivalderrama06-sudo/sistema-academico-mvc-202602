package com.uniajc.db;

// 1. Importar las clases necesarias de JDBC
import java.sql.DriverManager; // Importar la clase DriverManager para gestionar la conexión con la base de datos
import java.sql.Connection; // Importar la clase Connection para establecer la conexión con la base de datos
import java.sql.SQLException; // Importar la clase SQLException para manejar errores de conexión

// Otras importaciones necesarias para gestionar archivos de propiedades
import java.io.File; // Importar la clase File para manejar archivos
import java.io.FileInputStream; // Importar la clase FileInputStream para leer archivos de propiedades
import java.io.FileNotFoundException; // Importar la clase FileNotFoundException para manejar errores de archivo no encontrado
import java.io.IOException; // Importar la clase IOException para manejar errores de entrada/salida

import java.util.Properties; // Importar la clase Properties para manejar archivos de propiedades

public class DatabaseConexion {
    // Conexión única (singleton)
    private static Connection connection = null;

    /**
     * Obtiene la conexión a la base de datos. Si no existe, la crea usando
     * config.properties.
     * 
     * @return Objeto Connection listo para usar.
     */
    public static Connection getConnection() {

        Properties properties = new Properties();

        if (connection == null) {
            try {

                // Cargar las propiedades desde el archivo config.properties
                properties.load(new FileInputStream(new File("config.properties")));

                // Leer los parámetros de conexión
                String url = properties.get("URL").toString();
                String user = properties.get("USER").toString();
                String password = properties.get("PASSWORD").toString();

                //System.out.println("URL =>>> " + url);
                //System.out.println("User =>>> " + user);
                //System.out.println("Password =>>> " + password);

                // Establecer la conexión
                connection = DriverManager.getConnection(url, user, password);
                System.out.println("Conexion exitosa.");
            } catch (SQLException error) {
                System.out.println("Failed to establish connection. " + error.getMessage());
            } catch (FileNotFoundException e) {
                System.out.println("config.properties file not found.");
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("Error reading config.properties file.");
                e.printStackTrace();
            }
        }
        return connection;
    }

    /**
     * Cierra la conexión a la base de datos si está abierta.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Connection closed successfully.");
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Failed to close connection. " + e.getMessage());
            }
        }
    }
}
