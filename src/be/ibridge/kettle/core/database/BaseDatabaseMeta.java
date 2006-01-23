 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

  

package be.ibridge.kettle.core.database;

import java.util.Properties;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.value.Value;



/**
 * This class contains the basic information on a database connection.
 * It is not intended to be used other than the inheriting classes such as 
 * OracleDatabaseInfo, ...
 * 
 * @author Matt
 * @since  11-mrt-2005
 */
abstract class BaseDatabaseMeta implements Cloneable
{
	private String name;
	private int    accessType;        // Database.TYPE_ODBC / NATIVE / OCI
	private String hostname;
	private String databaseName;
	private int    databasePortNumber;
	private String username;
	private String password;
	private String servername;   // Informix only!
	
	private String dataTablespace;  // data storage location, For Oracle & perhaps others
	private String indexTablespace; // index storage location, For Oracle & perhaps others
	
	private boolean changed;
    
    private Properties attributes;
	
	private long id;


	/**
	 * Construct a new database connections.  Note that not all these parameters are not allways mandatory.
	 * 
	 * @param name The database name
	 * @param type The type of database
	 * @param access The type of database access
	 * @param host The hostname or IP address
	 * @param db The database name
	 * @param port The port on which the database listens.
	 * @param user The username
	 * @param pass The password
	 */
	public BaseDatabaseMeta(String name, String access, String host, String db, int port, String user, String pass)
	{
        this();
		this.name = name;
		this.accessType = DatabaseMeta.getAccessType(access);
		this.hostname = host;
		this.databaseName = db;
		this.databasePortNumber = port;
		this.username = user;
		this.password = pass;
		this.servername = null;
	}
	
	public BaseDatabaseMeta()
	{
        attributes = new Properties();
        changed = false;
	}

	/**
	 * @return Returns the accessType.
	 */
	public int getAccessType()
	{
		return accessType;
	}
	
	/**
	 * @param accessType The accessType to set.
	 */
	public void setAccessType(int accessType)
	{
		this.accessType = accessType;
	}
	
	/**
	 * @return Returns the changed.
	 */
	public boolean isChanged()
	{
		return changed;
	}
	
	/**
	 * @param changed The changed to set.
	 */
	public void setChanged(boolean changed)
	{
		this.changed = changed;
	}
	
	/**
	 * @return Returns the connection name.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * @param name The connection Name to set.
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * @return Returns the databaseName.
	 */
	public String getDatabaseName()
	{
		return databaseName;
	}
	
	/**
	 * @param databaseName The databaseName to set.
	 */
	public void setDatabaseName(String databaseName)
	{
		this.databaseName = databaseName;
	}
	
	/**
	 * @return Returns the databasePortNumber.
	 */
	public int getDatabasePortNumber()
	{
		return databasePortNumber;
	}
	
	/**
	 * @param databasePortNumber The databasePortNumber to set.
	 */
	public void setDatabasePortNumber(int databasePortNumber)
	{
		this.databasePortNumber = databasePortNumber;
	}
	
	/**
	 * @return Returns the databaseType.
	 */
	abstract int getDatabaseType();
	
	/**
	 * @return Returns the database type description (code).
	 */
	abstract String getDatabaseTypeDesc();

	/**
	 * @return Returns the database type long user description .
	 */
	abstract String getDatabaseTypeDescLong();

	/**
	 * @return Returns the hostname.
	 */
	public String getHostname()
	{
		return hostname;
	}
	
	/**
	 * @param hostname The hostname to set.
	 */
	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}
	
	/**
	 * @return Returns the id.
	 */
	public long getId()
	{
		return id;
	}
	
	/**
	 * @param id The id to set.
	 */
	public void setId(long id)
	{
		this.id = id;
	}
	
	/**
	 * @return Returns the password.
	 */
	public String getPassword()
	{
		return password;
	}
	
	/**
	 * @param password The password to set.
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}
	
	/**
	 * @return Returns the servername.
	 */
	public String getServername()
	{
		return servername;
	}
	
	/**
	 * @param servername The servername to set.
	 */
	public void setServername(String servername)
	{
		this.servername = servername;
	}
	
	/**
	 * @return Returns the tablespaceData.
	 */
	public String getDataTablespace()
	{
		return dataTablespace;
	}
	
	/**
	 * @param dataTablespace The data tablespace to set.
	 */
	public void setDataTablespace(String dataTablespace)
	{
		this.dataTablespace = dataTablespace;
	}
	
	/**
	 * @return Returns the index tablespace.
	 */
	public String getIndexTablespace()
	{
		return indexTablespace;
	}
	
	/**
	 * @param indexTablespace The index tablespace to set.
	 */
	public void setIndexTablespace(String indexTablespace)
	{
		this.indexTablespace = indexTablespace;
	}
	
	/**
	 * @return Returns the username.
	 */
	public String getUsername()
	{
		return username;
	}
	
	/**
	 * @param username The username to set.
	 */
	public void setUsername(String username)
	{
		this.username = username;
	}
    
    /**
     * @return The extra attributes for this database connection
     */
    public Properties getAttributes()
    {
        return attributes;
    }
    
    /**
     * Set extra attributes on this database connection
     * @param attributes The extra attributes to set on this database connection.
     */
    public void setAttributes(Properties attributes)
    {
        this.attributes = attributes;
    }
	
	/**
	 * Clone the basic settings for this connection!
	 */
	public Object clone()
	{
		BaseDatabaseMeta retval = null;
		try
		{
			retval = (BaseDatabaseMeta)super.clone();
		}
		catch(CloneNotSupportedException e)
		{
			throw new RuntimeException(e);
		}
		return retval;
	}

	/* 
	 ********************************************************************************
	 * DEFAULT SETTINGS FOR ALL DATABASES                                           *
	 ********************************************************************************
	 */	

	/**
	 * @return the default database port number
	 */
	public int getDefaultDatabasePort()
	{
		return -1; // No default port or not used.
	}

	/**
	 * See if this database supports the setCharacterStream() method on a PreparedStatement.
	 * 
	 * @return true if we can set a Stream on a field in a PreparedStatement.  False if not. 
	 */
	public boolean supportsSetCharacterStream()
	{
		return true;
		
		/*
		 *   TODO: Not yet tested on AS/400, Informix, Sybase, PostgreSQL, Hypersonic
		 */
	}
	
	/**
	 * @return Whether or not the database can use auto increment type of fields (pk)
	 */
	public boolean supportsAutoInc()
	{
		return true;
	}
	
	public String getLimitClause(int nrRows)
	{
		return "";	
	}

	public int getNotFoundTK(boolean use_autoinc)
	{
		return 0;
	}
	
	/**
	 * Get the SQL to get the next value of a sequence. (Oracle/PGSQL only) 
	 * @param sequenceName The sequence name
	 * @return the SQL to get the next value of a sequence. (Oracle/PGSQL only)
	 */
	public String getSQLNextSequenceValue(String sequenceName)
	{
		return "";
	}
    
    /**
     * Get the current value of a database sequence
     * @param sequenceName The sequence to check
     * @return The current value of a database sequence
     */
    public String getSQLCurrentSequenceValue(String sequenceName)
    {
        return "";
    }
    
    /**
     * Check if a sequence exists.
     * @param sequenceName The sequence to check
     * @return The SQL to get the name of the sequence back from the databases data dictionary
     */
    public String getSQLSequenceExists(String sequenceName)
    {
        return "";
    }


	/**
	 * Checks whether or not the command setFetchSize() is supported by the JDBC driver...
	 * @return true is setFetchSize() is supported!
	 */
	public boolean isFetchSizeSupported()
	{
		return true;
	}

	/**
	 * Indicates the need to insert a placeholder (0) for auto increment fields.
	 * @return true if we need a placeholder for auto increment fields in insert statements.
	 */
	public boolean needsPlaceHolder()
	{
		return false;
	}

	/**
	 * @return true if the database supports schemas
	 */
	public boolean supportsSchemas()
	{
		return true;
	}

	/**
	 * 
	 * @return true when the database engine supports empty transaction.
	 * (for example Informix does not on a non-ANSI database type!)
	 */
	public boolean supportsEmptyTransactions()
	{
		return true;
	}
	
	/**
	 * @return the function for SUM agrregate 
	 */
	public String getFunctionSum()
	{
		return "SUM";
	}

	/**
	 * @return the function for Average agrregate 
	 */
	public String getFunctionAverage()
	{
		return "AVG";
	}

	/**
	 * @return the function for Minimum agrregate 
	 */
	public String getFunctionMinimum()
	{
		return "MIN";
	}


	/**
	 * @return the function for Maximum agrregate 
	 */
	public String getFunctionMaximum()
	{
		return "MAX";
	}

	/**
	 * @return the function for Count agrregate 
	 */
	public String getFunctionCount()
	{
		return "COUNT";
	}

	/**
	 * Get the schema-table combination to query the right table.
	 * Usually that is SCHEMA.TABLENAME, however there are exceptions to this rule...
	 * @param schema_name The schema name
	 * @param table_part The tablename
	 * @return the schema-table combination to query the right table.
	 */
	public String getSchemaTableCombination(String schema_name, String table_part)
	{
		return schema_name+"."+table_part;
	}

	/**
	 * Get the maximum length of a text field for this database connection.
	 * This includes optional CLOB, Memo and Text fields. (the maximum!)
	 * @return The maximum text field length for this database type. (mostly CLOB_LENGTH)
	 */
	public int getMaxTextFieldLength()
	{
		return DatabaseMeta.CLOB_LENGTH;
	}
	
	/**
	 * @return true if the database supports transactions.
	 */
	public boolean supportsTransactions()
	{
		return true;
	}

	/**
	 * @return true if the database supports sequences
	 */
	public boolean supportsSequences()
	{
		return false;
	}
	
	/**
	 * @return true if the database supports bitmap indexes
	 */
	public boolean supportsBitmapIndex()
	{
		return true;
	}

	/**
	 * @return true if the database JDBC driver supports the setLong command
	 */
	public boolean supportsSetLong()
	{
		return true;
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
		return "ALTER TABLE "+tablename+" DROP "+v.getName()+Const.CR;
	}

	/**
	 * @return an array of reserved words for the database type...
	 */
	public String[] getReservedWords()
	{
		return new String[] {};
	}
	
	/**
	 * @return true if reserved words need to be double quoted ("password", "select", ...)
	 */
	public boolean quoteReservedWords()
	{
		return true;
	}

	/**
	 * @return The start quote sequence, mostly just double quote, but sometimes [, ...
	 */
	public String getStartQuote()
	{
		return "\"";
	}
	
	/**
	 * @return The end quote sequence, mostly just double quote, but sometimes ], ...
	 */
	public String getEndQuote()
	{
		return "\"";
	}

	/**
	 * @return true if Kettle can create a repository on this type of database.
	 */
	public boolean supportsRepository()
	{
		return true;
	}
	
	/**
	 * @return a list of table types to retrieve tables for the database
	 */
	public String[] getTableTypes()
	{
		return new String[] { "TABLE" };
	}

	/**
	 * @return a list of table types to retrieve views for the database
	 */
	public String[] getViewTypes()
	{
		return new String[] { "VIEW" };
	}

	/**
	 * @return a list of table types to retrieve synonyms for the database
	 */
	public String[] getSynonymTypes()
	{
		return new String[] { "SYNONYM" };
	}


	/**
	 * @return true if we need to supply the schema-name to getTables in order to get a correct list of items.
	 */
	public boolean useSchemaNameForTableList()
	{
		return false;
	}

	/**
	 * @return true if the database supports views
	 */
	public boolean supportsViews()
	{
		return true;
	}
	
	/**
	 * @return true if the database supports synonyms
	 */
	public boolean supportsSynonyms()
	{
		return false;
	}

	/**
	 * @return The SQL on this database to get a list of stored procedures.
	 */
	public String getSQLListOfProcedures()
	{
		return null;
	}
	
	/**
	 * @param tableName The table to be truncated.
	 * @return The SQL statement to truncate a table: remove all rows from it without a transaction
	 */
	public String getTruncateTableStatement(String tableName)
	{
	    return "TRUNCATE TABLE "+tableName;
	}

	/**
	 * Returns the minimal SQL to launch in order to determine the layout of the resultset for a given database table
	 * @param tableName The name of the table to determine the layout for
	 * @return The SQL to launch.
	 */
	public String getSQLQueryFields(String tableName)
	{
	    return "SELECT * FROM "+tableName;
	}

    /**
     * Most databases round number(7,2) 17.29999999 to 17.30, but some don't.
     * @return true if the database supports roundinf of floating point data on update/insert
     */
    public boolean supportsFloatRoundingOnUpdate()
    {
        return true;
    }
    
    /**
     * @param tableNames The names of the tables to lock
     * @return The SQL command to lock database tables for write purposes.
     *         null is returned in case locking is not supported on the target database.
     *         null is the default value
     */
    public String getSQLLockTables(String tableNames[])
    {
        return null;
    }
    
    /**
     * @param tableNames The names of the tables to unlock
     * @return The SQL command to unlock database tables. 
     *         null is returned in case locking is not supported on the target database.
     *         null is the default value
     */
    public String getSQLUnlockTables(String tableNames[])
    {
        return null;
    }
    

    /**
     * @return true if the database supports timestamp to date conversion.
     * For example Interbase doesn't support this!
     */
    public boolean supportsTimeStampToDateConversion()
    {
        return true;
    }

    /**
     * @return true if the database JDBC driver supports batch updates
     * For example Interbase doesn't support this!
     */
    public boolean supportsBatchUpdates()
    {
        return true;
    }

    /**
     * @return true if the database supports a boolean, bit, logical, ... datatype
     * The default is false: map to a string.
     */
    public boolean supportsBooleanDataType()
    {
        return false;
    }


}
