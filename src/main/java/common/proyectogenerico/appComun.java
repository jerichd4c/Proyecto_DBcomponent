package common.proyectogenerico;

import common.proyectogenerico.dbcomponent.core.*;
import common.proyectogenerico.dbcomponent.implementation.*;
import java.sql.*;

//demostracion del uso del DBcomponent
public class appComun {
    public static void main(String[] args) {
        //Config inicial de la BDD
        DBconfig postgresConfig = DBconfigLoader.cargarConfig("/dbConfig.properties", DBtype.GENERICA);

        //creacion del componente 
        DBinterface postgresComponent = new componenteGenerico(postgresConfig);

        try {

        //1. iniciar pool
        postgresComponent.inicializar();
        //2. probar conexion
        if (!postgresComponent.probarConexion()) {
            System.err.println("Conexion no disponible");
            return;
        }
        //3. ejecutar query
        try (ResultSet rs=postgresComponent.ejecutarQuery("SELECT * FROM tabla")) {

            while (rs.next()) {
                System.out.println("id: " + rs.getInt("id"));
                System.out.println("name: " + rs.getString("descript"));
            }
        }

    } catch (Exception e) {
        //mensaje de error
        e.printStackTrace();
    } finally {
        try {
            //4. cerrar el pool cuando se deje de usar
            postgresComponent.cerrar();
        } catch (Exception e) {
            //mensaje de error
            e.printStackTrace();
            }
        }   
    }
}