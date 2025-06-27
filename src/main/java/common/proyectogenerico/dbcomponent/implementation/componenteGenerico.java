package common.proyectogenerico.dbcomponent.implementation;

import common.proyectogenerico.dbcomponent.config.*;
import common.proyectogenerico.dbcomponent.pool.*;
import java.sql.*;

//implementacion de las firmas del DBcomponent
public class componenteGenerico implements DBinterface {

    private final DBpoolManager poolManager;
    private final DBconfig config;
    
    //variables para transacciones
    private Connection transaccionConex;
    private boolean transaccionEnCurso = false;

    //constructo para cargar la config desde un archivo (.properties)

    public componenteGenerico(DBconfig config) throws SQLException{
        //la config viene de una config proporcionada por el usuario, no el . properties (usar para pruebas)
        this.config = config;
        this.poolManager = new DBpoolManager(config);
    }

//metodos para inicializar y cerrar:

    //metodo para inicializar el pool 
    public void inicializar() throws SQLException {
        //crea el pool con los datos de la config
            poolManager.crearPool(config);
        }

    //metodo para cerrar el pool
    public void cerrar() throws SQLException {
        //si el booleano de la transaccion es verdadero, hace un rollback para evitar errores
        if (transaccionEnCurso) {
            //en caso si se usa el metodo cuando hay una transaccion en curso, se para en seco
            rollbackTransaccion();
        }
        poolManager.getPool().desconectarPool();
    }

//metodos para ejecutar consultas:  

    //metodo para ejecutar consultas
    //NOTA: no se puede manejar con try catch porque cierra el resultSet

    public ResultSet ejecutarQuery(String query) throws SQLException {
        Connection con= getConex();
        Statement stmt = con.createStatement();
        return stmt.executeQuery(query);
    }

    //metodo para ejecutar actualizaciones
    //IMPORTANTE: Al poner el antes de params, se puede maneja varargs que permite pasar un numero indefinido de parametros
    public int ejecutarUpdate(String query, Object... params) throws SQLException {
        Connection con = getConex();
        //IMPORTANTE: Se tiene que preparar un statement para asi poder insertar dos variables en un campo de la tabla
        try (PreparedStatement pstmt = con.prepareStatement(query)) {
        //itera sobre la cantidad de parametros introducidos, realisticamente siempre deberian ser 3, ej: obtenerQuery, nombre, apellido
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
            return pstmt.executeUpdate();
        }
    }

//metodos para las transacciones:

    //metodo para iniciar una transaccion
    public void iniciarTransaccion() throws SQLException {
        if (transaccionEnCurso) {
            //evitar reemplazar una transaccion ya en curso
            throw new SQLException("Ya hay una transaccion en curso");
        }
        transaccionConex= poolManager.getConnection();
        //evita cambios de autocommit en la BDD, es decir, en vez de que sea despues de CADA consulta, se tendra que llamar el metodo commitTransaccion despues para confoirmas todos los commits
        transaccionConex.setAutoCommit(false);
        transaccionEnCurso = true;
    }

    //metodo para el commit 
    public void commitTransaccion() throws SQLException {
        //booleano, si es falso no se puede hacer el commit ya que no hay ninguna transaccion
        if (!transaccionEnCurso) {
            throw new SQLException("No hay ninguna transaccion en curso");
        }
        //al terminar la transaccion, se realiza el commit
        transaccionConex.commit();
        terminarTransaccion();
    }

    //metodo para terminar una transaccion
    private void terminarTransaccion() throws SQLException {
        try {
            //hacer si y solo si la transaccion esta en curso o no esta cerrada 
        if (transaccionConex != null && !transaccionConex.isClosed()) {
   
        //se hacen los cambios en la BDD, en caso de que el commit fallo
        transaccionConex.setAutoCommit(true);
        //se cierra la transsacion
        transaccionConex.close();
            }
        //una vez que se termine la transaccion, se vacian las variables para volver a hacer otra
        } finally {
        //se vacian las variables para hacer otras transacciones
        transaccionConex= null;
        transaccionEnCurso = false;
        }
    }

    //metodo para realizar un rollback de una transaccion
    public void rollbackTransaccion() throws SQLException {
        if (!transaccionEnCurso) {
            throw new SQLException("No hay ninguna transaccion en curso");
        }
        //se revierten los cambios
        transaccionConex.rollback();
        terminarTransaccion();
    }

//metodos para conseguir conexiones y config:

    //metodo para conseguir conexiones
    public Connection getConex() throws SQLException {
        //si la transaccion esta en curso, se usa la transaccion existente, si no, se obtiene del pool
        //NT1: operador ? sirve como un if-else resumido
        //NT2: if transaccion en curso es true, retorna la transaccion existente, si no, obtiene una conexion nueva del pool
        return transaccionEnCurso ? transaccionConex : poolManager.getConnection();
    }

    //metodo para conseguir la config
    public DBconfig getConfig() {
        return config;
    }

//METODOS NUEVOS:

    //metodo para conseguir el identificador de la instancia de la BDD

    public String getDBidentifier() {
        return config.getUniqueIdentifier();
    }

    //metodo para conseguir la info de la conexion a la BDD

    public String getConexInfo() throws SQLException {
        //se intenta el metodo getConex, si se retorna una conexion, se obtiene la info de la misma
        try (Connection conex= getConex()) {
            //extraer nombre de la url para evitar confusiones
            String DBname= extractDBname(config.getUrl());
            //formato esperado: String, String, String, String
            //consigue: nombre de la 
            return String.format ("%s, %s, %s, v:%s", conex.getMetaData().getDatabaseProductName(), DBname, config.getUrl(), conex.getMetaData().getDatabaseProductVersion());
        }
    }

    //auxiliar: 

    //metodo para extraer el nombre de la base de datos de la url
    private String extractDBname(String url) {
        //identificar por slash
        if (url.contains("/")) {
            return url.substring(url.lastIndexOf('/') + 1);
        }
        return "DB_desconocida";
    }

    //metodo para verificar el tipo de BDD
    public String conseguirVersionBDD() throws SQLException {
        try (Connection con = poolManager.getConnection()) {
            //retorna el nombre de la BDD a traves de la metaData de la misma
           return con.getMetaData().getDatabaseProductName();
        }
    }

    //metodo para probar la conexion
    public boolean probarConexion() throws SQLException {
        try (Connection con = poolManager.getConnection()) {
            //timeout de 3 segundos para verificar que la conexion todavia este disponible
            return con.isValid(3);
        }
    }

}
