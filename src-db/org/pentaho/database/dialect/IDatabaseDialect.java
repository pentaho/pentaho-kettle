package org.pentaho.database.dialect;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.ValueMetaInterface;

public interface IDatabaseDialect {

  /**
   * @return The database type that contains a database type name, id, help URL, a default port and the supported access types.
   * In short: all the static information concerning a database dialect.
   */
  IDatabaseType getDatabaseType();
	
  /**
   * @return the default database port number
   */
  int getDefaultDatabasePort();

  /**
   * See if this database supports the setCharacterStream() method on a PreparedStatement.
   * 
   * @return true if we can set a Stream on a field in a PreparedStatement.  False if not. 
   */
  boolean supportsSetCharacterStream();

  /**
   * @return Whether or not the database can use auto increment type of fields (pk)
   */
  boolean supportsAutoInc();

  String getLimitClause(int nrRows);

  int getNotFoundTK(boolean use_autoinc);

  /**
   * Get the SQL to get the next value of a sequence. (Oracle/PGSQL only) 
   * @param sequenceName The sequence name
   * @return the SQL to get the next value of a sequence. (Oracle/PGSQL only)
   */
  String getSQLNextSequenceValue(String sequenceName);

  /**
   * Get the current value of a database sequence
   * @param sequenceName The sequence to check
   * @return The current value of a database sequence
   */
  String getSQLCurrentSequenceValue(String sequenceName);

  /**
   * Check if a sequence exists.
   * @param sequenceName The sequence to check
   * @return The SQL to get the name of the sequence back from the databases data dictionary
   */
  String getSQLSequenceExists(String sequenceName);

  /**
   * Checks whether or not the command setFetchSize() is supported by the JDBC driver...
   * @return true is setFetchSize() is supported!
   */
  boolean isFetchSizeSupported();

  /**
   * Indicates the need to insert a placeholder (0) for auto increment fields.
   * @return true if we need a placeholder for auto increment fields in insert statements.
   */
  boolean needsPlaceHolder();

  /**
   * @return true if the database supports schemas
   */
  boolean supportsSchemas();

  /**
   * @return true if the database supports catalogs
   */
  boolean supportsCatalogs();

  /**
   * 
   * @return true when the database engine supports empty transaction.
   * (for example Informix does not on a non-ANSI database type!)
   */
  boolean supportsEmptyTransactions();

  /**
   * @return the function for SUM agrregate 
   */
  String getFunctionSum();

  /**
   * @return the function for Average agrregate 
   */
  String getFunctionAverage();

  /**
   * @return the function for Minimum agrregate 
   */
  String getFunctionMinimum();

  /**
   * @return the function for Maximum agrregate 
   */
  String getFunctionMaximum();

  /**
   * @return the function for Count agrregate 
   */
  String getFunctionCount();

  /**
   * Get the schema-table combination to query the right table.
   * Usually that is SCHEMA.TABLENAME, however there are exceptions to this rule...
   * @param schema_name The schema name
   * @param table_part The tablename
   * @return the schema-table combination to query the right table.
   */
  String getSchemaTableCombination(String schema_name, String table_part);

  /**
   * Get the maximum length of a text field for this database connection.
   * This includes optional CLOB, Memo and Text fields. (the maximum!)
   * @return The maximum text field length for this database type. (mostly CLOB_LENGTH)
   */
  int getMaxTextFieldLength();

  /**
   * Get the maximum length of a text field (VARCHAR) for this database connection.
   * If this size is exceeded use a CLOB.
   * @return The maximum VARCHAR field length for this database type. (mostly identical to getMaxTextFieldLength() - CLOB_LENGTH)
   */
  int getMaxVARCHARLength();

  /**
   * @return true if the database supports transactions.
   */
  boolean supportsTransactions();

  /**
   * @return true if the database supports sequences
   */
  boolean supportsSequences();

  /**
   * @return true if the database supports bitmap indexes
   */
  boolean supportsBitmapIndex();

  /**
   * @return true if the database JDBC driver supports the setLong command
   */
  boolean supportsSetLong();

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
  String getDropColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
      String pk, boolean semicolon);

  /**
   * @return an array of reserved words for the database type...
   */
  String[] getReservedWords();

  /**
   * @return true if reserved words need to be double quoted ("password", "select", ...)
   */
  boolean quoteReservedWords();

  /**
   * @return The start quote sequence, mostly just double quote, but sometimes [, ...
   */
  String getStartQuote();

  /**
   * @return The end quote sequence, mostly just double quote, but sometimes ], ...
   */
  String getEndQuote();

  /**
   * @return true if Kettle can create a repository on this type of database.
   */
  boolean supportsRepository();

  /**
   * @return a list of table types to retrieve tables for the database
   */
  String[] getTableTypes();

  /**
   * @return a list of table types to retrieve views for the database
   */
  String[] getViewTypes();

  /**
   * @return a list of table types to retrieve synonyms for the database
   */
  String[] getSynonymTypes();

  /**
   * @return true if we need to supply the schema-name to getTables in order to get a correct list of items.
   */
  boolean useSchemaNameForTableList();

  /**
   * @return true if the database supports views
   */
  boolean supportsViews();

  /**
   * @return true if the database supports synonyms
   */
  boolean supportsSynonyms();

  /**
   * @param connection username may be required in sql generation
   * 
   * @return The SQL on this database to get a list of stored procedures.
   */
  String getSQLListOfProcedures(IDatabaseConnection connection);

  /**
   * @param tableName The table to be truncated.
   * @return The SQL statement to truncate a table: remove all rows from it without a transaction
   */
  String getTruncateTableStatement(String tableName);

  /**
   * Returns the minimal SQL to launch in order to determine the layout of the resultset for a given database table
   * @param tableName The name of the table to determine the layout for
   * @return The SQL to launch.
   */
  String getSQLQueryFields(String tableName);

  /**
   * Most databases round number(7,2) 17.29999999 to 17.30, but some don't.
   * @return true if the database supports roundinf of floating point data on update/insert
   */
  boolean supportsFloatRoundingOnUpdate();

  /**
   * @param tableNames The names of the tables to lock
   * @return The SQL command to lock database tables for write purposes.
   *         null is returned in case locking is not supported on the target database.
   *         null is the default value
   */
  String getSQLLockTables(String tableNames[]);

  /**
   * @param tableNames The names of the tables to unlock
   * @return The SQL command to unlock database tables. 
   *         null is returned in case locking is not supported on the target database.
   *         null is the default value
   */
  String getSQLUnlockTables(String tableNames[]);

  /**
   * @return true if the database supports timestamp to date conversion.
   * For example Interbase doesn't support this!
   */
  boolean supportsTimeStampToDateConversion();

  /**
   * @return true if the database JDBC driver supports batch updates
   * For example Interbase doesn't support this!
   */
  boolean supportsBatchUpdates();

  /**
   * @return true if the database supports a boolean, bit, logical, ... datatype
   * The default is false: map to a string.
   */
  boolean supportsBooleanDataType();

  /**
   * @return true if the database defaults to naming tables and fields in uppercase.
   * True for most databases except for stuborn stuff like Postgres ;-)
   */
  boolean isDefaultingToUppercase();

  /**
   * @return true if the database supports setting the maximum number of return rows in a resultset.
   */
  boolean supportsSetMaxRows();

  String getSQLTableExists(String tablename);

  String getSQLColumnExists(String columnname, String tablename);

  boolean needsToLockAllTables();

  /**
   * @return true if this database needs a transaction to perform a query (auto-commit turned off).
   */
  boolean isRequiringTransactionsOnQueries();
  
  String getDriverClass(IDatabaseConnection connection);
  
  String getURL(IDatabaseConnection connection)  throws KettleDatabaseException;
  
  String getURLWithExtraOptions(IDatabaseConnection connection) throws KettleDatabaseException;
  
  // String getSQLQueryColumnFields(String columnname, String tableName);
  
  boolean supportsOptionsInURL();
  
  String getAddColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon);
  
  String getModifyColumnStatement(String tablename, ValueMetaInterface v, String tk, boolean use_autoinc, String pk, boolean semicolon);
  
  String getFieldDefinition(ValueMetaInterface v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr);
  
  String getExtraOptionsHelpText();
  
  String[] getUsedLibraries();
  
  String getNativeDriver();
  
  IDatabaseConnection createNativeConnection(String jdbcUrl);
  
  // IDatabaseType getDatabaseType();
  
  boolean supportsGetBlob();
  
  String getExtraOptionSeparator();
  
  String getExtraOptionValueSeparator();
  
  String getExtraOptionIndicator();
  
  /** 
   * @return The name of the database connection helper factory class 
   **/
  String getDatabaseFactoryName();
}