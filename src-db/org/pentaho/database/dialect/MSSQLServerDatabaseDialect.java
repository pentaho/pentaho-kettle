package org.pentaho.database.dialect;

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMetaInterface;

public class MSSQLServerDatabaseDialect extends AbstractDatabaseDialect {

  public static final IDatabaseType DBTYPE = 
    new DatabaseType(
        "MS SQL Server",
        "MSSQL",
        DatabaseAccessType.getList(
            DatabaseAccessType.NATIVE, 
            DatabaseAccessType.ODBC, 
            DatabaseAccessType.JNDI
        ), 
        1433, 
        "http://jtds.sourceforge.net/faq.html#urlFormat"
    );
  
  public IDatabaseType getDatabaseType() {
    return DBTYPE;
  }

  @Override
  public String getNativeDriver() {
    return "net.sourceforge.jtds.jdbc.Driver";
  }
  
  @Override
  public String getNativeJdbcPre() {
    return "jdbc:jtds:sqlserver://";
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
  
  @Override
  public boolean supportsCatalogs() {
    return false;
  }
  
  
  /**
   * @return true if the database supports bitmap indexes
   */
  @Override
  public boolean supportsBitmapIndex()
  {
    return false;
  }

  /**
   * @return true if the database supports synonyms
   */
  @Override
  public boolean supportsSynonyms()
  {
    return false;
  }
    
  @Override
  public String getSQLQueryFields(String tableName)
  {
      return "SELECT TOP 1 * FROM "+tableName;
  }

  @Override
  public String getSQLTableExists(String tablename)
  {
      return  getSQLQueryFields(tablename);
  }
  
  @Override
  public String getSQLColumnExists(String columnname, String tablename)
  {
      return  getSQLQueryColumnFields(columnname, tablename);
  }
  
  public String getSQLQueryColumnFields(String columnname, String tableName)
  {
      return "SELECT TOP 1 " + columnname + " FROM "+tableName;
  }

  /**
   * @param tableNames The names of the tables to lock
   * @return The SQL command to lock database tables for write purposes.
   *         null is returned in case locking is not supported on the target database.
   *         null is the default value
   */
  @Override
  public String getSQLLockTables(String tableNames[])
  {
      StringBuffer sql=new StringBuffer(128);
      for (int i=0;i<tableNames.length;i++)
      {
          sql.append("SELECT top 0 * FROM ").append(tableNames[i]).append(" WITH (TABLOCKX, HOLDLOCK);").append(Const.CR);
      }
      return sql.toString();
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
    return "ALTER TABLE "+tablename+" ADD "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
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
    return "ALTER TABLE "+tablename+" ALTER COLUMN "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
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
    case ValueMetaInterface.TYPE_DATE   : retval+="DATETIME"; break;
    case ValueMetaInterface.TYPE_BOOLEAN:
      if (supportsBooleanDataType()) {
        retval+="BIT"; 
      } else {
        retval+="CHAR(1)";
      }
      break;
    case ValueMetaInterface.TYPE_NUMBER :
    case ValueMetaInterface.TYPE_INTEGER: 
        case ValueMetaInterface.TYPE_BIGNUMBER: 
      if (fieldname.equalsIgnoreCase(tk) ||  // Technical key
          fieldname.equalsIgnoreCase(pk)     // Primary key
          ) 
      {
        if (use_autoinc)
        {
          retval+="BIGINT PRIMARY KEY IDENTITY(0,1)";
        }
        else
        {
          retval+="BIGINT PRIMARY KEY";
        }
      } 
      else
      {
                if (precision==0)
                {
                    if (length>18)
                    {
                        retval+="DECIMAL("+length+",0)";
                    }
                    else
                    {
                        if (length>9)
                        {
                            retval+="BIGINT";
                        }
                        else
                        {
                            retval+="INT";
                        }
                    }
                }
                else
                {
                    if (precision>0)
                    {
                        if (length>0)
                        {
                            retval+="DECIMAL("+length+","+precision+")";
                        }
                    }
                    else
                    {
                        retval+="FLOAT(53)";
                    }
                }
      }
      break;
    case ValueMetaInterface.TYPE_STRING:
      if (length<8000)
      {
        //  Maybe use some default DB String length in case length<=0
        if (length>0)
        {
          retval+="VARCHAR("+length+")";  
        }
        else
        {
          retval+="VARCHAR(100)";
        } 
      }
      else
      {
        retval+="TEXT"; // Up to 2bilion characters.
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
   * @see org.pentaho.di.core.database.DatabaseInterface#getSQLListOfProcedures()
   */
  @Override
  public String getSQLListOfProcedures(IDatabaseConnection connection)
  {
    return "select o.name from sysobjects o, sysusers u where  xtype in ( 'FN', 'P' ) and o.uid = u.uid";
  }
  /* (non-Javadoc)
   * @see org.pentaho.di.core.database.DatabaseInterface#getReservedWords()
   */
  @Override
  public String[] getReservedWords()
  {
    return new String[]
    {
      /* Transact-SQL Reference: Reserved Keywords
       * Includes future keywords: could be reserved in future releases of SQL Server as new features are
       * implemented.
       * REMARK: When SET QUOTED_IDENTIFIER is ON (default), identifiers can be delimited by double quotation 
       * marks, and literals must be delimited by single quotation marks. 
       * When SET QUOTED_IDENTIFIER is OFF, identifiers cannot be quoted and 
       * must follow all Transact-SQL rules for identifiers.
       */
      "ABSOLUTE", "ACTION", "ADD", "ADMIN", "AFTER", "AGGREGATE", "ALIAS", "ALL", "ALLOCATE", "ALTER", "AND", 
      "ANY", "ARE", "ARRAY", "AS", "ASC", "ASSERTION", "AT", "AUTHORIZATION", 
      "BACKUP", "BEFORE", "BEGIN", "BETWEEN", "BINARY", "BIT", "BLOB", "BOOLEAN", "BOTH", "BREADTH", "BREAK", 
      "BROWSE", "BULK", "BY", 
      "CALL", "CASCADE", "CASCADED", "CASE", "CAST", "CATALOG", "CHAR", "CHARACTER", "CHECK", "CHECKPOINT", 
      "CLASS", "CLOB", "CLOSE", "CLUSTERED", "COALESCE", "COLLATE", "COLLATION", "COLUMN", "COMMIT", 
      "COMPLETION", "COMPUTE", "CONNECT", "CONNECTION", "CONSTRAINT", "CONSTRAINTS", "CONSTRUCTOR", 
      "CONTAINS", "CONTAINSTABLE", "CONTINUE", "CONVERT", "CORRESPONDING", "CREATE", "CROSS", "CUBE", 
      "CURRENT", "CURRENT_DATE", "CURRENT_PATH", "CURRENT_ROLE", "CURRENT_TIME", "CURRENT_TIMESTAMP", 
      "CURRENT_USER", "CURSOR", "CYCLE", 
      "DATA", "DATABASE", "DATE", "DAY", "DBCC", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", 
      "DEFERRABLE", "DEFERRED", "DELETE", "DENY", "DEPTH", "DEREF", "DESC", "DESCRIBE", "DESCRIPTOR", 
      "DESTROY", "DESTRUCTOR", "DETERMINISTIC", "DIAGNOSTICS", "DICTIONARY", "DISCONNECT", "DISK", "DISTINCT", 
      "DISTRIBUTED", "DOMAIN", "DOUBLE", "DROP", "DUMMY", "DUMP", "DYNAMIC", 
      "EACH", "ELSE", "END", "END-EXEC", "EQUALS", "ERRLVL", "ESCAPE", "EVERY", "EXCEPT", "EXCEPTION", "EXEC", 
      "EXECUTE", "EXISTS", "EXIT", "EXTERNAL", 
      "FALSE", "FETCH", "FILE", "FILLFACTOR", "FIRST", "FLOAT", "FOR", "FOREIGN", "FOUND", "FREE", "FREETEXT", 
      "FREETEXTTABLE", "FROM", "FULL", "FUNCTION", 
      "GENERAL", "GET", "GLOBAL", "GO", "GOTO", "GRANT", "GROUP", "GROUPING", 
      "HAVING", "HOLDLOCK", "HOST", "HOUR", 
      "IDENTITY", "IDENTITY_INSERT", "IDENTITYCOL", "IF", "IGNORE", "IMMEDIATE", "IN", "INDEX", "INDICATOR", 
      "INITIALIZE", "INITIALLY", "INNER", "INOUT", "INPUT", "INSERT", "INT", "INTEGER", "INTERSECT", "INTERVAL", 
      "INTO", "IS", "ISOLATION", "ITERATE", 
      "JOIN", 
      "KEY", "KILL", 
      "LANGUAGE", "LARGE", "LAST", "LATERAL", "LEADING", "LEFT", "LESS", "LEVEL", "LIKE", "LIMIT", "LINENO", 
      "LOAD", "LOCAL", "LOCALTIME", "LOCALTIMESTAMP", "LOCATOR", 
      "MAP", "MATCH", "MINUTE", "MODIFIES", "MODIFY", "MODULE", "MONTH", 
      "NAMES", "NATIONAL", "NATURAL", "NCHAR", "NCLOB", "NEW", "NEXT", "NO", "NOCHECK", "NONCLUSTERED", "NONE", 
      "NOT", "NULL", "NULLIF", "NUMERIC", 
      "OBJECT", "OF", "OFF", "OFFSETS", "OLD", "ON", "ONLY", "OPEN", "OPENDATASOURCE", "OPENQUERY", "OPENROWSET", 
      "OPENXML", "OPERATION", "OPTION", "OR", "ORDER", "ORDINALITY", "OUT", "OUTER", "OUTPUT", "OVER", 
      "PAD", "PARAMETER", "PARAMETERS", "PARTIAL", "PATH", "PERCENT", "PLAN", "POSTFIX", "PRECISION", "PREFIX", 
      "PREORDER", "PREPARE", "PRESERVE", "PRIMARY", "PRINT", "PRIOR", "PRIVILEGES", "PROC", "PROCEDURE", "PUBLIC", 
      "RAISERROR", "READ", "READS", "READTEXT", "REAL", "RECONFIGURE", "RECURSIVE", "REF", "REFERENCES", "REFERENCING", 
      "RELATIVE", "REPLICATION", "RESTORE", "RESTRICT", "RESULT", "RETURN", "RETURNS", "REVOKE", "RIGHT", "ROLE", 
      "ROLLBACK", "ROLLUP", "ROUTINE", "ROW", "ROWCOUNT", "ROWGUIDCOL", "ROWS", "RULE", 
      "SAVE", "SAVEPOINT", "SCHEMA", "SCOPE", "SCROLL", "SEARCH", "SECOND", "SECTION", "SELECT", "SEQUENCE", "SESSION", 
      "SESSION_USER", "SET", "SETS", "SETUSER", "SHUTDOWN", "SIZE", "SMALLINT", "SOME", "SPACE", "SPECIFIC", 
      "SPECIFICTYPE", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "START", "STATE", "STATEMENT", "STATIC", 
      "STATISTICS", "STRUCTURE", "SYSTEM_USER", 
      "TABLE", "TEMPORARY", "TERMINATE", "TEXTSIZE", "THAN", "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", 
      "TIMEZONE_MINUTE", "TO", "TOP", "TRAILING", "TRAN", "TRANSACTION", "TRANSLATION", "TREAT", "TRIGGER", "TRUE", 
      "TRUNCATE", "TSEQUAL", 
      "UNDER", "UNION", "UNIQUE", "UNKNOWN", "UNNEST", "UPDATE", "UPDATETEXT", "USAGE", "USE", "USER", "USING", 
      "VALUE", "VALUES", "VARCHAR", "VARIABLE", "VARYING", "VIEW", 
      "WAITFOR", "WHEN", "WHENEVER", "WHERE", "WHILE", "WITH", "WITHOUT", "WORK", "WRITE", "WRITETEXT", 
      "YEAR", 
      "ZONE"        
        };
  }

  @Override
  public String[] getUsedLibraries()
  {
      return new String[] { "jtds-1.2.jar" };
  }
  
  
}
