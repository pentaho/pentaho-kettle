package org.pentaho.database.dialect;

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

public class DB2DatabaseDialect extends AbstractDatabaseDialect {
  public static final IDatabaseType DBTYPE = 
    new DatabaseType(
        "IBM DB2",
        "DB2",
        DatabaseAccessType.getList(
            DatabaseAccessType.NATIVE, 
            DatabaseAccessType.ODBC, 
            DatabaseAccessType.JNDI
        ), 
        50000, 
        null
    );

  public IDatabaseType getDatabaseType() {
    return DBTYPE;
  }

  @Override
  public String getNativeDriver() {
    return "com.ibm.db2.jcc.DB2Driver";
  }
  
  @Override
  public String getNativeJdbcPre() {
    return "jdbc:db2://";
  }   
  
  @Override
  public boolean supportsSetCharacterStream()
  {
    return false;
  }
  
  @Override
  public String getURL(IDatabaseConnection connection)
    {
    if (connection.getAccessType()==DatabaseAccessType.ODBC) {
      return "jdbc:odbc:"+connection.getDatabaseName();
    }
    else
    {
      return getNativeJdbcPre() + connection.getHostname()+":"+connection.getDatabasePort()+"/"+connection.getDatabaseName();
    }
  }
  

  /**
   * @return true if the database supports schemas, DB2 supports it (v7 and v8 for sure).
   */
  @Override
  public boolean supportsSchemas()
  {
    return true;
  }
  
  /**
   * @param tableName The table to be truncated.
   * @return The SQL statement to truncate a table: remove all rows from it without a transaction
   */
  @Override
  public String getTruncateTableStatement(String tableName)
  {
      return "ALTER TABLE "+tableName+" ACTIVATE NOT LOGGED INITIALLY WITH EMPTY TABLE";
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
    return "ALTER TABLE "+tablename+" ADD COLUMN "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
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
    return "ALTER TABLE "+tablename+" DROP COLUMN "+v.getName()+Const.CR;
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
    String retval="";
    retval+="ALTER TABLE "+tablename+" DROP COLUMN "+v.getName()+Const.CR+";"+Const.CR;
    retval+="ALTER TABLE "+tablename+" ADD COLUMN "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
    return retval;
  }

  @Override
  public String getFieldDefinition(ValueMetaInterface v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr)
  {
    String retval="";
    
    String fieldname = v.getName();
    int    length    = v.getLength();
    int    precision = v.getPrecision();
    
    if (add_fieldname) retval+=fieldname+" ";
    
    int type         = v.getType();
    switch(type)
    {
    case ValueMetaInterface.TYPE_DATE   : retval+="TIMESTAMP"; break;
    case ValueMetaInterface.TYPE_BOOLEAN: retval+="CHARACTER(1)"; break;
    case ValueMetaInterface.TYPE_NUMBER :
        case ValueMetaInterface.TYPE_BIGNUMBER: 
      if (fieldname.equalsIgnoreCase(tk) && use_autoinc) // Technical key: auto increment field!
      {
        retval+="BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1, NOCACHE)";
      } 
      else
      {
        if (length>0)
        {
          retval+="DECIMAL("+length;
          if (precision>0)
          {
            retval+=", "+precision;
          }
          retval+=")";
        }
        else
        {
          retval+="FLOAT";
        }
      }
      break;
    case ValueMetaInterface.TYPE_INTEGER: 
      if (fieldname.equalsIgnoreCase(tk) && use_autoinc) // Technical key: auto increment field!
      {
        retval+="INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1, NOCACHE)";
      } 
      else
      {
        retval+="INTEGER";
      }
      break;      
    case ValueMetaInterface.TYPE_STRING:
      if (length>getMaxVARCHARLength() || length>=DatabaseMeta.CLOB_LENGTH)
      {
        retval+="CLOB";
      }
      else
      {
        retval+="VARCHAR"; 
        if (length>0)
        {
          retval+="("+length;
        }
        else
        {
          retval+="("; // Maybe use some default DB String length?
        }
        retval+=")";

      }
            break;
       case ValueMetaInterface.TYPE_BINARY:
      if (length>getMaxVARCHARLength() || length>=DatabaseMeta.CLOB_LENGTH)
      {
        retval+="BLOB("+length+")";
      }
      else
      {
        if (length>0)
        { 
            retval+="CHAR("+length+") FOR BIT DATA";
        }
        else
        {
          retval+="BLOB"; // not going to work, but very close
        }
      }     
            break;
        default:
      retval+=" UNKNOWN";
      break;
    }

    if (add_cr) retval+=Const.CR;
    
    return retval;
  }

  /* (non-Javadoc)
   * @see DatabaseInterface#getReservedWords()
   */
  @Override
  public String[] getReservedWords()
  {
    return new String[]
    {
      //http://publib.boulder.ibm.com/infocenter/db2luw/v8/index.jsp?topic=/com.ibm.db2.udb.doc/admin/r0001095.htm
      //For portability across the DB2 Universal Database products, the following should be considered reserved words.
      //The following list also contains the ISO/ANSI SQL99 reserved words for future compatibility.
      "ABSOLUTE", "ACTION", "ADD", "ADMIN", "AFTER", "AGGREGATE", "ALIAS", "ALL", "ALLOCATE", "ALLOW", "ALTER", 
      "AND", "ANY", "APPLICATION", "ARE", "ARRAY", "AS", "ASC", "ASSERTION", "ASSOCIATE", "ASUTIME", "AT", 
      "AUDIT", "AUTHORIZATION", "AUX", "AUXILIARY", 
      "BEFORE", "BEGIN", "BETWEEN", "BINARY", "BIT", "BLOB", "BOOLEAN", "BOTH", "BREADTH", "BUFFERPOOL", "BY", 
      "CACHE", "CALL", "CALLED", "CAPTURE", "CARDINALITY", "CASCADE", "CASCADED", "CASE", "CAST", "CATALOG", 
      "CCSID", "CHAR", "CHARACTER", "CHECK", "CLASS", "CLOB", "CLOSE", "CLUSTER", "COLLATE", "COLLATION", 
      "COLLECTION", "COLLID", "COLUMN", "COMMENT", "COMMIT", "COMPLETION", "CONCAT", "CONDITION", "CONNECT", 
      "CONNECTION", "CONSTRAINT", "CONSTRAINTS", "CONSTRUCTOR", "CONTAINS", "CONTINUE", "CORRESPONDING", 
      "COUNT", "COUNT_BIG", "CREATE", "CROSS", "CUBE", "CURRENT", "CURRENT_DATE", "CURRENT_LC_CTYPE", 
      "CURRENT_PATH", "CURRENT_ROLE", "CURRENT_SERVER", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_TIMEZONE", 
      "CURRENT_USER", "CURSOR", "CYCLE", 
      "DATA", "DATABASE", "DATE", "DAY", "DAYS", "DB2GENERAL", "DB2GENRL", "DB2SQL", "DBINFO", "DEALLOCATE", 
      "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DEFAULTS", "DEFERRABLE", "DEFERRED", "DEFINITION", "DELETE", 
      "DEPTH", "DEREF", "DESC", "DESCRIBE", "DESCRIPTOR", "DESTROY", "DESTRUCTOR", "DETERMINISTIC", 
      "DIAGNOSTICS", "DICTIONARY", "DISALLOW", "DISCONNECT", "DISTINCT", "DO", "DOMAIN", "DOUBLE", "DROP", 
      "DSNHATTR", "DSSIZE", "DYNAMIC", 
      "EACH", "EDITPROC", "ELSE", "ELSEIF", "ENCODING", "END", "END-EXEC", "END-EXEC1", "EQUALS", "ERASE", 
      "ESCAPE", "EVERY", "EXCEPT", "EXCEPTION", "EXCLUDING", "EXEC", "EXECUTE", "EXISTS", "EXIT", "EXTERNAL", 
      "FALSE", "FENCED", "FETCH", "FIELDPROC", "FILE", "FINAL", "FIRST", "FLOAT", "FOR", "FOREIGN", "FOUND", 
      "FREE", "FROM", "FULL", "FUNCTION", 
      "GENERAL", "GENERATED", "GET", "GLOBAL", "GO", "GOTO", "GRANT", "GRAPHIC", "GROUP", "GROUPING", 
      "HANDLER", "HAVING", "HOLD", "HOST", "HOUR", "HOURS", 
      "IDENTITY", "IF", "IGNORE", "IMMEDIATE", "IN", "INCLUDING", "INCREMENT", "INDEX", "INDICATOR", "INHERIT", 
      "INITIALIZE", "INITIALLY", "INNER", "INOUT", "INPUT", "INSENSITIVE", "INSERT", "INT", "INTEGER", 
      "INTEGRITY", "INTERSECT", "INTERVAL", "INTO", "IS", "ISOBID", "ISOLATION", "ITERATE", 
      "JAR", "JAVA", "JOIN", 
      "KEY", 
      "LABEL", "LANGUAGE", "LARGE", "LAST", "LATERAL", "LC_CTYPE", "LEADING", "LEAVE", "LEFT", "LESS", "LEVEL", 
      "LIKE", "LIMIT", "LINKTYPE", "LOCAL", "LOCALE", "LOCALTIME", "LOCALTIMESTAMP", "LOCATOR", "LOCATORS", 
      "LOCK", "LOCKMAX", "LOCKSIZE", "LONG", "LOOP", 
      "MAP", "MATCH", "MAXVALUE", "MICROSECOND", "MICROSECONDS", "MINUTE", "MINUTES", "MINVALUE", "MODE", 
      "MODIFIES", "MODIFY", "MODULE", "MONTH", "MONTHS", 
      "NAMES", "NATIONAL", "NATURAL", "NCHAR", "NCLOB", "NEW", "NEW_TABLE", "NEXT", "NO", "NOCACHE", "NOCYCLE", 
      "NODENAME", "NODENUMBER", "NOMAXVALUE", "NOMINVALUE", "NONE", "NOORDER", "NOT", "NULL", "NULLS", "NUMERIC", 
      "NUMPARTS", 
      "OBID", "OBJECT", "OF", "OFF", "OLD", "OLD_TABLE", "ON", "ONLY", "OPEN", "OPERATION", "OPTIMIZATION", 
      "OPTIMIZE", "OPTION", "OR", "ORDER", "ORDINALITY", "OUT", "OUTER", "OUTPUT", "OVERRIDING", 
      "PACKAGE", "PAD", "PARAMETER", "PARAMETERS", "PART", "PARTIAL", "PARTITION", "PATH", "PIECESIZE", "PLAN", 
      "POSITION", "POSTFIX", "PRECISION", "PREFIX", "PREORDER", "PREPARE", "PRESERVE", "PRIMARY", "PRIOR", 
      "PRIQTY", "PRIVILEGES", "PROCEDURE", "PROGRAM", "PSID", "PUBLIC", 
      "QUERYNO", 
      "READ", "READS", "REAL", "RECOVERY", "RECURSIVE", "REF", "REFERENCES", "REFERENCING", "RELATIVE", 
      "RELEASE", "RENAME", "REPEAT", "RESET", "RESIGNAL", "RESTART", "RESTRICT", "RESULT", 
      "RESULT_SET_LOCATOR", "RETURN", "RETURNS", "REVOKE", "RIGHT", "ROLE", "ROLLBACK", "ROLLUP", "ROUTINE", 
      "ROW", "ROWS", "RRN", "RUN", 
      "SAVEPOINT", "SCHEMA", "SCOPE", "SCRATCHPAD", "SCROLL", "SEARCH", "SECOND", "SECONDS", "SECQTY", 
      "SECTION", "SECURITY", "SELECT", "SENSITIVE", "SEQUENCE", "SESSION", "SESSION_USER", "SET", "SETS", 
      "SIGNAL", "SIMPLE", "SIZE", "SMALLINT", "SOME", "SOURCE", "SPACE", "SPECIFIC", "SPECIFICTYPE", "SQL", 
      "SQLEXCEPTION", "SQLID", "SQLSTATE", "SQLWARNING", "STANDARD", "START", "STATE", "STATEMENT", "STATIC", 
      "STAY", "STOGROUP", "STORES", "STRUCTURE", "STYLE", "SUBPAGES", "SUBSTRING", "SYNONYM", "SYSFUN", "SYSIBM", 
      "SYSPROC", "SYSTEM", "SYSTEM_USER", 
      "TABLE", "TABLESPACE", "TEMPORARY", "TERMINATE", "THAN", "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", 
      "TIMEZONE_MINUTE", "TO", "TRAILING", "TRANSACTION", "TRANSLATION", "TREAT", "TRIGGER", "TRIM", "TRUE", "TYPE", 
      "UNDER", "UNDO", "UNION", "UNIQUE", "UNKNOWN", "UNNEST", "UNTIL", "UPDATE", "USAGE", "USER", "USING", 
      "VALIDPROC", "VALUE", "VALUES", "VARCHAR", "VARIABLE", "VARIANT", "VARYING", "VCAT", "VIEW", "VOLUMES", 
      "WHEN", "WHENEVER", "WHERE", "WHILE", "WITH", "WITHOUT", "WLM", "WORK", "WRITE", 
      "YEAR", "YEARS", 
      "ZONE"        
        };
  }

  @Override
  public String getSQLLockTables(String tableNames[])
  {
      String sql="";
      for (int i=0;i<tableNames.length;i++)
      {
          sql+="LOCK TABLE "+tableNames[i]+" IN SHARE MODE;"+Const.CR;
      }
      return sql;
  }

  @Override
  public String getSQLUnlockTables(String tableName[])
  {
      return null; // lock release on commit point.
  }

  /**
   * Get the maximum length of a text field (VARCHAR) for this database connection.
   * If this size is exceeded use a CLOB.
   * @return The maximum VARCHAR field length for this database type. (mostly identical to getMaxTextFieldLength() - CLOB_LENGTH)
   */
  @Override
  public int getMaxVARCHARLength()
  {
    return 32672;
  }
  
  @Override
  public boolean supportsBatchUpdates()
  {
    // DB2 support batches but big decimals and binary data is
    // broken, so for the moment batches are not done in DB2.
      return false;
  }
    
  /**
   * @return false because the DB2 JDBC driver doesn't support getBlob on the resultset.  We must use getBytes() to get the data.
   */
  @Override
  public boolean supportsGetBlob()
  {
      return false;
  }

  @Override
  public String[] getUsedLibraries()
  {
      return new String[] { "db2jcc.jar" , "db2jcc_license_cu.jar" };
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
      return "SELECT * FROM SYSCAT.SEQUENCES WHERE SEQNAME = '"+sequenceName.toUpperCase()+"'";
  }
  
  /**
   * Get the current value of a database sequence
   * @param sequenceName The sequence to check
   * @return The current value of a database sequence
   */
  @Override
  public String getSQLCurrentSequenceValue(String sequenceName)
  {
      return "SELECT PREVIOUS VALUE FOR "+sequenceName+" FROM SYSIBM.SYSDUMMY1";
  }

  /**
   * Get the SQL to get the next value of a sequence. (Oracle only) 
   * @param sequenceName The sequence name
   * @return the SQL to get the next value of a sequence. (Oracle only)
   */
  @Override
  public String getSQLNextSequenceValue(String sequenceName)
  {
    return "SELECT NEXT VALUE FOR "+sequenceName+" FROM SYSIBM.SYSDUMMY1";
  }

  /**
   * @return This indicator separates the normal URL from the options. DB2 is special
   * in the sense that it requires a : instead of the usual ;.
   */
  @Override
  public String getExtraOptionIndicator()
  {
      return ":";
  }

 
  
}
