package common.proyectogenerico.dbcomponent.core;

//clase que define/enumera las distintas tipos de BDD relacionales
public enum DBtype {
    POSTGRESQL,
    MYSQL,
    SQLite,
    ORACLE,
    SQL_SERVER,
    //en caso de que la BDD no se encuentre definitda en esta clase
    GENERICA
}