
package be.ibridge.kettle.core.database;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.value.Value;

/**
 * Contains Informix specific information through static final members 
 * 
 * @author Matt
 * @since  11-mrt-2005
 */
public class InformixDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface
{
	/**
	 * Construct a new database connection.
	 * 
	 */
	public InformixDatabaseMeta(String name, String access, String host, String db, int port, String user, String pass)
	{
		super(name, access, host, db, port, user, pass);
	}
	
	public InformixDatabaseMeta()
	{
	}
	
	public String getDatabaseTypeDesc()
	{
		return "INFORMIX";
	}

	public String getDatabaseTypeDescLong()
	{
		return "Informix";
	}
	
	/**
	 * @return Returns the databaseType.
	 */
	public int getDatabaseType()
	{
		return DatabaseMeta.TYPE_DATABASE_INFORMIX;
	}
		
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC };
	}
	
	public int getDefaultDatabasePort()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE) return 1526;
		return -1;
	}

	/**
	 * @see be.ibridge.kettle.core.database.DatabaseInterface#getNotFoundTK(boolean)
	 */
	public int getNotFoundTK(boolean use_autoinc)
	{
		if ( supportsAutoInc() && use_autoinc)
		{
			return 1;
		}
		return super.getNotFoundTK(use_autoinc);
	}

	public String getDriverClass()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "sun.jdbc.odbc.JdbcOdbcDriver";
		}
		else
		{
			return "com.informix.jdbc.IfxDriver";
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
			return "jdbc:informix-sqli://"+getHostname()+":"+getDatabasePortNumber()+"/"+getDatabaseName()+":INFORMIXSERVER="+getServername();
		}
	}

	/**
	 * Indicates the need to insert a placeholder (0) for auto increment fields.
	 * @return true if we need a placeholder for auto increment fields in insert statements.
	 */
	public boolean needsPlaceHolder()
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
		return "ALTER TABLE "+tablename+" MODIFY "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
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
		case Value.VALUE_TYPE_DATE   : retval+="DATETIME YEAR to FRACTION"; break;
		case Value.VALUE_TYPE_BOOLEAN: retval+="CHAR(1)"; break;
		case Value.VALUE_TYPE_NUMBER :
		case Value.VALUE_TYPE_INTEGER: 
        case Value.VALUE_TYPE_BIGNUMBER: 
			if (fieldname.equalsIgnoreCase(tk) ||   // Technical key
			    fieldname.equalsIgnoreCase(pk)      // Primary key
			    )
			{
				if (use_autoinc)
				{
					retval+="SERIAL8";
				}
				else
				{
					retval+="INTEGER PRIMARY KEY";
				}
			} 
			else
			{
				if ( (length<0 && precision<0) || precision>0 || length>9)
				{
					retval+="FLOAT";
				}
				else // Precision == 0 && length<=9
				{
					retval+="INTEGER"; 
				}
			}
			break;
		case Value.VALUE_TYPE_STRING:
			if (length>=DatabaseMeta.CLOB_LENGTH)
			{
				retval+="CLOB";
			}
			else
			{
				if (length<256)
				{
					retval+="VARCHAR"; 
					if (length>0)
					{
						retval+="("+length+")";
					}
				}
				else
				{
					if (length<32768)
					{
						retval+="LVARCHAR"; 
					}
					else
					{
						retval+="TEXT";
					}
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
    
    public String getSQLLockTables(String tableNames[])
    {
        String sql="";
        for (int i=0;i<tableNames.length;i++)
        {
            sql+="LOCK TABLE "+tableNames[i]+" IN EXCLUSIVE MODE;"+Const.CR;
        }
        return sql;
    }

    public String getSQLUnlockTables(String tableNames[])
    {
        String sql="";
        for (int i=0;i<tableNames.length;i++)
        {
            sql+="UNLOCK TABLE "+tableNames[i]+";"+Const.CR;
        }
        return sql;
    }

}
