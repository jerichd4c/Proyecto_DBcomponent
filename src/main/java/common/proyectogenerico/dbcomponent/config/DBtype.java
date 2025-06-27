package common.proyectogenerico.dbcomponent.config;

//clase que define/enumera las distintas tipos de BDD relacionales, no tiene otra funcionalidad
public enum DBtype {
    POSTGRESQL,
    MYSQL,
    SQLite,
    ORACLE,
    SQL_SERVER,
    //en caso de que la BDD no se encuentre definitda en esta clase
    GENERICA
}