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

public class GenericDatabaseDialect extends AbstractDatabaseDialect {

  public static final String ATTRIBUTE_CUSTOM_URL          = "CUSTOM_URL"; 
  public static final String ATTRIBUTE_CUSTOM_DRIVER_CLASS = "CUSTOM_DRIVER_CLASS";

  public static final IDatabaseType DBTYPE = 
    new DatabaseType(
        "Generic database",
        "GENERIC",
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
  @Override
  public String getAddColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon)
  {
    return "ALTER TABLE "+tablename+" ADD "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
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
    case ValueMetaInterface.TYPE_BOOLEAN:
      if (supportsBooleanDataType()) {
        retval+="BOOLEAN"; 
      } else {
        retval+="CHAR(1)";
      }
      break;
    case ValueMetaInterface.TYPE_NUMBER : 
    case ValueMetaInterface.TYPE_INTEGER: 
        case ValueMetaInterface.TYPE_BIGNUMBER: 
      if (fieldname.equalsIgnoreCase(tk) || // Technical key
          fieldname.equalsIgnoreCase(pk)    // Primary key
          ) 
      {
        retval+="BIGSERIAL";
      } 
      else
      {
        if (length>0)
        {
          if (precision>0 || length>18)
          {
            retval+="NUMERIC("+length+", "+precision+")";
          }
          else
          {
            if (length>9)
            {
              retval+="BIGINT";
            }
            else
            {
              if (length<5)
              {
                retval+="SMALLINT";
              }
              else
              {
                retval+="INTEGER";
              }
            }
          }
          
        }
        else
        {
          retval+="DOUBLE PRECISION";
        }
      }
      break;
    case ValueMetaInterface.TYPE_STRING:
      if (length>=DatabaseMeta.CLOB_LENGTH)
      {
        retval+="TEXT";
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
    default:
      retval+=" UNKNOWN";
      break;
    }
    
    if (add_cr) retval+=Const.CR;
    
    return retval;
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
    return "ALTER TABLE "+tablename+" MODIFY "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
  }

  @Override
  public String getNativeDriver() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected String getNativeJdbcPre() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getURL(IDatabaseConnection connection) throws KettleDatabaseException {
    if (connection.getAccessType() == DatabaseAccessType.NATIVE)
    {
        String url = connection.getAttributes().get(ATTRIBUTE_CUSTOM_URL);
        if (url == null) {
          url = "";
        }
        return url;
    }
    else
    {
        return "jdbc:odbc:"+connection.getDatabaseName();
    }
  }
  
  /**
   * The Generic datasource should not attempt to append options to the url.
   */
  @Override
  public boolean supportsOptionsInURL()
  {
      return false;
  }

  @Override
  public String[] getUsedLibraries() {
    return new String[] {};
  }

  @Override
  public int getNotFoundTK(boolean use_autoinc)
  {
    if ( supportsAutoInc() && use_autoinc)
    {
      return 1;
    }
    return super.getNotFoundTK(use_autoinc);
  }
  

  /**
   * Checks whether or not the command setFetchSize() is supported by the JDBC driver...
   * @return true is setFetchSize() is supported!
   */
  @Override
  public boolean isFetchSizeSupported()
  {
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
   * @return true if Kettle can create a repository on this type of database.
   */
  @Override
  public boolean supportsRepository()
  {
    return false;
  }
  
  /**
   * @param tableName The table to be truncated.
   * @return The SQL statement to truncate a table: remove all rows from it without a transaction
   */
  @Override
  public String getTruncateTableStatement(String tableName)
  {
      return "DELETE FROM "+tableName;
  }

  @Override
  public IDatabaseConnection createNativeConnection(String jdbcUrl) {
    DatabaseConnection dbconn = new DatabaseConnection();
    dbconn.setDatabaseType(getDatabaseType());
    dbconn.setAccessType(DatabaseAccessType.NATIVE);
    dbconn.getAttributes().put(ATTRIBUTE_CUSTOM_URL, jdbcUrl);
    return dbconn;
  }



}
