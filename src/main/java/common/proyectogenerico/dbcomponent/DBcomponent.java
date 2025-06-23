package common.proyectogenerico.dbcomponent;

//driver JDBC para acceder y manipular la BDD
import java.sql.*;

//crear interfaz que se usara en la implementacion del DBcomponent (implements)
public interface DBcomponent {

    //conseguir conexiones de la BDD
    Connection getConex() throws SQLException;
    void returnConex(Connection conex);

    //ejectar consultas
    ResultSet ejecutarQuery (String query) throws SQLException;
    int ejecutarUpdate (String query) throws SQLException;

    //poolManager
    void inicializarPool() throws SQLException;
    void cerrarPool() throws SQLException;

    //metodos auxiliares
    boolean probarConexion() throws SQLException;
    
}
