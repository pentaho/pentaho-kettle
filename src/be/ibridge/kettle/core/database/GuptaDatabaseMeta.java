
package be.ibridge.kettle.core.database;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.value.Value;

/**
 * Contains Gupta SQLBase specific information through static final members 
 * 
 * @author Matt
 * @since  11-mrt-2005
 */
public class GuptaDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface
{
	/**
	 * Construct a new database connection.
	 * 
	 */
	public GuptaDatabaseMeta(String name, String access, String host, String db, int port, String user, String pass)
	{
		super(name, access, host, db, port, user, pass);
	}
	
	public GuptaDatabaseMeta()
	{
	}
	
	public String getDatabaseTypeDesc()
	{
		return "SQLBASE";
	}

	public String getDatabaseTypeDescLong()
	{
		return "Gupta SQL Base";
	}
	
	/**
	 * @return Returns the databaseType.
	 */
	public int getDatabaseType()
	{
		return DatabaseMeta.TYPE_DATABASE_GUPTA;
	}
		
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC };
	}
	
	public int getDefaultDatabasePort()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE) return 2155;
		return -1;
	}
	
	/**
	 * @return Whether or not the database can use auto increment type of fields (pk)
	 */
	public boolean supportsAutoInc()
	{
		return false;
	}

	public String getDriverClass()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "sun.jdbc.odbc.JdbcOdbcDriver";
		}
		else
		{
			return "jdbc.gupta.sqlbase.SqlbaseDriver";
		}
	}

	public String getURL()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "jdbc:odbc:"+getDatabaseName();
		}
		else
		{
			return "jdbc:sqlbase://"+getHostname()+":"+getDatabasePortNumber()+"/"+getDatabaseName();
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
	 * Generates the SQL statement to add a column to the specified table
	 * @param tablename The table to add
	 * @param v The column defined as a value
	 * @param tk the name of the technical key field
	 * @param use_autoinc whether or not this field uses auto increment
	 * @param pk the name of the primary key field
	 * @param semicolon whether or not to add a semi-colon behind the statement.
	 * @return the SQL statement to add a column to the specified table
	 */
	public String getAddColumnStatement(String tablename, Value v, String tk, boolean use_autoinc, String pk, boolean semicolon)
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
	public String getModifyColumnStatement(String tablename, Value v, String tk, boolean use_autoinc, String pk, boolean semicolon)
	{
		String retval="";
		retval+="ALTER TABLE "+tablename+" DROP "+v.getName()+Const.CR+";"+Const.CR;
		retval+="ALTER TABLE "+tablename+" ADD "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
		return retval;
	}


	public String getFieldDefinition(Value v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr)
	{
		String retval="";
		
		String fieldname = v.getName();
		int    length    = v.getLength();
		int    precision = v.getPrecision();
		
		if (add_fieldname) retval+=fieldname+" ";
		
		int type         = v.getType();
		switch(type)
		{
		case Value.VALUE_TYPE_DATE   : retval+="DATETIME NULL"; break;
		case Value.VALUE_TYPE_BOOLEAN: retval+="CHAR(1)"; break;
		case Value.VALUE_TYPE_NUMBER :
		case Value.VALUE_TYPE_INTEGER: 
        case Value.VALUE_TYPE_BIGNUMBER: 
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
		case Value.VALUE_TYPE_STRING:
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
}
