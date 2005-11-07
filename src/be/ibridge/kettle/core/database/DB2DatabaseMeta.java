
package be.ibridge.kettle.core.database;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.value.Value;

/**
 * Contains DB2 specific information through static final members 
 * 
 * @author Matt
 * @since  11-mrt-2005
 */
public class DB2DatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface
{
	/**
	 * Construct a new database connections.  Note that not all these parameters are not allways mandatory.
	 * 
	 * @param name The database name
	 * @param access The type of database access
	 * @param host The hostname or IP address
	 * @param db The database name
	 * @param port The port on which the database listens.
	 * @param user The username
	 * @param pass The password
	 */
	public DB2DatabaseMeta(String name, String access, String host, String db, int port, String user, String pass)
	{
		super(name, access, host, db, port, user, pass);
	}
	
	public DB2DatabaseMeta()
	{
	}
	
	public String getDatabaseTypeDesc()
	{
		return "DB2";
	}

	public String getDatabaseTypeDescLong()
	{
		return "IBM DB2";
	}
	
	/**
	 * @return Returns the databaseType.
	 */
	public int getDatabaseType()
	{
		return DatabaseMeta.TYPE_DATABASE_DB2;
	}
		
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC };
	}
	
	public int getDefaultDatabasePort()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE) return 50000;
		return -1;
	}
	
	public boolean supportsSetCharacterStream()
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
			return "com.ibm.db2.jcc.DB2Driver";
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
			return "jdbc:db2://"+getHostname()+":"+getDatabasePortNumber()+"/"+getDatabaseName();
		}
	}

	/**
	 * @return true if the database supports schemas
	 */
	public boolean supportsSchemas()
	{
		return false;
	}
	
	/**
	 * @param tableName The table to be truncated.
	 * @return The SQL statement to truncate a table: remove all rows from it without a transaction
	 */
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
	public String getAddColumnStatement(String tablename, Value v, String tk, boolean use_autoinc, String pk, boolean semicolon)
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
	public String getDropColumnStatement(String tablename, Value v, String tk, boolean use_autoinc, String pk, boolean semicolon)
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
	public String getModifyColumnStatement(String tablename, Value v, String tk, boolean use_autoinc, String pk, boolean semicolon)
	{
		String retval="";
		retval+="ALTER TABLE "+tablename+" DROP COLUMN "+v.getName()+Const.CR+";"+Const.CR;
		retval+="ALTER TABLE "+tablename+" ADD COLUMN "+getFieldDefinition(v, tk, pk, use_autoinc, true, false);
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
		case Value.VALUE_TYPE_DATE   : retval+="TIMESTAMP"; break;
		case Value.VALUE_TYPE_BOOLEAN: retval+="CHARACTER(1)"; break;
		case Value.VALUE_TYPE_NUMBER :
		case Value.VALUE_TYPE_INTEGER:
        case Value.VALUE_TYPE_BIGNUMBER: 
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
				break;
			}
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
            sql+="LOCK TABLE "+tableNames[i]+" IN SHARE MODE;"+Const.CR;
        }
        return sql;
    }

    public String getSQLUnlockTables(String tableName[])
    {
        return null; // lock release on commit point.
    }
}

