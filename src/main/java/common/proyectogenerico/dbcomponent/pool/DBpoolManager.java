package common.proyectogenerico.dbcomponent.pool;

import common.proyectogenerico.dbcomponent.config.*;
import java.sql.*;

//clase para manejar el pool (poolManager)
public class DBpoolManager {
    private final DBpoolDeConexiones pool;
    private DBconfig config;

    //constructor del pool manager (pool de conexiones usando config del DBconfig)
    public DBpoolManager(DBconfig config) throws SQLException{
        this.pool = new DBpoolDeConexiones(config);
        this.pool.initialize(config);
    }

    //metodo para crear el pool
    public void crearPool(DBconfig config) throws SQLException {
        this.config = config;
        pool.initialize(config);
    }

    //metodo para obtener conexion (no confundir con metodo de DBinterface)
    public Connection getConnection() throws SQLException {
        return pool.getConnection();
    }

    //metodo para devolver conexion al pool
    public void returnConnection(Connection conex) {
        pool.devolverConexion(conex);
    }

    //metodo para añadir conexiones al pool
    public void addConnection(Connection conex) throws SQLException {
        pool.crecerPool();
    }

    //metodo para obtener instancia del pool
    public DBpoolDeConexiones getPool() {
        return pool;
    }

}
