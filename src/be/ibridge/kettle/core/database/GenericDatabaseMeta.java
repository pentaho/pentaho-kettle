
package be.ibridge.kettle.core.database;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.value.Value;

/**
 * Contains Hypersonic specific information through static final members 
 * 
 * @author Matt
 * @since  11-mrt-2005
 */
public class GenericDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface
{
    public static final String ATRRIBUTE_CUSTOM_URL          = "CUSTOM_URL"; 
    public static final String ATRRIBUTE_CUSTOM_DRIVER_CLASS = "CUSTOM_DRIVER_CLASS";
    
	/**
	 * Construct a new database connection.
	 * 
	 */
	public GenericDatabaseMeta(String name, String access, String host, String db, int port, String user, String pass)
	{
		super(name, access, host, db, port, user, pass);
	}
	
	public GenericDatabaseMeta()
	{
	}
	
	public String getDatabaseTypeDesc()
	{
		return "GENERIC";
	}

	public String getDatabaseTypeDescLong()
	{
		return "Generic database";
	}
	
	/**
	 * @return Returns the databaseType.
	 */
	public int getDatabaseType()
	{
		return DatabaseMeta.TYPE_DATABASE_GENERIC;
	}
		
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC };
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
        if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE)
        {
            return getAttributes().getProperty(ATRRIBUTE_CUSTOM_DRIVER_CLASS, "");
        }
        else
        {
            return "sun.jdbc.odbc.JdbcOdbcDriver"; // always ODBC!
        }

	}
	
	public String getURL()
	{
        if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE)
        {
            return getAttributes().getProperty(ATRRIBUTE_CUSTOM_URL, "");
        }
        else
        {
            return "jdbc:odbc:"+getDatabaseName();
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
	 * @see be.ibridge.kettle.core.database.DatabaseInterface#getSchemaTableCombination(java.lang.String, java.lang.String)
	 */
	public String getSchemaTableCombination(String schema_name, String table_part)
	{
		return "\""+schema_name+"\".\""+table_part+"\"";
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
		case Value.VALUE_TYPE_DATE   : retval+="TIMESTAMP"; break;
		case Value.VALUE_TYPE_BOOLEAN: retval+="CHAR(1)"; break;
		case Value.VALUE_TYPE_NUMBER : 
		case Value.VALUE_TYPE_INTEGER: 
        case Value.VALUE_TYPE_BIGNUMBER: 
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
		case Value.VALUE_TYPE_STRING:
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
}
