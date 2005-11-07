
package be.ibridge.kettle.core.database;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.value.Value;

/**
 * Contains Firebird specific information through static final members 
 * 
 * @author Matt
 * @since  11-mrt-2005
 */
public class PostgreSQLDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface
{
	/**
	 * Construct a new database connection.
	 * 
	 */
	public PostgreSQLDatabaseMeta(String name, String access, String host, String db, int port, String user, String pass)
	{
		super(name, access, host, db, port, user, pass);
	}
	
	public PostgreSQLDatabaseMeta()
	{
	}
	
	public String getDatabaseTypeDesc()
	{
		return "POSTGRESQL";
	}

	public String getDatabaseTypeDescLong()
	{
		return "PostgreSQL";
	}
	
	/**
	 * @return Returns the databaseType.
	 */
	public int getDatabaseType()
	{
		return DatabaseMeta.TYPE_DATABASE_POSTGRES;
	}
		
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC };
	}
	
	public int getDefaultDatabasePort()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE) return 5432;
		return -1;
	}

	public String getDriverClass()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "sun.jdbc.odbc.JdbcOdbcDriver";
		}
		else
		{
			return "org.postgresql.Driver";
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
			return "jdbc:postgresql://"+getHostname()+":"+getDatabasePortNumber()+"/"+getDatabaseName();
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
	 * @return true if the database supports synonyms
	 */
	public boolean supportsSynonyms()
	{
		return false;
	}
    
    public boolean supportsSequences()
    {
        return true;
    }
    
    /**
     * Support for the serial field is only fake in PostgreSQL.
     * You can't get back the value after the inserts (getGeneratedKeys) through JDBC calls.
     * Therefor it's wiser to use the built-in sequence support directly, not the auto increment features.
     */
    public boolean supportsAutoInc()
    {
        return false;
    }
    
    /**
     * Get the SQL to get the next value of a sequence. (PostgreSQL version) 
     * @param sequenceName The sequence name
     * @return the SQL to get the next value of a sequence.
     */
    public String getSQLNextSequenceValue(String sequenceName)
    {
        return "SELECT nextval('"+sequenceName+"')";
    }
    
    /**
     * Get the SQL to get the next value of a sequence. (PostgreSQL version) 
     * @param sequenceName The sequence name
     * @return the SQL to get the next value of a sequence.
     */
    public String getSQLCurrentSequenceValue(String sequenceName)
    {
        return "SELECT last_value FROM "+sequenceName;
    }
    
    /**
     * Check if a sequence exists.
     * @param sequenceName The sequence to check
     * @return The SQL to get the name of the sequence back from the databases data dictionary
     */
    public String getSQLSequenceExists(String sequenceName)
    {
        return "SELECT relname AS sequence_name FROM pg_statio_all_sequences WHERE relname = '"+sequenceName.toLowerCase()+"'";
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
	
	/* (non-Javadoc)
	 * @see be.ibridge.kettle.core.database.DatabaseInterface#getSQLListOfProcedures()
	 */
	public String getSQLListOfProcedures()
	{
		return  "select proname " +
				"from pg_proc, pg_user " +
				"where pg_user.usesysid = pg_proc.proowner " +
				"and upper(pg_user.usename) = '"+getUsername().toUpperCase()+"'"
				;
	}
    
    /**
     * @param tableNames The names of the tables to lock
     * @return The SQL commands to lock database tables for write purposes.
     */
    public String getSQLLockTables(String tableNames[])
    {
        String sql="LOCK TABLE ";
        for (int i=0;i<tableNames.length;i++)
        {
            if (i>0) sql+=", ";
            sql+=tableNames[i]+" ";
        }
        sql+="IN ACCESS EXCLUSIVE MODE;"+Const.CR;

        return sql;
    }

    /**
     * @param tableName The name of the table to unlock
     * @return The SQL command to unlock a database table.
     */
    public String getSQLUnlockTables(String tableName[])
    {
        return null; // commit unlocks everything!
    }

}
