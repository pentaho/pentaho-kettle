package org.pentaho.database.dialect;

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.ValueMetaInterface;

public class OracleDatabaseDialect extends AbstractDatabaseDialect {
  
  public static final IDatabaseType DBTYPE = 
    new DatabaseType(
        "Oracle", 
        "ORACLE", 
        DatabaseAccessType.getList(
            DatabaseAccessType.NATIVE, 
            DatabaseAccessType.ODBC, 
            DatabaseAccessType.OCI, 
            DatabaseAccessType.JNDI
        ), 
        1521, 
        "http://download.oracle.com/docs/cd/B19306_01/java.102/b14355/urls.htm#i1006362"
      );
  
  public IDatabaseType getDatabaseType() {
    return DBTYPE;
  }

  @Override
  public String getNativeDriver() {
    return "oracle.jdbc.driver.OracleDriver";
  }
  
  /**
   * @return Whether or not the database can use auto increment type of fields (pk)
   */
  @Override
  public boolean supportsAutoInc()
  {
    return false;
  }
  
  /**
   * @see org.pentaho.di.core.database.DatabaseInterface#getLimitClause(int)
   */
  @Override
  public String getLimitClause(int nrRows)
  {
    return " WHERE ROWNUM <= "+nrRows;
  }
  
  /**
   * Returns the minimal SQL to launch in order to determine the layout of the resultset for a given database table
   * @param tableName The name of the table to determine the layout for
   * @return The SQL to launch.
   */
  @Override
  public String getSQLQueryFields(String tableName)
  {
      return "SELECT /*+FIRST_ROWS*/ * FROM "+tableName+" WHERE ROWNUM < 1";
  }
  
  @Override
  public String getSQLTableExists(String tablename)
  {
      return getSQLQueryFields(tablename);
  }
  
  @Override
  public String getSQLColumnExists(String columnname, String tablename)
  {
      return  getSQLQueryColumnFields(columnname, tablename);
  }
  
  public String getSQLQueryColumnFields(String columnname, String tableName)
  {
      return "SELECT /*+FIRST_ROWS*/ " + columnname + " FROM "+tableName +" WHERE ROWNUM < 1";
  }

  @Override
  public boolean needsToLockAllTables()
  {
        return false;
  }
  
  @Override
  public String getURL(IDatabaseConnection databaseConnection) throws KettleDatabaseException
  {
    String databaseName = databaseConnection.getDatabaseName();
    String port = databaseConnection.getDatabasePort();
    String hostname = databaseConnection.getHostname();
      
    if (databaseConnection.getAccessType()==DatabaseAccessType.ODBC)
    {
      return "jdbc:odbc:"+databaseName;
    }
    else
    if (databaseConnection.getAccessType()==DatabaseAccessType.NATIVE)
    {
      // the database name can be a SID (starting with :) or a Service (starting with /)
      //<host>:<port>/<service>
      //<host>:<port>:<SID>
      if (databaseName != null && databaseName.length()>0 && 
          (databaseName.startsWith("/") || databaseName.startsWith(":"))) {
        return getNativeJdbcPre() + hostname + ":" + port + databaseName;
      }
      else if (Const.isEmpty(port) && 
          (Const.isEmpty(port) || port.equals("-1"))) {  //-1 when file based stored connection
        // support RAC with a self defined URL in databaseName like
        // (DESCRIPTION = (ADDRESS = (PROTOCOL = TCP)(HOST = host1-vip)(PORT = 1521))(ADDRESS = (PROTOCOL = TCP)(HOST = host2-vip)(PORT = 1521))(LOAD_BALANCE = yes)(CONNECT_DATA =(SERVER = DEDICATED)(SERVICE_NAME = db-service)(FAILOVER_MODE =(TYPE = SELECT)(METHOD = BASIC)(RETRIES = 180)(DELAY = 5))))
        // or (DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=PRIMARY_NODE_HOSTNAME)(PORT=1521))(ADDRESS=(PROTOCOL=TCP)(HOST=SECONDARY_NODE_HOSTNAME)(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=DATABASE_SERVICENAME)))
        // or (DESCRIPTION=(FAILOVER=ON)(ADDRESS_LIST=(LOAD_BALANCE=ON)(ADDRESS=(PROTOCOL=TCP)(HOST=xxxxx)(PORT=1526))(ADDRESS=(PROTOCOL=TCP)(HOST=xxxx)(PORT=1526)))(CONNECT_DATA=(SERVICE_NAME=somesid)))
        return getNativeJdbcPre()+databaseName;
      }
      else {
        // by default we assume a SID
        return getNativeJdbcPre()+hostname+":"+port+":"+databaseName;
      }
    }
    else // OCI
    {
      // Let's see if we have an database name
      if (databaseName != null && databaseName.length()>0)
      {
          // Has the user specified hostname & port number?
          if (hostname!=null && hostname.length()>0 && port!=null && port.length()>0) {
              // User wants the full url
              return "jdbc:oracle:oci:@(description=(address=(host="+hostname+")(protocol=tcp)(port="+port+"))(connect_data=(sid="+databaseName+")))";
          } else {
              // User wants the shortcut url
              return "jdbc:oracle:oci:@"+databaseName;
          }               
      }
      else
      {
          throw new KettleDatabaseException("Unable to construct a JDBC URL: at least the database name must be specified");
      }
    }
  }
    
  public String getNativeJdbcPre() {
    return "jdbc:oracle:thin:@";
  }
  
  /**
   * Oracle doesn't support options in the URL, we need to put these in a Properties object at connection time...
   */
  @Override
  public boolean supportsOptionsInURL()
  {
      return false;
  }

  /**
   * @return true if the database supports sequences
   */
  @Override
  public boolean supportsSequences()
  {
    return true;
  }

  /**
   * Check if a sequence exists.
   * @param sequenceName The sequence to check
   * @return The SQL to get the name of the sequence back from the databases data dictionary
   */
  @Override
  public String getSQLSequenceExists(String sequenceName)
  {
      return "SELECT * FROM USER_SEQUENCES WHERE SEQUENCE_NAME = '"+sequenceName.toUpperCase()+"'";
  }
  
  /**
   * Get the current value of a database sequence
   * @param sequenceName The sequence to check
   * @return The current value of a database sequence
   */
  @Override
  public String getSQLCurrentSequenceValue(String sequenceName)
  {
      return "SELECT "+sequenceName+".currval FROM DUAL";
  }

  /**
   * Get the SQL to get the next value of a sequence. (Oracle only) 
   * @param sequenceName The sequence name
   * @return the SQL to get the next value of a sequence. (Oracle only)
   */
  @Override
  public String getSQLNextSequenceValue(String sequenceName)
  {
      return "SELECT "+sequenceName+".nextval FROM dual";
  }

  /**
   * @return true if we need to supply the schema-name to getTables in order to get a correct list of items.
   */
  @Override
  public boolean useSchemaNameForTableList()
  {
    return true;
  }

  /**
   * @return true if the database supports synonyms
   */
  @Override
  public boolean supportsSynonyms()
  {
    return true;
  }

  /**
   * Generates the SQL statement to add a column to the specified table
   * @param tablename The table to add
   * @param v The column defined as a value
   * @param tk the name of the technical key field
   * @param use_autoinc whether or not this field uses auto increment
   * @param pk the name of the primary key field
   * @param semicolon whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to add a column to the specified table
   */
  @Override
  public String getAddColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon)
  {
    return "ALTER TABLE "+tablename+" ADD ( "+getFieldDefinition(v, tk, pk, use_autoinc, true, false)+" ) ";
  }

  /**
   * Generates the SQL statement to drop a column from the specified table
   * @param tablename The table to add
   * @param v The column defined as a value
   * @param tk the name of the technical key field
   * @param use_autoinc whether or not this field uses auto increment
   * @param pk the name of the primary key field
   * @param semicolon whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to drop a column from the specified table
   */
  @Override
  public String getDropColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon)
  {
    return "ALTER TABLE "+tablename+" DROP ( "+v.getName()+" ) "+Const.CR;
  }

  /**
   * Generates the SQL statement to modify a column in the specified table
   * @param tablename The table to add
   * @param v The column defined as a value
   * @param tk the name of the technical key field
   * @param use_autoinc whether or not this field uses auto increment
   * @param pk the name of the primary key field
   * @param semicolon whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to modify a column in the specified table
   */
  @Override
  public String getModifyColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon)
  {
        ValueMetaInterface tmpColumn = v.clone(); 
        int threeoh = v.getName().length()>=30 ? 30 : v.getName().length();
        
        tmpColumn.setName(v.getName().substring(0,threeoh)+"_KTL"); // should always be less then 35
        
        String sql="";
        
        // Create a new tmp column
        sql+=getAddColumnStatement(tablename, tmpColumn, tk, use_autoinc, pk, semicolon)+";"+Const.CR;
        // copy the old data over to the tmp column
        sql+="UPDATE "+tablename+" SET "+tmpColumn.getName()+"="+v.getName()+";"+Const.CR;
        // drop the old column
        sql+=getDropColumnStatement(tablename, v, tk, use_autoinc, pk, semicolon)+";"+Const.CR;
        // create the wanted column
        sql+=getAddColumnStatement(tablename, v, tk, use_autoinc, pk, semicolon)+";"+Const.CR;
        // copy the data from the tmp column to the wanted column (again)  
        // All this to avoid the rename clause as this is not supported on all Oracle versions
        sql+="UPDATE "+tablename+" SET "+v.getName()+"="+tmpColumn.getName()+";"+Const.CR;
        // drop the temp column
        sql+=getDropColumnStatement(tablename, tmpColumn, tk, use_autoinc, pk, semicolon);
        
        return sql;
  }

  public String getFieldDefinition(ValueMetaInterface v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr)
  {
    StringBuffer retval=new StringBuffer(128);
    
    String fieldname = v.getName();
    int    length    = v.getLength();
    int    precision = v.getPrecision();
    
    if (add_fieldname) retval.append(fieldname).append(' ');
    
    int type         = v.getType();
    switch(type)
    {
    case ValueMetaInterface.TYPE_DATE   : retval.append("DATE"); break;
    case ValueMetaInterface.TYPE_BOOLEAN: retval.append("CHAR(1)"); break;
    case ValueMetaInterface.TYPE_NUMBER : 
        case ValueMetaInterface.TYPE_BIGNUMBER: 
      retval.append("NUMBER"); 
      if (length>0)
      {
        retval.append('(').append(length);
        if (precision>0)
        {
          retval.append(", ").append(precision);
        }
        retval.append(')');
      }
      break;
    case ValueMetaInterface.TYPE_INTEGER:  
      retval.append("INTEGER"); 
      break;      
    case ValueMetaInterface.TYPE_STRING:
      if (length>=DatabaseMeta.CLOB_LENGTH)
      {
        retval.append("CLOB");
      }
      else
      {
        if (length==1) {
          retval.append("CHAR(1)");
        } else if (length>0 && length<=2000)
        {
          retval.append("VARCHAR2(").append(length).append(')');
        }
        else
        {
                    if (length<=0)
                    {
                        retval.append("VARCHAR2(2000)"); // We don't know, so we just use the maximum...
                    }
                    else
                    {
                        retval.append("CLOB"); 
                    }
        }
      }
      break;
        case ValueMetaInterface.TYPE_BINARY: // the BLOB can contain binary data.
            {
                retval.append("BLOB");
            }
            break;
    default:
      retval.append(" UNKNOWN");
      break;
    }
    
    if (add_cr) retval.append(Const.CR);
    
    return retval.toString();
  }
  
  /* (non-Javadoc)
   * @see com.ibridge.kettle.core.database.DatabaseInterface#getReservedWords()
   */
  @Override
  public String[] getReservedWords()
  {
    return new String[] 
       {
      "ACCESS", "ADD", "ALL", "ALTER", "AND", "ANY", "ARRAYLEN", "AS", "ASC", "AUDIT", "BETWEEN",
      "BY", "CHAR", "CHECK", "CLUSTER", "COLUMN", "COMMENT", "COMPRESS", "CONNECT", "CREATE", "CURRENT", "DATE",
      "DECIMAL", "DEFAULT", "DELETE", "DESC", "DISTINCT", "DROP", "ELSE", "EXCLUSIVE", "EXISTS", "FILE", "FLOAT",
      "FOR", "FROM", "GRANT", "GROUP", "HAVING", "IDENTIFIED", "IMMEDIATE", "IN", "INCREMENT", "INDEX", "INITIAL",
      "INSERT", "INTEGER", "INTERSECT", "INTO", "IS", "LEVEL", "LIKE", "LOCK", "LONG", "MAXEXTENTS", "MINUS",
      "MODE", "MODIFY", "NOAUDIT", "NOCOMPRESS", "NOT", "NOTFOUND", "NOWAIT", "NULL", "NUMBER", "OF", "OFFLINE",
      "ON", "ONLINE", "OPTION", "OR", "ORDER", "PCTFREE", "PRIOR", "PRIVILEGES", "PUBLIC", "RAW", "RENAME",
      "RESOURCE", "REVOKE", "ROW", "ROWID", "ROWLABEL", "ROWNUM", "ROWS", "SELECT", "SESSION", "SET", "SHARE",
      "SIZE", "SMALLINT", "SQLBUF", "START", "SUCCESSFUL", "SYNONYM", "SYSDATE", "TABLE", "THEN", "TO", "TRIGGER",
      "UID", "UNION", "UNIQUE", "UPDATE", "USER", "VALIDATE", "VALUES", "VARCHAR", "VARCHAR2", "VIEW", "WHENEVER",
      "WHERE", "WITH"
     };
  }
  
  /**
   * @return The SQL on this database to get a list of stored procedures.
   */
  @Override
  public String getSQLListOfProcedures(IDatabaseConnection connection)
  {
    return  "SELECT DISTINCT DECODE(package_name, NULL, '', package_name||'.')||object_name FROM user_arguments"; 
  }

  @Override
  public String getSQLLockTables(String tableNames[])
  {
      StringBuffer sql=new StringBuffer(128);
      for (int i=0;i<tableNames.length;i++)
      {
          sql.append("LOCK TABLE ").append(tableNames[i]).append(" IN EXCLUSIVE MODE;").append(Const.CR);
      }
      return sql.toString();
  }
    
  @Override
  public String getSQLUnlockTables(String tableNames[])
  {
      return null; // commit handles the unlocking!
  }

  @Override
  public String[] getUsedLibraries()
  {
      return new String[] { "ojdbc14.jar", "orai18n.jar" };
  }
  
  public IDatabaseConnection createNativeConnection(String jdbcUrl) {
    if (!jdbcUrl.startsWith(getNativeJdbcPre())) {
      throw new RuntimeException("JDBC URL " + jdbcUrl + " does not start with " + getNativeJdbcPre());
    }
    DatabaseConnection dbconn = new DatabaseConnection();
    dbconn.setDatabaseType(getDatabaseType());
    dbconn.setAccessType(DatabaseAccessType.NATIVE);
    String str = jdbcUrl.substring(getNativeJdbcPre().length());
    String hostname = null;
    String port = null;
    String databaseName = null;
    
    // hostname:port:dbname
    // OR
    // dbname
    // OR
    // hostname:dbname
    
    // TODO: Support Parameters

    if (str.indexOf(":") >= 0) {
      hostname = str.substring(0, str.indexOf(":"));
      str = str.substring(str.indexOf(":") + 1);
      if (str.indexOf(":") >= 0) {
        port = str.substring(0, str.indexOf(":")); 
        databaseName = str.substring(str.indexOf(":")+1);
      } else {
        port = str;
      }
    } else {
        databaseName = str;
    }
    
    if (hostname != null) {
      dbconn.setHostname(hostname);
    }
    if (port != null) {
      dbconn.setDatabasePort(port);
    }
    if (databaseName != null) {
      // note, oracle does not support url attributes
      dbconn.setDatabaseName(databaseName);
    }
    return dbconn;
  }

}
