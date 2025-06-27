package common.proyectogenerico.dbcomponent.util;

import java.io.*;
import java.util.*;


public class DBqueryManager {
    private static final Properties queries = new Properties();

    //constructor para cargar el .properties de queries
    static {
        cargarQueries();
    }

    //metodo para cargar queries desde el archivo

    private static void cargarQueries() {

        System.out.println("Buscando archivo de queries");

        InputStream input = null;

        try {
        
        //buscar .properties desde el sistema de archivos
        //mismo folder que el .properties de configuración

        if (input == null) {
            File file = new File("src/main/resources/dbqueries.properties");
            if (file.exists()) {
            input = new FileInputStream(file);
            }   
        }

        if (input == null) {
            throw new RuntimeException("Archivo de configuración no encontrado");
        }

        Properties properties= new Properties();
        properties.load(input);
        queries.putAll(properties);

        } catch (Exception e) {
            throw new RuntimeException("No se pudo cargar el archivo de queries");
        }
    }

    //metodo para obtener las queries una vez ya cargado el archivo 

    public static String obtenerQueries(String dbType, String queryKey) {
        //normaliza el nombre de la llame a buscar en el archivo (lo transforma en minusculas)
        String key= dbType.toLowerCase() + "." + queryKey;
        //en el argumento del metodo, se escribe la llave de la query y la encuentra
        String query = queries.getProperty(key);
        if (query == null) {
            throw new RuntimeException("Query no encontrada: " + key);
        }
        return query;
        }    
    }
