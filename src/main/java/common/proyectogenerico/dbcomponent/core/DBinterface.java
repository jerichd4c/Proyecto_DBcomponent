package common.proyectogenerico.dbcomponent.core;

//driver JDBC para acceder y manipular la BDD
import java.sql.*;

//crear interfaz que se usara en la implementacion del DBcomponent (implements y firmas)
public interface DBinterface {

    //firmas:

    //transacciones
    void iniciarTransaccion() throws SQLException;
    void commitTransaccion() throws SQLException;   
    void rollbackTransaccion() throws SQLException;

    //ejectar consultas
    ResultSet ejecutarQuery (String query) throws SQLException;
    int ejecutarUpdate (String query) throws SQLException;

    //poolManager
    void inicializar() throws SQLException;
    void cerrar() throws SQLException;

    //config 
    DBconfig getConfig();

    //metodos auxiliares
    boolean probarConexion() throws SQLException;
    String conseguirVersionBDD () throws SQLException;
    
}
