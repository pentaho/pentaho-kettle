package org.pentaho.database.dialect;

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

public class DerbyDatabaseDialect extends AbstractDatabaseDialect {

  public static final IDatabaseType DBTYPE = 
    new DatabaseType(
        "Apache Derby", 
        "DERBY", 
        DatabaseAccessType.getList(
            DatabaseAccessType.NATIVE, 
            DatabaseAccessType.ODBC, 
            DatabaseAccessType.JNDI
        ), 
        -1, 
        null
    );
  
  public IDatabaseType getDatabaseType() {
    return DBTYPE;
  }
  
  public String getNativeDriver() {
    return "org.apache.derby.jdbc.ClientDriver";
  }
  
  protected String getNativeJdbcPre() {
	return "jdbc:derby:";
  }

  public String getURL(IDatabaseConnection connection) {
      if (connection.getAccessType()==DatabaseAccessType.NATIVE)
      {
          if (!Const.isEmpty(connection.getHostname()))
          {
              String url=getNativeJdbcPre()+connection.getHostname();
              if (!Const.isEmpty(connection.getDatabasePort())) url+=":"+connection.getDatabasePort();
              url+="/"+connection.getDatabaseName();
              return url;
          }
          else // Simple format: jdbc:derby:<dbname>
          {
              return "jdbc:derby:"+connection.getDatabaseName();
          }
      }
      else
      {
          return "jdbc:odbc:"+connection.getDatabaseName();
      }
	}
  
	/**
	 * Checks whether or not the command setFetchSize() is supported by the JDBC driver...
	 * @return true is setFetchSize() is supported!
	 */
	public boolean isFetchSizeSupported()
	{
		return true;
	}
	
	/**
	 * @return true if the database supports bitmap indexes
	 */
	public boolean supportsBitmapIndex()
	{
		return false;
	}
	
	/**
	 * @return true if Kettle can create a repository on this type of database.
	 */
	public boolean supportsRepository()
	{
		return true;
	}
	
	/**
	 * @param tableName The table to be truncated.
	 * @return The SQL statement to truncate a table: remove all rows from it without a transaction
	 */
	public String getTruncateTableStatement(String tableName)
	{
	    return "DELETE FROM "+tableName;
	}


	/**
	 * Generates the SQL statement to add a column to the specified table
	 * For this generic type, i set it to the most common possibility.
	 * 
	 * @param tablename The table to add
	 * @param v The column defined as a value
	 * @param tk the name of the technical key field
	 * @param use_autoinc whether or not this field uses auto increment
	 * @param pk the name of the primary key field
	 * @param semicolon whether or not to add a semi-colon behind the statement.
	 * @return the SQL statement to add a column to the specified table
	 */
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
	public String getModifyColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon)
	{
		return "ALTER TABLE "+tablename+" ALTER "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
	}

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
		case ValueMetaInterface.TYPE_BOOLEAN: retval+="CHAR(1)"; break;
      case ValueMetaInterface.TYPE_NUMBER    :
      case ValueMetaInterface.TYPE_INTEGER   : 
      case ValueMetaInterface.TYPE_BIGNUMBER : 
          if (fieldname.equalsIgnoreCase(tk) || // Technical key
              fieldname.equalsIgnoreCase(pk)    // Primary key
              ) 
          {
              if (use_autoinc)
              {
                  retval+="BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1)";
              }
              else
              {
                  retval+="BIGINT NOT NULL PRIMARY KEY";
              }
          } 
          else
          {
              // Integer values...
              if (precision==0)
              {
                  if (length>9)
                  {
                      retval+="BIGINT";
                  }
                  else
                  {
                      if (length>4)
                      {
                          retval+="INTEGER";
                      }
                      else
                      {
                          retval+="SMALLINT";
                      }
                  }
              }
              // Floating point values...
              else  
              {
                  if (length>18)
                  {
                      retval+="DECIMAL("+length;
                      if (precision>0) retval+=", "+precision;
                      retval+=")";
                  }
                  else
                  {
                      retval+="FLOAT";
                  }
              }
          }
          break;
		case ValueMetaInterface.TYPE_STRING:
			if (length>=DatabaseMeta.CLOB_LENGTH || length>32700)
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
          retval+="BLOB";
          break;
		default:
			retval+="UNKNOWN";
			break;
		}
		
		if (add_cr) retval+=Const.CR;
		
		return retval;
	}

  public String[] getUsedLibraries()
  {
      return new String[] { "derbyclient.jar" };
  }
  
  public int getDefaultDatabasePort()
  {
      return 1527;
  }
  
  public boolean supportsGetBlob()
  {
      return false;
  }
  
  public String getExtraOptionsHelpText()
  {
      return "http://db.apache.org/derby/papers/DerbyClientSpec.html";
  }
  
  @Override
  public String[] getReservedWords() {
  	return new String[] {
  		      "ADD"
  		    , "ALL"
  		    , "ALLOCATE"
  		    , "ALTER"
  		    , "AND"
  		    , "ANY"
  		    , "ARE"
  		    , "AS"
  		    , "ASC"
  		    , "ASSERTION"
  		    , "AT"
  		    , "AUTHORIZATION"
  		    , "AVG"
  		    , "BEGIN"
  		    , "BETWEEN"
  		    , "BIT"
  		    , "BOOLEAN"
  		    , "BOTH"
  		    , "BY"
  		    , "CALL"
  		    , "CASCADE"
  		    , "CASCADED"
  		    , "CASE"
  		    , "CAST"
  		    , "CHAR"
  		    , "CHARACTER"
  		    , "CHECK"
  		    , "CLOSE"
  		    , "COLLATE"
  		    , "COLLATION"
  		    , "COLUMN"
  		    , "COMMIT"
  		    , "CONNECT"
  		    , "CONNECTION"
  		    , "CONSTRAINT"
  		    , "CONSTRAINTS"
  		    , "CONTINUE"
  		    , "CONVERT"
  		    , "CORRESPONDING"
  		    , "COUNT"
  		    , "CREATE"
  		    , "CURRENT"
  		    , "CURRENT_DATE"
  		    , "CURRENT_TIME"
  		    , "CURRENT_TIMESTAMP"
  		    , "CURRENT_USER"
  		    , "CURSOR"
  		    , "DEALLOCATE"
  		    , "DEC"
  		    , "DECIMAL"
  		    , "DECLARE"
  		    , "DEFERRABLE"
  		    , "DEFERRED"
  		    , "DELETE"
  		    , "DESC"
  		    , "DESCRIBE"
  		    , "DIAGNOSTICS"
  		    , "DISCONNECT"
  		    , "DISTINCT"
  		    , "DOUBLE"
  		    , "DROP"
  		    , "ELSE"
  		    , "END"
  		    , "ENDEXEC"
  		    , "ESCAPE"
  		    , "EXCEPT"
  		    , "EXCEPTION"
  		    , "EXEC"
  		    , "EXECUTE"
  		    , "EXISTS"
  		    , "EXPLAIN"
  		    , "EXTERNAL"
  		    , "FALSE"
  		    , "FETCH"
  		    , "FIRST"
  		    , "FLOAT"
  		    , "FOR"
  		    , "FOREIGN"
  		    , "FOUND"
  		    , "FROM"
  		    , "FULL"
  		    , "FUNCTION"
  		    , "GET"
  		    , "GET_CURRENT_CONNECTION"
  		    , "GLOBAL"
  		    , "GO"
  		    , "GOTO"
  		    , "GRANT"
  		    , "GROUP"
  		    , "HAVING"
  		    , "HOUR"
  		    , "IDENTITY"
  		    , "IMMEDIATE"
  		    , "IN"
  		    , "INDICATOR"
  		    , "INITIALLY"
  		    , "INNER"
  		    , "INOUT"
  		    , "INPUT"
  		    , "INSENSITIVE"
  		    , "INSERT"
  		    , "INT"
  		    , "INTEGER"
  		    , "INTERSECT"
  		    , "INTO"
  		    , "IS"
  		    , "ISOLATION"
  		    , "JOIN"
  		    , "KEY"
  		    , "LAST"
  		    , "LEFT"
  		    , "LIKE"
  		    , "LONGINT"
  		    , "LOWER"
  		    , "LTRIM"
  		    , "MATCH"
  		    , "MAX"
  		    , "MIN"
  		    , "MINUTE"
  		    , "NATIONAL"
  		    , "NATURAL"
  		    , "NCHAR"
  		    , "NVARCHAR"
  		    , "NEXT"
  		    , "NO"
  		    , "NOT"
  		    , "NULL"
  		    , "NULLIF"
  		    , "NUMERIC"
  		    , "OF"
  		    , "ON"
  		    , "ONLY"
  		    , "OPEN"
  		    , "OPTION"
  		    , "OR"
  		    , "ORDER"
  		    , "OUT"
  		    , "OUTER"
  		    , "OUTPUT"
  		    , "OVERLAPS"
  		    , "PAD"
  		    , "PARTIAL"
  		    , "PREPARE"
  		    , "PRESERVE"
  		    , "PRIMARY"
  		    , "PRIOR"
  		    , "PRIVILEGES"
  		    , "PROCEDURE"
  		    , "PUBLIC"
  		    , "READ"
  		    , "REAL"
  		    , "REFERENCES"
  		    , "RELATIVE"
  		    , "RESTRICT"
  		    , "REVOKE"
  		    , "RIGHT"
  		    , "ROLLBACK"
  		    , "ROWS"
  		    , "RTRIM"
  		    , "SCHEMA"
  		    , "SCROLL"
  		    , "SECOND"
  		    , "SELECT"
  		    , "SESSION_USER"
  		    , "SET"
  		    , "SMALLINT"
  		    , "SOME"
  		    , "SPACE"
  		    , "SQL"
  		    , "SQLCODE"
  		    , "SQLERROR"
  		    , "SQLSTATE"
  		    , "SUBSTR"
  		    , "SUBSTRING"
  		    , "SUM"
  		    , "SYSTEM_USER"
  		    , "TABLE"
  		    , "TEMPORARY"
  		    , "TIMEZONE_HOUR"
  		    , "TIMEZONE_MINUTE"
  		    , "TO"
  		    , "TRAILING"
  		    , "TRANSACTION"
  		    , "TRANSLATE"
  		    , "TRANSLATION"
  		    , "TRUE"
  		    , "UNION"
  		    , "UNIQUE"
  		    , "UNKNOWN"
  		    , "UPDATE"
  		    , "UPPER"
  		    , "USER"
  		    , "USING"
  		    , "VALUES"
  		    , "VARCHAR"
  		    , "VARYING"
  		    , "VIEW"
  		    , "WHENEVER"
  		    , "WHERE"
  		    , "WITH"
  		    , "WORK"
  		    , "WRITE"
  		    , "XML"
  		    , "XMLEXISTS"
  		    , "XMLPARSE"
  		    , "XMLSERIALIZE"
  		    , "YEAR"
  	};
  }
}
