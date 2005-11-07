
package be.ibridge.kettle.core.database;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.value.Value;

/**
 * Contains Oracle specific information through static final members 
 * 
 * @author Matt
 * @since  11-mrt-2005
 */
public class OracleDatabaseMeta extends BaseDatabaseMeta implements DatabaseInterface
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
	public OracleDatabaseMeta(String name, String access, String host, String db, int port, String user, String pass)
	{
		super(name, access, host, db, port, user, pass);
	}
	
	public OracleDatabaseMeta()
	{
	}

	public String getDatabaseTypeDesc()
	{
		return "ORACLE";
	}

	public String getDatabaseTypeDescLong()
	{
		return "Oracle";
	}
	
	/**
	 * @return Returns the databaseType.
	 */
	public int getDatabaseType()
	{
		return DatabaseMeta.TYPE_DATABASE_ORACLE;
	}
		
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC, DatabaseMeta.TYPE_ACCESS_OCI};
	}
	
	public int getDefaultDatabasePort()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE) return 1521;
		return -1;
	}
	
	/**
	 * @return Whether or not the database can use auto increment type of fields (pk)
	 */
	public boolean supportsAutoInc()
	{
		return false;
	}
	
	/**
	 * @see be.ibridge.kettle.core.database.DatabaseInterface#getLimitClause(int)
	 */
	public String getLimitClause(int nrRows)
	{
		return " WHERE ROWNUM <= "+nrRows;
	}
	
	/**
	 * Returns the minimal SQL to launch in order to determine the layout of the resultset for a given database table
	 * @param tableName The name of the table to determine the layout for
	 * @return The SQL to launch.
	 */
	public String getSQLQueryFields(String tableName)
	{
	    return "SELECT /*+FIRST_ROWS*/ * FROM "+tableName+" WHERE ROWNUM < 1";
	}

	
	public String getDriverClass()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "sun.jdbc.odbc.JdbcOdbcDriver";
		}
		else
		{
			return "oracle.jdbc.driver.OracleDriver";
		}
	}


	
	public String getURL()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "jdbc:odbc:"+getDatabaseName();
		}
		else
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE)
		{
			return "jdbc:oracle:thin:@"+getHostname()+":"+getDatabasePortNumber()+":"+getDatabaseName();
		}
		else // OCI
		{
			if (getDatabaseName()!=null && getDatabaseName().length()>0 && getDatabasePortNumber()!=0)
			{
				return "jdbc:oracle:oci:@(description=(address=(host="+getHostname()+")(protocol=tcp)(port="+getDatabasePortNumber()+"))(connect_data=(sid="+getDatabaseName()+")))";
			}
			else
			{
				return "jdbc:oracle:oci:@"+getDatabaseName();
			}
		}
	}

	/**
	 * @return true if the database supports sequences
	 */
	public boolean supportsSequences()
	{
		return true;
	}

    /**
     * Check if a sequence exists.
     * @param sequenceName The sequence to check
     * @return The SQL to get the name of the sequence back from the databases data dictionary
     */
    public String getSQLSequenceExists(String sequenceName)
    {
        return "SELECT * FROM USER_SEQUENCES WHERE SEQUENCE_NAME = '"+sequenceName.toUpperCase()+"'";
    }
    
    /**
     * Get the current value of a database sequence
     * @param sequenceName The sequence to check
     * @return The current value of a database sequence
     */
    public String getSQLCurrentSequenceValue(String sequenceName)
    {
        return "SELECT "+sequenceName+".currval FROM DUAL";
    }

    /**
     * Get the SQL to get the next value of a sequence. (Oracle only) 
     * @param sequenceName The sequence name
     * @return the SQL to get the next value of a sequence. (Oracle only)
     */
    public String getSQLNextSequenceValue(String sequenceName)
    {
        return "SELECT "+sequenceName+".nextval FROM dual";
    }


	/**
	 * @return true if we need to supply the schema-name to getTables in order to get a correct list of items.
	 */
	public boolean useSchemaNameForTableList()
	{
		return true;
	}

	/**
	 * @return true if the database supports synonyms
	 */
	public boolean supportsSynonyms()
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
		return "ALTER TABLE "+tablename+" ADD ( "+getFieldDefinition(v, tk, pk, use_autoinc, true, false)+" ) ";
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
		return "ALTER TABLE "+tablename+" DROP ( "+v.getName()+" ) "+Const.CR;
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
		return "ALTER TABLE "+tablename+" MODIFY ("+getFieldDefinition(v, tk, pk, use_autoinc, true, false)+" )";
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
		case Value.VALUE_TYPE_DATE   : retval+="DATE"; break;
		case Value.VALUE_TYPE_BOOLEAN: retval+="CHAR(1)"; break;
		case Value.VALUE_TYPE_NUMBER : 
		case Value.VALUE_TYPE_INTEGER: 
        case Value.VALUE_TYPE_BIGNUMBER: 
			retval+="NUMBER"; 
			if (length>0)
			{
				retval+="("+length;
				if (precision>0)
				{
					retval+=", "+precision;
				}
				retval+=")";
			}
			break;
		case Value.VALUE_TYPE_STRING:
			if (length>=DatabaseMeta.CLOB_LENGTH)
			{
				retval+="CLOB";
			}
			else
			{
				retval+="VARCHAR2"; 
				if (length>0 && length<=2000)
				{
					retval+="("+length;
				}
				else
				{
					retval+="(2000"; // We don't know, so we just use the maximum...
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
	 * @see com.ibridge.kettle.core.database.DatabaseInterface#getReservedWords()
	 */
	public String[] getReservedWords()
	{
		return new String[] 
	     {
			"ACCESS", "ADD", "ALL", "ALTER", "AND", "ANY", "ARRAYLEN", "AS", "ASC", "AUDIT", "BETWEEN",
			"BY", "CHAR", "CHECK", "CLUSTER", "COLUMN", "COMMENT", "COMPRESS", "CONNECT", "CREATE", "CURRENT", "DATE",
			"DECIMAL", "DEFAULT", "DELETE", "DESC", "DISTINCT", "DROP", "ELSE", "EXCLUSIVE", "EXISTS", "FILE", "FLOAT",
			"FOR", "FROM", "GRANT", "GROUP", "HAVING", "IDENTIFIED", "IMMEDIATE", "IN", "INCREMENT", "INDEX", "INITIAL",
			"INSERT", "INTEGER", "INTERSECT", "INTO", "IS", "LEVEL", "LIKE", "LOCK", "LONG", "MAXEXTENTS", "MINUS",
			"MODE", "MODIFY", "NOAUDIT", "NOCOMPRESS", "NOT", "NOTFOUND", "NOWAIT", "NULL", "NUMBER", "OF", "OFFLINE",
			"ON", "ONLINE", "OPTION", "OR", "ORDER", "PCTFREE", "PRIOR", "PRIVILEGES", "PUBLIC", "RAW", "RENAME",
			"RESOURCE", "REVOKE", "ROW", "ROWID", "ROWLABEL", "ROWNUM", "ROWS", "SELECT", "SESSION", "SET", "SHARE",
			"SIZE", "SMALLINT", "SQLBUF", "START", "SUCCESSFUL", "SYNONYM", "SYSDATE", "TABLE", "THEN", "TO", "TRIGGER",
			"UID", "UNION", "UNIQUE", "UPDATE", "USER", "VALIDATE", "VALUES", "VARCHAR", "VARCHAR2", "VIEW", "WHENEVER",
			"WHERE", "WITH"
		 };
	}
	
	/**
	 * @return The SQL on this database to get a list of stored procedures.
	 */
	public String getSQLListOfProcedures()
	{
		return  "SELECT DISTINCT DECODE(package_name, NULL, '', package_name||'.')||object_name FROM user_arguments"; 
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
        return null; // commit handles the unlocking!
    }
}
