package common.proyectogenerico.dbcomponent;

import java.sql.*;

public class DBImplementation implements DBcomponent {
    private final DBpoolManager poolManager;
    private final DBconfig config;
    private boolean instanciaIniciada = false;

    //dos maneras de cargar la configuracion:

    //constructor por defecto (sin parametros)
    //se usa cuando la configuracion viene de un archivo (.properties)
    public DBImplementation() {
        this.config = new DBconfig();
        this.poolManager = new DBpoolManager();
    }

    //constructor con parametros personalizados
    public DBImplementation(DBconfig config) {
        //la config viene de una config proporcionada por el usuario, no el . properties (usar para pruebas)
        this.config = config;
        this.poolManager = new DBpoolManager();
    }

//metodos para inicializar y cerrar:

    //metodo para inicializar el pool 
    public void inicializarPool() throws SQLException {
        //si el booleano de la instancia es falsa, se inicial el pool
        if (!instanciaIniciada) {
            poolManager.crearPool(config);
            instanciaIniciada = true;
        }
    }

    //metodo para cerrar el pool
    public void cerrarPool() throws SQLException {
        //si el booleano de la instancia es verdadero, se cierra el pool
        if (instanciaIniciada) {
            poolManager.getPool().desconectarPool();
            instanciaIniciada = false;
        }
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
    public int ejecutarUpdate(String query) throws SQLException {
        try (Connection con = getConex();
            Statement stmt = con.createStatement()) {
            return stmt.executeUpdate(query);
        }
    }

//metodos para conseguir conexiones:
    
    //metodo para conseguir conexiones
    public Connection getConex() throws SQLException {
        //verificar que la instancia este iniciada
        verificarInstancia();
        return poolManager.getConnection();
    }

    //metodo para devolver conexiones
    public void returnConex(Connection conex) {
        poolManager.returnConnection(conex);
    }

    //auxiliar: 

    //metodo para verificar que la instancia este inicializada
    private void verificarInstancia() throws SQLException {
        if (!instanciaIniciada) {
            throw new SQLException("DBcomponent no ha sido iniciado, llamar a metodo inicializar");
        }
    }

    //metodo para probar la conexion
    public boolean probarConexion() throws SQLException {
        try (Connection con = getConex()) {
            //timeout de 3 segundos para verificar que la conexion todavia este disponible
            return con.isValid(3);
        }
    }

}
