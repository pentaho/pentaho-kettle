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

import java.util.Map;
import java.util.Properties;

import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.repository.ObjectId;

/**
 * This interface describes the methods that a database connection needs to have in order to describe it properly.
 * 
 * @author Matt
 * @since  11-mrt-2005
 */
public interface DatabaseInterface extends Cloneable
{
	/**
	 * @return the plugin id of this database
	 */
	public String getPluginId();
	
	/**
	 * @param pluginId set the plugin id of this plugin (after instantiation)
	 */
	public void setPluginId(String pluginId);
	
	
	/**
	 * @return the plugin name of this database, the same thing as the annotation typeDescription
	 */
	public String getPluginName();
	
	/**
	 * 
	 * @param pluginName set the plugin name of this plugin (after instantiation)
	 */
	public void setPluginName(String pluginName);
	/*
	/**
	 * 
	 * @return The database type
	 */
	// public int getDatabaseType();
	

	/**
	 * @return Returns the accessType.
	 */
	public int getAccessType();
	
	/**
	 * @param accessType The accessType to set.
	 */
	public void setAccessType(int accessType);
	
	/**
	 * @return Returns the changed.
	 */
	public boolean isChanged();
	
	/**
	 * @param changed The changed to set.
	 */
	public void setChanged(boolean changed);
	
	/**
	 * @return Returns the connection Name.
	 */
	public String getName();
	
	/**
	 * @param name The connection Name to set.
	 */
	public void setName(String name);
	
	/**
	 * @return Returns the databaseName.
	 */
	public String getDatabaseName();
	
	/**
	 * @param databaseName The databaseName to set.
	 */
	public void setDatabaseName(String databaseName);
	
	/**
	 * @return Returns the databasePortNumber as a string.
	 */
	public String getDatabasePortNumberString();
	
	/**
	 * @param databasePortNumberString The databasePortNumber to set as a string.
	 */
	public void setDatabasePortNumberString(String databasePortNumberString);

	/**
	 * @return Returns the hostname.
	 */
	public String getHostname();
	
	/**
	 * @param hostname The hostname to set.
	 */
	public void setHostname(String hostname);
	
	/**
	 * @return Returns the id.
	 */
	public ObjectId getObjectId();
	
	/**
	 * @param id The id to set.
	 */
	public void setObjectId(ObjectId id);
	
	/**
	 * @return the username to log onto the database
	 */
	public String getUsername();
	
	/**
	 * @param username Sets the username to log onto the database with.
	 */
	public void setUsername(String username);
	
	/**
	 * @return Returns the password.
	 */
	public String getPassword();
	
	/**
	 * @param password The password to set.
	 */
	public void setPassword(String password);
	
	/**
	 * @return Returns the servername.
	 */
	public String getServername();
	
	/**
	 * @param servername The servername to set.
	 */
	public void setServername(String servername);
	
	/** 
	 * @return the tablespace to store data in. (create table)
	 */
	public String getDataTablespace();
	
	/**
	 * @param data_tablespace the tablespace to store data in
	 */
	public void setDataTablespace(String data_tablespace);
	
	/**
	 * @return the tablespace to store indexes in
	 */
	public String getIndexTablespace();

	/**
	 * @param index_tablespace the tablespace to store indexes in
	 */
	public void setIndexTablespace(String index_tablespace);
	
    /**
    * @return The extra attributes for this database connection
    */
    public Properties getAttributes();
   
   /**
    * Set extra attributes on this database connection
    * @param attributes The extra attributes to set on this database connection.
    */
    public void setAttributes(Properties attributes);
	
	/**
	 * See if this database supports the setCharacterStream() method on a PreparedStatement.
	 * 
	 * @return true if we can set a Stream on a field in a PreparedStatement.  False if not. 
	 */
	public boolean supportsSetCharacterStream();
	
	/**
	 * @return Whether or not the database can use auto increment type of fields (pk)
	 */
	public boolean supportsAutoInc();

	/**
	 * Describe a Value as a field in the database.
	 * @param v The value to describe
	 * @param tk The field that's going to be the technical key
	 * @param pk The field that's going to be the primary key
	 * @param use_autoinc Use autoincrement or not
	 * @param add_fieldname Add the fieldname to the definition or not
	 * @param add_cr Add a cariage return at the end of the definition or not.
	 * @return a value described as a field in this database.
	 */
	public String getFieldDefinition(ValueMetaInterface v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr);

	/**
	 * Get the list of possible access types for a database.
	 * @return the list of possible access types for a database.
	 */
	public int[] getAccessTypeList();

	/**
	 * @return the default database port number
	 */
	public int getDefaultDatabasePort();

	/**
	 * @param nrRows The number of rows to which we want to limit the result of the query.
	 * @return the clause after a select statement to limit the number of rows
	 */
	public String getLimitClause(int nrRows);

	/**
	 * Returns the minimal SQL to launch in order to determine the layout of the resultset for a given database table
	 * @param tableName The name of the table to determine the layout for
	 * @return The SQL to launch.
	 */
	public String getSQLQueryFields(String tableName);

	/**
	 * Get the not found technical key.
	 * @param use_autoinc Whether or not we want to use an auto increment field
	 * @return the lowest possible technical key to be used as the NOT FOUND row in a slowly changing dimension.
	 */
	public int getNotFoundTK(boolean use_autoinc);

	/**
	 * Obtain the name of the JDBC driver class that we need to use!
	 * @return the name of the JDBC driver class for the specific database
	 */
	public String getDriverClass();

    /**
     * @param hostname the hostname
     * @param port the port as a string
     * @param databaseName the database name
     * @return the URL to use for connecting to the database.
     * @throws KettleDatabaseException in case a configuration error is detected.
     */
	public String getURL(String hostname, String port, String databaseName) throws KettleDatabaseException;

    /**
     * @return true if the database supports sequences
     */
    public boolean supportsSequences();

	/**
	 * Get the SQL to get the next value of a sequence. 
	 * @param sequenceName The sequence name
	 * @return the SQL to get the next value of a sequence.
	 */
	public String getSQLNextSequenceValue(String sequenceName);

    /**
     * Get the current value of a database sequence
     * @param sequenceName The sequence to check
     * @return The current value of a database sequence
     */
    public String getSQLCurrentSequenceValue(String sequenceName);
    
    /**
     * Check if a sequence exists.
     * @param sequenceName The sequence to check
     * @return The SQL to get the name of the sequence back from the databases data dictionary
     */
    public String getSQLSequenceExists(String sequenceName);

	/**
	 * Checks whether or not the command setFetchSize() is supported by the JDBC driver...
	 * @return true is setFetchSize() is supported!
	 */
	public boolean isFetchSizeSupported();

	/**
	 * @return true if the database supports transactions.
	 */
	public boolean supportsTransactions();

	/**
	 * @return true if the database supports bitmap indexes
	 */
	public boolean supportsBitmapIndex();

	/**
	 * @return true if the database JDBC driver supports the setLong command
	 */
	public boolean supportsSetLong();

	/**
	 * @return true if the database supports schemas
	 */
	public boolean supportsSchemas();
	
    /**
     * @return true if the database supports catalogs
     */
    public boolean supportsCatalogs();

	/**
	 * 
	 * @return true when the database engine supports empty transaction.
	 * (for example Informix does not!)
	 */
	public boolean supportsEmptyTransactions();
	
	/**
	 * Indicates the need to insert a placeholder (0) for auto increment fields.
	 * @return true if we need a placeholder for auto increment fields in insert statements.
	 */
	public boolean needsPlaceHolder();

	/**
	 * @return the function for Sum agrregate 
	 */
	public String getFunctionSum();

	/**
	 * @return the function for Average agrregate 
	 */
	public String getFunctionAverage();

	/**
	 * @return the function for Minimum agrregate 
	 */
	public String getFunctionMinimum();

	/**
	 * @return the function for Maximum agrregate 
	 */
	public String getFunctionMaximum();

	/**
	 * @return the function for Count agrregate 
	 */
	public String getFunctionCount();

	/**
	 * Get the schema-table combination to query the right table.
	 * Usually that is SCHEMA.TABLENAME, however there are exceptions to this rule...
	 * @param schema_name The schema name
	 * @param table_part The tablename
	 * @return the schema-table combination to query the right table.
	 */
	public String getSchemaTableCombination(String schema_name, String table_part);

	/**
	 * Get the maximum length of a text field for this database connection.
	 * This includes optional CLOB, Memo and Text fields. (the maximum!)
	 * @return The maximum text field length for this database type. (mostly CLOB_LENGTH)
	 */
	public int getMaxTextFieldLength();

	/**
	 * Get the maximum length of a text field (VARCHAR) for this database connection.
	 * If this size is exceeded use a CLOB.
	 * @return The maximum VARCHAR field length for this database type. (mostly identical to getMaxTextFieldLength() - CLOB_LENGTH)
	 */
	public int getMaxVARCHARLength();

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
	public String getAddColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon);

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
	public String getDropColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon);

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
	public String getModifyColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon);

	/**
	 * Clone this database interface: copy all info to a new object
	 * @return the cloned Database Interface object.
	 */
	public Object clone();
	
	/**
	 * @return an array of reserved words for the database type...
	 */
	public String[] getReservedWords();
	
	/**
	 * @return true if reserved words need to be double quoted ("password", "select", ...)
	 */
	public boolean quoteReservedWords();
	
	/**
	 * @return The start quote sequence, mostly just double quote, but sometimes [, ...
	 */
	public String getStartQuote();
	
	/**
	 * @return The end quote sequence, mostly just double quote, but sometimes ], ...
	 */
	public String getEndQuote();
	
	/**
	 * @return true if Kettle can create a repository on this type of database.
	 */
	public boolean supportsRepository();
	
	/**
	 * @return a list of table types to retrieve tables for the database
	 * This is mostly just { "TABLE" }
	 */
	public String[] getTableTypes();

	/**
	 * @return a list of table types to retrieve views for the database
	 * This is mostly just { "VIEW" }
	 */
	public String[] getViewTypes();

	/**
	 * @return a list of table types to retrieve synonyms for the database
	 */
	public String[] getSynonymTypes();

	/**
	 * @return true if we need to supply the schema-name to getTables in order to get a correct list of items.
	 */
	public boolean useSchemaNameForTableList();
	
	
	/**
	 * @return true if the database supports views
	 */
	public boolean supportsViews();
	
	/**
	 * @return true if the database supports synonyms
	 */
	public boolean supportsSynonyms();

	/**
	 * @return The SQL on this database to get a list of stored procedures.
	 */
	public String getSQLListOfProcedures();
	
	
	/**
	 * @param tableName The table to be truncated.
	 * @return The SQL statement to truncate a table: remove all rows from it without a transaction
	 */
	public String getTruncateTableStatement(String tableName);
    
    /**
     * @return true if the database rounds floating point numbers to the right precision.
     * For example if the target field is number(7,2) the value 12.399999999 is converted into 12.40
     */
    public boolean supportsFloatRoundingOnUpdate();
    
    /**
     * @param tableNames The names of the tables to lock
     * @return The SQL command to lock database tables for write purposes.
     *         null is returned in case locking is not supported on the target database.
     */
    public String getSQLLockTables(String tableNames[]);
    
    /**
     * @param tableNames The names of the tables to unlock
     * @return The SQL command to unlock the database tables. 
     *         null is returned in case locking is not supported on the target database.
     */
    public String getSQLUnlockTables(String tableNames[]);

    /**
     * @return true if the database resultsets support getTimeStamp() to retrieve date-time. (Date)
     */
    public boolean supportsTimeStampToDateConversion();

    /**
     * @return true if the database JDBC driver supports batch updates
     * For example Interbase doesn't support this!
     */
    public boolean supportsBatchUpdates();

    /**
     * @return true if the database supports a boolean, bit, logical, ... datatype
     */
    public boolean supportsBooleanDataType();
    
    /**
     * @param b Set to true if the database supports a boolean, bit, logical, ... datatype
     */
	public void setSupportsBooleanDataType(boolean b);

    /**
     * @return true if the database defaults to naming tables and fields in upper case.
     * True for most databases except for stuborn stuff like Postgres ;-)
     */
    public boolean isDefaultingToUppercase();

    /**
     * @return a map of all the extra URL options you want to set, retrieved from the attributes list (NOT synchronized!)
     */
    public Map<String, String> getExtraOptions();

    /**
     * Add an extra option to the attributes list
     * @param databaseTypeCode The database type code for which the option applies
     * @param option The option to set
     * @param value The value of the option
     */
    public void addExtraOption(String databaseTypeCode, String option, String value);
    
    /**
     * @return The extra option separator in database URL for this platform (usually this is semicolon ; ) 
     */
    public String getExtraOptionSeparator();
    
    /**
     * @return The extra option value separator in database URL for this platform (usually this is the equal sign = ) 
     */
    public String getExtraOptionValueSeparator();

    /**
     * @return This indicator separates the normal URL from the options
     */
    public String getExtraOptionIndicator();

    /**
     * @return true if the database supports connection options in the URL, false if they are put in a Properties object.
     */
    public boolean supportsOptionsInURL();
    
    /**
     * @return extra help text on the supported options on the selected database platform.
     */
    public String getExtraOptionsHelpText();
    
    /**
     * @return true if the database JDBC driver supports getBlob on the resultset.  If not we must use getBytes() to get the data.
     */
    public boolean supportsGetBlob();

    /**
     * @return The SQL to execute right after connecting
     */
    public String getConnectSQL();

    /**
     * @param sql The SQL to execute right after connecting
     */
    public void setConnectSQL(String sql);

    /**
     * @return true if the database supports setting the maximum number of return rows in a resultset.
     */
    public boolean supportsSetMaxRows();

    /**
     * @return true if we want to use a database connection pool
     */
    public boolean isUsingConnectionPool();
    
    /**
     * @param usePool true if we want to use a database connection pool
     */
    public void setUsingConnectionPool(boolean usePool);
    
    /**
     * @return the maximum pool size
     */
    public int getMaximumPoolSize();

    /**
     * @param maximumPoolSize the maximum pool size
     */
    public void setMaximumPoolSize(int maximumPoolSize);

    /**
     * @return the initial pool size
     */
    public int getInitialPoolSize();
    
    /**
     * @param initalPoolSize the initial pool size
     */
    public void setInitialPoolSize(int initalPoolSize);    
    

    /**
     * @return true if the connection contains partitioning information
     */
    public boolean isPartitioned();
    
    /**
     * @param partitioned true if the connection is set to contain partitioning information
     */
    public void setPartitioned(boolean partitioned);
    
    /**
     * @return the available partition/host/databases/port combinations in the cluster
     */
    public PartitionDatabaseMeta[] getPartitioningInformation();
    
    /**
     * @param partitionInfo the available partition/host/databases/port combinations in the cluster
     */
    public void setPartitioningInformation(PartitionDatabaseMeta[] partitionInfo);
    
    /**
     * @return the required libraries (in libext) for this database connection.
     */
    public String[] getUsedLibraries();

    /**
     * @return The set of properties that allows you to set the connection pooling parameters
     */
    public Properties getConnectionPoolingProperties();

    /** set the connection pooling properties */
    public void setConnectionPoolingProperties(Properties properties);

    /**
     * @param tablename The table to verify the existance for
     * @return The SQL to execute to verify if the given table exists.  If an Exception is thrown for this SQL, we don't have the table.
     */
    public String getSQLTableExists(String tablename);

    
    /**
     * @param columnname The column to verify the existance for
     * @param tablename The table to verify the existance for
     * @return The SQL to execute to verify if the given table exists.  If an Exception is thrown for this SQL, we don't have the column.
     */
    public String getSQLColumnExists(String column, String tablename);
    
    /**
     * @return true if the database needs all repository tables to be locked, not just one ref table (R_REPOSITORY_LOG)
     */
    public boolean needsToLockAllTables();

    /**
     * @return true if the database is streaming results (normally this is an option just for MySQL).
     */
    public boolean isStreamingResults();
   
    /**
     * @param useStreaming true if we want the database to stream results (normally this is an option just for MySQL).
     */
    public void setStreamingResults(boolean useStreaming);

    /**
     * @return true if all fields should always be quoted in db
     */
    public boolean isQuoteAllFields();

    /**
     * @param quoteAllFields true if all fields in DB should be quoted.
     */
    public void setQuoteAllFields(boolean quoteAllFields);
    
    /**
     * @return true if all identifiers should be forced to lower case
     */
    public boolean isForcingIdentifiersToLowerCase();
    
    /**
     * @param forceLowerCase true if all identifiers should be forced to lower case
     */
    public void setForcingIdentifiersToLowerCase(boolean forceLowerCase);
    
    /**
     * @return true if all identifiers should be forced to upper case
     */
    public boolean isForcingIdentifiersToUpperCase();
    
    /**
     * @param forceLowerCase true if all identifiers should be forced to upper case
     */
    public void setForcingIdentifiersToUpperCase(boolean forceUpperCase);
    
    /**
     * @return true if we use a double decimal separator to specify schema/table combinations on MS-SQL server
     */
    public boolean isUsingDoubleDecimalAsSchemaTableSeparator();

    /**
     * @param useDoubleDecimalSeparator true if we should use a double decimal separator to specify schema/table combinations on MS-SQL server
     */
    public void setUsingDoubleDecimalAsSchemaTableSeparator(boolean useDoubleDecimalSeparator);
    
	/**
	 * @return true if this database needs a transaction to perform a query (auto-commit turned off).
	 */
	public boolean isRequiringTransactionsOnQueries();
	
	/**
	 * You can use this method to supply an alternate factory for the test method in the dialogs.
	 * This is useful for plugins like SAP/R3 and PALO.
	 *  
	 * @return the name of the database test factory to use.
	 */
	public String getDatabaseFactoryName();

	/**
     * @return The preferred schema name of this database connection.
     */
    public String getPreferredSchemaName();
    
    /**
     * @param preferredSchemaName The preferred schema name of this database connection.
     */
    public void setPreferredSchemaName(String preferredSchemaName);

    /**
     * Verifies on the specified database connection if an index exists on the fields with the specified name.
     * 
     * @param database
     * @param schemaName
     * @param tableName
     * @param idxFields
     * @return
     * @throws KettleDatabaseException
     */
	public boolean checkIndexExists(Database database, String schemaName, String tableName, String[] idxFields) throws KettleDatabaseException;

	/**
	 * @return true if the database supports sequences with a maximum value option.
	 * The default is true.
	 */
	public boolean supportsSequenceNoMaxValueOption();

	/**
	 * @return true if we need to append the PRIMARY KEY block in the create table block after the fields, required for Caché.
	 */
	public boolean requiresCreateTablePrimaryKeyAppend();

	/**
	 * @return true if the database requires you to cast a parameter to varchar before comparing to null.
	 * 
	 */
	public boolean requiresCastToVariousForIsNull();

	/**
	 * @return Handles the special case of DB2 where the display size returned is twice the precision.
	 * In that case, the length is the precision.
	 */
	public boolean isDisplaySizeTwiceThePrecision();

	/**
	 * Most databases allow you to retrieve result metadata by preparing a SELECT statement.
	 * 
	 * @return true if the database supports retrieval of query metadata from a prepared statement.  False if the query needs to be executed first.
	 */
	public boolean supportsPreparedStatementMetadataRetrieval();

	/**
	 * @param tableName
	 * @return true if the specified table is a system table
	 */
	public boolean isSystemTable(String tableName);

	/**
	 * @return true if the database supports newlines in a SQL statements.
	 */
	public boolean supportsNewLinesInSQL();

	/**
	 * @return the SQL to retrieve the list of schemas
	 */
	public String getSQLListOfSchemas();

	/**
	 * @return The maximum number of columns in a database, <=0 means: no known limit
	 */
	public int getMaxColumnsInIndex();

	/**
	 * @return true if the database supports error handling (recovery of failure) while doing batch updates.
	 */
	public boolean supportsErrorHandlingOnBatchUpdates();

	/**
	 * Get the SQL to insert a new empty unknown record in a dimension.
	 * @param schemaTable the schema-table name to insert into
	 * @param keyField The key field
	 * @param versionField the version field
	 * @return the SQL to insert the unknown record into the SCD.
	 */
	public String getSQLInsertAutoIncUnknownDimensionRow(String schemaTable, String keyField, String versionField);
}
