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


        //declarar variables para no ser afectadas por try-catch y finally
         componenteGenerico primaryDB = null;
        componenteGenerico secondaryDB = null;



        try {

        //crear los componentes

        primaryDB = new componenteGenerico(config1);
        secondaryDB = new componenteGenerico(config2);
        

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

        //info basica:

        //url

        System.out.println("Base de datos 1: " + bdd1.getConfig().getUrl());
        System.out.println("Base de datos 2: " + bdd2.getConfig().getUrl());

        //extraer nombres de las BDDs de la url

        String DBname1 = extractDBname(bdd1.getConfig().getUrl());
        String DBname2 = extractDBname(bdd2.getConfig().getUrl());

        System.out.println("Base de datos 1: " + DBname1);
        System.out.println("Base de datos 2: " + DBname2);

        //diferentes casos para la verificacion

        //caso 1: si ambos componentes apuntan a la MISMA bdd (ej: dbcomp1 y 2 apuntan a bdd registro)
        if (DBname1.equals(DBname2)) {
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
        System.out.println("Usuario 'Fulano Mengano' insertado!");

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
         String insertarRegistro = DBqueryManager.obtenerQueries("genericdb", "insert_registro");
         bdd.ejecutarUpdate(insertarRegistro, 1, "login");
         bdd.ejecutarUpdate(insertarRegistro, 2, "perfil_visto");
         System.out.println("Registros insertados!");

         //3. Consultar LOGs 
         //manejar el query Con un resultSet
         try (ResultSet rs= bdd.ejecutarQuery(DBqueryManager.obtenerQueries("genericDB", "select_registros"))) {
            System.out.println("Registros: ");
            //recorrer el resultSet hasta que ya no haya datos
            while (rs.next()) {
                System.out.println(rs.getString("usuario_id") + " - " + rs.getString("accion") + " - " + rs.getTimestamp("timestamp"));
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
            String insertarRegistro = DBqueryManager.obtenerQueries("genericdb", "insert_registro");
            secondaryDB.ejecutarUpdate(insertarRegistro, 2, "usuario_creado");
            System.out.println("Registro de creacion insertado!");

            //obtener el último ID insertado en usuarios
            int ultimoUsuarioID;
            try (ResultSet rs = primaryDB.ejecutarQuery("SELECT MAX(id) FROM usuarios")) {
                rs.next();
                //retorna el indice de la columna
                ultimoUsuarioID = rs.getInt(1);
            }

            secondaryDB.ejecutarUpdate(insertarRegistro, ultimoUsuarioID, "usuario_creado");
            System.out.println("Registro de creación 'usuario_creado' insertado!");

            //confirmar transacciones (commit)
            primaryDB.commitTransaccion();
            secondaryDB.commitTransaccion();
            System.out.println("Transacciones commiteadas exitosamente!");
        } catch (SQLException e) {
            //Si hay un error en la transaccion, cancelarla (rollback)
            primaryDB.rollbackTransaccion();
            secondaryDB.rollbackTransaccion();
            System.out.println("Error en la transaccion, haciendo rollback!");
            throw e;
        }
    }

    //metodo para borrar las pruebas hechas y cerrar los componentes (incluye shutdown)
    private static void limpiarTablas(componenteGenerico... BDDs) {
        System.out.println("---Programa finalizado, Limpiando pruebas---");
        try {
        //bucle for que itera sobre todos los componentes
        for (componenteGenerico bdd : BDDs) {
            //si la BDD no esta vacia, borrar tablas
            if (bdd !=null) {
            try {
                //le hace CASCADE a las dos tablas para borrarlas
                //se ejecutan las actualizaciones en orden inverso a la creación (primero eliminar dependencias)

                    bdd.ejecutarUpdate(DBqueryManager.obtenerQueries("genericdb", "delete_tabla_registros"));

                    bdd.ejecutarUpdate(DBqueryManager.obtenerQueries("genericdb", "delete_tabla_usuarios"));

                    bdd.cerrar();
            } catch (SQLException e) {
            System.out.println("Error limpiando BD " + bdd.getDBidentifier() + ": " + e.getMessage());
                }   
            }   
        }
        System.out.println("datos y tablas limpiadas!");
        } catch (Exception e) {
           System.out.println("ERROR: " + e.getMessage());
        }
    }

    //auxiliar: obtener nombre de la base de datos de la url

    // Método auxiliar para extraer nombre de BD de la URL
    private static String extractDBname(String url) {
        if (url.contains("/")) {
            String temp = url.substring(url.lastIndexOf('/') + 1);
            // Eliminar parámetros adicionales (si los hay)
            if (temp.contains("?")) {
                return temp.substring(0, temp.indexOf('?'));
            }
            return temp;
    }
        return "DB_desconocida";
    }
}
