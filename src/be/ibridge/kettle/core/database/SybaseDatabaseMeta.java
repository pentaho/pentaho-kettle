
package be.ibridge.kettle.core.database;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.value.Value;

/**
 * Contains Sybase specific information through static final members 
 * 
 * @author Matt
 * @since  11-mrt-2005
 */
public class SybaseDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface
{
	/**
	 * Construct a new database connection.
	 * 
	 */
	public SybaseDatabaseMeta(String name, String access, String host, String db, int port, String user, String pass)
	{
		super(name, access, host, db, port, user, pass);
	}
	
	public SybaseDatabaseMeta()
	{
	}
	
	public String getDatabaseTypeDesc()
	{
		return "SYBASE";
	}

	public String getDatabaseTypeDescLong()
	{
		return "Sybase";
	}
	
	/**
	 * @return Returns the databaseType.
	 */
	public int getDatabaseType()
	{
		return DatabaseMeta.TYPE_DATABASE_SYBASE;
	}
		
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC };
	}
	
	public int getDefaultDatabasePort()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE) return 5001;
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
			return "net.sourceforge.jtds.jdbc.Driver";
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
		    // jdbc:jtds:<server_type>://<server>[:<port>][/<database>][;<property>=<value>[;...]]
			return "jdbc:jtds:sybase://"+getHostname()+":"+getDatabasePortNumber()+"/"+getDatabaseName();
		}
	}


	/**
	 * @see be.ibridge.kettle.core.database.DatabaseInterface#getSchemaTableCombination(java.lang.String, java.lang.String)
	 */
	public String getSchemaTableCombination(String schema_name, String table_part)
	{
		return table_part;
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
		case Value.VALUE_TYPE_DATE   : retval+="DATETIME NULL"; break;
		case Value.VALUE_TYPE_BOOLEAN: retval+="CHAR(1)"; break;
		case Value.VALUE_TYPE_NUMBER :
		case Value.VALUE_TYPE_INTEGER: 
        case Value.VALUE_TYPE_BIGNUMBER: 
			if (fieldname.equalsIgnoreCase(tk)  ||  // Technical key: auto increment field!
			    fieldname.equalsIgnoreCase(pk)      // Primary key 
			   ) 
			{
				if (use_autoinc)
				{
					retval+="INTEGER IDENTITY NOT NULL";
				}
				else
				{
					retval+="INTEGER NOT NULL PRIMARY KEY";
				}
			} 
			else
			{
				if ( precision!=0 || (precision==0 && length>9))
				{
					if (precision>0 && length>0)
					{
						retval+="DECIMAL("+length+", "+precision+") NULL";
					}
					else
					{
						retval+="DOUBLE PRECISION NULL";
					}
				}
				else // Precision == 0 && length<=9
				{
					if (length<3)
					{
						retval+="TINYINT NULL";
					}
					else
					if (length<5)
					{
						retval+="SMALLINT NULL";
					}
					else
					{
						retval+="INTEGER NULL";
					}
				}
			}
			break;
		case Value.VALUE_TYPE_STRING:
			if (length>=2048)
			{
				retval+="TEXT NULL";
			}
			else
			{
				retval+="VARCHAR"; 
				if (length>0)
				{
					retval+="("+length+")";
				}
				retval+=" NULL";
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
