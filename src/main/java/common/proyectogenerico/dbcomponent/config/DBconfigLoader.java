package common.proyectogenerico.dbcomponent.config;

import java.io.*;
import java.util.*;

//clase para cargar la configuracion desde .properties
public class DBconfigLoader {
    public static DBconfig cargarConfig(String path, DBtype bdd) {

        System.out.println("Buscando archivo de configuración"+ path);

        InputStream input = null;

        try {
        
        //buscar .properties desde el sistema de archivos para BDD especifica
        if (input == null) {
            File file = new File("src/main/resources/" + path);
            if (file.exists()) {
            input = new FileInputStream(file);
            }   
        }

        if (input == null) {
            throw new RuntimeException("Archivo de configuración no encontrado");
        }

        Properties properties= new Properties();
        properties.load(input);

        //aplicar metodos de la clase DBconfig
        return new DBconfig()
            .getTipoDeBDD(bdd)
            .getUrl(properties.getProperty("db.url"))
            .getCredentials(
                properties.getProperty("db.user"),
                properties.getProperty("db.password")
            )
            .getPoolSettings(
                Integer.parseInt(properties.getProperty("db.minConexiones")),
                Integer.parseInt(properties.getProperty("db.maxConexiones")),
                Integer.parseInt(properties.getProperty("db.incrementoConex"))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}