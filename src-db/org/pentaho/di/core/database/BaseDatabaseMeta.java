 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

  

package org.pentaho.di.core.database;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.repository.ObjectId;



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
     * The port number of the database as string: allows for parameterization.
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

    /**
     * The clustering database username prefix
     */
    public static final String ATTRIBUTE_CLUSTER_USERNAME_PREFIX = "CLUSTER_USERNAME_";
    
    /**
     * The clustering database password prefix
     */
    public static final String ATTRIBUTE_CLUSTER_PASSWORD_PREFIX = "CLUSTER_PASSWORD_";
    
    /** The pooling parameters */
    public static final String ATTRIBUTE_POOLING_PARAMETER_PREFIX = "POOLING_";
    
    /**
     * A flag to determine if we should use result streaming on MySQL
     */
    public static final String ATTRIBUTE_USE_RESULT_STREAMING = "STREAM_RESULTS";

    /**
     * A flag to determine if we should use a double decimal separator to specify schema/table combinations on MS-SQL server
     */
    public static final String ATTRIBUTE_MSSQL_DOUBLE_DECIMAL_SEPARATOR = "MSSQL_DOUBLE_DECIMAL_SEPARATOR";

    /**
     * A flag to determine if we should quote all fields
     */
    public static final String ATTRIBUTE_QUOTE_ALL_FIELDS = "QUOTE_ALL_FIELDS";

    /**
     * A flag to determine if we should force all identifiers to lower case
     */
    public static final String ATTRIBUTE_FORCE_IDENTIFIERS_TO_LOWERCASE = "FORCE_IDENTIFIERS_TO_LOWERCASE";

    /**
     * A flag to determine if we should force all identifiers to UPPER CASE
     */
    public static final String ATTRIBUTE_FORCE_IDENTIFIERS_TO_UPPERCASE = "FORCE_IDENTIFIERS_TO_UPPERCASE";
    
    /**
     * The preferred schema to use if no other has been specified.
     */
    public static final String ATTRIBUTE_PREFERRED_SCHEMA_NAME = "PREFERRED_SCHEMA_NAME";
    
    /**
     * Checkbox to allow you to configure if the database supports the boolean data type or not.
     * Defaults to "false" for backward compatibility! 
     */
    public static final String ATTRIBUTE_SUPPORTS_BOOLEAN_DATA_TYPE = "SUPPORTS_BOOLEAN_DATA_TYPE";
    
    /**
     * Boolean to indicate if savepoints can be released
     * Most databases do, so we set it to true.  
     * Child classes can overwrite with false if need be.
     */
    protected boolean releaseSavepoint = true;
    
    /**
     * The SQL, minus the table name, to select the number 
     * of rows from a table
     */
    public static final String SELECT_COUNT_STATEMENT = "select count(*) FROM";
        
    public static final DatabaseConnectionPoolParameter[] poolingParameters = new DatabaseConnectionPoolParameter[]
        {
           new DatabaseConnectionPoolParameter("defaultAutoCommit", "true", "The default auto-commit state of connections created by this pool."), 
           new DatabaseConnectionPoolParameter("defaultReadOnly", null, "The default read-only state of connections created by this pool.\nIf not set then the setReadOnly method will not be called.\n (Some drivers don't support read only mode, ex: Informix)"), 
           new DatabaseConnectionPoolParameter("defaultTransactionIsolation", null, "the default TransactionIsolation state of connections created by this pool. One of the following: (see javadoc)\n\n  * NONE\n  * READ_COMMITTED\n  * READ_UNCOMMITTED\n  * REPEATABLE_READ  * SERIALIZABLE\n"), 
           new DatabaseConnectionPoolParameter("defaultCatalog", null, "The default catalog of connections created by this pool."),
           
           new DatabaseConnectionPoolParameter("initialSize", "0", "The initial number of connections that are created when the pool is started."), 
           new DatabaseConnectionPoolParameter("maxActive", "8", "The maximum number of active connections that can be allocated from this pool at the same time, or non-positive for no limit."), 
           new DatabaseConnectionPoolParameter("maxIdle", "8", "The maximum number of connections that can remain idle in the pool, without extra ones being released, or negative for no limit."), 
           new DatabaseConnectionPoolParameter("minIdle", "0", "The minimum number of connections that can remain idle in the pool, without extra ones being created, or zero to create none."), 
           new DatabaseConnectionPoolParameter("maxWait", "-1", "The maximum number of milliseconds that the pool will wait (when there are no available connections) for a connection to be returned before throwing an exception, or -1 to wait indefinitely."),
           
           new DatabaseConnectionPoolParameter("validationQuery", null, "The SQL query that will be used to validate connections from this pool before returning them to the caller.\nIf specified, this query MUST be an SQL SELECT statement that returns at least one row."), 
           new DatabaseConnectionPoolParameter("testOnBorrow", "true", "The indication of whether objects will be validated before being borrowed from the pool.\nIf the object fails to validate, it will be dropped from the pool, and we will attempt to borrow another.\nNOTE - for a true value to have any effect, the validationQuery parameter must be set to a non-null string."), 
           new DatabaseConnectionPoolParameter("testOnReturn", "false", "The indication of whether objects will be validated before being returned to the pool.\nNOTE - for a true value to have any effect, the validationQuery parameter must be set to a non-null string."), 
           new DatabaseConnectionPoolParameter("testWhileIdle", "false", "The indication of whether objects will be validated by the idle object evictor (if any). If an object fails to validate, it will be dropped from the pool.\nNOTE - for a true value to have any effect, the validationQuery parameter must be set to a non-null string."), 
           new DatabaseConnectionPoolParameter("timeBetweenEvictionRunsMillis", null, "The number of milliseconds to sleep between runs of the idle object evictor thread. When non-positive, no idle object evictor thread will be run."),
           
           new DatabaseConnectionPoolParameter("poolPreparedStatements", "false", "Enable prepared statement pooling for this pool."), 
           new DatabaseConnectionPoolParameter("maxOpenPreparedStatements", "-1", "The maximum number of open statements that can be allocated from the statement pool at the same time, or zero for no limit."), 
           new DatabaseConnectionPoolParameter("accessToUnderlyingConnectionAllowed", "false", "Controls if the PoolGuard allows access to the underlying connection."), 
           new DatabaseConnectionPoolParameter("removeAbandoned", "false", "Flag to remove abandoned connections if they exceed the removeAbandonedTimout.\nIf set to true a connection is considered abandoned and eligible for removal if it has been idle longer than the removeAbandonedTimeout. Setting this to true can recover db connections from poorly written applications which fail to close a connection."), 
           new DatabaseConnectionPoolParameter("removeAbandonedTimeout", "300", "Timeout in seconds before an abandoned connection can be removed."), 
           new DatabaseConnectionPoolParameter("logAbandoned", "false", "Flag to log stack traces for application code which abandoned a Statement or Connection.\nLogging of abandoned Statements and Connections adds overhead for every Connection open or new Statement because a stack trace has to be generated."), 
        };

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
	
	private ObjectId objectId;

	private String pluginId;
	private String pluginName;

	public BaseDatabaseMeta()
	{
        attributes = new Properties();
        changed = false;
        if (getAccessTypeList()!=null && getAccessTypeList().length>0) {
        	accessType = getAccessTypeList()[0];
        }
	}
	
	/**
	 * @return plugin ID of this class
	 */
	public String getPluginId() {
		return pluginId;
	}
	
	/**
	 * @param pluginId The plugin ID to set.
	 */
	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	 /**
   * @return plugin name of this class
   */
  public String getPluginName() {
    return pluginName;
  }
  
  /**
   * @param pluginName The plugin name to set.
   */
  public void setPluginName(String pluginName) {
    this.pluginName = pluginName;
  }
  
  abstract public int[] getAccessTypeList();
	
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
	public ObjectId getObjectId()
	{
		return objectId;
	}
	
	/**
	 * @param id The id to set.
	 */
	public void setObjectId(ObjectId id)
	{
		this.objectId = id;
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
	 * Checks for quotes before quoting schema and table.  Many dialects had hardcoded quotes, they probably didn't get updated properly
     * when quoteFields() was introduced to DatabaseMeta.
	 * 
	 * @param schemaPart
	 * @param tablePart
	 * @return quoted schema and table
	 * 
	 * @deprecated we should phase this out in 5.0, but it's there to keep backwards compatibility in the 4.x releases.
	 */
	public String getBackwardsCompatibleSchemaTableCombination(String schemaPart, String tablePart) {
      String schemaTable = "";
      if (schemaPart != null && (schemaPart.contains(getStartQuote()) || schemaPart.contains(getEndQuote()))) {
        schemaTable+=schemaPart;
      } else {
        schemaTable+=getStartQuote()+schemaPart+getEndQuote();
      }
      schemaTable+=".";
      if (tablePart != null && (tablePart.contains(getStartQuote()) || tablePart.contains(getEndQuote()))) {
        schemaTable+=tablePart;
      } else {
        schemaTable+=getStartQuote()+tablePart+getEndQuote();
      }
      return schemaTable;
    }
	
    /**
     * Checks for quotes before quoting table.  Many dialects had hardcoded quotes, they probably didn't get updated properly
     * when quoteFields() was introduced to DatabaseMeta.
     * 
     * @param tablePart
     * 
     * @return quoted table
     * 
     * @deprecated we should phase this out in 5.0, but it's there to keep backwards compatibility in the 4.x releases.
     */
    public String getBackwardsCompatibleTable(String tablePart) {
      if (tablePart != null && tablePart.contains(getStartQuote()) || tablePart.contains(getEndQuote())) {
        return tablePart;
      } else {
        return getStartQuote()+tablePart+getEndQuote();
      }
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
	public String getDropColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon)
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
	 * @return The SQL on this database to get a list of sequences.
	 */
	public String getSQLListOfSequences()
	{
		return  null; 
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
        String usePool = attributes.getProperty(ATTRIBUTE_SUPPORTS_BOOLEAN_DATA_TYPE, "N");
        return "Y".equalsIgnoreCase(usePool);
    }
    
    /**
     * @param b Set to true if the database supports a boolean, bit, logical, ... datatype
     */
	public void setSupportsBooleanDataType(boolean b) 
	{
		attributes.setProperty(ATTRIBUTE_SUPPORTS_BOOLEAN_DATA_TYPE, b?"Y":"N");
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
    public Map<String, String> getExtraOptions()
    {
        Map<String,String> map = new Hashtable<String,String>();
        
        for (Enumeration<Object> keys = attributes.keys() ; keys.hasMoreElements();)
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
        attributes.setProperty(ATTRIBUTE_INITIAL_POOL_SIZE, Integer.toString(initialPoolSize));
    }
    
    
    
    /**
     * @return true if we want to use a database connection pool
     */
    public boolean isPartitioned()
    {
        String isClustered = attributes.getProperty(ATTRIBUTE_IS_CLUSTERED);
        return "Y".equalsIgnoreCase(isClustered);
    }
    
    /**
     * @param usePool true if we want to use a database connection pool
     */
    public void setPartitioned(boolean clustered)
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
            String username    = attributes.getProperty(ATTRIBUTE_CLUSTER_USERNAME_PREFIX+nr);
            String password    = attributes.getProperty(ATTRIBUTE_CLUSTER_PASSWORD_PREFIX+nr);
            clusterInfo[nr] = new PartitionDatabaseMeta(partitionId, hostname, port, dbName);
            clusterInfo[nr].setUsername(username);
            clusterInfo[nr].setPassword( Encr.decryptPasswordOptionallyEncrypted(password) );
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
            attributes.put(ATTRIBUTE_CLUSTER_USERNAME_PREFIX+nr, Const.NVL(meta.getUsername(), ""));
            attributes.put(ATTRIBUTE_CLUSTER_PASSWORD_PREFIX+nr, Const.NVL( Encr.encryptPasswordIfNotUsingVariables(meta.getPassword()), ""));
        }
    }
    
    /**
     * @return The set of properties (newly created object) that contains the connection pooling parameters
     * All environment variables will be replaced here.
     */
    public Properties getConnectionPoolingProperties()
    {
        Properties properties = new Properties();
        
        for (Iterator<Object> iter = attributes.keySet().iterator(); iter.hasNext();)
        {
            String element = (String) iter.next();
            if (element.startsWith(ATTRIBUTE_POOLING_PARAMETER_PREFIX))
            {
                String key = element.substring(ATTRIBUTE_POOLING_PARAMETER_PREFIX.length());
                String value = attributes.getProperty(element);
                properties.put(key, value);
            }
        }
        
        return properties;
    }
    
    public void setConnectionPoolingProperties(Properties properties)
    {
        // Clear our the previous set of pool parameters
        for (Iterator<Object> iter = attributes.keySet().iterator(); iter.hasNext();)
        {
            String key = (String) iter.next();
            if (key.startsWith(ATTRIBUTE_POOLING_PARAMETER_PREFIX))
            {
                attributes.remove(key);
            }
        }
        
        for (Iterator<Object> iter = properties.keySet().iterator(); iter.hasNext();)
        {
            String element = (String) iter.next();
            String value = properties.getProperty(element);
            if (!Const.isEmpty(element) && !Const.isEmpty(value))
            {
                attributes.put(ATTRIBUTE_POOLING_PARAMETER_PREFIX+element, value);
            }
        }
    }
    
    public String getSQLTableExists(String tablename)
    {
        return "SELECT 1 FROM "+tablename;
    }
    public String getSQLColumnExists(String columnname, String tablename)
    {
        return "SELECT " + columnname + " FROM "+tablename;
    }
    public boolean needsToLockAllTables()
    {
        return true;
    }
    
    
    /**
     * @return true if the database is streaming results (normally this is an option just for MySQL).
     */
    public boolean isStreamingResults()
    {
        String usePool = attributes.getProperty(ATTRIBUTE_USE_RESULT_STREAMING, "Y"); // DEFAULT TO YES!!
        return "Y".equalsIgnoreCase(usePool);
    }
    
    /**
     * @param useStreaming true if we want the database to stream results (normally this is an option just for MySQL).
     */
    public void setStreamingResults(boolean useStreaming)
    {
        attributes.setProperty(ATTRIBUTE_USE_RESULT_STREAMING, useStreaming?"Y":"N");
    }

    /**
     * @return true if all fields should always be quoted in db
     */
    public boolean isQuoteAllFields()
    {
        String quoteAllFields = attributes.getProperty(ATTRIBUTE_QUOTE_ALL_FIELDS, "N"); // DEFAULT TO NO!!
        return "Y".equalsIgnoreCase(quoteAllFields);
    }
    
    /**
     * @param useStreaming true if we want the database to stream results (normally this is an option just for MySQL).
     */
    public void setQuoteAllFields(boolean quoteAllFields)
    {
        attributes.setProperty(ATTRIBUTE_QUOTE_ALL_FIELDS, quoteAllFields?"Y":"N");
    }
    
    /**
     * @return true if all identifiers should be forced to lower case
     */
    public boolean isForcingIdentifiersToLowerCase()
    {
        String forceLowerCase = attributes.getProperty(ATTRIBUTE_FORCE_IDENTIFIERS_TO_LOWERCASE, "N"); // DEFAULT TO NO!!
        return "Y".equalsIgnoreCase(forceLowerCase);
    }
    
    /**
     * @param forceLowerCase true if all identifiers should be forced to lower case
     */
    public void setForcingIdentifiersToLowerCase(boolean forceLowerCase)
    {
        attributes.setProperty(ATTRIBUTE_FORCE_IDENTIFIERS_TO_LOWERCASE, forceLowerCase?"Y":"N");
    }
    
    /**
     * @return true if all identifiers should be forced to upper case
     */
    public boolean isForcingIdentifiersToUpperCase()
    {
        String forceUpperCase = attributes.getProperty(ATTRIBUTE_FORCE_IDENTIFIERS_TO_UPPERCASE, "N"); // DEFAULT TO NO!!
        return "Y".equalsIgnoreCase(forceUpperCase);
    }
    
    /**
     * @param forceLowerCase true if all identifiers should be forced to upper case
     */
    public void setForcingIdentifiersToUpperCase(boolean forceUpperCase)
    {
        attributes.setProperty(ATTRIBUTE_FORCE_IDENTIFIERS_TO_UPPERCASE, forceUpperCase?"Y":"N");
    }
    
    /**
     * @return true if we use a double decimal separator to specify schema/table combinations on MS-SQL server
     */
    public boolean isUsingDoubleDecimalAsSchemaTableSeparator()
    {
        String usePool = attributes.getProperty(ATTRIBUTE_MSSQL_DOUBLE_DECIMAL_SEPARATOR, "N"); // DEFAULT TO YES!!
        return "Y".equalsIgnoreCase(usePool);
    }
    
    /**
     * @param useDoubleDecimalSeparator true if we should use a double decimal separator to specify schema/table combinations on MS-SQL server
     */
    public void setUsingDoubleDecimalAsSchemaTableSeparator(boolean useDoubleDecimalSeparator)
    {
        attributes.setProperty(ATTRIBUTE_MSSQL_DOUBLE_DECIMAL_SEPARATOR, useDoubleDecimalSeparator?"Y":"N");
    }
    
	/**
	 * @return true if this database needs a transaction to perform a query (auto-commit turned off).
	 */
	public boolean isRequiringTransactionsOnQueries()
	{
		return true;
	}

	/**
	 * You can use this method to supply an alternate factory for the test method in the dialogs.
	 * This is useful for plugins like SAP/R3 and PALO.
	 *  
	 * @return the name of the database test factory to use.
	 */
	public String getDatabaseFactoryName()
	{
		return DatabaseFactory.class.getName();
	}
	
    /**
     * @return The preferred schema name of this database connection.
     */
    public String getPreferredSchemaName()
    {
        return attributes.getProperty(ATTRIBUTE_PREFERRED_SCHEMA_NAME);
    }
    
    /**
     * @param preferredSchemaName The preferred schema name of this database connection.
     */
    public void setPreferredSchemaName(String preferredSchemaName)
    {
        attributes.setProperty(ATTRIBUTE_PREFERRED_SCHEMA_NAME, preferredSchemaName);
    }
    
    /**
     * Verifies on the specified database connection if an index exists on the fields with the specified name.
     * 
     * @param database a connected database
     * @param schemaName
     * @param tableName
     * @param idxFields
     * @return true if the index exists, false if it doesn't.
     * @throws KettleException
     */
	public boolean checkIndexExists(Database database, String schemaName, String tableName, String[] idx_fields) throws KettleDatabaseException {
		
        String tablename = database.getDatabaseMeta().getQuotedSchemaTableCombination(schemaName, tableName);

		boolean exists[] = new boolean[idx_fields.length];
		for (int i=0;i<exists.length;i++) exists[i]=false;
		
		try
		{
			// Get a list of all the indexes for this table
	        ResultSet indexList = null;
	        try  
	        {
	        	indexList = database.getDatabaseMetaData().getIndexInfo(null,null,tablename,false,true);
	        	while (indexList.next())
	        	{
	        		// String tablen  = indexList.getString("TABLE_NAME");
	        		// String indexn  = indexList.getString("INDEX_NAME");
	        		String column  = indexList.getString("COLUMN_NAME");
	        		// int    pos     = indexList.getShort("ORDINAL_POSITION");
	        		// int    type    = indexList.getShort("TYPE");
	        		
	        		int idx = Const.indexOfString(column, idx_fields);
	        		if (idx>=0)
	        		{
	        			exists[idx]=true;
	        		}
	        	}
	        }
	        finally 
	        {
	            if ( indexList != null ) indexList.close();
	        }
			
	        // See if all the fields are indexed...
	        boolean all=true;
	        for (int i=0;i<exists.length && all;i++) if (!exists[i]) all=false;
	        
			return all;
		}
		catch(Exception e)
		{
			throw new KettleDatabaseException("Unable to determine if indexes exists on table ["+tablename+"]", e);
		}

	}

	/**
	 * @return true if the database supports the NOMAXVALUE sequence option.
	 * The default is false, AS/400 and DB2 support this. 
	 */
	public boolean supportsSequenceNoMaxValueOption() {
		return false;
	}

	/**
	 * @return true if we need to append the PRIMARY KEY block in the create table block after the fields, required for Caché.
	 */
	public boolean requiresCreateTablePrimaryKeyAppend() {
		return false;
	}

	/**
	 * @return true if the database requires you to cast a parameter to varchar before comparing to null.  Only required for DB2 and Vertica
	 * 
	 */
	public boolean requiresCastToVariousForIsNull() {
		return false;
	}
	
	/**
	 * @return Handles the special case of DB2 where the display size returned is twice the precision.
	 * In that case, the length is the precision.
	 * 
	 */
	public boolean isDisplaySizeTwiceThePrecision() {
		return false;
	}
	
	/**
	 * Most databases allow you to retrieve result metadata by preparing a SELECT statement.
	 * 
	 * @return true if the database supports retrieval of query metadata from a prepared statement.  False if the query needs to be executed first.
	 */
	public boolean supportsPreparedStatementMetadataRetrieval() {
		return true;
	}

	/**
	 * @param tableName
	 * @return true if the specified table is a system table
	 */
	public boolean isSystemTable(String tableName) {
		return false;
	}
	
	/**
	 * @return true if the database supports newlines in a SQL statements.
	 */
	public boolean supportsNewLinesInSQL() {
		return true;
	}

	/**
	 * @return the SQL to retrieve the list of schemas or null if the JDBC metadata needs to be used.
	 */
	public String getSQLListOfSchemas() {
		return null;
	}
	
	/**
	 * @return The maximum number of columns in a database, <=0 means: no known limit
	 */
	public int getMaxColumnsInIndex() {
		return 0;
	}
	
	/**
	 * @return true if the database supports error handling (recovery of failure) while doing batch updates.
	 */
	public boolean supportsErrorHandlingOnBatchUpdates() {
		return true;
	}

	/**
	 * Get the SQL to insert a new empty unknown record in a dimension.
	 * @param schemaTable the schema-table name to insert into
	 * @param keyField The key field
	 * @param versionField the version field
	 * @return the SQL to insert the unknown record into the SCD.
	 */
	public String getSQLInsertAutoIncUnknownDimensionRow(String schemaTable, String keyField, String versionField) {
		return "insert into "+schemaTable+"("+keyField+", "+versionField+") values (0, 1)";		
	}

	/**
	 * @return true if this is a relational database you can explore.
	 * Return false for SAP, PALO, etc.
	 */
	public boolean isExplorable() {
		return true;
	}

	/**
	 * @return The name of the XUL overlay file to display extra options.
	 * This is only used in case of a non-standard plugin.
	 * Usually this method returns null.
	 */
	public String getXulOverlayFile() {
		return null;
	}
	
	/**
   * @param string
   * @return A string that is properly quoted for use in a SQL statement (insert, update, delete, etc)
   */
  public String quoteSQLString(String string) {
    string = string.replaceAll("'", "''"); 
    string = string.replaceAll("\\n", "\\\\n");
    string = string.replaceAll("\\r", "\\\\r");
    return "'"+string+"'";
  }

  
  /**
   * Build the SQL to count the number of rows in the passed table.
   * @param tableName
   * @return
   */
  public String getSelectCountStatement(String tableName) {
      return SELECT_COUNT_STATEMENT+" "+tableName;
  }
  
  public String generateColumnAlias(int columnIndex, String suggestedName) {
    return "COL" + Integer.toString(columnIndex); //$NON-NLS-1$
  }
  
  /**
   * Parse all possible statements from the provided SQL script.
   * 
   * @param sqlScript Raw SQL Script to be parsed into executable statements.
   * @return List of parsed SQL statements to be executed separately.
   */
  public List<String> parseStatements(String sqlScript) {
    List<String> statements = new ArrayList<String>();
    String all = sqlScript;
    int from = 0;
    int to = 0;
    int length = all.length();

    while (to < length) {
      char c = all.charAt(to);

      // Skip comment lines...
      //
      while (all.substring(from).startsWith("--")) {
        int nextLineIndex = all.indexOf(Const.CR, from);
        from = nextLineIndex + Const.CR.length();
        if (to >= length)
          break;
        c = all.charAt(c);
      }
      if (to >= length)
        break;

      // Skip over double quotes...
      //
      if (c == '"') {
        int nextDQuoteIndex = all.indexOf('"', to + 1);
        if (nextDQuoteIndex >= 0) {
          to = nextDQuoteIndex + 1;
        }
      }
      
      // Skip over back-ticks
      if (c == '`') {
        int nextBacktickIndex = all.indexOf('`', to + 1);
        if (nextBacktickIndex >= 0) {
          to = nextBacktickIndex + 1;
        }
      }
      
      c = all.charAt(to);
      if (c == '\'') {
        boolean skip = true;

        // Don't skip over \' or ''
        //
        if (to > 0) {
          char prevChar = all.charAt(to - 1);
          if (prevChar == '\\' || prevChar == '\'') {
            skip = false;
          }
        }

        // Jump to the next quote and continue from there.
        //
        while (skip) {
          int nextQuoteIndex = all.indexOf('\'', to + 1);
          if (nextQuoteIndex >= 0) {
            to = nextQuoteIndex + 1;

            skip = false;

            if (to < all.length()) {
              char nextChar = all.charAt(to);
              if (nextChar == '\'') {
                skip = true;
                to++;
              }
            }
            if (to > 0) {
              char prevChar = all.charAt(to - 2);
              if (prevChar == '\\') {
                skip = true;
                to++;
              }
            }
          }
        }
      }

      c = all.charAt(to);

      if (c == ';' || to >= length - 1) // end of statement
      {
        if (to >= length - 1)
          to++; // grab last char also!

        String stat = all.substring(from, to);
        if (!onlySpaces(stat)) {
          statements.add(Const.trim(stat));
        }
        to++;
        from = to;
      } else {
        to++;
      }
    }
    return statements;
  }

  /**
   * @param str 
   * @return True if {@code str} contains only spaces.
   */
  protected boolean onlySpaces(String str) {
    for (int i = 0; i < str.length(); i++) {
      int c = str.charAt(i);
      if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
        return false;
      }
    }
    return true;
  }
  
  /**
   * @return true if the database is a MySQL variant, like MySQL 5.1, InfiniDB, InfoBright, and so on.
   */
  public boolean isMySQLVariant() {
    return false;
  }
  
  /**
   * Returns a true of savepoints can be released, false if not.
   * @return
   */
  public boolean releaseSavepoint() {
     return releaseSavepoint;
  }
}