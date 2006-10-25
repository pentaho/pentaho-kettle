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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
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
public abstract class BaseDatabaseMeta implements Cloneable
{
    /**
     * The port number of the database as string: allows for parameterisation.
     */
    public static final String ATTRIBUTE_PORT_NUMBER            = "PORT_NUMBER"; 

    /**
     * The SQL to execute at connect time (right after connecting)
     */
    public static final String ATTRIBUTE_SQL_CONNECT            = "SQL_CONNECT";

    /**
     * A flag to determine if we should use connection pooling or not.
     */
    public static final String ATTRIBUTE_USE_POOLING            = "USE_POOLING";

    /**
     * If we use connection pooling, this would contain the maximum pool size
     */
    public static final String ATTRIBUTE_MAXIMUM_POOL_SIZE          = "MAXIMUM_POOL_SIZE";

    /**
     * If we use connection pooling, this would contain the initial pool size
     */
    public static final String ATTRIBUTE_INITIAL_POOL_SIZE          = "INITIAL_POOL_SIZE";

    /**
     * The prefix for all the extra options attributes
     */
    public static final String ATTRIBUTE_PREFIX_EXTRA_OPTION    = "EXTRA_OPTION_"; 

    
    /**
     * A flag to determine if the connection is clustered or not.
     */
    public static final String ATTRIBUTE_IS_CLUSTERED            = "IS_CLUSTERED";

    /**
     * The clustering partition ID name prefix
     */
    private static final String ATTRIBUTE_CLUSTER_PARTITION_PREFIX = "CLUSTER_PARTITION_";

    /**
     * The clustering hostname prefix
     */
    public static final String ATTRIBUTE_CLUSTER_HOSTNAME_PREFIX = "CLUSTER_HOSTNAME_";

    /**
     * The clustering port prefix
     */
    public static final String ATTRIBUTE_CLUSTER_PORT_PREFIX = "CLUSTER_PORT_";

    /**
     * The clustering database name prefix
     */
    public static final String ATTRIBUTE_CLUSTER_DBNAME_PREFIX = "CLUSTER_DBNAME_";

    
    

	private String name;
	private int    accessType;        // Database.TYPE_ODBC / NATIVE / OCI
	private String hostname;
	private String databaseName;
	private String username;
	private String password;
	private String servername;   // Informix only!
	
	private String dataTablespace;  // data storage location, For Oracle & perhaps others
	private String indexTablespace; // index storage location, For Oracle & perhaps others
	
	private boolean changed;
    
    private Properties attributes;
	
	private long id;


	/**
	 * Constructs a new database connections.  Note that not all these parameters are not allways mandatory.
	 * 
	 * @param name The database name
	 * @param access The type of database access
	 * @param host The hostname or IP address
	 * @param db The database name
	 * @param port The port on which the database listens.
	 * @param user The username
	 * @param pass The password
	 */
	public BaseDatabaseMeta(String name, String access, String host, String db, String port, String user, String pass)
	{
        this();
		this.name = name;
		this.accessType = DatabaseMeta.getAccessType(access);
		this.hostname = host;
		this.databaseName = db;
		setDatabasePortNumberString(port);
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
	 * @param databasePortNumberString The databasePortNumber string to set.
	 */
	public void setDatabasePortNumberString(String databasePortNumberString)
	{
        if (databasePortNumberString!=null) getAttributes().put(BaseDatabaseMeta.ATTRIBUTE_PORT_NUMBER, databasePortNumberString);
	}
	
	/**
	 * @return Returns the databasePortNumber string.
	 */
	public String getDatabasePortNumberString()
	{
		return getAttributes().getProperty(ATTRIBUTE_PORT_NUMBER, "-1");
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
            
            // CLone the attributes as well...
            retval.attributes = (Properties) attributes.clone();
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
     * @return true if the database supports catalogs
     */
    public boolean supportsCatalogs()
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
	 * Get the maximum length of a text field (VARCHAR) for this database connection.
	 * If this size is exceeded use a CLOB.
	 * @return The maximum VARCHAR field length for this database type. (mostly identical to getMaxTextFieldLength() - CLOB_LENGTH)
	 */
	public int getMaxVARCHARLength()
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

    /**
     * @return true if the database defaults to naming tables and fields in uppercase.
     * True for most databases except for stuborn stuff like Postgres ;-)
     */
    public boolean isDefaultingToUppercase()
    {
        return true;
    }
    
    /**
     * @return all the extra options that are set to be used for the database URL
     */
    public Map getExtraOptions()
    {
        Map map = new Hashtable();
        
        for (Enumeration keys = attributes.keys();keys.hasMoreElements();)
        {
            String attribute = (String) keys.nextElement();
            if (attribute.startsWith(ATTRIBUTE_PREFIX_EXTRA_OPTION))
            {
                String value = attributes.getProperty(attribute, "");
                
                // Add to the map...
                map.put(attribute.substring(ATTRIBUTE_PREFIX_EXTRA_OPTION.length()), value);
            }
        }
        
        return map;
    }
    
    /**
     * Add an extra option to the attributes list
     * @param databaseTypeCode The database type code for which the option applies
     * @param option The option to set
     * @param value The value of the option
     */
    public void addExtraOption(String databaseTypeCode, String option, String value)
    {
        attributes.put(ATTRIBUTE_PREFIX_EXTRA_OPTION+databaseTypeCode+"."+option, value);
    }

    /**
     * @return The extra option separator in database URL for this platform (usually this is semicolon ; ) 
     */
    public String getExtraOptionSeparator()
    {
        return ";";
    }
    
    /**
     * @return The extra option value separator in database URL for this platform (usually this is the equal sign = ) 
     */
    public String getExtraOptionValueSeparator()
    {
        return "=";
    }
    
    /**
     * @return This indicator separates the normal URL from the options
     */
    public String getExtraOptionIndicator()
    {
        return ";";
    }

    /**
     * @return true if the database supports connection options in the URL, false if they are put in a Properties object.
     */
    public boolean supportsOptionsInURL()
    {
        return true;
    }
    
    /**
     * @return extra help text on the supported options on the selected database platform.
     */
    public String getExtraOptionsHelpText()
    {
        return null;
    }
    
    /**
     * @return true if the database JDBC driver supports getBlob on the resultset.  If not we must use getBytes() to get the data.
     */
    public boolean supportsGetBlob()
    {
        return true;
    }
    
    /**
     * @return The SQL to execute right after connecting
     */
    public String getConnectSQL()
    {
        return attributes.getProperty(ATTRIBUTE_SQL_CONNECT);
    }

    /**
     * @param sql The SQL to execute right after connecting
     */
    public void setConnectSQL(String sql)
    {
        attributes.setProperty(ATTRIBUTE_SQL_CONNECT, sql);
    }

    /**
     * @return true if the database supports setting the maximum number of return rows in a resultset.
     */
    public boolean supportsSetMaxRows()
    {
        return true;
    }
    
    /**
     * @return true if we want to use a database connection pool
     */
    public boolean isUsingConnectionPool()
    {
        String usePool = attributes.getProperty(ATTRIBUTE_USE_POOLING);
        return "Y".equalsIgnoreCase(usePool);
    }
    
    /**
     * @param usePool true if we want to use a database connection pool
     */
    public void setUsingConnectionPool(boolean usePool)
    {
        attributes.setProperty(ATTRIBUTE_USE_POOLING, usePool?"Y":"N");
    }
    
    /**
     * @return the maximum pool size
     */
    public int getMaximumPoolSize()
    {
        return Const.toInt(attributes.getProperty(ATTRIBUTE_MAXIMUM_POOL_SIZE), ConnectionPoolUtil.defaultMaximumNrOfConnections);
    }

    /**
     * @param maximumPoolSize the maximum pool size
     */
    public void setMaximumPoolSize(int maximumPoolSize)
    {
        attributes.setProperty(ATTRIBUTE_MAXIMUM_POOL_SIZE, Integer.toString(maximumPoolSize));
    }

    /**
     * @return the initial pool size
     */
    public int getInitialPoolSize()
    {
        return Const.toInt(attributes.getProperty(ATTRIBUTE_INITIAL_POOL_SIZE), ConnectionPoolUtil.defaultInitialNrOfConnections);
    }
    
    /**
     * @param initialPoolSize the initial pool size
     */
    public void setInitialPoolSize(int initialPoolSize)
    {
        attributes.setProperty(ATTRIBUTE_MAXIMUM_POOL_SIZE, Integer.toString(initialPoolSize));
    }
    
    
    
    /**
     * @return true if we want to use a database connection pool
     */
    public boolean isClustered()
    {
        String isClustered = attributes.getProperty(ATTRIBUTE_IS_CLUSTERED);
        return "Y".equalsIgnoreCase(isClustered);
    }
    
    /**
     * @param usePool true if we want to use a database connection pool
     */
    public void setClustered(boolean clustered)
    {
        attributes.setProperty(ATTRIBUTE_IS_CLUSTERED, clustered?"Y":"N");
    }
    
    
    /**
     * @return the available partition/host/databases/port combinations in the cluster
     */
    public PartitionDatabaseMeta[] getPartitioningInformation()
    {
        // find the maximum number of attributes starting with ATTRIBUTE_CLUSTER_HOSTNAME_PREFIX 
        
        int nr = 0;
        while ( (attributes.getProperty(ATTRIBUTE_CLUSTER_HOSTNAME_PREFIX+nr))!=null ) nr++;
        
        PartitionDatabaseMeta[] clusterInfo = new PartitionDatabaseMeta[nr];
        
        for (nr=0;nr<clusterInfo.length;nr++)
        {
            String partitionId = attributes.getProperty(ATTRIBUTE_CLUSTER_PARTITION_PREFIX+nr);
            String hostname    = attributes.getProperty(ATTRIBUTE_CLUSTER_HOSTNAME_PREFIX+nr);
            String port        = attributes.getProperty(ATTRIBUTE_CLUSTER_PORT_PREFIX+nr);
            String dbName      = attributes.getProperty(ATTRIBUTE_CLUSTER_DBNAME_PREFIX+nr);
            clusterInfo[nr] = new PartitionDatabaseMeta(partitionId, hostname, port, dbName);
        }
        
        return clusterInfo;
    }
    
    /**
     * @param clusterInfo the available partition/host/databases/port combinations in the cluster
     */
    public void setPartitioningInformation(PartitionDatabaseMeta[] clusterInfo)
    {
        for (int nr=0;nr<clusterInfo.length;nr++)
        {
            PartitionDatabaseMeta meta = clusterInfo[nr];
            
            attributes.put(ATTRIBUTE_CLUSTER_PARTITION_PREFIX+nr, Const.NVL(meta.getPartitionId(), ""));
            attributes.put(ATTRIBUTE_CLUSTER_HOSTNAME_PREFIX+nr, Const.NVL(meta.getHostname(), ""));
            attributes.put(ATTRIBUTE_CLUSTER_PORT_PREFIX+nr, Const.NVL(meta.getPort(), ""));
            attributes.put(ATTRIBUTE_CLUSTER_DBNAME_PREFIX+nr, Const.NVL(meta.getDatabaseName(), ""));
        }
    } 
}
