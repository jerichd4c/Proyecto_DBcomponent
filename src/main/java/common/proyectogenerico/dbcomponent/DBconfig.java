package common.proyectogenerico.dbcomponent;

import java.io.*;
import java.util.*;

//clase que carga a configuracion de la Base de datos (sacado de: resources/DBConfig.properties)
public class DBconfig {
    private String url;
    private String user;
    private String password;
    private int minConexiones;
    private int maxConexiones;
    private int incrementoConex;

    //constructor con las propiedades iniciales
    public DBconfig() {
        cargarConfigDefault();
    }

//metodo privado para cargar la config en la BDD (logica interna)
private void cargarConfigDefault() {
    //getClass: obtiene la clase asignada a la instancia actual
    //getClassLoader: devuelve el classLoader de la clase actual
    //getResourceAsStream: devuelve un InputStream para el recurso especificado (propiedades)
    // try (InputStream input = getClass().getClassLoader().getResourceAsStream(ARCHIVO_CONFIG)) {

    System.out.println("Buscando archivo de configuración");

        InputStream input = null;

    try {
        
        //buscar .properties desde el sitema de archivos
        if (input == null) {
            File file = new File("src/main/resources/dbconfig.properties");
            if (file.exists()) {
            input = new FileInputStream(file);
            }   
        }

        if (input == null) {
            throw new RuntimeException("Archivo de configuración no encontrado");
        }

        Properties properties= new Properties();
        properties.load(input);

        this.url = properties.getProperty("db.url");
        this.user = properties.getProperty("db.user");
        this.password = properties.getProperty("db.password");
        this.minConexiones = Integer.parseInt(properties.getProperty("db.minConexiones"));
        this.maxConexiones = Integer.parseInt(properties.getProperty("db.maxConexiones"));
        this.incrementoConex = Integer.parseInt(properties.getProperty("db.incrementoConex"));
        System.out.println("Configuración cargada exitosamente");
    } catch (IOException e) {
        throw new RuntimeException("Error al cargar la configuracion inicial", e);
        } finally {
            if (input != null) {
                try { input.close(); } catch (IOException e) {}
            }
        }
    }

    //Getters para otros metodos
    public String getUrl() {
        return url;
    }
    public String getUser() {
        return user;
    }
    public String getPassword() {
        return password;
    }
    public int getMinConexiones() {
        return minConexiones;
    }
    public int getMaxConexiones() {
        return maxConexiones;
    }
    public int getIncrementoConex() {
        return incrementoConex;
    }
}