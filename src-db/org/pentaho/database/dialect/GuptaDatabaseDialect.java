package org.pentaho.database.dialect;

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.row.ValueMetaInterface;

public class GuptaDatabaseDialect extends AbstractDatabaseDialect {

  public static final IDatabaseType DBTYPE = 
    new DatabaseType(
        "Gupta SQL Base",
        "SQLBASE",
        DatabaseAccessType.getList(
            DatabaseAccessType.NATIVE, 
            DatabaseAccessType.ODBC, 
            DatabaseAccessType.JNDI
        ), 
        2155, 
        null
    );
  
  public IDatabaseType getDatabaseType() {
    return DBTYPE;
  }
  
  public String getNativeDriver() {
	return "jdbc.gupta.sqlbase.SqlbaseDriver";
  }
  
	protected String getNativeJdbcPre() {
		return "jdbc:sqlbase://";
	}
	
  @Override
  public String getURL(IDatabaseConnection connection)
    {
    if (connection.getAccessType()==DatabaseAccessType.ODBC) {
      return "jdbc:odbc:"+connection.getDatabaseName();
    }
    else
    {
            if (Const.isEmpty(connection.getDatabasePort()))
            {
                return getNativeJdbcPre() + connection.getHostname()+"/"+connection.getDatabaseName();
            }
            else
            {
                return getNativeJdbcPre() + connection.getHostname()+":"+connection.getDatabasePort()+"/"+connection.getDatabaseName();
            }
    }
  }

  
	/**
	 * Checks whether or not the command setFetchSize() is supported by the JDBC driver...
	 * @return true is setFetchSize() is supported!
	 */
	public boolean isFetchSizeSupported()
	{
		return false;
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
		return false;
	}
  
  /**
   * @return true if the database supports catalogs
   */
  public boolean supportsCatalogs()
  {
      return false;
  }

  /**
   * @return true if the database supports timestamp to date conversion.
   * Gupta doesn't support this!
   */
  public boolean supportsTimeStampToDateConversion()
  {
      return false;
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
		String retval="";
		retval+="ALTER TABLE "+tablename+" DROP "+v.getName()+Const.CR+";"+Const.CR;
		retval+="ALTER TABLE "+tablename+" ADD "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
		return retval;
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
		case ValueMetaInterface.TYPE_DATE   : retval+="DATETIME NULL"; break;
		case ValueMetaInterface.TYPE_BOOLEAN: retval+="CHAR(1)"; break;
		case ValueMetaInterface.TYPE_NUMBER :
		case ValueMetaInterface.TYPE_INTEGER: 
      case ValueMetaInterface.TYPE_BIGNUMBER: 
			if (fieldname.equalsIgnoreCase(tk) ||   // Technical key
			    fieldname.equalsIgnoreCase(tk)      // Primary key
			    )
			{
				retval+="INTEGER NOT NULL";
			} 
			else
			{
				if ( (length<0 && precision<0) || precision>0 || length>9)
				{
					retval+="DOUBLE PRECISION";
				}
				else // Precision == 0 && length<=9
				{
					retval+="INTEGER"; 
				}
			}
			break;
		case ValueMetaInterface.TYPE_STRING:
		    if (length>254 || length<0)
		    {
		    	retval+="LONG VARCHAR";
		    }
		    else
		    {
				retval+="VARCHAR("+length+")";
			}
			break;
		default:
			retval+=" UNKNOWN";
			break;
		}
		
		if (add_cr) retval+=Const.CR;
		
		return retval;
	}


    public String[] getUsedLibraries()
    {
        return new String[] { "SQLBaseJDBC.jar" };
    }
}
