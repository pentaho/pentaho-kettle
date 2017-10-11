// CHECKSTYLE:FileLength:OFF
/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.ObjectId;

/**
 * This class contains the basic information on a database connection. It is not intended to be used other than the
 * inheriting classes such as OracleDatabaseInfo, ...
 *
 * @author Matt
 * @since 11-mrt-2005
 */
public abstract class BaseDatabaseMeta implements Cloneable, DatabaseInterfaceExtended {
  /**
   * The port number of the database as string: allows for parameterization.
   */
  public static final String ATTRIBUTE_PORT_NUMBER = "PORT_NUMBER";

  /**
   * The SQL to execute at connect time (right after connecting)
   */
  public static final String ATTRIBUTE_SQL_CONNECT = "SQL_CONNECT";

  /**
   * A flag to determine if we should use connection pooling or not.
   */
  public static final String ATTRIBUTE_USE_POOLING = "USE_POOLING";

  /**
   * If we use connection pooling, this would contain the maximum pool size
   */
  public static final String ATTRIBUTE_MAXIMUM_POOL_SIZE = "MAXIMUM_POOL_SIZE";

  /**
   * If we use connection pooling, this would contain the initial pool size
   */
  public static final String ATTRIBUTE_INITIAL_POOL_SIZE = "INITIAL_POOL_SIZE";

  /**
   * The prefix for all the extra options attributes
   */
  public static final String ATTRIBUTE_PREFIX_EXTRA_OPTION = "EXTRA_OPTION_";

  /**
   * A flag to determine if the connection is clustered or not.
   */
  public static final String ATTRIBUTE_IS_CLUSTERED = "IS_CLUSTERED";

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
   * A flag to determine if we should use a double decimal separator to specify schema/table combinations on MS-SQL
   * server
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
   * Checkbox to allow you to configure if the database supports the boolean data type or not. Defaults to "false" for
   * backward compatibility!
   */
  public static final String ATTRIBUTE_SUPPORTS_BOOLEAN_DATA_TYPE = "SUPPORTS_BOOLEAN_DATA_TYPE";

  /**
   * Checkbox to allow you to configure if the database supports the Timestamp data type or not. Defaults to "false" for
   * backward compatibility!
   */
  public static final String ATTRIBUTE_SUPPORTS_TIMESTAMP_DATA_TYPE = "SUPPORTS_TIMESTAMP_DATA_TYPE";

  /**
   * Checkbox to allow you to configure if the reserved words will have their case changed during the handleCase call
   */
  public static final String ATTRIBUTE_PRESERVE_RESERVED_WORD_CASE = "PRESERVE_RESERVED_WORD_CASE";

  public static final String SEQUENCE_FOR_BATCH_ID = "SEQUENCE_FOR_BATCH_ID";
  public static final String AUTOINCREMENT_SQL_FOR_BATCH_ID = "AUTOINCREMENT_SQL_FOR_BATCH_ID";

  /**
   * Boolean to indicate if savepoints can be released Most databases do, so we set it to true. Child classes can
   * overwrite with false if need be.
   */
  protected boolean releaseSavepoint = true;

  /**
   * The SQL, minus the table name, to select the number of rows from a table
   */
  public static final String SELECT_COUNT_STATEMENT = "select count(*) FROM";

  public static final DatabaseConnectionPoolParameter[] poolingParameters = new DatabaseConnectionPoolParameter[] {
    new DatabaseConnectionPoolParameter(
      "defaultAutoCommit", "true", "The default auto-commit state of connections created by this pool." ),
    new DatabaseConnectionPoolParameter(
      "defaultReadOnly", null, "The default read-only state of connections created by this pool.\n"
        + "If not set then the setReadOnly method will not be called.\n "
        + "(Some drivers don't support read only mode, ex: Informix)" ),
    new DatabaseConnectionPoolParameter(
      "defaultTransactionIsolation", null,
      "the default TransactionIsolation state of connections created by this pool. "
        + "One of the following: (see javadoc)\n\n  * NONE\n  * "
        + "READ_COMMITTED\n  * READ_UNCOMMITTED\n  * REPEATABLE_READ  * SERIALIZABLE\n" ),
    new DatabaseConnectionPoolParameter(
      "defaultCatalog", null, "The default catalog of connections created by this pool." ),

    new DatabaseConnectionPoolParameter(
      "initialSize", "0", "The initial number of connections that are created when the pool is started." ),
    new DatabaseConnectionPoolParameter(
      "maxActive", "8",
      "The maximum number of active connections that can be allocated from this pool at the same time, "
        + "or non-positive for no limit." ),
    new DatabaseConnectionPoolParameter(
      "maxIdle", "8", "The maximum number of connections that can remain idle in the pool, "
        + "without extra ones being released, or negative for no limit." ),
    new DatabaseConnectionPoolParameter(
      "minIdle", "0", "The minimum number of connections that can remain idle in the pool, "
        + "without extra ones being created, or zero to create none." ),
    new DatabaseConnectionPoolParameter(
      "maxWait", "-1", "The maximum number of milliseconds that the pool will wait "
        + "(when there are no available connections) for a connection to be returned "
        + "before throwing an exception, or -1 to wait indefinitely." ),

    new DatabaseConnectionPoolParameter(
      "validationQuery", null, "The SQL query that will be used to validate connections from this pool "
        + "before returning them to the caller.\n"
        + "If specified, this query MUST be an SQL SELECT statement that returns at least one row." ),
    new DatabaseConnectionPoolParameter(
      "testOnBorrow", "true",
      "The indication of whether objects will be validated before being borrowed from the pool.\n"
        + "If the object fails to validate, it will be dropped from the pool, "
        + "and we will attempt to borrow another.\n"
        + "NOTE - for a true value to have any effect, the validationQuery parameter "
        + "must be set to a non-null string." ),
    new DatabaseConnectionPoolParameter(
      "testOnReturn", "false",
      "The indication of whether objects will be validated before being returned to the pool.\n"
        + "NOTE - for a true value to have any effect, the validationQuery parameter must be set "
        + "to a non-null string." ),
    new DatabaseConnectionPoolParameter(
      "testWhileIdle", "false",
      "The indication of whether objects will be validated by the idle object evictor (if any). "
        + "If an object fails to validate, it will be dropped from the pool.\n"
        + "NOTE - for a true value to have any effect, the validationQuery parameter must be set to a "
        + "non-null string." ),
    new DatabaseConnectionPoolParameter(
      "timeBetweenEvictionRunsMillis", null,
      "The number of milliseconds to sleep between runs of the idle object evictor thread. "
        + "When non-positive, no idle object evictor thread will be run." ),

    new DatabaseConnectionPoolParameter(
      "poolPreparedStatements", "false", "Enable prepared statement pooling for this pool." ),
    new DatabaseConnectionPoolParameter(
      "maxOpenPreparedStatements", "-1",
      "The maximum number of open statements that can be allocated from the statement pool at the same time, "
        + "or zero for no limit." ),
    new DatabaseConnectionPoolParameter(
      "accessToUnderlyingConnectionAllowed", "false",
      "Controls if the PoolGuard allows access to the underlying connection." ),
    new DatabaseConnectionPoolParameter(
      "removeAbandoned", "false",
      "Flag to remove abandoned connections if they exceed the removeAbandonedTimout.\n"
        + "If set to true a connection is considered abandoned and eligible for removal "
        + "if it has been idle longer than the removeAbandonedTimeout. "
        + "Setting this to true can recover db connections from poorly written applications which "
        + "fail to close a connection." ),
    new DatabaseConnectionPoolParameter(
      "removeAbandonedTimeout", "300", "Timeout in seconds before an abandoned connection can be removed." ),
    new DatabaseConnectionPoolParameter(
      "logAbandoned", "false",
      "Flag to log stack traces for application code which abandoned a Statement or Connection.\n"
        + "Logging of abandoned Statements and Connections adds overhead for every Connection open or "
        + "new Statement because a stack trace has to be generated." ), };

  private static final String FIELDNAME_PROTECTOR = "_";

  private String name;
  private String displayName;
  private int accessType; // Database.TYPE_ODBC / NATIVE / OCI
  private String hostname;
  private String databaseName;
  private String username;
  private String password;
  private String servername; // Informix only!

  private String dataTablespace; // data storage location, For Oracle & perhaps others
  private String indexTablespace; // index storage location, For Oracle & perhaps others

  private boolean changed;

  private Properties attributes;

  private ObjectId objectId;

  private String pluginId;
  private String pluginName;

  public BaseDatabaseMeta() {
    attributes = new Properties();
    changed = false;
    if ( getAccessTypeList() != null && getAccessTypeList().length > 0 ) {
      accessType = getAccessTypeList()[0];
    }
  }

  /**
   * @return plugin ID of this class
   */
  @Override
  public String getPluginId() {
    return pluginId;
  }

  /**
   * @param pluginId
   *          The plugin ID to set.
   */
  @Override
  public void setPluginId( String pluginId ) {
    this.pluginId = pluginId;
  }

  /**
   * @return plugin name of this class
   */
  @Override
  public String getPluginName() {
    return pluginName;
  }

  /**
   * @param pluginName
   *          The plugin name to set.
   */
  @Override
  public void setPluginName( String pluginName ) {
    this.pluginName = pluginName;
  }

  @Override
  public abstract int[] getAccessTypeList();

  /**
   * @return Returns the accessType.
   */
  @Override
  public int getAccessType() {
    return accessType;
  }

  /**
   * @param accessType
   *          The accessType to set.
   */
  @Override
  public void setAccessType( int accessType ) {
    this.accessType = accessType;
    if ( this.accessType == DatabaseMeta.TYPE_ACCESS_JNDI ) {
      this.username = "";
      this.password = "";
    }
  }

  /**
   * @return Returns the changed.
   */
  @Override
  public boolean isChanged() {
    return changed;
  }

  /**
   * @param changed
   *          The changed to set.
   */
  @Override
  public void setChanged( boolean changed ) {
    this.changed = changed;
  }

  /**
   * @return Returns the connection name.
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          The connection Name to set.
   */
  @Override
  public void setName( String name ) {
    this.name = name;

    // Default display name to be the same as connection name if it has not
    // been initialized before
    if ( ( getDisplayName() == null ) || ( getDisplayName().length() == 0 ) ) {
      setDisplayName( name );
    }
  }

  /**
   * @return Returns the un-escaped connection Name.
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * @param displayName The un-escaped connection Name to set.
   */
  public void setDisplayName( String displayName ) {
    this.displayName = displayName;
  }

  /**
   * @return Returns the databaseName.
   */
  @Override
  public String getDatabaseName() {
    return databaseName;
  }

  /**
   * @param databaseName
   *          The databaseName to set.
   */
  @Override
  public void setDatabaseName( String databaseName ) {
    this.databaseName = databaseName;
  }

  /**
   * @param databasePortNumberString
   *          The databasePortNumber string to set.
   */
  @Override
  public void setDatabasePortNumberString( String databasePortNumberString ) {
    if ( databasePortNumberString != null ) {
      getAttributes().put( BaseDatabaseMeta.ATTRIBUTE_PORT_NUMBER, databasePortNumberString );
    }
  }

  /**
   * @return Returns the databasePortNumber string.
   */
  @Override
  public String getDatabasePortNumberString() {
    return getAttributes().getProperty( ATTRIBUTE_PORT_NUMBER, "-1" );
  }

  /**
   * @return Returns the hostname.
   */
  @Override
  public String getHostname() {
    return hostname;
  }

  /**
   * @param hostname
   *          The hostname to set.
   */
  @Override
  public void setHostname( String hostname ) {
    this.hostname = hostname;
  }

  /**
   * @return Returns the id.
   */
  @Override
  public ObjectId getObjectId() {
    return objectId;
  }

  /**
   * @param id
   *          The id to set.
   */
  @Override
  public void setObjectId( ObjectId id ) {
    this.objectId = id;
  }

  /**
   * @return Returns the password.
   */
  @Override
  public String getPassword() {
    return password;
  }

  /**
   * @param password
   *          The password to set.
   */
  @Override
  public void setPassword( String password ) {
    if ( this.accessType == DatabaseMeta.TYPE_ACCESS_JNDI ) {
      this.password = "";
    } else {
      this.password = password;
    }
  }

  /**
   * @return Returns the servername.
   */
  @Override
  public String getServername() {
    return servername;
  }

  /**
   * @param servername
   *          The servername to set.
   */
  @Override
  public void setServername( String servername ) {
    this.servername = servername;
  }

  /**
   * @return Returns the tablespaceData.
   */
  @Override
  public String getDataTablespace() {
    return dataTablespace;
  }

  /**
   * @param dataTablespace
   *          The data tablespace to set.
   */
  @Override
  public void setDataTablespace( String dataTablespace ) {
    this.dataTablespace = dataTablespace;
  }

  /**
   * @return Returns the index tablespace.
   */
  @Override
  public String getIndexTablespace() {
    return indexTablespace;
  }

  /**
   * @param indexTablespace
   *          The index tablespace to set.
   */
  @Override
  public void setIndexTablespace( String indexTablespace ) {
    this.indexTablespace = indexTablespace;
  }

  /**
   * @return Returns the username.
   */
  @Override
  public String getUsername() {
    return username;
  }

  /**
   * @param username
   *          The username to set.
   */
  @Override
  public void setUsername( String username ) {
    if ( this.accessType == DatabaseMeta.TYPE_ACCESS_JNDI ) {
      this.username = "";
    }
    this.username = username;
  }

  /**
   * @return The extra attributes for this database connection
   */
  @Override
  public Properties getAttributes() {
    return attributes;
  }

  /**
   * Set extra attributes on this database connection
   *
   * @param attributes
   *          The extra attributes to set on this database connection.
   */
  @Override
  public void setAttributes( Properties attributes ) {
    this.attributes = attributes;
  }

  /**
   * Clone the basic settings for this connection!
   */
  @Override
  public Object clone() {
    BaseDatabaseMeta retval = null;
    try {
      retval = (BaseDatabaseMeta) super.clone();

      // CLone the attributes as well...
      retval.attributes = (Properties) attributes.clone();
    } catch ( CloneNotSupportedException e ) {
      throw new RuntimeException( e );
    }
    return retval;
  }

  /*
   * *******************************************************************************
   * DEFAULT SETTINGS FOR ALL DATABASES ********************************************************************************
   */

  /**
   * @return the default database port number
   */
  @Override
  public int getDefaultDatabasePort() {
    return -1; // No default port or not used.
  }

  @Override public Map<String, String> getDefaultOptions() {
    return Collections.emptyMap();
  }

  /**
   * See if this database supports the setCharacterStream() method on a PreparedStatement.
   *
   * @return true if we can set a Stream on a field in a PreparedStatement. False if not.
   */
  @Override
  public boolean supportsSetCharacterStream() {
    return true;
  }

  /**
   * @return Whether or not the database can use auto increment type of fields (pk)
   */
  @Override
  public boolean supportsAutoInc() {
    return true;
  }

  @Override
  public String getLimitClause( int nrRows ) {
    return "";
  }

  @Override
  public int getNotFoundTK( boolean use_autoinc ) {
    return 0;
  }

  /**
   * Get the SQL to get the next value of a sequence. (Oracle/PGSQL only)
   *
   * @param sequenceName
   *          The sequence name
   * @return the SQL to get the next value of a sequence. (Oracle/PGSQL only)
   */
  @Override
  public String getSQLNextSequenceValue( String sequenceName ) {
    return "";
  }

  /**
   * Get the current value of a database sequence
   *
   * @param sequenceName
   *          The sequence to check
   * @return The current value of a database sequence
   */
  @Override
  public String getSQLCurrentSequenceValue( String sequenceName ) {
    return "";
  }

  /**
   * Check if a sequence exists.
   *
   * @param sequenceName
   *          The sequence to check
   * @return The SQL to get the name of the sequence back from the databases data dictionary
   */
  @Override
  public String getSQLSequenceExists( String sequenceName ) {
    return "";
  }

  /**
   * Checks whether or not the command setFetchSize() is supported by the JDBC driver...
   *
   * @return true is setFetchSize() is supported!
   */
  @Override
  public boolean isFetchSizeSupported() {
    return true;
  }

  /**
   * Indicates the need to insert a placeholder (0) for auto increment fields.
   *
   * @return true if we need a placeholder for auto increment fields in insert statements.
   */
  @Override
  public boolean needsPlaceHolder() {
    return false;
  }

  /**
   * @return true if the database supports schemas
   */
  @Override
  public boolean supportsSchemas() {
    return true;
  }

  /**
   * @return true if the database supports catalogs
   */
  @Override
  public boolean supportsCatalogs() {
    return true;
  }

  /**
   *
   * @return true when the database engine supports empty transaction. (for example Informix does not on a non-ANSI
   *         database type!)
   */
  @Override
  public boolean supportsEmptyTransactions() {
    return true;
  }

  /**
   * @return the function for SUM agrregate
   */
  @Override
  public String getFunctionSum() {
    return "SUM";
  }

  /**
   * @return the function for Average agrregate
   */
  @Override
  public String getFunctionAverage() {
    return "AVG";
  }

  /**
   * @return the function for Minimum agrregate
   */
  @Override
  public String getFunctionMinimum() {
    return "MIN";
  }

  /**
   * @return the function for Maximum agrregate
   */
  @Override
  public String getFunctionMaximum() {
    return "MAX";
  }

  /**
   * @return the function for Count agrregate
   */
  @Override
  public String getFunctionCount() {
    return "COUNT";
  }

  /**
   * Get the schema-table combination to query the right table. Usually that is SCHEMA.TABLENAME, however there are
   * exceptions to this rule...
   *
   * @param schema_name
   *          The schema name
   * @param table_part
   *          The tablename
   * @return the schema-table combination to query the right table.
   */
  @Override
  public String getSchemaTableCombination( String schema_name, String table_part ) {
    return schema_name + "." + table_part;
  }

  /**
   * Checks for quotes before quoting schema and table. Many dialects had hardcoded quotes, they probably didn't get
   * updated properly when quoteFields() was introduced to DatabaseMeta.
   *
   * @param schemaPart
   * @param tablePart
   * @return quoted schema and table
   *
   * @deprecated we should phase this out in 5.0, but it's there to keep backwards compatibility in the 4.x releases.
   */
  @Deprecated
  public String getBackwardsCompatibleSchemaTableCombination( String schemaPart, String tablePart ) {
    String schemaTable = "";
    if ( schemaPart != null && ( schemaPart.contains( getStartQuote() ) || schemaPart.contains( getEndQuote() ) ) ) {
      schemaTable += schemaPart;
    } else {
      schemaTable += getStartQuote() + schemaPart + getEndQuote();
    }
    schemaTable += ".";
    if ( tablePart != null && ( tablePart.contains( getStartQuote() ) || tablePart.contains( getEndQuote() ) ) ) {
      schemaTable += tablePart;
    } else {
      schemaTable += getStartQuote() + tablePart + getEndQuote();
    }
    return schemaTable;
  }

  /**
   * Checks for quotes before quoting table. Many dialects had hardcoded quotes, they probably didn't get updated
   * properly when quoteFields() was introduced to DatabaseMeta.
   *
   * @param tablePart
   *
   * @return quoted table
   *
   * @deprecated we should phase this out in 5.0, but it's there to keep backwards compatibility in the 4.x releases.
   */
  @Deprecated
  public String getBackwardsCompatibleTable( String tablePart ) {
    if ( tablePart != null && ( tablePart.contains( getStartQuote() ) || tablePart.contains( getEndQuote() ) ) ) {
      return tablePart;
    } else {
      return getStartQuote() + tablePart + getEndQuote();
    }
  }

  /**
   * Get the maximum length of a text field for this database connection. This includes optional CLOB, Memo and Text
   * fields. (the maximum!)
   *
   * @return The maximum text field length for this database type. (mostly CLOB_LENGTH)
   */
  @Override
  public int getMaxTextFieldLength() {
    return DatabaseMeta.CLOB_LENGTH;
  }

  /**
   * Get the maximum length of a text field (VARCHAR) for this database connection. If this size is exceeded use a CLOB.
   *
   * @return The maximum VARCHAR field length for this database type. (mostly identical to getMaxTextFieldLength() -
   *         CLOB_LENGTH)
   */
  @Override
  public int getMaxVARCHARLength() {
    return DatabaseMeta.CLOB_LENGTH;
  }

  /**
   * @return true if the database supports transactions.
   */
  @Override
  public boolean supportsTransactions() {
    return true;
  }

  /**
   * @return true if the database supports sequences
   */
  @Override
  public boolean supportsSequences() {
    return false;
  }

  /**
   * @return true if the database supports bitmap indexes
   */
  @Override
  public boolean supportsBitmapIndex() {
    return true;
  }

  /**
   * @return true if the database JDBC driver supports the setLong command
   */
  @Override
  public boolean supportsSetLong() {
    return true;
  }

  /**
   * Generates the SQL statement to drop a column from the specified table
   *
   * @param tablename
   *          The table to add
   * @param v
   *          The column defined as a value
   * @param tk
   *          the name of the technical key field
   * @param use_autoinc
   *          whether or not this field uses auto increment
   * @param pk
   *          the name of the primary key field
   * @param semicolon
   *          whether or not to add a semi-colon behind the statement.
   * @return the SQL statement to drop a column from the specified table
   */
  @Override
  public String getDropColumnStatement( String tablename, ValueMetaInterface v, String tk, boolean use_autoinc,
    String pk, boolean semicolon ) {
    return "ALTER TABLE " + tablename + " DROP " + v.getName() + Const.CR;
  }

  /**
   * @return an array of reserved words for the database type...
   */
  @Override
  public String[] getReservedWords() {
    return new String[] {};
  }

  /**
   * @return true if reserved words need to be double quoted ("password", "select", ...)
   */
  @Override
  public boolean quoteReservedWords() {
    return true;
  }

  /**
   * @return The start quote sequence, mostly just double quote, but sometimes [, ...
   */
  @Override
  public String getStartQuote() {
    return "\"";
  }

  /**
   * @return The end quote sequence, mostly just double quote, but sometimes ], ...
   */
  @Override
  public String getEndQuote() {
    return "\"";
  }

  /**
   * @return true if Kettle can create a repository on this type of database.
   */
  @Override
  public boolean supportsRepository() {
    return false;
  }

  /**
   * @return a list of table types to retrieve tables for the database
   */
  @Override
  public String[] getTableTypes() {
    return new String[] { "TABLE" };
  }

  /**
   * @return a list of table types to retrieve views for the database
   */
  @Override
  public String[] getViewTypes() {
    return new String[] { "VIEW" };
  }

  /**
   * @return a list of table types to retrieve synonyms for the database
   */
  @Override
  public String[] getSynonymTypes() {
    return new String[] { "SYNONYM" };
  }

  /**
   * @return true if we need to supply the schema-name to getTables in order to get a correct list of items.
   */
  @Override
  public boolean useSchemaNameForTableList() {
    return false;
  }

  /**
   * @return true if the database supports views
   */
  @Override
  public boolean supportsViews() {
    return true;
  }

  /**
   * @return true if the database supports synonyms
   */
  @Override
  public boolean supportsSynonyms() {
    return false;
  }

  /**
   * @return The SQL on this database to get a list of stored procedures.
   */
  @Override
  public String getSQLListOfProcedures() {
    return null;
  }

  /**
   * @return The SQL on this database to get a list of sequences.
   */
  @Override
  public String getSQLListOfSequences() {
    return null;
  }

  /**
   * @param tableName
   *          The table to be truncated.
   * @return The SQL statement to truncate a table: remove all rows from it without a transaction
   */
  @Override
  public String getTruncateTableStatement( String tableName ) {
    return "TRUNCATE TABLE " + tableName;
  }

  /**
   * Returns the minimal SQL to launch in order to determine the layout of the resultset for a given database table
   *
   * @param tableName
   *          The name of the table to determine the layout for
   * @return The SQL to launch.
   */
  @Override
  public String getSQLQueryFields( String tableName ) {
    return "SELECT * FROM " + tableName;
  }

  /**
   * Most databases round number(7,2) 17.29999999 to 17.30, but some don't.
   *
   * @return true if the database supports roundinf of floating point data on update/insert
   */
  @Override
  public boolean supportsFloatRoundingOnUpdate() {
    return true;
  }

  /**
   * @param tableNames
   *          The names of the tables to lock
   * @return The SQL command to lock database tables for write purposes. null is returned in case locking is not
   *         supported on the target database. null is the default value
   */
  @Override
  public String getSQLLockTables( String[] tableNames ) {
    return null;
  }

  /**
   * @param tableNames
   *          The names of the tables to unlock
   * @return The SQL command to unlock database tables. null is returned in case locking is not supported on the target
   *         database. null is the default value
   */
  @Override
  public String getSQLUnlockTables( String[] tableNames ) {
    return null;
  }

  /**
   * @return true if the database supports timestamp to date conversion. For example Interbase doesn't support this!
   */
  @Override
  public boolean supportsTimeStampToDateConversion() {
    return true;
  }

  /**
   * @return true if the database JDBC driver supports batch updates For example Interbase doesn't support this!
   */
  @Override
  public boolean supportsBatchUpdates() {
    return true;
  }

  /**
   * @return true if the database supports a boolean, bit, logical, ... datatype The default is false: map to a string.
   */
  @Override
  public boolean supportsBooleanDataType() {
    String usePool = attributes.getProperty( ATTRIBUTE_SUPPORTS_BOOLEAN_DATA_TYPE, "N" );
    return "Y".equalsIgnoreCase( usePool );
  }

  /**
   * @param b
   *          Set to true if the database supports a boolean, bit, logical, ... datatype
   */
  @Override
  public void setSupportsBooleanDataType( boolean b ) {
    attributes.setProperty( ATTRIBUTE_SUPPORTS_BOOLEAN_DATA_TYPE, b ? "Y" : "N" );
  }

  /**
   * @return true if the database supports the Timestamp data type (nanosecond precision and all)
   */
  @Override
  public boolean supportsTimestampDataType() {
    String supportsTimestamp = attributes.getProperty( ATTRIBUTE_SUPPORTS_TIMESTAMP_DATA_TYPE, "N" );
    return "Y".equalsIgnoreCase( supportsTimestamp );
  }

  /**
   *
   * @param b
   *          Set to true if the database supports the Timestamp data type (nanosecond precision and all)
   */
  @Override
  public void setSupportsTimestampDataType( boolean b ) {
    attributes.setProperty( ATTRIBUTE_SUPPORTS_TIMESTAMP_DATA_TYPE, b ? "Y" : "N" );
  }

  /**
   * @return true if reserved words' case should be preserved
   */
  @Override
  public boolean preserveReservedCase() {
    String usePool = attributes.getProperty( ATTRIBUTE_PRESERVE_RESERVED_WORD_CASE, "Y" );
    return "Y".equalsIgnoreCase( usePool );
  }

  /**
   * @param b
   *          Set to true if reserved words' case should be preserved
   */
  @Override
  public void setPreserveReservedCase( boolean b ) {
    attributes.setProperty( ATTRIBUTE_PRESERVE_RESERVED_WORD_CASE, b ? "Y" : "N" );
  }

  /**
   * @return true if the database defaults to naming tables and fields in uppercase. True for most databases except for
   *         stuborn stuff like Postgres ;-)
   */
  @Override
  public boolean isDefaultingToUppercase() {
    return true;
  }

  /**
   * @return all the extra options that are set to be used for the database URL
   */
  @Override
  public Map<String, String> getExtraOptions() {
    Map<String, String> map = new Hashtable<String, String>();

    for ( Enumeration<Object> keys = attributes.keys(); keys.hasMoreElements(); ) {
      String attribute = (String) keys.nextElement();
      if ( attribute.startsWith( ATTRIBUTE_PREFIX_EXTRA_OPTION ) ) {
        String value = attributes.getProperty( attribute, "" );

        // Add to the map...
        map.put( attribute.substring( ATTRIBUTE_PREFIX_EXTRA_OPTION.length() ), value );
      }
    }

    return map;
  }

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
  @Override
  public void addExtraOption( String databaseTypeCode, String option, String value ) {
    attributes.put( ATTRIBUTE_PREFIX_EXTRA_OPTION + databaseTypeCode + "." + option, value );
  }

  /**
   * @return The extra option separator in database URL for this platform (usually this is semicolon ; )
   */
  @Override
  public String getExtraOptionSeparator() {
    return ";";
  }

  /**
   * @return The extra option value separator in database URL for this platform (usually this is the equal sign = )
   */
  @Override
  public String getExtraOptionValueSeparator() {
    return "=";
  }

  /**
   * @return This indicator separates the normal URL from the options
   */
  @Override
  public String getExtraOptionIndicator() {
    return ";";
  }

  /**
   * @return true if the database supports connection options in the URL, false if they are put in a Properties object.
   */
  @Override
  public boolean supportsOptionsInURL() {
    return true;
  }

  /**
   * @return extra help text on the supported options on the selected database platform.
   */
  @Override
  public String getExtraOptionsHelpText() {
    return null;
  }

  /**
   * @return true if the database JDBC driver supports getBlob on the resultset. If not we must use getBytes() to get
   *         the data.
   */
  @Override
  public boolean supportsGetBlob() {
    return true;
  }

  /**
   * @return The SQL to execute right after connecting
   */
  @Override
  public String getConnectSQL() {
    return attributes.getProperty( ATTRIBUTE_SQL_CONNECT );
  }

  /**
   * @param sql
   *          The SQL to execute right after connecting
   */
  @Override
  public void setConnectSQL( String sql ) {
    attributes.setProperty( ATTRIBUTE_SQL_CONNECT, sql );
  }

  /**
   * @return true if the database supports setting the maximum number of return rows in a resultset.
   */
  @Override
  public boolean supportsSetMaxRows() {
    return true;
  }

  /**
   * @return true if we want to use a database connection pool
   */
  @Override
  public boolean isUsingConnectionPool() {
    String usePool = attributes.getProperty( ATTRIBUTE_USE_POOLING );
    return "Y".equalsIgnoreCase( usePool );
  }

  /**
   * @param usePool
   *          true if we want to use a database connection pool
   */
  @Override
  public void setUsingConnectionPool( boolean usePool ) {
    attributes.setProperty( ATTRIBUTE_USE_POOLING, usePool ? "Y" : "N" );
  }

  /**
   * @return the maximum pool size
   */
  @Override
  public int getMaximumPoolSize() {
    return Const.toInt(
      attributes.getProperty( ATTRIBUTE_MAXIMUM_POOL_SIZE ), ConnectionPoolUtil.defaultMaximumNrOfConnections );
  }

  /**
   * @param maximumPoolSize
   *          the maximum pool size
   */
  @Override
  public void setMaximumPoolSize( int maximumPoolSize ) {
    attributes.setProperty( ATTRIBUTE_MAXIMUM_POOL_SIZE, Integer.toString( maximumPoolSize ) );
  }

  /**
   * @return the initial pool size
   */
  @Override
  public int getInitialPoolSize() {
    return Const.toInt(
      attributes.getProperty( ATTRIBUTE_INITIAL_POOL_SIZE ), ConnectionPoolUtil.defaultInitialNrOfConnections );
  }

  /**
   * @param initialPoolSize
   *          the initial pool size
   */
  @Override
  public void setInitialPoolSize( int initialPoolSize ) {
    attributes.setProperty( ATTRIBUTE_INITIAL_POOL_SIZE, Integer.toString( initialPoolSize ) );
  }

  /**
   * @return true if we want to use a database connection pool
   */
  @Override
  public boolean isPartitioned() {
    String isClustered = attributes.getProperty( ATTRIBUTE_IS_CLUSTERED );
    return "Y".equalsIgnoreCase( isClustered );
  }

  /**
   * @param clustered
   *          true if we want to use a database connection pool
   */
  @Override
  public void setPartitioned( boolean clustered ) {
    attributes.setProperty( ATTRIBUTE_IS_CLUSTERED, clustered ? "Y" : "N" );
  }

  /**
   * @return the available partition/host/databases/port combinations in the cluster
   */
  @Override
  public PartitionDatabaseMeta[] getPartitioningInformation() {
    // find the maximum number of attributes starting with ATTRIBUTE_CLUSTER_HOSTNAME_PREFIX

    int nr = 0;
    while ( ( attributes.getProperty( ATTRIBUTE_CLUSTER_HOSTNAME_PREFIX + nr ) ) != null ) {
      nr++;
    }

    PartitionDatabaseMeta[] clusterInfo = new PartitionDatabaseMeta[nr];

    for ( nr = 0; nr < clusterInfo.length; nr++ ) {
      String partitionId = attributes.getProperty( ATTRIBUTE_CLUSTER_PARTITION_PREFIX + nr );
      String hostname = attributes.getProperty( ATTRIBUTE_CLUSTER_HOSTNAME_PREFIX + nr );
      String port = attributes.getProperty( ATTRIBUTE_CLUSTER_PORT_PREFIX + nr );
      String dbName = attributes.getProperty( ATTRIBUTE_CLUSTER_DBNAME_PREFIX + nr );
      String username = attributes.getProperty( ATTRIBUTE_CLUSTER_USERNAME_PREFIX + nr );
      String password = attributes.getProperty( ATTRIBUTE_CLUSTER_PASSWORD_PREFIX + nr );
      clusterInfo[nr] = new PartitionDatabaseMeta( partitionId, hostname, port, dbName );
      clusterInfo[nr].setUsername( username );
      clusterInfo[nr].setPassword( Encr.decryptPasswordOptionallyEncrypted( password ) );
    }

    return clusterInfo;
  }

  /**
   * @param clusterInfo
   *          the available partition/host/databases/port combinations in the cluster
   */
  @Override
  public void setPartitioningInformation( PartitionDatabaseMeta[] clusterInfo ) {
    for ( int nr = 0; nr < clusterInfo.length; nr++ ) {
      PartitionDatabaseMeta meta = clusterInfo[nr];

      attributes.put( ATTRIBUTE_CLUSTER_PARTITION_PREFIX + nr, Const.NVL( meta.getPartitionId(), "" ) );
      attributes.put( ATTRIBUTE_CLUSTER_HOSTNAME_PREFIX + nr, Const.NVL( meta.getHostname(), "" ) );
      attributes.put( ATTRIBUTE_CLUSTER_PORT_PREFIX + nr, Const.NVL( meta.getPort(), "" ) );
      attributes.put( ATTRIBUTE_CLUSTER_DBNAME_PREFIX + nr, Const.NVL( meta.getDatabaseName(), "" ) );
      attributes.put( ATTRIBUTE_CLUSTER_USERNAME_PREFIX + nr, Const.NVL( meta.getUsername(), "" ) );
      attributes.put( ATTRIBUTE_CLUSTER_PASSWORD_PREFIX + nr, Const.NVL( Encr
        .encryptPasswordIfNotUsingVariables( meta.getPassword() ), "" ) );
    }
  }

  /**
   * @return The set of properties (newly created object) that contains the connection pooling parameters All
   *         environment variables will be replaced here.
   */
  @Override
  public Properties getConnectionPoolingProperties() {
    Properties properties = new Properties();

    for ( Iterator<Object> iter = attributes.keySet().iterator(); iter.hasNext(); ) {
      String element = (String) iter.next();
      if ( element.startsWith( ATTRIBUTE_POOLING_PARAMETER_PREFIX ) ) {
        String key = element.substring( ATTRIBUTE_POOLING_PARAMETER_PREFIX.length() );
        String value = attributes.getProperty( element );
        properties.put( key, value );
      }
    }

    return properties;
  }

  @Override
  public void setConnectionPoolingProperties( Properties properties ) {
    // Clear our the previous set of pool parameters
    for ( Iterator<Object> iter = attributes.keySet().iterator(); iter.hasNext(); ) {
      String key = (String) iter.next();
      if ( key.startsWith( ATTRIBUTE_POOLING_PARAMETER_PREFIX ) ) {
        attributes.remove( key );
      }
    }

    for ( Iterator<Object> iter = properties.keySet().iterator(); iter.hasNext(); ) {
      String element = (String) iter.next();
      String value = properties.getProperty( element );
      if ( !Utils.isEmpty( element ) && !Utils.isEmpty( value ) ) {
        attributes.put( ATTRIBUTE_POOLING_PARAMETER_PREFIX + element, value );
      }
    }
  }

  @Override
  public String getSQLTableExists( String tablename ) {
    return "SELECT 1 FROM " + tablename;
  }

  @Override
  public String getSQLColumnExists( String columnname, String tablename ) {
    return "SELECT " + columnname + " FROM " + tablename;
  }

  @Override
  public boolean needsToLockAllTables() {
    return true;
  }

  /**
   * @return true if the database is streaming results (normally this is an option just for MySQL).
   */
  @Override
  public boolean isStreamingResults() {
    String usePool = attributes.getProperty( ATTRIBUTE_USE_RESULT_STREAMING, "Y" ); // DEFAULT TO YES!!
    return "Y".equalsIgnoreCase( usePool );
  }

  /**
   * @param useStreaming
   *          true if we want the database to stream results (normally this is an option just for MySQL).
   */
  @Override
  public void setStreamingResults( boolean useStreaming ) {
    attributes.setProperty( ATTRIBUTE_USE_RESULT_STREAMING, useStreaming ? "Y" : "N" );
  }

  /**
   * @return true if all fields should always be quoted in db
   */
  @Override
  public boolean isQuoteAllFields() {
    String quoteAllFields = attributes.getProperty( ATTRIBUTE_QUOTE_ALL_FIELDS, "N" ); // DEFAULT TO NO!!
    return "Y".equalsIgnoreCase( quoteAllFields );
  }

  /**
   * @param quoteAllFields
   *          true if we want the database to stream results (normally this is an option just for MySQL).
   */
  @Override
  public void setQuoteAllFields( boolean quoteAllFields ) {
    attributes.setProperty( ATTRIBUTE_QUOTE_ALL_FIELDS, quoteAllFields ? "Y" : "N" );
  }

  /**
   * @return true if all identifiers should be forced to lower case
   */
  @Override
  public boolean isForcingIdentifiersToLowerCase() {
    String forceLowerCase = attributes.getProperty( ATTRIBUTE_FORCE_IDENTIFIERS_TO_LOWERCASE, "N" ); // DEFAULT TO NO!!
    return "Y".equalsIgnoreCase( forceLowerCase );
  }

  /**
   * @param forceLowerCase
   *          true if all identifiers should be forced to lower case
   */
  @Override
  public void setForcingIdentifiersToLowerCase( boolean forceLowerCase ) {
    attributes.setProperty( ATTRIBUTE_FORCE_IDENTIFIERS_TO_LOWERCASE, forceLowerCase ? "Y" : "N" );
  }

  /**
   * @return true if all identifiers should be forced to upper case
   */
  @Override
  public boolean isForcingIdentifiersToUpperCase() {
    String forceUpperCase = attributes.getProperty( ATTRIBUTE_FORCE_IDENTIFIERS_TO_UPPERCASE, "N" ); // DEFAULT TO NO!!
    return "Y".equalsIgnoreCase( forceUpperCase );
  }

  /**
   * @param forceUpperCase
   *          true if all identifiers should be forced to upper case
   */
  @Override
  public void setForcingIdentifiersToUpperCase( boolean forceUpperCase ) {
    attributes.setProperty( ATTRIBUTE_FORCE_IDENTIFIERS_TO_UPPERCASE, forceUpperCase ? "Y" : "N" );
  }

  /**
   * @return true if we use a double decimal separator to specify schema/table combinations on MS-SQL server
   */
  @Override
  public boolean isUsingDoubleDecimalAsSchemaTableSeparator() {
    String usePool = attributes.getProperty( ATTRIBUTE_MSSQL_DOUBLE_DECIMAL_SEPARATOR, "N" ); // DEFAULT TO YES!!
    return "Y".equalsIgnoreCase( usePool );
  }

  /**
   * @param useDoubleDecimalSeparator
   *          true if we should use a double decimal separator to specify schema/table combinations on MS-SQL server
   */
  @Override
  public void setUsingDoubleDecimalAsSchemaTableSeparator( boolean useDoubleDecimalSeparator ) {
    attributes.setProperty( ATTRIBUTE_MSSQL_DOUBLE_DECIMAL_SEPARATOR, useDoubleDecimalSeparator ? "Y" : "N" );
  }

  /**
   * @return true if this database needs a transaction to perform a query (auto-commit turned off).
   */
  @Override
  public boolean isRequiringTransactionsOnQueries() {
    return true;
  }

  /**
   * You can use this method to supply an alternate factory for the test method in the dialogs. This is useful for
   * plugins like SAP/R3 and PALO.
   *
   * @return the name of the database test factory to use.
   */
  @Override
  public String getDatabaseFactoryName() {
    return DatabaseFactory.class.getName();
  }

  /**
   * @return The preferred schema name of this database connection.
   */
  @Override
  public String getPreferredSchemaName() {
    return attributes.getProperty( ATTRIBUTE_PREFERRED_SCHEMA_NAME );
  }

  /**
   * @param preferredSchemaName
   *          The preferred schema name of this database connection.
   */
  @Override
  public void setPreferredSchemaName( String preferredSchemaName ) {
    attributes.setProperty( ATTRIBUTE_PREFERRED_SCHEMA_NAME, preferredSchemaName );
  }

  /**
   * Verifies on the specified database connection if an index exists on the fields with the specified name.
   *
   * @param database
   *          a connected database
   * @param schemaName
   * @param tableName
   * @param idx_fields
   * @return true if the index exists, false if it doesn't.
   * @throws KettleDatabaseException
   */
  @Override
  public boolean checkIndexExists( Database database, String schemaName, String tableName, String[] idx_fields ) throws KettleDatabaseException {

    String tablename = database.getDatabaseMeta().getQuotedSchemaTableCombination( schemaName, tableName );

    boolean[] exists = new boolean[idx_fields.length];
    for ( int i = 0; i < exists.length; i++ ) {
      exists[i] = false;
    }

    try {
      // Get a list of all the indexes for this table
      ResultSet indexList = null;
      try {
        indexList = database.getDatabaseMetaData().getIndexInfo( null, null, tablename, false, true );
        while ( indexList.next() ) {
          // String tablen = indexList.getString("TABLE_NAME");
          // String indexn = indexList.getString("INDEX_NAME");
          String column = indexList.getString( "COLUMN_NAME" );
          // int pos = indexList.getShort("ORDINAL_POSITION");
          // int type = indexList.getShort("TYPE");

          int idx = Const.indexOfString( column, idx_fields );
          if ( idx >= 0 ) {
            exists[idx] = true;
          }
        }
      } finally {
        if ( indexList != null ) {
          indexList.close();
        }
      }

      // See if all the fields are indexed...
      boolean all = true;
      for ( int i = 0; i < exists.length && all; i++ ) {
        if ( !exists[i] ) {
          all = false;
        }
      }

      return all;
    } catch ( Exception e ) {
      throw new KettleDatabaseException( "Unable to determine if indexes exists on table [" + tablename + "]", e );
    }

  }

  /**
   * @return true if the database supports the NOMAXVALUE sequence option. The default is false, AS/400 and DB2 support
   *         this.
   */
  @Override
  public boolean supportsSequenceNoMaxValueOption() {
    return false;
  }

  /**
   * @return true if we need to append the PRIMARY KEY block in the create table block after the fields, required for
   *         Cache.
   */
  @Override
  public boolean requiresCreateTablePrimaryKeyAppend() {
    return false;
  }

  /**
   * @return true if the database requires you to cast a parameter to varchar before comparing to null. Only required
   *         for DB2 and Vertica
   *
   */
  @Override
  public boolean requiresCastToVariousForIsNull() {
    return false;
  }

  /**
   * @return Handles the special case of DB2 where the display size returned is twice the precision. In that case, the
   *         length is the precision.
   *
   */
  @Override
  public boolean isDisplaySizeTwiceThePrecision() {
    return false;
  }

  /**
   * Most databases allow you to retrieve result metadata by preparing a SELECT statement.
   *
   * @return true if the database supports retrieval of query metadata from a prepared statement. False if the query
   *         needs to be executed first.
   */
  @Override
  public boolean supportsPreparedStatementMetadataRetrieval() {
    return true;
  }

  /**
   * @return true if this database only supports metadata retrieval on a result set, never on a statement (even if the
   *         statement has been executed)
   */
  @Override
  public boolean supportsResultSetMetadataRetrievalOnly() {
    return false;
  }

  /**
   * @param tableName
   * @return true if the specified table is a system table
   */
  @Override
  public boolean isSystemTable( String tableName ) {
    return false;
  }

  /**
   * @return true if the database supports newlines in a SQL statements.
   */
  @Override
  public boolean supportsNewLinesInSQL() {
    return true;
  }

  /**
   * @return the SQL to retrieve the list of schemas or null if the JDBC metadata needs to be used.
   */
  @Override
  public String getSQLListOfSchemas() {
    return null;
  }

  /**
   * @return The maximum number of columns in a database, <=0 means: no known limit
   */
  @Override
  public int getMaxColumnsInIndex() {
    return 0;
  }

  /**
   * @return true if the database supports error handling (recovery of failure) while doing batch updates.
   */
  @Override
  public boolean supportsErrorHandlingOnBatchUpdates() {
    return true;
  }

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
  @Override
  public String getSQLInsertAutoIncUnknownDimensionRow( String schemaTable, String keyField, String versionField ) {
    return "insert into " + schemaTable + "(" + keyField + ", " + versionField + ") values (0, 1)";
  }

  /**
   * @return true if this is a relational database you can explore. Return false for SAP, PALO, etc.
   */
  @Override
  public boolean isExplorable() {
    return true;
  }

  /**
   * @return The name of the XUL overlay file to display extra options. This is only used in case of a non-standard
   *         plugin. Usually this method returns null.
   */
  @Override
  public String getXulOverlayFile() {
    return null;
  }

  /**
   * @param string
   * @return A string that is properly quoted for use in a SQL statement (insert, update, delete, etc)
   */
  @Override
  public String quoteSQLString( String string ) {
    string = string.replaceAll( "'", "''" );
    string = string.replaceAll( "\\n", "\\\\n" );
    string = string.replaceAll( "\\r", "\\\\r" );
    return "'" + string + "'";
  }

  /**
   * Build the SQL to count the number of rows in the passed table.
   *
   * @param tableName
   * @return
   */
  @Override
  public String getSelectCountStatement( String tableName ) {
    return SELECT_COUNT_STATEMENT + " " + tableName;
  }

  @Override
  public String generateColumnAlias( int columnIndex, String suggestedName ) {
    return "COL" + Integer.toString( columnIndex );
  }

  /**
   * Parse all possible statements from the provided SQL script.
   *
   * @param sqlScript
   *          Raw SQL Script to be parsed into executable statements.
   * @return List of parsed SQL statements to be executed separately.
   */
  @Override
  public List<String> parseStatements( String sqlScript ) {

    List<SqlScriptStatement> scriptStatements = getSqlScriptStatements( sqlScript );
    List<String> statements = new ArrayList<String>();
    for ( SqlScriptStatement scriptStatement : scriptStatements ) {
      statements.add( scriptStatement.getStatement() );
    }
    return statements;
  }

  /**
   * Parse the statements in the provided SQL script, provide more information about where each was found in the script.
   *
   * @param sqlScript
   *          Raw SQL Script to be parsed into executable statements.
   * @return List of SQL script statements to be executed separately.
   */
  @Override
  public List<SqlScriptStatement> getSqlScriptStatements( String sqlScript ) {
    List<SqlScriptStatement> statements = new ArrayList<SqlScriptStatement>();
    String all = sqlScript;
    int from = 0;
    int to = 0;
    int length = all.length();

    while ( to < length ) {
      char c = all.charAt( to );

      // Skip comment lines...
      //
      while ( all.substring( from ).startsWith( "--" ) ) {
        int nextLineIndex = all.indexOf( Const.CR, from );
        from = nextLineIndex + Const.CR.length();
        if ( to >= length ) {
          break;
        }
        c = all.charAt( c );
      }
      if ( to >= length ) {
        break;
      }

      // Skip over double quotes...
      //
      if ( c == '"' ) {
        int nextDQuoteIndex = all.indexOf( '"', to + 1 );
        if ( nextDQuoteIndex >= 0 ) {
          to = nextDQuoteIndex + 1;
        }
      }

      // Skip over back-ticks
      if ( c == '`' ) {
        int nextBacktickIndex = all.indexOf( '`', to + 1 );
        if ( nextBacktickIndex >= 0 ) {
          to = nextBacktickIndex + 1;
        }
      }

      c = all.charAt( to );
      if ( c == '\'' ) {
        boolean skip = true;

        // Don't skip over \' or ''
        //
        if ( to > 0 ) {
          char prevChar = all.charAt( to - 1 );
          if ( prevChar == '\\' || prevChar == '\'' ) {
            skip = false;
          }
        }

        // Jump to the next quote and continue from there.
        //
        while ( skip ) {
          int nextQuoteIndex = all.indexOf( '\'', to + 1 );
          if ( nextQuoteIndex >= 0 ) {
            to = nextQuoteIndex + 1;

            skip = false;

            if ( to < all.length() ) {
              char nextChar = all.charAt( to );
              if ( nextChar == '\'' ) {
                skip = true;
                to++;
              }
            }
            if ( to > 0 ) {
              char prevChar = all.charAt( to - 2 );
              if ( prevChar == '\\' ) {
                skip = true;
                to++;
              }
            }
          }
        }
      }

      c = all.charAt( to );

      // end of statement
      if ( c == ';' || to >= length - 1 ) {
        if ( to >= length - 1 ) {
          to++; // grab last char also!
        }

        String stat = all.substring( from, to );
        if ( !onlySpaces( stat ) ) {
          String s = Const.trim( stat );
          statements.add( new SqlScriptStatement(
            s, from, to, s.toUpperCase().startsWith( "SELECT" ) || s.toLowerCase().startsWith( "show" ) ) );
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
  protected boolean onlySpaces( String str ) {
    for ( int i = 0; i < str.length(); i++ ) {
      int c = str.charAt( i );
      if ( c != ' ' && c != '\t' && c != '\n' && c != '\r' ) {
        return false;
      }
    }
    return true;
  }

  /**
   * @return true if the database is a MySQL variant, like MySQL 5.1, InfiniDB, InfoBright, and so on.
   */
  @Override
  public boolean isMySQLVariant() {
    return false;
  }

  /**
   * @return true if the database type can be tested against a database instance
   */
  public boolean canTest() {
    return true;
  }

  /**
   * @return true if the database name is a required parameter
   */
  public boolean requiresName() {
    return true;
  }

  /**
   * Returns a true of savepoints can be released, false if not.
   *
   * @return
   */
  @Override
  public boolean releaseSavepoint() {
    return releaseSavepoint;
  }

  public Long getNextBatchIdUsingSequence( String sequenceName, String schemaName, DatabaseMeta dbm, Database ldb ) throws KettleDatabaseException {
    return ldb.getNextSequenceValue( schemaName, sequenceName, null );
  }

  public Long getNextBatchIdUsingAutoIncSQL( String autoIncSQL, DatabaseMeta dbm, Database ldb ) throws KettleDatabaseException {
    Long rtn = null;
    PreparedStatement stmt = ldb.prepareSQL( autoIncSQL, true );
    try {
      stmt.executeUpdate();
      RowMetaAndData rmad = ldb.getGeneratedKeys( stmt );
      if ( rmad.getRowMeta().size() > 0 ) {
        rtn = rmad.getRowMeta().getInteger( rmad.getData(), 0 );
      } else {
        throw new KettleDatabaseException( "Unable to retrieve value of auto-generated technical key : "
          + "no value found!" );
      }
    } catch ( KettleValueException kve ) {
      throw new KettleDatabaseException( kve );
    } catch ( SQLException sqlex ) {
      throw new KettleDatabaseException( sqlex );
    } finally {
      try {
        stmt.close();
      } catch ( SQLException ignored ) {
        // Ignored
      }
    }
    return rtn;
  }

  public Long getNextBatchIdUsingLockTables( DatabaseMeta dbm, Database ldb, String schemaName, String tableName,
    String fieldName ) throws KettleDatabaseException {
    // The old way of doing things...
    Long rtn = null;
    // Make sure we lock that table to avoid concurrency issues
    String schemaAndTable = dbm.getQuotedSchemaTableCombination( schemaName, tableName );
    ldb.lockTables( new String[] { schemaAndTable, } );
    try {

      // Now insert value -1 to create a real write lock blocking the other
      // requests.. FCFS
      String sql = "INSERT INTO " + schemaAndTable + " (" + dbm.quoteField( fieldName ) + ") values (-1)";
      ldb.execStatement( sql );

      // Now this next lookup will stall on the other connections
      //
      rtn = ldb.getNextValue( null, schemaName, tableName, fieldName );
    } finally {
      // Remove the -1 record again...
      String sql = "DELETE FROM " + schemaAndTable + " WHERE " + dbm.quoteField( fieldName ) + "= -1";
      ldb.execStatement( sql );
      ldb.unlockTables( new String[] { schemaAndTable, } );
    }
    return rtn;
  }

  @Override
  public Long getNextBatchId( DatabaseMeta dbm, Database ldb,
    String schemaName, String tableName, String fieldName ) throws KettleDatabaseException {
    // Always take off autocommit.
    ldb.setCommit( 10 );

    //
    // Temporary work-around to handle batch-id from extended options
    // Eventually want this promoted to proper dialogs and such
    //

    Map<String, String> connectionExtraOptions = this.getExtraOptions();
    String sequenceProp = this.getPluginId() + "." + SEQUENCE_FOR_BATCH_ID;
    String autoIncSQLProp = this.getPluginId() + "." + AUTOINCREMENT_SQL_FOR_BATCH_ID;
    if ( connectionExtraOptions != null ) {
      if ( this.supportsSequences() && connectionExtraOptions.containsKey( sequenceProp ) ) {
        return getNextBatchIdUsingSequence( connectionExtraOptions.get( sequenceProp ), schemaName, dbm, ldb );
      } else if ( this.supportsAutoInc() && connectionExtraOptions.containsKey( autoIncSQLProp ) ) {
        return getNextBatchIdUsingAutoIncSQL( connectionExtraOptions.get( autoIncSQLProp ), dbm, ldb );
      }
    }
    return getNextBatchIdUsingLockTables( dbm, ldb, schemaName, tableName, fieldName );
  }

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
  @Override
  public String getDataTablespaceDDL( VariableSpace variables, DatabaseMeta databaseMeta ) {
    return getTablespaceDDL( variables, databaseMeta, databaseMeta.getDatabaseInterface().getDataTablespace() );
  }

  /**
   * Returns the tablespace DDL fragment for a "Index" tablespace.
   *
   * @param variables
   *          variables used for possible substitution
   * @param databaseMeta
   *          databaseMeta the database meta used for possible string enclosure of the tablespace. This method needs
   *          this as this is done after environmental substitution.
   *
   * @return String the tablespace name for indices in the format "tablespace TABLESPACE_NAME". The TABLESPACE_NAME and
   *         the passed DatabaseMata determines if TABLESPACE_NAME is to be enclosed in quotes.
   */
  @Override
  public String getIndexTablespaceDDL( VariableSpace variables, DatabaseMeta databaseMeta ) {
    return getTablespaceDDL( variables, databaseMeta, databaseMeta.getDatabaseInterface().getIndexTablespace() );
  }

  /**
   * Returns an empty string as most databases do not support tablespaces. Subclasses can override this method to
   * generate the DDL.
   *
   * @param variables
   *          variables needed for variable substitution.
   * @param databaseMeta
   *          databaseMeta needed for it's quoteField method. Since we are doing variable substitution we need to meta
   *          so that we can act on the variable substitution first and then the creation of the entire string that will
   *          be retuned.
   * @param tablespaceName
   *          tablespaceName name of the tablespace.
   *
   * @return String an empty String as most databases do not use tablespaces.
   */
  public String getTablespaceDDL( VariableSpace variables, DatabaseMeta databaseMeta, String tablespaceName ) {
    return "";
  }

  /**
   * This method allows a database dialect to convert database specific data types to Kettle data types.
   *
   * @param rs
   *          The result set to use
   * @param val
   *          The description of the value to retrieve
   * @param i
   *          the index on which we need to retrieve the value, 0-based.
   * @return The correctly converted Kettle data type corresponding to the valueMeta description.
   * @throws KettleDatabaseException
   */
  @Override
  public Object getValueFromResultSet( ResultSet rs, ValueMetaInterface val, int i ) throws KettleDatabaseException {

    return val.getValueFromResultSet( this, rs, i );

  }

  /**
   * @return true if the database supports the use of safe-points and if it is appropriate to ever use it (default to
   *         false)
   */
  @Override
  public boolean useSafePoints() {
    return false;
  }

  /**
   * @return true if the database supports error handling (the default). Returns false for certain databases (SQLite)
   *         that invalidate a prepared statement or even the complete connection when an error occurs.
   */
  @Override
  public boolean supportsErrorHandling() {
    return true;
  }

  @Override
  public String getSQLValue( ValueMetaInterface valueMeta, Object valueData, String dateFormat ) throws KettleValueException {

    StringBuilder ins = new StringBuilder();

    if ( valueMeta.isNull( valueData ) ) {
      ins.append( "null" );
    } else {
      // Normal cases...
      //
      switch ( valueMeta.getType() ) {
        case ValueMetaInterface.TYPE_BOOLEAN:
        case ValueMetaInterface.TYPE_STRING:
          String string = valueMeta.getString( valueData );
          // Have the database dialect do the quoting.
          // This also adds the single quotes around the string (thanks to PostgreSQL)
          //
          string = quoteSQLString( string );
          ins.append( string );
          break;
        case ValueMetaInterface.TYPE_DATE:
          Date date = valueMeta.getDate( valueData );

          if ( Utils.isEmpty( dateFormat ) ) {
            ins.append( "'" + valueMeta.getString( valueData ) + "'" );
          } else {
            try {
              java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat( dateFormat );
              ins.append( "'" + formatter.format( date ) + "'" );
            } catch ( Exception e ) {
              throw new KettleValueException( "Error : ", e );
            }
          }
          break;
        default:
          ins.append( valueMeta.getString( valueData ) );
          break;
      }
    }

    return ins.toString();
  }

  protected String getFieldnameProtector() {
    return FIELDNAME_PROTECTOR;
  }

  /**
   * Sanitize a string for usage as a field name
   * <ul>
   * <li>Append an underscore to any field name that matches a reserved word</li>
   * <li>Replaces spaces with underscores</li>
   * <li>Prefixes a string with underscore that begins with a number</li>
   * </ul>
   *
   * @param fieldname
   *          value to sanitize
   * @return
   */
  @Override
  public String getSafeFieldname( String fieldname ) {
    StringBuilder newName = new StringBuilder( fieldname.length() );

    char[] protectors = getFieldnameProtector().toCharArray();

    // alpha numerics , underscores, field protectors only
    for ( int idx = 0; idx < fieldname.length(); idx++ ) {
      char c = fieldname.charAt( idx );
      if ( ( c >= 'a' && c <= 'z' ) || ( c >= 'A' && c <= 'Z' ) || ( c >= '0' && c <= '9' ) || ( c == '_' ) ) {
        newName.append( c );
      } else if ( c == ' ' ) {
        newName.append( '_' );
      } else {
        // allow protectors
        for ( char protector : protectors ) {
          if ( c == protector ) {
            newName.append( c );
          }
        }
      }
      // else {
      // swallow this character
      // }
    }
    fieldname = newName.toString();

    // don't allow reserved words
    for ( String reservedWord : getReservedWords() ) {
      if ( fieldname.equalsIgnoreCase( reservedWord ) ) {
        fieldname = fieldname + getFieldnameProtector();
      }
    }

    fieldname = fieldname.replace( " ", getFieldnameProtector() );

    // can't start with a number
    if ( fieldname.matches( "^[0-9].*" ) ) {
      fieldname = getFieldnameProtector() + fieldname;
    }
    return fieldname;
  }

  /**
   * @return string with the no max value sequence option.
   */
  @Override
  public String getSequenceNoMaxValueOption() {
    return "NOMAXVALUE";
  }

  /**
   * @return true if the database supports autoGeneratedKeys
   */
  @Override
  public boolean supportsAutoGeneratedKeys() {
    return true;
  }


  /**
   * Customizes the ValueMetaInterface defined in the base
   *
   * @param v the determined valueMetaInterface
   * @param rm the sql result
   * @param index the index to the column
   * @return ValueMetaInterface customized with the data base specific types
   */
  @Override
  public ValueMetaInterface customizeValueFromSQLType( ValueMetaInterface v, java.sql.ResultSetMetaData rm, int index )
    throws SQLException {
    return null;
  }

  /**
   * Customizes the ValueMetaInterface defined in the base
   *
   * @return String the create table statement
   */
  @Override
  public String getCreateTableStatement() {
    return "CREATE TABLE ";
  }

  /**
   * Forms drop table statement.
   * This standard construct syntax is not legal for certain RDBMSs,
   * and should be overridden according to their specifics.
   *
   * @param tableName Name of the table to drop
   * @return Standard drop table statement
   */
  @Override
  public String getDropTableIfExistsStatement( String tableName ) {
    return "DROP TABLE IF EXISTS " + tableName;
  }

  @Override
  public boolean fullExceptionLog( Exception e ) {
    return true;
  }

  @Override
  public void addDefaultOptions() {

  }
}
