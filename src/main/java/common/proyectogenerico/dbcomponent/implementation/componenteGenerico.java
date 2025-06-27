package common.proyectogenerico.dbcomponent.implementation;

import common.proyectogenerico.dbcomponent.core.*;
import common.proyectogenerico.dbcomponent.pool.*;
import java.sql.*;

//implementacion de las firmas del DBcomponent
public class componenteGenerico implements DBinterface {

    private final DBpoolManager poolManager;
    private final DBconfig config;
    
    //variables para transacciones
    private Connection transaccionConex;
    private boolean transaccionEnCurso = false;

    //dos maneras de cargar la configuracion:

    //constructor con parametros personalizados

    public componenteGenerico() {
        this.config = new DBconfig();
        this.poolManager = new DBpoolManager();
    }

    //constructor por defecto (sin parametros)
    //se usa cuando la configuracion viene de un archivo (.properties)

    public componenteGenerico(DBconfig config) {
        //la config viene de una config proporcionada por el usuario, no el . properties (usar para pruebas)
        this.config = config;
        this.poolManager = new DBpoolManager();
    }

//metodos para inicializar y cerrar:

    //metodo para inicializar el pool 
    public void inicializar() throws SQLException {
            poolManager.crearPool(config);
        }

    //metodo para cerrar el pool
    public void cerrar() throws SQLException {
        //si el booleano de la transaccion es verdadero, hace un rollback para evitar errores
        if (transaccionEnCurso) {
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
    //IMPORTANTE: Al poner el antes de params, se puede manejar la funcion varargs que permite pasar un numero indefinido de parametros
    public int ejecutarUpdate(String query, Object... params) throws SQLException {
        try (Connection con = getConex();
        //IMPORTANTE: Se tiene que preparar un statement para asi poder insertar dos variables en un campo de la tabla
        PreparedStatement pstmt = con.prepareStatement(query)) {
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
            //evitar transacciones anidadas
            throw new SQLException("Ya hay una transaccion en curso");
        }
        transaccionConex= poolManager.getConnection();
        //evita cambios de autocommit en la BDD
        transaccionConex.setAutoCommit(false);
        transaccionEnCurso = true;
    }

    //metodo para commit 
    public void commitTransaccion() throws SQLException {
        if (!transaccionEnCurso) {
            throw new SQLException("No hay ninguna transaccion en curso");
        }
        //al terminar la transaccion, se realiza el commit
        transaccionConex.commit();
        terminarTransaccion();
    }

    //metodo para terminar una transaccion
    private void terminarTransaccion() throws SQLException {
        //se hacen los cambios en la BDD
        transaccionConex.setAutoCommit(true);
        //se devuelve la conexion al pool
        poolManager.returnConnection(transaccionConex);
        //se vacian las variables
        transaccionConex= null;
        transaccionEnCurso = false;
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
        //NT: operador ? sirve como un if-else resumido
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
            //formato esperado: String, String, String, String
            //consigue: nombre de la 
            return String.format ("%s, %s, %s, %s", conex.getMetaData().getDatabaseProductName(), conex.getCatalog(), config.getUrl(), conex.getMetaData().getDatabaseProductVersion());
        }
    }

    //auxiliar: 

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
