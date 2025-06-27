package common.proyectogenerico.dbcomponent.pool;

import common.proyectogenerico.dbcomponent.core.*;
import java.sql.*;
import java.util.*;

public class DBpoolDeConexiones {

    //Atributos
    private final LinkedList<Connection> conexionesDisponibles= new LinkedList<>();
    private final LinkedList<Connection> conexionesEnUso= new LinkedList<>();
    private DBconfig config;

    //construtor vacio para ya crear la instancia antes de todos los procesos
    //private DBpoolDeConexiones(){}

    // //metodo para asegurar una sola instancia del pool por BDD
    // public static synchronized DBpoolDeConexiones getInstance() {
    //     //si la instancia no existe, se crea
    //     if (instancia == null) {
    //         instancia = new DBpoolDeConexiones();
    //     }
    //     //si no, la devuelve
    //     return instancia;
    // }

    //nuevo constructor del pool de conexiones

    public DBpoolDeConexiones(DBconfig config)  {
        this.config= config;
        
    }

    //metodo para iniciar el pool con la config inicial (.properties)
    public synchronized void initialize(DBconfig config) throws SQLException {
        this.config = config;

        //crear conexiones iniciales
        for (int i=0; i < config.getMinConexiones(); i++) {
            conexionesDisponibles.add(crearConexionFisica());
        }
    }

    //metodo para crear una conexion fisica (la que se hace en postgres) 
    //1 sola conex
    private Connection crearConexionFisica() throws SQLException {
        //driverManager crea una clase para usar el driver (JDBC)
        return DriverManager.getConnection(
            config.getUrl(),
            config.getUser(),
            config.getPassword()
        );
    }
    
    //metodo para obtener una conexion del pool
    public synchronized Connection getConnection() throws SQLException {

    // connection timeout: variable local para controlar el tiempo de espera de la conexion
        
        //bucle while que se hace cuando ya no hay conexiones disponibles
        while (conexionesDisponibles.isEmpty()) {

        //caso 1: si no hay conexiones disponibles usa el metodo para crecer el pool usando crecerPool
        if (conexionesTotales() < config.getMaxConexiones()) {
        crecerPool();
        }

        //si no hay conexiones disponibles, espera en bloques pequeños
        try {
            //esperar hasta encontrar otra conexion disponible
            wait(100);

        } catch (InterruptedException e) {
            //si se interrumpe la espera, saldra un mensaje de error que se interrumpio el metodo
            Thread.currentThread().interrupt();
            throw new SQLException("Error al esperar la conexion disponible: ");
            }
        }
        
        //referencia al hilo
        synchronized (this) {
        //remueve la primera conexion disponible de la linkedList de conexiones disponibles y la agrega a la linkedList de conexiones usadas
        Connection conn = conexionesDisponibles.removeFirst();
        //lo agrega a la lista de conexiones que se usaron
        conexionesEnUso.add(conn);
        //crea una nuevo conexion disponible
        return conn;
        }
    } 

    //metodo para devolver conexion al pool 
    public void devolverConexion(Connection conn) {
        synchronized (this) {
            //si se remueve la conexion, se añadira a la lista enlazada de conexiones disponibles
            if (conexionesEnUso.remove(conn)) {
                try {

                    //esperar 2 segundos si la conex no esta disponible
                    if (!conn.isClosed() && !conn.isValid(2)) {
                        //resetear la conexion del pool (se devuelve)
                        //si no tiene el autocommit activo, lo activa
                        if (!conn.getAutoCommit()) conn.setAutoCommit(true);
                        conexionesDisponibles.add(conn);
                    } else {
                        //reemplazar conexion en caso de que fue invalida, intenta otra vez
                        conexionesDisponibles.add(crearConexionFisica());
                        }
                    } catch (SQLException ex) {
                        //si no se puede crear una nueva conexion despues de intentar 3 metodos, error definitivo
                        System.err.println("Error: " + ex.getMessage());
                    }
                    notifyAll();
                }
            }
        }
    
    //metodo para desconectar el pool de conexiones de la BDD
    public synchronized void desconectarPool() throws SQLException {
        //recorre la lista de conexiones usadas y las cierra
        for (Connection conn : conexionesEnUso) {
            conn.close();
        }
        //recorre la lista de conexiones disponibles y las cierra
        for (Connection conn : conexionesDisponibles) {
            conn.close();
        }
        //devuelve la lista de conexiones usadas a null, simulando un cierre
        conexionesEnUso.clear();
        //devuelve la lista de conexiones disponibles a null, simulando un cierre
        conexionesDisponibles.clear();
    }

    //metodo para crecer un pool de conexiones 
    public synchronized void crecerPool() throws SQLException {
        int crecimiento = config.getIncrementoConex();
        for (int i=0 ; i<crecimiento ; i++) {
            conexionesDisponibles.add(crearConexionFisica());
        }
    }

    //metodo para verificar el total de conexiones de un pool (auxiliar)
    private synchronized int conexionesTotales() {
        //sumara la cantidad de conexiones disponibles y usadas, para asi tener el total del pool
        return conexionesDisponibles.size() + conexionesEnUso.size();
    }
}