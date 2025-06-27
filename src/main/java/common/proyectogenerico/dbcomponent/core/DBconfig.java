package common.proyectogenerico.dbcomponent.core;

import java.util.*;

//clase que carga a configuracion de la Base de datos (sacado de: resources/DBConfig.properties)
public class DBconfig {

    private String DBname;
    private String url;
    private String user;
    private String password;
    private int minConexiones;
    private int maxConexiones;
    private int incrementoConex;

    //nuevas variables para manejar N BDD
    private DBtype tipoDeBDD;
    private Properties propiedadesDriver = new Properties();

    //metodo para obtener el nombre especifico de la BDD

    public DBconfig getDBname(String DBname) {
        this.DBname = DBname;
        //retorna el nombre de la bdd que se esta usando
        return this;
    }

    //metodo para obtener el tipo de BDD
    public DBconfig getTipoDeBDD(DBtype bdd) {
        this.tipoDeBDD= bdd;
        //retorna el tipo de bdd (relacional) que se esta usando
        return this;
    }

    //metodo para obtener la url de la BDD
    public DBconfig getUrl (String url) {
        this.url = url;
        //retorna la url de la bdd que se esta usando
        return this;
    }

    //metodo para obtener las credenciales de la BDD
    public DBconfig getCredentials (String user, String password) {
        this.user = user;
        this.password = password;
        //retorna la password de la bdd que se esta usando
        return this;
    }

    //metodo para obtener los settings del pool
    public DBconfig getPoolSettings( int min, int man, int growth) {
        this.minConexiones = min;
        this.maxConexiones = man;
        this.incrementoConex = growth;
        //retorna los settings del pool de la bdd que se esta usando
        return this;
    }

    //metodo para obtener las propiedades de la bdd (archivo.properties)
    public DBconfig getPropiedades(String key, String valor) {
        //ej1: key = "user" y valor = "postgres"
        //ej2: key = "password" y valor = "1234"
        this.propiedadesDriver.setProperty(key, valor);
        //retorna las propiedades de la bdd que se esta usando
        return this;
    }

    //IMPORTANTE: 
    //metodo para obtener un identificador unico para la config especifica de la instancia de la BDD (BDD + url + DBname)
    public String getUniqueIdentifier() {
        return tipoDeBDD + "_" + url + "_" + DBname;
    }

    //Getters para otros metodos

    public String getDBname() {
        return DBname;
    }
    public String getUrl() {
        return url;
    }
    public String getUser() {
        return user;
    }
    public String getPassword() {
        return password;
    }
    public int getMinConexiones() {
        return minConexiones;
    }
    public int getMaxConexiones() {
        return maxConexiones;
    }
    public int getIncrementoConex() {
        return incrementoConex;
    }
    public DBtype getTipoDeBDD() {
        return tipoDeBDD;
    }
    public Properties getPropiedadesDriver() {
        return propiedadesDriver;
    }
}