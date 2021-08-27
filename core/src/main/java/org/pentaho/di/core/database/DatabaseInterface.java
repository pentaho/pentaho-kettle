/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.database;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.ObjectId;

/**
 * This interface describes the methods that a database connection needs to have in order to describe it properly.
 *
 * @author Matt
 * @since 11-mrt-2005
 */
public interface DatabaseInterface extends Cloneable {
  /**
   * @return the plugin id of this database
   */
  String getPluginId();

  /**
   * @param pluginId
   *          set the plugin id of this plugin (after instantiation)
   */
  void setPluginId( String pluginId );

  /**
   * @return the plugin name of this database, the same thing as the annotation typeDescription
   */
  String getPluginName();

  /**
   *
   * @param pluginName
   *          set the plugin name of this plugin (after instantiation)
   */
  void setPluginName( String pluginName );

  /**
   * @return Returns the accessType.
   */
  int getAccessType();

  /**
   * @param accessType
   *          The accessType to set.
   */
  void setAccessType( int accessType );

  /**
   * @return Returns the changed.
   */
  boolean isChanged();

  /**
   * @param changed
   *          The changed to set.
   */
  void setChanged( boolean changed );

  /**
   * @return Returns the connection Name.
   */
  String getName();

  /**
   * @param name
   *          The connection Name to set.
   */
  void setName( String name );

  /**
   * @return Returns the un-escaped connection Name.
   */
  String getDisplayName();

  /**
   * @param displayName
   *          The un-escaped connection Name to set.
   */
  void setDisplayName( String displayName );

  /**
   * @return Returns the databaseName.
   */
  String getDatabaseName();

  /**
   * @param databaseName
   *          The databaseName to set.
   */
  void setDatabaseName( String databaseName );

  /**
   * @return Returns the databasePortNumber as a string.
   */
  String getDatabasePortNumberString();

  /**
   * @param databasePortNumberString
   *          The databasePortNumber to set as a string.
   */
  void setDatabasePortNumberString( String databasePortNumberString );

  /**
   * @return Returns the hostname.
   */
  String getHostname();

  /**
   * @param hostname
   *          The hostname to set.
   */
  void setHostname( String hostname );

  /**
   * @return Returns the id.
   */
  ObjectId getObjectId();

  /**
   * @param id
   *          The id to set.
   */
  void setObjectId( ObjectId id );

  /**
   * @return the username to log onto the database
   */
  String getUsername();

  /**
   * @param username
   *          Sets the username to log onto the database with.
   */
  void setUsername( String username );

  /**
   * @return Returns the password.
   */
  String getPassword();

  /**
   * @param password
   *          The password to set.
   */
  void setPassword( String password );

  /**
   * @return Returns the servername.
   */
  String getServername();

  /**
   * @param servername
   *          The servername to set.
   */
  void setServername( String servername );

  /**
   * @return the tablespace to store data in. (create table)
   */
  String getDataTablespace();

  /**
   * @param dataTablespace
   *          the tablespace to store data in
   */
  void setDataTablespace( String dataTablespace );

  /**
   * @return the tablespace to store indexes in
   */
  String getIndexTablespace();

  /**
   * @param indexTablespace
   *          the tablespace to store indexes in
   */
  void setIndexTablespace( String indexTablespace );

  /**
   * @return The extra attributes for this database connection
   */
  Properties getAttributes();

  /**
   * Set extra attributes on this database connection
   *
   * @param attributes
   *          The extra attributes to set on this database connection.
   */
  void setAttributes( Properties attributes );

  /**
   * Add extra attribute on this connection
   * @param attributeId the attribute identifier
   * @param value the value of the attribute
   */
  default void addAttribute( String attributeId, String value ) {
    // Default implementation does nothing
  }

  /**
   * Gets an attribute from the connection
   * @param attributeId the attribute identifier
   * @param defaultValue the default value in case the attribute is not found
   * @return the attribute value
   */
  default String getAttribute( String attributeId, String defaultValue ) {
    return "";
  }

  /**
   * See if this database supports the setCharacterStream() method on a PreparedStatement.
   *
   * @return true if we can set a Stream on a field in a PreparedStatement. False if not.
   */
  boolean supportsSetCharacterStream();

  /**
   * @return Whether or not the database can use auto increment type of fields (pk)
   */
  boolean supportsAutoInc();

  /**
   * Describe a Value as a field in the database.
   *
   * @param v
   *          The value to describe
   * @param tk
   *          The field that's going to be the technical key
   * @param pk
   *          The field that's going to be the primary key
   * @param useAutoinc
   *          Use autoincrement or not
   * @param addFieldName
   *          Add the fieldname to the definition or not
   * @param addCr
   *          Add a cariage return at the end of the definition or not.
   * @return a value described as a field in this database.
   */
  String getFieldDefinition( ValueMetaInterface v, String tk, String pk, boolean useAutoinc,
                             boolean addFieldName, boolean addCr );

  /**
   * Get the list of possible access types for a database.
   *
   * @return the list of possible access types for a database.
   */
  int[] getAccessTypeList();

  /**
   * @return the default database port number
   */
  int getDefaultDatabasePort();

  /**
   * @return default extra Options
   */
  Map<String, String> getDefaultOptions();

  /**
   * @param nrRows
   *          The number of rows to which we want to limit the result of the query.
   * @return the clause after a select statement to limit the number of rows
   */
  String getLimitClause( int nrRows );

  /**
   * Returns the minimal SQL to launch in order to determine the layout of the resultset for a given database table
   *
   * @param tableName
   *          The name of the table to determine the layout for
   * @return The SQL to launch.
   */
  String getSQLQueryFields( String tableName );

  /**
   * Get the not found technical key.
   *
   * @param useAutoinc
   *          Whether or not we want to use an auto increment field
   * @return the lowest possible technical key to be used as the NOT FOUND row in a slowly changing dimension.
   */
  int getNotFoundTK( boolean useAutoinc );

  /**
   * Obtain the name of the JDBC driver class that we need to use!
   *
   * @return the name of the JDBC driver class for the specific database
   */
  String getDriverClass();

  /**
   * @param hostname
   *          the hostname
   * @param port
   *          the port as a string
   * @param databaseName
   *          the database name
   * @return the URL to use for connecting to the database.
   * @throws KettleDatabaseException
   *           in case a configuration error is detected.
   */
  String getURL( String hostname, String port, String databaseName ) throws KettleDatabaseException;

  /**
   * @return true if the database supports sequences
   */
  boolean supportsSequences();

  /**
   * Get the SQL to get the next value of a sequence.
   *
   * @param sequenceName
   *          The sequence name
   * @return the SQL to get the next value of a sequence.
   */
  String getSQLNextSequenceValue( String sequenceName );

  /**
   * Get the current value of a database sequence
   *
   * @param sequenceName
   *          The sequence to check
   * @return The current value of a database sequence
   */
  String getSQLCurrentSequenceValue( String sequenceName );

  /**
   * Check if a sequence exists.
   *
   * @param sequenceName
   *          The sequence to check
   * @return The SQL to get the name of the sequence back from the databases data dictionary
   */
  String getSQLSequenceExists( String sequenceName );

  /**
   * Checks whether or not the command setFetchSize() is supported by the JDBC driver...
   *
   * @return true is setFetchSize() is supported!
   */
  boolean isFetchSizeSupported();

  /**
   * @return true if the database supports transactions.
   */
  boolean supportsTransactions();

  /**
   * @return true if the database supports bitmap indexes
   */
  boolean supportsBitmapIndex();

  /**
   * @return true if the database supports indexes at all.  (Exasol and Snowflake do not)
   */
  default boolean supportsIndexes() {
    return true;
  }

  /**
   * @return true if the database JDBC driver supports the setLong command
   */
  boolean supportsSetLong();

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
   * @return true when the database engine supports empty transaction. (for example Informix does not!)
   */
  boolean supportsEmptyTransactions();

  /**
   * Indicates the need to insert a placeholder (0) for auto increment fields.
   *
   * @return true if we need a placeholder for auto increment fields in insert statements.
   */
  boolean needsPlaceHolder();

  /**
   * @return the function for Sum agrregate
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
   * Get the schema-table combination to query the right table. Usually that is SCHEMA.TABLENAME, however there are
   * exceptions to this rule...
   *
   * @param schemaName
   *          The schema name
   * @param tablePart
   *          The tablename
   * @return the schema-table combination to query the right table.
   */
  String getSchemaTableCombination( String schemaName, String tablePart );

  /**
   * Get the maximum length of a text field for this database connection. This includes optional CLOB, Memo and Text
   * fields. (the maximum!)
   *
   * @return The maximum text field length for this database type. (mostly CLOB_LENGTH)
   */
  int getMaxTextFieldLength();

  /**
   * Get the maximum length of a text field (VARCHAR) for this database connection. If this size is exceeded use a CLOB.
   *
   * @return The maximum VARCHAR field length for this database type. (mostly identical to getMaxTextFieldLength() -
   *         CLOB_LENGTH)
   */
  int getMaxVARCHARLength();

  /**
   * Generates the SQL statement to add a column to the specified table
   *
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param useAutoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to add a column to the specified table
   */
  String getAddColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
                                String pk, boolean semicolon );

  /**
   * Generates the SQL statement to drop a column from the specified table
   *
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param useAutoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to drop a column from the specified table
   */
  String getDropColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
                                 String pk, boolean semicolon );

  /**
   * Generates the SQL statement to modify a column in the specified table
   *
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param useAutoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to modify a column in the specified table
   */
  String getModifyColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean useAutoinc,
                                   String pk, boolean semicolon );

  /**
   * Clone this database interface: copy all info to a new object
   *
   * @return the cloned Database Interface object.
   */
  Object clone();

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
   * @return a list of table types to retrieve tables for the database This is mostly just { "TABLE" }
   */
  String[] getTableTypes();

  /**
   * @return a list of table types to retrieve views for the database This is mostly just { "VIEW" }
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
   * @return The SQL on this database to get a list of stored procedures.
   */
  String getSQLListOfProcedures();

  /**
   * @param tableName
   *          The table to be truncated.
   * @return The SQL statement to truncate a table: remove all rows from it without a transaction
   */
  String getTruncateTableStatement( String tableName );

  /**
   * @return true if the database rounds floating point numbers to the right precision. For example if the target field
   *         is number(7,2) the value 12.399999999 is converted into 12.40
   */
  boolean supportsFloatRoundingOnUpdate();

  /**
   * @param tableNames
   *          The names of the tables to lock
   * @return The SQL command to lock database tables for write purposes. null is returned in case locking is not
   *         supported on the target database.
   */
  String getSQLLockTables( String[] tableNames );

  /**
   * @param tableNames
   *          The names of the tables to unlock
   * @return The SQL command to unlock the database tables. null is returned in case locking is not supported on the
   *         target database.
   */
  String getSQLUnlockTables( String[] tableNames );

  /**
   * @return true if the database resultsets support getTimeStamp() to retrieve date-time. (Date)
   */
  boolean supportsTimeStampToDateConversion();

  /**
   * @return true if the database JDBC driver supports batch updates For example Interbase doesn't support this!
   */
  boolean supportsBatchUpdates();

  /**
   * @return true if the database supports a boolean, bit, logical, ... datatype
   */
  boolean supportsBooleanDataType();

  /**
   * @param b
   *          Set to true if the database supports a boolean, bit, logical, ... datatype
   */
  void setSupportsBooleanDataType( boolean b );

  /**
   * @return true if reserved words' case should be preserved
   */
  boolean preserveReservedCase();

  /**
   * @param b
   *          Set to true if reserved words' case should be preserved
   */
  void setPreserveReservedCase( boolean b );

  /**
   * @return true if the database defaults to naming tables and fields in upper case. True for most databases except for
   *         stuborn stuff like Postgres ;-)
   */
  boolean isDefaultingToUppercase();

  /**
   * @return a map of all the extra URL options you want to set, retrieved from the attributes list (NOT synchronized!)
   */
  Map<String, String> getExtraOptions();

  /**
   * Add an extra option to the attributes list
   *
   * @param databaseTypeCode
   *          The database type code for which the option applies
   * @param option
   *          The option to set
   * @param value
   *          The value of the option
   */
  void addExtraOption( String databaseTypeCode, String option, String value );

  /**
   * @return The extra option separator in database URL for this platform (usually this is semicolon ; )
   */
  String getExtraOptionSeparator();

  /**
   * @return The extra option value separator in database URL for this platform (usually this is the equal sign = )
   */
  String getExtraOptionValueSeparator();

  /**
   * @return This indicator separates the normal URL from the options
   */
  String getExtraOptionIndicator();

  /**
   * @return true if the database supports connection options in the URL, false if they are put in a Properties object.
   */
  boolean supportsOptionsInURL();

  /**
   * @return extra help text on the supported options on the selected database platform.
   */
  String getExtraOptionsHelpText();

  /**
   * @return true if the database JDBC driver supports getBlob on the resultset. If not we must use getBytes() to get
   *         the data.
   */
  boolean supportsGetBlob();

  /**
   * @return The SQL to execute right after connecting
   */
  String getConnectSQL();

  /**
   * @param sql
   *          The SQL to execute right after connecting
   */
  void setConnectSQL( String sql );

  /**
   * @return true if the database supports setting the maximum number of return rows in a resultset.
   */
  boolean supportsSetMaxRows();

  /**
   * @return true if we want to use a database connection pool
   */
  boolean isUsingConnectionPool();

  /**
   * @param usePool
   *          true if we want to use a database connection pool
   */
  void setUsingConnectionPool( boolean usePool );

  /**
   * @return the maximum pool size
   */
  int getMaximumPoolSize();

  /**
    * @return the maximum pool size variable name
   */
  String getMaximumPoolSizeString();

  /**
   * @param maximumPoolSize
   *          the maximum pool size
   */
  void setMaximumPoolSize( int maximumPoolSize );

  /**
   * @param maximumPoolSize
   *          the maximum pool size variable name
   */
  void setMaximumPoolSizeString( String maximumPoolSize );

  /**
   * @return the initial pool size
   */
  int getInitialPoolSize();

  /**
   * @return the initial pool size variable name
   */
  String getInitialPoolSizeString();

  /**
   * @param initalPoolSize
   *          the initial pool size
   */
  void setInitialPoolSize( int initalPoolSize );

  /**
   * @param initialPoolSize
   *          the initial pool size variable name
   */
  void setInitialPoolSizeString( String initialPoolSize );

  /**
   * @return true if the connection contains partitioning information
   */
  boolean isPartitioned();

  /**
   * @param partitioned
   *          true if the connection is set to contain partitioning information
   */
  void setPartitioned( boolean partitioned );

  /**
   * @return the available partition/host/databases/port combinations in the cluster
   */
  PartitionDatabaseMeta[] getPartitioningInformation();

  /**
   * @param partitionInfo
   *          the available partition/host/databases/port combinations in the cluster
   */
  void setPartitioningInformation( PartitionDatabaseMeta[] partitionInfo );

  /**
   * @return the required libraries (in lib) for this database connection.
   */
  String[] getUsedLibraries();

  /**
   * @return The set of properties that allows you to set the connection pooling parameters
   */
  Properties getConnectionPoolingProperties();

  /** set the connection pooling properties */
  void setConnectionPoolingProperties( Properties properties );

  /**
   * @param tablename
   *          The table to verify the existance for
   * @return The SQL to execute to verify if the given table exists. If an Exception is thrown for this SQL, we don't
   *         have the table.
   */
  String getSQLTableExists( String tablename );

  /**
   * @param column
   *          The column to verify the existance for
   * @param tablename
   *          The table to verify the existance for
   * @return The SQL to execute to verify if the given table exists. If an Exception is thrown for this SQL, we don't
   *         have the column.
   */
  String getSQLColumnExists( String column, String tablename );

  /**
   * @return true if the database needs all repository tables to be locked, not just one ref table (R_REPOSITORY_LOG)
   */
  boolean needsToLockAllTables();

  /**
   * @return true if the database is streaming results (normally this is an option just for MySQL).
   */
  boolean isStreamingResults();

  /**
   * @param useStreaming
   *          true if we want the database to stream results (normally this is an option just for MySQL).
   */
  void setStreamingResults( boolean useStreaming );

  /**
   * @return true if all fields should always be quoted in db
   */
  boolean isQuoteAllFields();

  /**
   * @param quoteAllFields
   *          true if all fields in DB should be quoted.
   */
  void setQuoteAllFields( boolean quoteAllFields );

  /**
   * @return true if all identifiers should be forced to lower case
   */
  boolean isForcingIdentifiersToLowerCase();

  /**
   * @param forceLowerCase
   *          true if all identifiers should be forced to lower case
   */
  void setForcingIdentifiersToLowerCase( boolean forceLowerCase );

  /**
   * @return true if all identifiers should be forced to upper case
   */
  boolean isForcingIdentifiersToUpperCase();

  /**
   * @param forceUpperCase
   *          true if all identifiers should be forced to upper case
   */
  void setForcingIdentifiersToUpperCase( boolean forceUpperCase );

  /**
   * @return true if we use a double decimal separator to specify schema/table combinations on MS-SQL server
   */
  boolean isUsingDoubleDecimalAsSchemaTableSeparator();

  /**
   * @param useDoubleDecimalSeparator
   *          true if we should use a double decimal separator to specify schema/table combinations on MS-SQL server
   */
  void setUsingDoubleDecimalAsSchemaTableSeparator( boolean useDoubleDecimalSeparator );

  /**
   * @return true if this database needs a transaction to perform a query (auto-commit turned off).
   */
  boolean isRequiringTransactionsOnQueries();

  /**
   * You can use this method to supply an alternate factory for the test method in the dialogs. This is useful for
   * plugins like SAP/R3 and PALO.
   *
   * @return the name of the database test factory to use.
   */
  String getDatabaseFactoryName();

  /**
   * @return The preferred schema name of this database connection.
   */
  String getPreferredSchemaName();

  /**
   * @param preferredSchemaName
   *          The preferred schema name of this database connection.
   */
  void setPreferredSchemaName( String preferredSchemaName );

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
  boolean checkIndexExists( Database database, String schemaName, String tableName, String[] idxFields ) throws KettleDatabaseException;

  /**
   * @return true if the database supports sequences with a maximum value option. The default is true.
   */
  boolean supportsSequenceNoMaxValueOption();

  /**
   * @return true if we need to append the PRIMARY KEY block in the create table block after the fields, required for
   *         Cache.
   */
  boolean requiresCreateTablePrimaryKeyAppend();

  /**
   * @return true if the database requires you to cast a parameter to varchar before comparing to null.
   *
   */
  boolean requiresCastToVariousForIsNull();

  /**
   * @return Handles the special case of DB2 where the display size returned is twice the precision. In that case, the
   *         length is the precision.
   */
  boolean isDisplaySizeTwiceThePrecision();

  /**
   * Most databases allow you to retrieve result metadata by preparing a SELECT statement.
   *
   * @return true if the database supports retrieval of query metadata from a prepared statement. False if the query
   *         needs to be executed first.
   */
  boolean supportsPreparedStatementMetadataRetrieval();

  /**
   * @param tableName
   * @return true if the specified table is a system table
   */
  boolean isSystemTable( String tableName );

  /**
   * @return true if the database supports newlines in a SQL statements.
   */
  boolean supportsNewLinesInSQL();

  /**
   * @return the SQL to retrieve the list of schemas
   */
  String getSQLListOfSchemas();

  /**
   * @param dbMeta
   * @return the SQL to retrieve the list of schemas
   */
  default String getSQLListOfSchemas( DatabaseMeta dbMeta ) {
    // Return the previous behavior as default implementation
    return getSQLListOfSchemas();
  }

  /**
   * @return The maximum number of columns in a database, <=0 means: no known limit
   */
  int getMaxColumnsInIndex();

  /**
   * @return true if the database supports error handling (recovery of failure) while doing batch updates.
   */
  boolean supportsErrorHandlingOnBatchUpdates();

  /**
   * Get the SQL to insert a new empty unknown record in a dimension.
   *
   * @param schemaTable
   *          the schema-table name to insert into
   * @param keyField
   *          The key field
   * @param versionField
   *          the version field
   * @return the SQL to insert the unknown record into the SCD.
   */
  String getSQLInsertAutoIncUnknownDimensionRow( String schemaTable, String keyField, String versionField );

  /**
   * @return true if this is a relational database you can explore. Return false for SAP, PALO, etc.
   */
  boolean isExplorable();

  /**
   * @return The name of the XUL overlay file to display extra options. This is only used in case of a non-standard
   *         plugin. Usually this method returns null.
   */
  String getXulOverlayFile();

  /**
   * @return The SQL on this database to get a list of sequences.
   */
  String getSQLListOfSequences();

  /**
   * Adds quotes around the string according to the database dialect and also escapes special characters like CR, LF and
   * the quote character itself.
   *
   * @param string
   * @return A string that is properly quoted for use in a SQL statement (insert, update, delete, etc)
   */
  String quoteSQLString( String string );

  /**
   * Returns the SQL Statement that counts the number of rows in the table.
   *
   * @param tableName
   * @return
   */
  String getSelectCountStatement( String tableName );

  /**
   * Generate a column alias given the column index and suggested name.
   *
   * @param columnIndex
   *          Index of column in query
   * @param suggestedName
   *          Suggested column name
   * @return Column alias that is valid for this database
   */
  String generateColumnAlias( int columnIndex, String suggestedName );

  /**
   * Parse all possible statements from the provided SQL script.
   *
   * @param sqlScript
   *          Raw SQL Script to be parsed into executable statements.
   * @return List of parsed SQL statements to be executed separately.
   */
  List<String> parseStatements( String sqlScript );

  /**
   * Parse the statements in the provided SQL script, provide more information about where each was found in the script.
   *
   * @param sqlScript
   *          Raw SQL Script to be parsed into executable statements.
   * @return List of SQL script statements to be executed separately.
   */
  List<SqlScriptStatement> getSqlScriptStatements( String sqlScript );

  /**
   * @return true if the database is a MySQL variant, like MySQL 5.1, InfiniDB, InfoBright, and so on.
   */
  boolean isMySQLVariant();

  /**
   * Returns a true of savepoints can be release, false if not.
   *
   * @return
   */
  boolean releaseSavepoint();

  /**
   * Get the next Batch ID from the logging tables.
   *
   * @param dbm
   *          DatabaseMeta object
   * @param ldb
   *          Database connection
   * @param schemaName
   *          Logging Schema Name
   * @param tableName
   *          Logging Table Name
   * @param fieldName
   *          Batch Id Field name
   * @return next batch ID
   * @throws KettleDatabaseException
   */
  Long getNextBatchId( DatabaseMeta dbm, Database ldb, String schemaName, String tableName, String fieldName ) throws KettleDatabaseException;

  /**
   * Returns the tablespace DDL fragment for a "Data" tablespace. In most databases that use tablespaces this is where
   * the tables are to be created.
   *
   * @param variables
   *          variables used for possible substitution
   * @param databaseMeta
   *          databaseMeta the database meta used for possible string enclosure of the tablespace. This method needs
   *          this as this is done after environmental substitution.
   *
   * @return String the tablespace name for tables in the format "tablespace TABLESPACE_NAME". The TABLESPACE_NAME and
   *         the passed DatabaseMata determines if TABLESPACE_NAME is to be enclosed in quotes.
   */
  String getDataTablespaceDDL( VariableSpace variables, DatabaseMeta databaseMeta );

  /**
   * Returns the tablespace DDL fragment for a "Index" tablespace.
   *
   * @param variables
   *          variables used for possible substitution
   * @param databaseMeta
   *          databaseMeta the database meta used for possible string enclosure of the tablespace. This method needs
   *          this as this is done after environmental substitution.
   *
   * @return String the tablespace name for indicis in the format "tablespace TABLESPACE_NAME". The TABLESPACE_NAME and
   *         the passed DatabaseMata determines if TABLESPACE_NAME is to be enclosed in quotes.
   */
  String getIndexTablespaceDDL( VariableSpace variables, DatabaseMeta databaseMeta );

  /**
   * This method allows a database dialect to convert database specific data types to Kettle data types.
   *
   * @param resultSet
   *          The result set to use
   * @param valueMeta
   *          The description of the value to retrieve
   * @param index
   *          the index on which we need to retrieve the value, 0-based.
   * @return The correctly converted Kettle data type corresponding to the valueMeta description.
   * @throws KettleDatabaseException
   */
  Object getValueFromResultSet( ResultSet resultSet, ValueMetaInterface valueMeta, int index ) throws KettleDatabaseException;

  /**
   * @return true if the database supports the use of safe-points and if it is appropriate to ever use it (default to
   *         false)
   */
  boolean useSafePoints();

  /**
   * @return true if the database supports error handling (the default). Returns false for certain databases (SQLite)
   *         that invalidate a prepared statement or even the complete connection when an error occurs.
   */
  boolean supportsErrorHandling();

  /**
   * Convert a value in the SQL equivalent. For example, convert String "Pentaho" into 'Pentaho' or into Oracle date
   * format TO_DATE('2012/08/16 15:36:59', 'YYYY/MM/DD HH24:MI:SS')
   *
   * @param valueMeta
   *          The description of the value. The date format used is taken from this value unless dateFormat is specified
   *          (not null or empty)
   * @param valueData
   *          The data to convert.
   * @return The value SQL clause
   * @throws KettleValueException
   *           in case there is a data conversion error.
   */
  String getSQLValue( ValueMetaInterface valueMeta, Object valueData, String dateFormat ) throws KettleValueException;

  /**
   * @return true if this database only supports metadata retrieval on a result set, never on a statement (even if the
   *         statement has been executed)
   */
  boolean supportsResultSetMetadataRetrievalOnly();

  /**
   * @return true if the database supports the Timestamp data type (nanosecond precision and all)
   */
  boolean supportsTimestampDataType();

  /**
   *
   * @param b
   *          Set to true if the database supports the Timestamp data type (nanosecond precision and all)
   */
  void setSupportsTimestampDataType( boolean b );

  /**
   * Given a String, this will sanitize and return a value safe for usage as a column name
   *
   * @param fieldName
   *          value to sanitize
   * @return a String safe for usage as a column name without the need for quoting
   */
  String getSafeFieldname( String fieldName );

  /**
   * @return true if the database supports sequences with a maximum value option. The default is true.
   */
  String getSequenceNoMaxValueOption();

  /**
   * @return true if the database supports autoGeneratedKeys
   */
  boolean supportsAutoGeneratedKeys();

  /**
   * Customizes the ValueMetaInterface defined in the base
   *
   * @param v the determined valueMetaInterface
   * @param rm the sql column type
   * @param index the index to the column to customize
   * @return ValueMetaInterface customized with the data base specific types
   */
  ValueMetaInterface customizeValueFromSQLType( ValueMetaInterface v, java.sql.ResultSetMetaData rm, int index )
      throws SQLException;

  /**
   * Customizes the ValueMetaInterface defined in the base
   *
   * @return String the create table statement
   */
  String getCreateTableStatement();

  /**
   * Set default options for this database
   *
   * @deprecated No longer works with the UI, Use getDefaultOptions, instead
   */
  @Deprecated
  default void addDefaultOptions() {
    // Default implementation does nothing
  }

  /**
   * Create SqlScriptParser for current database dialect
   * @return instance of SqlScriptParser for current database dialect
   */
  default SqlScriptParser createSqlScriptParser() {
    return new SqlScriptParser( true );
  }

  /**
   * @return true if database supports the standard table output step
   */
  default boolean supportsStandardTableOutput() {
    return true;
  }

  /**
   * @return the unsupported message if database does not support standard table output step
   */
  default String getUnsupportedTableOutputMessage() {
    return "";
  }

  /**
   * Allows to get the column name for JDBC drivers with different behavior for aliases depending on the connector version.
   *
   * @param dbMetaData
   * @param rsMetaData
   * @param index
   * @return empty if the database doesn't support the legacy column name feature
   * @throws KettleDatabaseException
   */
  default String getLegacyColumnName( DatabaseMetaData dbMetaData, ResultSetMetaData rsMetaData, int index ) throws KettleDatabaseException {
    return "";
  }

  default void putOptionalOptions( Map<String, String> extraOptions ) {
  }

  default ResultSet getSchemas( DatabaseMetaData databaseMetaData, DatabaseMeta dbMeta ) throws SQLException {
    return databaseMetaData.getSchemas();
  }

  default ResultSet getTables( DatabaseMetaData databaseMetaData, DatabaseMeta dbMeta,
                               String schemaPattern, String tableNamePattern, String[] tableTypes ) throws SQLException {
    return databaseMetaData.getTables( null, schemaPattern, tableNamePattern, tableTypes );
  }

  /**
   *
   * @return The hadoop cluster associated with the connection
   */
  default String getNamedCluster() {
    return "";
  }

  /**
   * Set the hadoop cluster associated with the connection
   * @param namedCluster The hadoop cluster name
   */
  default void setNamedCluster( String namedCluster ) {

  }

  default List<String> getNamedClusterList() {
    return null;
  }
}
