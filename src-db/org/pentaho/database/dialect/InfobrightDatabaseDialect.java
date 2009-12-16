package org.pentaho.database.dialect;

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseType;

public class InfobrightDatabaseDialect extends MySQLDatabaseDialect {

  public static final IDatabaseType DBTYPE = 
    new DatabaseType(
        "Infobright", 
        "INFOBRIGHT", 
        DatabaseAccessType.getList(
            DatabaseAccessType.NATIVE, 
            DatabaseAccessType.ODBC, 
            DatabaseAccessType.JNDI
        ), 
        1526, 
        "http://dev.mysql.com/doc/refman/5.0/en/connector-j-reference-configuration-properties.html"
    );

}
