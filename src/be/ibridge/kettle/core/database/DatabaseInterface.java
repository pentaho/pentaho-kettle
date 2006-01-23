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

import be.ibridge.kettle.core.value.Value;

/**
 * This interface describes the methods that a database connection needs to have in order to describe it properly.
 * 
 * @author Matt
 * @since  11-mrt-2005
 */
public interface DatabaseInterface extends Cloneable
{
	public static final Class[] implementingClasses =
		{
			MySQLDatabaseMeta.class,
			OracleDatabaseMeta.class,
			AS400DatabaseMeta.class,
			MSAccessDatabaseMeta.class,
			MSSQLServerDatabaseMeta.class,
			DB2DatabaseMeta.class,
			PostgreSQLDatabaseMeta.class,			
			CacheDatabaseMeta.class,
			InformixDatabaseMeta.class,
			SybaseDatabaseMeta.class,
			GuptaDatabaseMeta.class,
			DbaseDatabaseMeta.class,
			FirebirdDatabaseMeta.class,
			SAPDBDatabaseMeta.class,
			HypersonicDatabaseMeta.class,
			GenericDatabaseMeta.class,
            SAPR3DatabaseMeta.class,
            IngresDatabaseMeta.class,
            InterbaseDatabaseMeta.class
		};
	
	/**
	 * 
	 * @return The database type
	 */
	public int getDatabaseType();
	
	/**
	 * @return The short description (code) of the database type
	 */
	public String getDatabaseTypeDesc();
	
	/**
	 * @return The long description (user description) of the database type
	 */
	public String getDatabaseTypeDescLong();
	
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
	 * @return Returns the databasePortNumber.
	 */
	public int getDatabasePortNumber();
	
	/**
	 * @param databasePortNumber The databasePortNumber to set.
	 */
	public void setDatabasePortNumber(int databasePortNumber);
	
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
	public long getId();
	
	/**
	 * @param id The id to set.
	 */
	public void setId(long id);
	
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
	public String getFieldDefinition(Value v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr);

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
	 * @return the URL to use for connecting to the database.
	 */
	public String getURL();

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
	 * Generates the SQL statement to add a column to the specified table
	 * @param tablename The table to add
	 * @param v The column defined as a value
	 * @param tk the name of the technical key field
	 * @param use_autoinc whether or not this field uses auto increment
	 * @param pk the name of the primary key field
	 * @param semicolon whether or not to add a semi-colon behind the statement.
	 * @return the SQL statement to add a column to the specified table
	 */
	public String getAddColumnStatement(String tablename, Value v, String tk, boolean use_autoinc, String pk, boolean semicolon);

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
	public String getDropColumnStatement(String tablename, Value v, String tk, boolean use_autoinc, String pk, boolean semicolon);

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
	public String getModifyColumnStatement(String tablename, Value v, String tk, boolean use_autoinc, String pk, boolean semicolon);

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
}
