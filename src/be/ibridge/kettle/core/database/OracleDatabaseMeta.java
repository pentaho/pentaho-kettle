
package be.ibridge.kettle.core.database;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
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
	public OracleDatabaseMeta(String name, String access, String host, String db, String port, String user, String pass)
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
		return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC, DatabaseMeta.TYPE_ACCESS_OCI, DatabaseMeta.TYPE_ACCESS_JNDI };
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


	
    public String getURL(String hostname, String port, String databaseName) throws KettleDatabaseException
    {
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "jdbc:odbc:"+databaseName;
		}
		else
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE)
		{
			return "jdbc:oracle:thin:@"+hostname+":"+port+":"+databaseName;
		}
		else // OCI
		{
		    // Let's see if we have an database name
            if (getDatabaseName()!=null && getDatabaseName().length()>0)
            {
                // Has the user specified hostname & port number?
                if (getHostname()!=null && getHostname().length()>0 && getDatabasePortNumberString()!=null && getDatabasePortNumberString().length()>0) {
                    // User wants the full url
                    return "jdbc:oracle:oci:@(description=(address=(host="+getHostname()+")(protocol=tcp)(port="+getDatabasePortNumberString()+"))(connect_data=(sid="+getDatabaseName()+")))";
                } else {
                    // User wants the shortcut url
                    return "jdbc:oracle:oci:@"+getDatabaseName();
                }               
            }
            else
            {
                throw new KettleDatabaseException("Unable to construct a JDBC URL: at least the database name must be specified");
            }
		}
	}
    
    /**
     * Oracle doesn't support options in the URL, we need to put these in a Properties object at connection time...
     */
    public boolean supportsOptionsInURL()
    {
        return false;
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
		StringBuffer retval=new StringBuffer(128);
		
		String fieldname = v.getName();
		int    length    = v.getLength();
		int    precision = v.getPrecision();
		
		if (add_fieldname) retval.append(fieldname).append(' ');
		
		int type         = v.getType();
		switch(type)
		{
		case Value.VALUE_TYPE_DATE   : retval.append("DATE"); break;
		case Value.VALUE_TYPE_BOOLEAN: retval.append("CHAR(1)"); break;
		case Value.VALUE_TYPE_NUMBER : 
		case Value.VALUE_TYPE_INTEGER: 
        case Value.VALUE_TYPE_BIGNUMBER: 
			retval.append("NUMBER"); 
			if (length>0)
			{
				retval.append('(').append(length);
				if (precision>0)
				{
					retval.append(", ").append(precision);
				}
				retval.append(')');
			}
			break;
		case Value.VALUE_TYPE_STRING:
			if (length>=DatabaseMeta.CLOB_LENGTH)
			{
				retval.append("CLOB");
			}
			else
			{
				if (length>0 && length<=2000)
				{
					retval.append("VARCHAR2(").append(length).append(')');
				}
				else
				{
                    if (length<=0)
                    {
                        retval.append("VARCHAR2(2000)"); // We don't know, so we just use the maximum...
                    }
                    else
                    {
                        retval.append("CLOB"); 
                    }
				}
			}
			break;
        case Value.VALUE_TYPE_BINARY: // the BLOB can contain binary data.
            {
                retval.append("BLOB");
            }
            break;
		default:
			retval.append(" UNKNOWN");
			break;
		}
		
		if (add_cr) retval.append(Const.CR);
		
		return retval.toString();
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
        StringBuffer sql=new StringBuffer(128);
        for (int i=0;i<tableNames.length;i++)
        {
            sql.append("LOCK TABLE ").append(tableNames[i]).append(" IN EXCLUSIVE MODE;").append(Const.CR);
        }
        return sql.toString();
    }
    
    public String getSQLUnlockTables(String tableNames[])
    {
        return null; // commit handles the unlocking!
    }
    
    /**
     * @return extra help text on the supported options on the selected database platform.
     */
    public String getExtraOptionsHelpText()
    {
        return 
            "Source of information: http://www.oracle.com/technology/tech/java/sqlj_jdbc/htdocs/jdbc_faq.htm"+Const.CR+
            Const.CR+
            "Key                            Value       Comment"+Const.CR+
            "-----------------------------  ----------  ------------------------------------------------------------------------------------"+Const.CR+
            "user                           String      The value of this property is used as the user name when connecting to the database."+Const.CR+
            "password                       String      The value of this property is used as the password when connecting to the database."+Const.CR+
            "database                       String      The value of this property is used as the SID of the database."+Const.CR+
            "server                         String      The value of this property is used as the host name of the database."+Const.CR+
            "internal_logon                 String      The value of this property is used as the user name when performing an internal logon. Usually this will be SYS or SYSDBA."+Const.CR+
            "defaultRowPrefetch             int         The value of this property is used as the default number of rows to prefetch."+Const.CR+
            "defaultExecuteBatch            int         The value of this property is used as the default batch size when using Oracle style batching."+Const.CR+
            "processEscapes                 boolean     If the value of this property is 'false' then the default setting for Statement.setEscapeProccessing is false."+Const.CR+
            "disableDefineColumnType        boolean     When this connection property has the value true, the method defineColumnType is has no effect. This is highly recommended when using the Thin driver, especially when the database character set contains four byte characters that expand to two UCS2 surrogate characters, e.g. AL32UTF8. The method defineColumnType provides no performance benefit (or any other benefit) when used with the 10.1.0 Thin driver. This property is provided so that you do not have to remove the calls from your code. This is especially valuable if you use the same code with Thin driver and either the OCI or Server Internal driver."+Const.CR+
            "DMSName                        String      Set the name of the DMS Noun that is the parent of all JDBC DMS metrics."+Const.CR+
            "DMSType                        String      Set the type of the DMS Noun that is the parent of all JDBC DMS metrics."+Const.CR+
            "AccumulateBatchResult          boolean     When using Oracle style batching, JDBC determines when to flush a batch to the database. If this property is true, then the number of modified rows accumulated across all batches flushed from a single statement. The default is to count each batch separately."+Const.CR+
            "oracle.jdbc.J2EE13Compliant    boolean     If the value of this property is 'true', JDBC uses strict compliance for some edge cases. " +Const.CR+
            "                                           In general Oracle's JDBC drivers will allow some operations that are not permitted in the strict interpretation of J2EE 1.3. " +Const.CR+
            "                                           Setting this property to true will cause those cases to throw SQLExceptions. " +Const.CR+
            "                                           There are some other edge cases where Oracle's JDBC drivers have slightly different behavior than defined in J2EE 1.3. " +Const.CR+
            "                                           This results from Oracle having defined the behavior prior to the J2EE 1.3 specification and the resultant need for compatibility with existing customer code. " +Const.CR+
            "                                           Setting this property will result in full J2EE 1.3 compliance at the cost of incompatibility with some customer code. " +Const.CR+
            "                                           Can be either a system property or a connection property. The default value of this property is 'false' in classes12.jar and ojdbc12.jar. " +Const.CR+
            "                                           The default value is 'true' in classes12dms.jar and ojdbc14dms.jar. " +Const.CR+
            "                                           It is true in the dms jars because they are used almost exclusively in Oracle Application Server and so J2EE compatibility is more important than compatibility with previous Oracle versions."+Const.CR+
            "oracle.jdbc.TcpNoDelay         boolean     If the value of this property is 'true', the TCP_NODELAY property is set on the socket when using the Thin driver. See java.net.SocketOptions.TCP_NODELAY. Can be either a system property or a connection property."+Const.CR+
            "defaultNChar                   boolean     If the value of this property is 'true', the default mode for all character data columns will be NCHAR."+Const.CR+
            "useFetchSizeWithLongColumn     boolean     If the value of this property is 'true', then JDBC will prefetch rows even though there is a LONG or LONG RAW column in the result. By default JDBC fetches only one row at a time if there are LONG or LONG RAW columns in the result. Setting this property to true can improve performance but can also cause SQLExceptions if the results are too big."+Const.CR+
            "remarksReporting               boolean     If the value of this property is 'true', OracleDatabaseMetaData will include remarks in the metadata. This can result in a substantial reduction in performance."+Const.CR+
            "includeSynonyms                boolean     If the value of this property is 'true', JDBC will include synonyms when getting information about a column."+Const.CR+
            "restrictGetTables              boolean     If the value of this property is 'true', JDBC will return a more refined value for DatabaseMetaData.getTables. By default JDBC will return things that are not accessible tables. These can be non-table objects or accessible synonymns for inaccessible tables. If this property is true JDBC will return only accessible tables. This has a substantial performance penalty."+Const.CR+
            "fixedString                    boolean     If the value of this property is 'true', JDBC will use FIXED CHAR semantic when setObject is called with a String argument. By default JDBC uses VARCHAR semantics. The difference is in blank padding. With the default there is no blank padding so, for example, 'a' does not equal 'a ' in a CHAR(4). If true these two will be equal."+Const.CR+
            "oracle.jdbc.ocinativelibrary   String      Set the name of the native library for the oci driver. If not set, the default name, libocijdbcX (X is a version number), is used."+Const.CR+
            "SetBigStringTryClob            boolean     Setting this property to 'true' forces PreparedStatement.setString() method to use setStringForClob() if the data is larger than 32765 bytes. Please note that using this method with VARCHAR and LONG columns may cause large data to be truncated silently, or cause other errors differing from the normal behavior of setString()."+Const.CR
            ;
    }
}
