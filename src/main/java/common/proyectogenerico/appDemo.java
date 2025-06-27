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

            //hacer operaciones de la BDD 1
            hacerOperacionesCRUDs(primaryDB);

            //hacer operaciones de la BDD 2
            hacerOperacionesLOGs(secondaryDB);

            //testear transacciones
            hacerOperacionesTransacciones(primaryDB, secondaryDB);
            
        } catch (SQLException e) {
             System.out.println(e.getMessage());
        } finally {
            limpiarTablas(primaryDB, secondaryDB);
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

    //metodo para realizar las operacion CRUD basicas del DBcomponent (en BDD 1)
    private static void hacerOperacionesCRUDs(componenteGenerico bdd) throws SQLException {
    
         System.out.println("---Haciendo operaciones CRUD en: " + bdd.getDBidentifier() + "---");

         //1. crear las tablas en BDD 1
        bdd.ejecutarUpdate(DBqueryManager.obtenerQueries("genericdb", "create_usuarios_table"));
        System.out.println("Tabla 'usuarios' creada!");

        //2. insertar usuarios en BDD1

        String insertarSQL = DBqueryManager.obtenerQueries("genericdb", "insert_usuario");
        bdd.ejecutarUpdate(insertarSQL, "Fulano Mengano", "superman@gmail.com");
        //metodo ejecutarUpdate se usa cuando se quiere actualizar por ejemplo, una tabla
        System.out.println("Usuario 'fulano Mengano' insertado!");

        //3. consultar usuario
        //manejar el query Con un resultSet
        try (ResultSet rs= bdd.ejecutarQuery(DBqueryManager.obtenerQueries("genericdb", "select_usuarios"))) {
            System.out.println("Usuarios: ");
            //recorrer el resultSet hasta que ya no haya datos
            while (rs.next()) {
                System.out.println(rs.getString("name") + " - " + rs.getString("email"));
            }
        }
    }

    //metodo para realizar las operacion LOGs basicas del DBcomponent (en BDD 2)
    //NT: logs=registros
    private static void hacerOperacionesLOGs(componenteGenerico bdd) throws SQLException {
    
         System.out.println("---Haciendo operaciones LOGs en: " + bdd.getDBidentifier() + "---");
         
         //1. crear la tabla de logs en BDD 2
         bdd.ejecutarUpdate(DBqueryManager.obtenerQueries("genericdb", "create_registros_table"));
         System.out.println("Tabla 'registros' creada!");

         //2, insertar LOGs en BDD2
         String insertarRegistro = DBqueryManager.obtenerQueries("genericdb", "insert_registros");
         bdd.ejecutarUpdate(insertarRegistro, 1, "login");
         bdd.ejecutarUpdate(insertarRegistro, 1, "perfil_visto");
         System.out.println("Registros insertados!");

         //3. Consultar LOGs 
         //manejar el query Con un resultSet
         try (ResultSet rs= bdd.ejecutarQuery(DBqueryManager.obtenerQueries("genericDB", "select_registros"))) {
            System.out.println("Registros: ");
            //recorrer el resultSet hasta que ya no haya datos
            while (rs.next()) {
                System.out.println(rs.getString("user_id") + " - " + rs.getString("accion") + " - " + rs.getTimestamp("timestamp"));
            }
        }
    }

    //metodo para demostrar los multiples metodos de transacciones
    private static void hacerOperacionesTransacciones(componenteGenerico primaryDB, componenteGenerico secondaryDB) throws SQLException {
        
        System.out.println("---Haciendo operaciones de transacciones entre BDDs---");

        try {
            //iniciar transacciones
            primaryDB.iniciarTransaccion();
            secondaryDB.iniciarTransaccion();

            //insertar usuario en BDD 1
            String insertarSQL = DBqueryManager.obtenerQueries("genericdb", "insert_usuario");
            primaryDB.ejecutarUpdate(insertarSQL, "Yu Narukari", "persona4au@hotmail.com");
            System.out.println("Usuario 'Yu Narukari' insertado!");

            //registrar creacion en BDD 2
            String insertarRegistro = DBqueryManager.obtenerQueries("genericdb", "insert_registros");
            secondaryDB.ejecutarUpdate(insertarRegistro, 5, "usuario_creado");
            System.out.println("Registro de creacion insertado!");

            //confirmar transacciones (commit)
            primaryDB.commitTransaccion();
            secondaryDB.commitTransaccion();
            System.out.println("Transacciones commiteadas exitosamente!");
        } catch (SQLException e) {
            //Si hay un error en la transaccion, cancelarla (rollback)
            primaryDB.rollbackTransaccion();
            secondaryDB.rollbackTransaccion();
            System.out.println("Error en la transaccion, haciendo rollback!");
        }
    }

    //metodo para borrar las pruebas hechas y cerrar los componentes (incluye shutdown)
    private static void limpiarTablas(componenteGenerico... BDDs) {
        System.out.println("---Programa finalizado, Limpiando pruebas---");
        try {
        //bucle for que itera sobre todos los componentes
        for (componenteGenerico bdd : BDDs) {
            //le hace CASCADE a las dos tablas para borrarlas
            bdd.ejecutarUpdate(DBqueryManager.obtenerQueries("genericdb", "delete_registros_viejos"));
            bdd.cerrar();
        }   
        System.out.println("datos y tablas limpiadas!");
        } catch (SQLException e) {
           System.out.println("ERROR: " + e.getMessage());
        }
    }
}
