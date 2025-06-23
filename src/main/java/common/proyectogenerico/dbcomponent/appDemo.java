package common.proyectogenerico.dbcomponent;

import java.sql.*;

//codigo standBy para probar el DBcomponent (cambiar mas adelante)
public class appDemo {
    public static void main (String[] args) {

    DBcomponent db = new DBImplementation();
    //probando los metodos del DBcomponent en orden
    try {

        //1. iniciar pool
        db.inicializarPool();
        //2. probar conexion
        if (!db.probarConexion()) {
            System.err.println("Conexion no disponible");
            return;
        }
        //3. ejecutar query
        try (ResultSet rs=db.ejecutarQuery("SELECT * FROM tabla")) {

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
            db.cerrarPool();
        } catch (Exception e) {
            //mensaje de error
            e.printStackTrace();
            }
        }   
    }
}
