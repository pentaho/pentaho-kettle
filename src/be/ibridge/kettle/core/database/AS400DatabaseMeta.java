
package be.ibridge.kettle.core.database;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.value.Value;

/**
 * Contains AS/400 specific information through static final members 
 * 
 * @author Matt
 * @since  11-mrt-2005
 */
public class AS400DatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface
{
	/**
	 * Construct a new database connection.
	 * 
	 */
	public AS400DatabaseMeta(String name, String access, String host, String db, int port, String user, String pass)
	{
		super(name, access, host, db, port, user, pass);
	}
	
	public AS400DatabaseMeta()
	{
	}
	
	public String getDatabaseTypeDesc()
	{
		return "AS/400";
	}

	public String getDatabaseTypeDescLong()
	{
		return "AS/400";
	}
	
	/**
	 * @return Returns the databaseType.
	 */
	public int getDatabaseType()
	{
		return DatabaseMeta.TYPE_DATABASE_AS400;
	}
		
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC };
	}
	
	public String getDriverClass()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "sun.jdbc.odbc.JdbcOdbcDriver";
		}
		else
		{
			return "com.ibm.as400.access.AS400JDBCDriver";
		}
	}

	/**
	 * Get the maximum length of a text field for this database connection.
	 * This includes optional CLOB, Memo and Text fields. (the maximum!)
	 * @return The maximum text field length for this database type. (mostly CLOB_LENGTH)
	 */
	public int getMaxTextFieldLength()
	{
		return 65536;
	}

	public String getURL()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "jdbc:odbc:"+getDatabaseName();
		}
		else
		{
			return "jdbc:as400://"+getHostname()+"/"+getDatabaseName();
		}
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
		return "ALTER TABLE "+tablename+" ALTER COLUMN "+v.getName()+" SET "+getFieldDefinition(v, tk, pk, use_autoinc, false, false);
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
		case Value.VALUE_TYPE_DATE   : retval+="DATETIME"; break;
		case Value.VALUE_TYPE_BOOLEAN: retval+="CHAR(1)"; break;
		case Value.VALUE_TYPE_NUMBER : 
		case Value.VALUE_TYPE_INTEGER: 
        case Value.VALUE_TYPE_BIGNUMBER: 
            if (length<=0 && precision<=0)
            {
                retval+="DOUBLE";
            }
            else
            {
    			retval+="DECIMAL"; 
    			if (length>0)
    			{
    				retval+="("+length;
    				if (precision>0)
    				{
    					retval+=", "+precision;
    				}
    				retval+=")";
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
     * Most databases round number(7,2) 17.29999999 to 17.30, but some don't.
     * @return true if the database supports roundinf of floating point data on update/insert
     */
    public boolean supportsFloatRoundingOnUpdate()
    {
        return false;
    }
}
