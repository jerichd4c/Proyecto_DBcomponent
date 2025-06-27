package common.proyectogenerico;

import common.proyectogenerico.dbcomponent.core.*;
import common.proyectogenerico.dbcomponent.implementation.*;
import common.proyectogenerico.dbcomponent.util.DBqueryManager;
import java.sql.*;

//demostracion del uso del DBcomponent
public class appDemo {
    public static void main(String[] args) {
        //configurar dos BDD diferentes
        DBconfig config1 = DBconfigLoader.cargarConfig("/primarydbconfig.properties", DBtype.GENERICA);
        DBconfig config2 = DBconfigLoader.cargarConfig("/secondarydbconfig.properties", DBtype.GENERICA);
        //crear los componentes
       componenteGenerico primaryDB = new componenteGenerico(config1);
       componenteGenerico secondaryDB = new componenteGenerico(config2);
        
        try {
            //se inicializan los componentes
            primaryDB.inicializar();
            secondaryDB.inicializar();

            //verificar las propiedades y relaciones entre las BDD  
            verificarBBD(primaryDB, secondaryDB);

            //hacer operaciones de la BDD
            hacerOperacionesCRUDs(primaryDB);



            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

        }










    }

    //metodo para verficiar las propiedades y relaciones entre las BDD
    private static void verificarBBD(componenteGenerico bdd1, componenteGenerico bdd2) throws SQLException {
        
        System.out.println("---Verificando base de datos---");

        //info basica

        System.out.println("Base de datos 1: " + bdd1.getConexInfo());
        System.out.println("Base de datos 2: " + bdd2.getConexInfo());

        //diferentes casos para la verificacion

        //caso 1: si ambos componentes apuntan a la MISMA bdd (ej: dbcomp1 y 2 apuntan a bdd registro)
        if (bdd1.getDBidentifier().equals(bdd2.getDBidentifier())) {
            System.out.println("Los DBcomponent apuntan a la MISMA BDD");
        } else {
            System.out.println("Los DBcomponent apuntan a DIFERENTES BDD");
        }
        System.out.println(" ");
    }

    //metodo para realizar las operacion CRUD basicas del DBcomponent 
    private static void hacerOperacionesCRUDs(componenteGenerico bdd) throws SQLException {
    
         System.out.println("---Haciendo operaciones CRUD en: " + bdd.getDBidentifier() + "---");

         //1. crear las tablas en BDD 1
        bdd.ejecutarUpdate(DBqueryManager.obtenerQueries("genericDB", "create_usuarios_table"));
        System.out.println("Tabla 'usuarios' creada!");

        //2. insertar usuarios en BDD1

        String insertarSQL = DBqueryManager.obtenerQueries("genericDB", "insert_usuarios");
        bdd.ejecutarUpdate(insertarSQL, "Fulano Mengano", "superman@gmail.com");
        //metodo ejecutarUpdate se usa cuando se quiere actualizar por ejemplo, una tabla
        System.out.println("Usuario 'Fulano Mengano' insertado!");

        //3. consultar usuario
        //manejar el query Con un resultSet
        try (ResultSet rs= bdd.ejecutarQuery(DBqueryManager.obtenerQueries("genericDB", "select_usuarios"))) {
            System.out.println("Usuarios: ");
            //recorrer el resultSet hasta que ya no haya datos
            while (rs.next()) {
                System.out.println(rs.getString("name") + " - " + rs.getString("email"));
            }
        }
    }


    }