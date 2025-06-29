package common.proyectogenerico;

import common.proyectogenerico.dbcomponent.config.*;
import common.proyectogenerico.dbcomponent.implementation.*;
import common.proyectogenerico.dbcomponent.util.*;
import java.sql.*;

/**
 * Clase de demostración principal para el componente de base de datos.
 * <p>
 * Esta clase muestra las capacidades del DBComponent mediante operaciones CRUD,
 * gestión de LOGs y transacciones entre BDDs.
 * </p>
 */

public class appDemo {

    /**
     * Punto de entrada principal de la aplicación.
     * <p>
     * Configura dos bases de datos, realiza operaciones de demostración y limpia los recursos.
     * </p>
     */

    public static void main(String[] args) {

        //configurar dos BDD diferentes
        //GENERICA: bdd RELACIONAL estandard (postgres, mysql, sqlite, oracle, sqlserver)
        DBconfig config1 = DBconfigLoader.cargarConfig("/primarydbconfig.properties", DBtype.GENERICA);
        DBconfig config2 = DBconfigLoader.cargarConfig("/secondarydbconfig.properties", DBtype.GENERICA);
        //mejora visual
        System.out.println(" ");


        //declarar variables para no ser afectadas por try-catch y el finally
        componenteGenerico primaryDB = null;
        componenteGenerico secondaryDB = null;

        try {
        //IMPORTANTE: esto NO seran los metodos, esto solo son metodos que agrupan las funcionalidades basicas del DBcomponent 

        //crear los componentes

        primaryDB = new componenteGenerico(config1);
        secondaryDB = new componenteGenerico(config2);
        

            //se inicializan los componentes
            primaryDB.inicializar();
            secondaryDB.inicializar();

            //verificar las propiedades y relaciones entre las BDD  
            System.out.println("===1. Verificando base de datos===");
            verificarBBD(primaryDB, secondaryDB);

            //hacer operaciones de la BDD 1
            System.out.println("===2. Haciendo operaciones CRUD (primera BDD)===");
            hacerOperacionesCRUDs(primaryDB);

            //hacer operaciones de la BDD 2
            System.out.println("===3. Haciendo operaciones de registro_actividades (segunda BDD)===");
            hacerOperacionesLOGs(secondaryDB);

            //testear transacciones
            System.out.println("===4. Haciendo operaciones de transacciones entre diferentes BDDs)===");
            hacerOperacionesTransacciones(primaryDB, secondaryDB);
            
        } catch (SQLException e) {
             System.out.println(e.getMessage());
        } finally {
            limpiarTablas(primaryDB, secondaryDB);
        }
    }

    /**
     * Verifica y compara las bases de datos configuradas.
     * <p>
     * Muestra información básica de conexión y determina si los componentes
     * apuntan a la misma base de datos o a bases de datos diferentes.
     * </p>
     * 
     * param bdd1 Primer componente de base de datos
     * param bdd2 Segundo componente de base de datos
     * throws SQLException Si ocurre un error al acceder a la información de la base de datos
     */

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

        System.out.println("Nombre de base de datos 1: " + DBname1);
        System.out.println("Nombre de base de datos 2: " + DBname2);

        //diferentes casos para la verificacion

        //caso 1: si ambos componentes apuntan a la MISMA bdd (ej: dbcomp1 y 2 apuntan a bdd registro_actividades)
        if (DBname1.equals(DBname2)) {
            System.out.println("Los DBcomponent apuntan a la MISMA BDD");
        } else {
            System.out.println("Los DBcomponent apuntan a DIFERENTES BDD");
        }
        System.out.println(" ");
    }

    /**
     * Realiza operaciones CRUD básicas en la base de datos especificada.
     * <p>
     * - Crea la tabla de usuarios
     * - Inserta un usuario de prueba
     * - Consulta y muestra los usuarios existentes
     * </p>
     * 
     * param bdd Componente de base de datos donde se ejecutarán las operaciones
     * throws SQLException Si ocurre un error en las operaciones de base de datos
     */

    //metodo para realizar las operacion CRUD basicas del DBcomponent (en BDD 1)
    private static void hacerOperacionesCRUDs(componenteGenerico bdd) throws SQLException {
    
        String DBName = extractDBname(bdd.getConfig().getUrl());

        System.out.println("---Haciendo operaciones CRUDs en: " + DBName + "---");
         
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
        System.out.println(" ");
    }

    /**
     * Realiza operaciones de registro (logs) en la base de datos especificada.
     * <p>
     * - Crea la tabla de registros
     * - Inserta registros de actividad de prueba
     * - Consulta y muestra los registros existentes
     * </p>
     * 
     * param bdd Componente de base de datos donde se ejecutarán las operaciones
     * throws SQLException Si ocurre un error en las operaciones de base de datos
     */

    //metodo para realizar las operacion LOGs basicas del DBcomponent (en BDD 2)
    //NT: logs=registros
    private static void hacerOperacionesLOGs(componenteGenerico bdd) throws SQLException {
        
        String DBName = extractDBname(bdd.getConfig().getUrl());

         System.out.println("---Haciendo operaciones en tabla registro en: " + DBName + "---");
         
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
        System.out.println(" ");
    }

    /**
     * Demuestra transacciones distribuidas entre dos bases de datos.
     * <p>
     * - Inserta un usuario en la primera base de datos
     * - Registra la creación en la segunda base de datos
     * - Muestra los resultados de ambas operaciones
     * </p>
     * 
     * param primaryDB Componente de base de datos principal (para usuarios)
     * param secondaryDB Componente de base de datos secundario (para registros)
     * throws SQLException Si ocurre un error durante las transacciones
     */

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
            secondaryDB.ejecutarUpdate(insertarRegistro, 3, "usuario_creado");
            System.out.println("Registro de creacion insertado!");

            secondaryDB.ejecutarUpdate(insertarRegistro, 4, "usuario_creado");
            System.out.println("Registro de creación 'usuario_creado' insertado!");

            //confirmar transacciones (commit)
            primaryDB.commitTransaccion();
            secondaryDB.commitTransaccion();

            //mostrar tablas en consola:
            System.out.println("Transacciones commiteadas exitosamente!");

            //NT: verificar nombre de variables de las BDDs para no seleccionar una tabla inexistente

            //tabla de BDD1:
            System.out.println("Usuarios:"); 
            try (ResultSet rs= primaryDB.ejecutarQuery(DBqueryManager.obtenerQueries("genericdb", "select_usuarios"))) {
                //recorrer el resultSet hasta que ya no haya datos
                while (rs.next()) {
                    System.out.println(rs.getString("name") + " - " + rs.getString("email"));
                }
            }  

            //tabla de BDD2:
            System.out.println("Registros:"); 
            try (ResultSet rs= secondaryDB.ejecutarQuery(DBqueryManager.obtenerQueries("genericdb", "select_registros"))) {
                //recorrer el resultSet hasta que ya no haya datos
                while (rs.next()) {
                    System.out.println(rs.getString("usuario_id") + " - " + rs.getString("accion") + " - " + rs.getTimestamp("timestamp"));
                }
            }  

        } catch (SQLException e) {
            //Si hay un error en la transaccion, cancelarla (rollback)
            primaryDB.rollbackTransaccion();
            secondaryDB.rollbackTransaccion();
            System.out.println("Error en la transaccion, haciendo rollback!");
            throw e;
        }
        System.out.println(" ");
    }

    /**
     * Limpia las tablas de prueba en las bases de datos.
     * <p>
     * Elimina las tablas creadas durante la demostración y cierra las conexiones.
     * </p>
     * 
     * param BDDs Componentes de base de datos a limpiar
     */

    //metodo para borrar las pruebas hechas y cerrar los componentes (incluye shutdown)
    private static void limpiarTablas(componenteGenerico... BDDs) {
        System.out.println("---Programa finalizado, Limpiando pruebas---");
        try {
        //bucle for que itera sobre todos los componentes
        for (componenteGenerico bdd : BDDs) {
            //si la BDD no esta vacia, borrar tablas
            if (bdd !=null) {
            //sysout para orientacion en la consola
            String dbName = extractDBname(bdd.getConfig().getUrl());
            System.out.println("Limpiando: " + dbName);
            try {
                //le hace CASCADE a las dos tablas para borrarlas
                //se ejecutan las actualizaciones en orden inverso a la creación (de esta manera se evita que tablas se eliminen por dependencias)

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
        System.out.println(" ");
    }

    //auxiliar: obtener nombre de la base de datos de la url

    /**
     * Extrae el nombre de la base de datos de una URL JDBC.
     * <p>
     * Ejemplo: "jdbc:postgresql://localhost:5432/myDB" → "myDB"
     * </p>
     * 
     * param url URL de conexión a la base de datos
     * return Nombre de la base de datos extraído de la URL
     */

    // Método auxiliar para extraer nombre de BD de la URL, mismo metodo que en DBconfig
    private static String extractDBname(String url) {
        if (url.contains("/")) {
            String temp = url.substring(url.lastIndexOf('/') + 1);
            // Eliminar parámetros adicionales (si los hay)
            if (temp.contains("/")) {
                return temp.substring(0, temp.indexOf('/'));
            }
            return temp;
    }
        return "DB_desconocida";
    }
}