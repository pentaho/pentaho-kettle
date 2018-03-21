// CHECKSTYLE:FileLength:OFF
/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.BatchUpdateException;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.DBCache;
import org.pentaho.di.core.DBCacheEntry;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DataSourceProviderInterface.DatasourceType;
import org.pentaho.di.core.database.map.DatabaseConnectionMap;
import org.pentaho.di.core.database.util.DatabaseLogExceptionFactory;
import org.pentaho.di.core.database.util.DatabaseUtil;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseBatchException;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LogStatus;
import org.pentaho.di.core.logging.LogTableCoreInterface;
import org.pentaho.di.core.logging.LogTableField;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.Metrics;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.core.row.value.ValueMetaBinary;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaInternetAddress;
import org.pentaho.di.core.row.value.ValueMetaNone;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.row.value.ValueMetaTimestamp;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.RepositoryDirectory;

/**
 * Database handles the process of connecting to, reading from, writing to and updating databases. The database specific
 * parameters are defined in DatabaseInfo.
 *
 * @author Matt
 * @since 05-04-2003
 */
public class Database implements VariableSpace, LoggingObjectInterface {
  /**
   * for i18n purposes, needed by Translator2!!
   */
  private static final Class<?> PKG = Database.class;

  private static final Map<String, Set<String>> registeredDrivers = new HashMap<String, Set<String>>();

  private DatabaseMeta databaseMeta;

  private int rowlimit;
  private int commitsize;

  private Connection connection;

  private Statement sel_stmt;
  private PreparedStatement pstmt;
  private PreparedStatement prepStatementLookup;
  private PreparedStatement prepStatementUpdate;
  private PreparedStatement prepStatementInsert;
  private PreparedStatement pstmt_seq;
  private CallableStatement cstmt;

  // private ResultSetMetaData rsmd;
  private DatabaseMetaData dbmd;

  private RowMetaInterface rowMeta;

  private int written;

  private LogChannelInterface log;
  private LoggingObjectInterface parentLoggingObject;
  private static final String[] TABLE_TYPES_TO_GET = { "TABLE", "VIEW" };
  private static final String TABLES_META_DATA_TABLE_NAME = "TABLE_NAME";

  /**
   * Number of times a connection was opened using this object. Only used in the context of a database connection map
   */
  private volatile int opened;

  /**
   * The copy is equal to opened at the time of creation.
   */
  private volatile int copy;

  private String connectionGroup;
  private String partitionId;

  private VariableSpace variables = new Variables();

  private LogLevel logLevel = DefaultLogLevel.getLogLevel();

  private String containerObjectId;

  private int nrExecutedCommits;

  private static List<ValueMetaInterface> valueMetaPluginClasses;

  static {
    try {
      valueMetaPluginClasses = ValueMetaFactory.getValueMetaPluginClasses();
      Collections.sort( valueMetaPluginClasses, new Comparator<ValueMetaInterface>() {
        @Override
        public int compare( ValueMetaInterface o1, ValueMetaInterface o2 ) {
          // Reverse the sort list
          return ( Integer.valueOf( o1.getType() ).compareTo( Integer.valueOf( o2.getType() ) ) ) * -1;
        }
      } );
    } catch ( Exception e ) {
      throw new RuntimeException( "Unable to get list of instantiated value meta plugin classes", e );
    }
  }

  /**
   * Construct a new Database Connection
   *
   * @param databaseMeta The Database Connection Info to construct the connection with.
   * @deprecated Please specify the parent object so that we can see which object is initiating a database connection
   */
  @Deprecated
  public Database( DatabaseMeta databaseMeta ) {
    this.parentLoggingObject = null;
    this.databaseMeta = databaseMeta;
    shareVariablesWith( databaseMeta );

    // In this case we don't have the parent object, so we don't know which
    // object makes the connection.
    // We also don't know what log level to attach to it, so we have to stick to
    // the default
    // As such, this constructor is @deprecated.
    //
    log = new LogChannel( this );
    logLevel = log.getLogLevel();
    containerObjectId = log.getContainerObjectId();

    pstmt = null;
    rowMeta = null;
    dbmd = null;

    rowlimit = 0;

    written = 0;

    opened = copy = 0;

    if ( log.isDetailed() ) {
      log.logDetailed( "New database connection defined" );
    }
  }

  /**
   * Construct a new Database Connection
   *
   * @param databaseMeta The Database Connection Info to construct the connection with.
   */
  public Database( LoggingObjectInterface parentObject, DatabaseMeta databaseMeta ) {
    this.parentLoggingObject = parentObject;
    this.databaseMeta = databaseMeta;

    shareVariablesWith( databaseMeta );
    if ( parentObject instanceof VariableSpace ) {
      shareVariablesWith( (VariableSpace) parentObject );
    }

    log = new LogChannel( this, parentObject );
    this.containerObjectId = log.getContainerObjectId();
    this.logLevel = log.getLogLevel();
    if ( parentObject != null ) {
      log.setGatheringMetrics( parentObject.isGatheringMetrics() );
    }

    pstmt = null;
    rowMeta = null;
    dbmd = null;

    rowlimit = 0;

    written = 0;

    opened = copy = 0;

    if ( log.isDetailed() ) {
      log.logDetailed( "New database connection defined" );
    }
  }

  /**
   * This implementation is NullPointerException subject, and may not follow fundamental equals contract.
   * <p/>
   * Databases equality is based on {@link DatabaseMeta} equality.
   */
  @Override
  public boolean equals( Object obj ) {
    Database other = (Database) obj;
    return this.databaseMeta.equals( other.databaseMeta );
  }

  /**
   * Allows for the injection of a "life" connection, generated by a piece of software outside of Kettle.
   *
   * @param connection
   */
  public void setConnection( Connection connection ) {
    this.connection = connection;
  }

  /**
   * @return Returns the connection.
   */
  public Connection getConnection() {
    return connection;
  }

  /**
   * Set the maximum number of records to retrieve from a query.
   *
   * @param rows
   */
  public void setQueryLimit( int rows ) {
    rowlimit = rows;
  }

  /**
   * @return Returns the prepStatementInsert.
   */
  public PreparedStatement getPrepStatementInsert() {
    return prepStatementInsert;
  }

  /**
   * @return Returns the prepStatementLookup.
   */
  public PreparedStatement getPrepStatementLookup() {
    return prepStatementLookup;
  }

  /**
   * @return Returns the prepStatementUpdate.
   */
  public PreparedStatement getPrepStatementUpdate() {
    return prepStatementUpdate;
  }

  /**
   * Open the database connection.
   *
   * @throws KettleDatabaseException if something went wrong.
   */
  public void connect() throws KettleDatabaseException {
    connect( null );
  }

  /**
   * Open the database connection.
   *
   * @param partitionId the partition ID in the cluster to connect to.
   * @throws KettleDatabaseException if something went wrong.
   */
  public void connect( String partitionId ) throws KettleDatabaseException {
    connect( null, partitionId );
  }

  public synchronized void connect( String group, String partitionId ) throws KettleDatabaseException {
    try {

      log.snap( Metrics.METRIC_DATABASE_CONNECT_START, databaseMeta.getName() );

      // Before anything else, let's see if we already have a connection defined
      // for this group/partition!
      // The group is called after the thread-name of the transformation or job
      // that is running
      // The name of that thread name is expected to be unique (it is in Kettle)
      // So the deal is that if there is another thread using that, we go for
      // it.
      //
      if ( !Utils.isEmpty( group ) ) {
        this.connectionGroup = group;
        this.partitionId = partitionId;

        // Try to find the connection for the group
        Database lookup = DatabaseConnectionMap.getInstance().getOrStoreIfAbsent( group, partitionId, this );
        if ( lookup == null ) {
          // There was no mapped value before
          lookup = this;
        }
        lookup.shareConnectionWith( partitionId, this );
      } else {
        // Proceed with a normal connect
        normalConnect( partitionId );
      }

      try {
        ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.DatabaseConnected.id, this );
      } catch ( KettleException e ) {
        throw new KettleDatabaseException( e );
      }

    } finally {
      log.snap( Metrics.METRIC_DATABASE_CONNECT_STOP, databaseMeta.getName() );

    }
  }

  private synchronized void shareConnectionWith( String partitionId, Database anotherDb )
    throws KettleDatabaseException {
    // inside synchronized block we can increment 'opened' directly
    this.opened++;

    if ( this.connection == null ) {
      normalConnect( partitionId );
      this.copy = this.opened;

      // If we have a connection group or transaction ID, disable auto commit!
      //
      setAutoCommit( false );
    }

    anotherDb.connection = this.connection;
    anotherDb.copy = this.opened;
  }

  /**
   * Open the database connection. The algorithm is:
   * <ol>
   * <li>If <code>databaseMeta.getAccessType()</code> returns
   * <code>DatabaseMeta.TYPE_ACCESS_JNDI</code>, then the connection's datasource is looked up in JNDI </li>
   * <li>If <code>databaseMeta.isUsingConnectionPool()</code>, then the connection's datasource is looked up in the pool</li>
   * <li>otherwise, the connection is established via {@linkplain java.sql.DriverManager}</li>
   * </ol>
   *
   * @param partitionId the partition ID in the cluster to connect to.
   * @throws KettleDatabaseException if something went wrong.
   */
  public void normalConnect( String partitionId ) throws KettleDatabaseException {
    if ( databaseMeta == null ) {
      throw new KettleDatabaseException( "No valid database connection defined!" );
    }

    try {
      DataSourceProviderInterface dsp = DataSourceProviderFactory.getDataSourceProviderInterface();
      if ( dsp == null ) {
        // since DataSourceProviderFactory is initialised with new DatabaseUtil(),
        // this assignment is correct
        dsp = new DatabaseUtil();
      }

      if ( databaseMeta.getAccessType() == DatabaseMeta.TYPE_ACCESS_JNDI ) {
        String jndiName = environmentSubstitute( databaseMeta.getDatabaseName() );
        try {
          this.connection = dsp.getNamedDataSource( jndiName, DatasourceType.JNDI ).getConnection();
        } catch ( DataSourceNamingException e ) {
          log.logError( "Unable to find datasource by JNDI name: " + jndiName, e );
          throw e;
        }
      } else {
        if ( databaseMeta.isUsingConnectionPool() ) {
          String name = databaseMeta.getName();
          try {
            try {
              this.connection = dsp.getNamedDataSource( name, DatasourceType.POOLED ).getConnection();
            } catch ( UnsupportedOperationException | NullPointerException e ) {
              // UnsupportedOperationException is happen at DatabaseUtil doesn't support pooled DS, use legacy routine
              // NullPointerException is happen when we will try to run the transformation on the remote server but server does not have such databases, so will using legacy routine as well
              this.connection = ConnectionPoolUtil.getConnection( log, databaseMeta, partitionId );
            }
            if ( getConnection().getAutoCommit() != isAutoCommit() ) {
              setAutoCommit( isAutoCommit() );
            }
          } catch ( DataSourceNamingException e ) {
            log.logError( "Unable to find pooled datasource by its name: " + name, e );
            throw e;
          }
        } else {
          // using non-jndi and non-pooled connection -- just a simple JDBC
          connectUsingClass( databaseMeta.getDriverClass(), partitionId );
        }
      }

      // See if we need to execute extra SQL statement...
      String sql = environmentSubstitute( databaseMeta.getConnectSQL() );

      // only execute if the SQL is not empty, null and is not just a bunch of
      // spaces, tabs, CR etc.
      if ( !Utils.isEmpty( sql ) && !Const.onlySpaces( sql ) ) {
        execStatements( sql );
        if ( log.isDetailed() ) {
          log.logDetailed( "Executed connect time SQL statements:" + Const.CR + sql );
        }
      }
    } catch ( Exception e ) {
      throw new KettleDatabaseException( "Error occurred while trying to connect to the database", e );
    }
  }

  /**
   * Connect using the correct classname
   *
   * @param classname for example "org.gjt.mm.mysql.Driver"
   * @return true if the connect was successful, false if something went wrong.
   */
  private void connectUsingClass( String classname, String partitionId ) throws KettleDatabaseException {
    // Install and load the jdbc Driver
    PluginInterface plugin =
      PluginRegistry.getInstance().getPlugin( DatabasePluginType.class, databaseMeta.getDatabaseInterface() );

    try {
      synchronized ( java.sql.DriverManager.class ) {
        ClassLoader classLoader = PluginRegistry.getInstance().getClassLoader( plugin );
        Class<?> driverClass = classLoader.loadClass( classname );

        // Only need DelegatingDriver for drivers not from our classloader
        if ( driverClass.getClassLoader() != this.getClass().getClassLoader() ) {
          String pluginId =
            PluginRegistry.getInstance().getPluginId( DatabasePluginType.class, databaseMeta.getDatabaseInterface() );
          Set<String> registeredDriversFromPlugin = registeredDrivers.get( pluginId );
          if ( registeredDriversFromPlugin == null ) {
            registeredDriversFromPlugin = new HashSet<String>();
            registeredDrivers.put( pluginId, registeredDriversFromPlugin );
          }
          // Prevent registering multiple delegating drivers for same class, plugin
          if ( !registeredDriversFromPlugin.contains( driverClass.getCanonicalName() ) ) {
            DriverManager.registerDriver( new DelegatingDriver( (Driver) driverClass.newInstance() ) );
            registeredDriversFromPlugin.add( driverClass.getCanonicalName() );
          }
        } else {
          // Trigger static register block in driver class
          Class.forName( classname );
        }
      }
    } catch ( NoClassDefFoundError e ) {
      throw new KettleDatabaseException( BaseMessages.getString( PKG,
        "Database.Exception.UnableToFindClassMissingDriver", classname, plugin.getName() ), e );
    } catch ( ClassNotFoundException e ) {
      throw new KettleDatabaseException( BaseMessages.getString( PKG,
        "Database.Exception.UnableToFindClassMissingDriver", classname, plugin.getName() ), e );
    } catch ( Exception e ) {
      throw new KettleDatabaseException( "Exception while loading class", e );
    }

    try {
      String url;

      if ( databaseMeta.isPartitioned() && !Utils.isEmpty( partitionId ) ) {
        url = environmentSubstitute( databaseMeta.getURL( partitionId ) );
      } else {
        url = environmentSubstitute( databaseMeta.getURL() );
      }

      String clusterUsername = null;
      String clusterPassword = null;
      if ( databaseMeta.isPartitioned() && !Utils.isEmpty( partitionId ) ) {
        // Get the cluster information...
        PartitionDatabaseMeta partition = databaseMeta.getPartitionMeta( partitionId );
        if ( partition != null ) {
          clusterUsername = partition.getUsername();
          clusterPassword = Encr.decryptPasswordOptionallyEncrypted( partition.getPassword() );
        }
      }

      String username;
      String password;
      if ( !Utils.isEmpty( clusterUsername ) ) {
        username = clusterUsername;
        password = clusterPassword;
      } else {
        username = environmentSubstitute( databaseMeta.getUsername() );
        password = Encr.decryptPasswordOptionallyEncrypted( environmentSubstitute( databaseMeta.getPassword() ) );
      }

      Properties properties = databaseMeta.getConnectionProperties();

      if ( databaseMeta.supportsOptionsInURL() ) {
        if ( !Utils.isEmpty( username ) || !Utils.isEmpty( password ) ) {
          // Allow for empty username with given password, in this case username must be given with one space
          properties.put( "user", Const.NVL( username, " " ) );
          properties.put( "password", Const.NVL( password, "" ) );
          if ( databaseMeta.getDatabaseInterface() instanceof MSSQLServerNativeDatabaseMeta ) {
            // Handle MSSQL Instance name. Would rather this was handled in the dialect
            // but cannot (without refactor) get to variablespace for variable substitution from
            // a BaseDatabaseMeta subclass.
            String instance = environmentSubstitute( databaseMeta.getSQLServerInstance() );
            if ( !Utils.isEmpty( instance ) ) {
              url += ";instanceName=" + instance;
            }
          }
          connection = DriverManager.getConnection( url, properties );
        } else {
          // Perhaps the username is in the URL or no username is required...
          connection = DriverManager.getConnection( url, properties );
        }
      } else {
        if ( !Utils.isEmpty( username ) ) {
          properties.put( "user", username );
        }
        if ( !Utils.isEmpty( password ) ) {
          properties.put( "password", password );
        }

        connection = DriverManager.getConnection( url, properties );
      }
    } catch ( SQLException e ) {
      throw new KettleDatabaseException( "Error connecting to database: (using class " + classname + ")", e );
    } catch ( Throwable e ) {
      throw new KettleDatabaseException( "Error connecting to database: (using class " + classname + ")", e );
    }
  }

  /**
   * Disconnect from the database and close all open prepared statements.
   */
  public synchronized void disconnect() {
    if ( connection == null ) {
      return; // Nothing to do...
    }
    try {
      if ( connection.isClosed() ) {
        return; // Nothing to do...
      }
    } catch ( SQLException ex ) {
      // cannot do anything about this but log it
      log.logError( "Error checking closing connection:" + Const.CR + ex.getMessage() );
      log.logError( Const.getStackTracker( ex ) );
    }

    if ( pstmt != null ) {
      try {
        pstmt.close();
      } catch ( SQLException ex ) {
        // cannot do anything about this but log it
        log.logError( "Error closing statement:" + Const.CR + ex.getMessage() );
        log.logError( Const.getStackTracker( ex ) );
      }
      pstmt = null;
    }
    if ( prepStatementLookup != null ) {
      try {
        prepStatementLookup.close();
      } catch ( SQLException ex ) {
        // cannot do anything about this but log it
        log.logError( "Error closing lookup statement:" + Const.CR + ex.getMessage() );
        log.logError( Const.getStackTracker( ex ) );
      }
      prepStatementLookup = null;
    }
    if ( prepStatementInsert != null ) {
      try {
        prepStatementInsert.close();
      } catch ( SQLException ex ) {
        // cannot do anything about this but log it
        log.logError( "Error closing insert statement:" + Const.CR + ex.getMessage() );
        log.logError( Const.getStackTracker( ex ) );
      }
      prepStatementInsert = null;
    }
    if ( prepStatementUpdate != null ) {
      try {
        prepStatementUpdate.close();
      } catch ( SQLException ex ) {
        // cannot do anything about this but log it
        log.logError( "Error closing update statement:" + Const.CR + ex.getMessage() );
        log.logError( Const.getStackTracker( ex ) );
      }
      prepStatementUpdate = null;
    }
    if ( pstmt_seq != null ) {
      try {
        pstmt_seq.close();
      } catch ( SQLException ex ) {
        // cannot do anything about this but log it
        log.logError( "Error closing seq statement:" + Const.CR + ex.getMessage() );
        log.logError( Const.getStackTracker( ex ) );
      }
      pstmt_seq = null;
    }

    // See if there are other steps using this connection in a connection
    // group.
    // If so, we will hold commit & connection close until then.
    //
    if ( !Utils.isEmpty( connectionGroup ) ) {
      return;
    } else {
      if ( !isAutoCommit() ) {
        // Do we really still need this commit??
        try {
          commit();
        } catch ( KettleDatabaseException ex ) {
          // cannot do anything about this but log it
          log.logError( "Error committing:" + Const.CR + ex.getMessage() );
          log.logError( Const.getStackTracker( ex ) );
        }
      }
    }
    try {
      ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.DatabaseDisconnected.id, this );
    } catch ( KettleException e ) {
      log.logError( "Error disconnecting from database:" + Const.CR + e.getMessage() );
      log.logError( Const.getStackTracker( e ) );
    } finally {
      // Always close the connection, irrespective of what happens above...
      try {
        closeConnectionOnly();
      } catch ( KettleDatabaseException ignoredKde ) { // The only exception thrown from closeConnectionOnly()
        // cannot do anything about this but log it
        log.logError(
          "Error disconnecting from database - closeConnectionOnly failed:" + Const.CR + ignoredKde.getMessage() );
        log.logError( Const.getStackTracker( ignoredKde ) );
      }
    }
  }

  /**
   * Only for unique connections usage, typically you use disconnect() to disconnect() from the database.
   *
   * @throws KettleDatabaseException in case there is an error during connection close.
   */
  public synchronized void closeConnectionOnly() throws KettleDatabaseException {
    try {
      if ( connection != null ) {
        connection.close();
        if ( !databaseMeta.isUsingConnectionPool() ) {
          connection = null;
        }
      }

      if ( log.isDetailed() ) {
        log.logDetailed( "Connection to database closed!" );
      }
    } catch ( SQLException e ) {
      throw new KettleDatabaseException( "Error disconnecting from database '" + toString() + "'", e );
    }
  }

  /**
   * Cancel the open/running queries on the database connection
   *
   * @throws KettleDatabaseException
   */
  public void cancelQuery() throws KettleDatabaseException {
    // Canceling statements only if we're not streaming results on MySQL with
    // the v3 driver
    //
    if ( databaseMeta.isMySQLVariant()
      && databaseMeta.isStreamingResults() && getDatabaseMetaData().getDriverMajorVersion() == 3 ) {
      return;
    }

    cancelStatement( pstmt );
    cancelStatement( sel_stmt );
  }

  /**
   * Cancel an open/running SQL statement
   *
   * @param statement the statement to cancel
   * @throws KettleDatabaseException
   */
  public void cancelStatement( Statement statement ) throws KettleDatabaseException {
    try {
      if ( statement != null ) {
        statement.cancel();
      }
      if ( log.isDebug() ) {
        log.logDebug( "Statement canceled!" );
      }
    } catch ( SQLException ex ) {
      throw new KettleDatabaseException( "Error cancelling statement", ex );
    }
  }

  /**
   * Specify after how many rows a commit needs to occur when inserting or updating values.
   *
   * @param commsize The number of rows to wait before doing a commit on the connection.
   */
  public void setCommit( int commsize ) {
    commitsize = commsize;
    String onOff = ( commitsize <= 0 ? "on" : "off" );
    try {
      connection.setAutoCommit( commitsize <= 0 );
      if ( log.isDetailed() ) {
        log.logDetailed( "Auto commit " + onOff );
      }
    } catch ( Exception e ) {
      if ( log.isDebug() ) {
        log.logDebug( "Can't turn auto commit " + onOff + Const.CR + Const.getStackTracker( e ) );
      }
    }
  }

  public void setAutoCommit( boolean useAutoCommit ) throws KettleDatabaseException {
    try {
      connection.setAutoCommit( useAutoCommit );
    } catch ( SQLException e ) {
      if ( useAutoCommit ) {
        throw new KettleDatabaseException( BaseMessages.getString(
          PKG, "Database.Exception.UnableToEnableAutoCommit", toString() ) );
      } else {
        throw new KettleDatabaseException( BaseMessages.getString(
          PKG, "Database.Exception.UnableToDisableAutoCommit", toString() ) );
      }

    }
  }

  /**
   * Perform a commit the connection if this is supported by the database
   */
  public void commit() throws KettleDatabaseException {
    commit( false );
  }

  public void commit( boolean force ) throws KettleDatabaseException {
    try {
      // Don't do the commit, wait until the end of the transformation.
      // When the last database copy (opened counter) is about to be closed, we
      // do a commit
      // There is one catch, we need to catch the rollback
      // The transformation will stop everything and then we'll do the rollback.
      // The flag is in "performRollback", private only
      //
      if ( !Utils.isEmpty( connectionGroup ) && !force ) {
        return;
      }
      if ( getDatabaseMetaData().supportsTransactions() ) {
        if ( log.isDebug() ) {
          log.logDebug( "Commit on database connection [" + toString() + "]" );
        }
        connection.commit();
        nrExecutedCommits++;
      } else {
        if ( log.isDetailed() ) {
          log.logDetailed( "No commit possible on database connection [" + toString() + "]" );
        }
      }
    } catch ( Exception e ) {
      if ( databaseMeta.supportsEmptyTransactions() ) {
        throw new KettleDatabaseException( "Error comitting connection", e );
      }
    }
  }

  /**
   * This methods may be removed in future.
   *
   * @param logTable
   * @throws KettleDatabaseException
   */
  public void commitLog( LogTableCoreInterface logTable ) throws KettleDatabaseException {
    this.commitLog( false, logTable );
  }

  /**
   * This methods may be removed in future.
   *
   * @param force
   * @param logTable
   * @throws KettleDatabaseException
   */
  public void commitLog( boolean force, LogTableCoreInterface logTable ) throws KettleDatabaseException {
    try {
      commitInternal( force );
    } catch ( Exception e ) {
      DatabaseLogExceptionFactory.getExceptionStrategy( logTable )
        .registerException( log, e, PKG, "Database.Error.UnableToCommitToLogTable",
          logTable.getActualTableName() );
    }
  }

  /**
   * this is a copy of {@link #commit(boolean)} - but delegates exception handling to caller. Can be possibly be removed
   * in future.
   *
   * @param force
   * @throws KettleDatabaseException
   * @throws SQLException
   */
  @Deprecated
  private void commitInternal( boolean force ) throws KettleDatabaseException, SQLException {
    if ( !Utils.isEmpty( connectionGroup ) && !force ) {
      return;
    }
    if ( getDatabaseMetaData().supportsTransactions() ) {
      if ( log.isDebug() ) {
        log.logDebug( "Commit on database connection [" + toString() + "]" );
      }
      connection.commit();
      nrExecutedCommits++;
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( "No commit possible on database connection [" + toString() + "]" );
      }
    }
  }

  public void rollback() throws KettleDatabaseException {
    rollback( false );
  }

  public void rollback( boolean force ) throws KettleDatabaseException {
    try {
      if ( !Utils.isEmpty( connectionGroup ) && !force ) {
        return; // Will be handled by Trans --> endProcessing()
      }
      if ( getDatabaseMetaData().supportsTransactions() ) {
        if ( connection != null ) {
          if ( log.isDebug() ) {
            log.logDebug( "Rollback on database connection [" + toString() + "]" );
          }
          connection.rollback();
        }
      } else {
        if ( log.isDetailed() ) {
          log.logDetailed( "No rollback possible on database connection [" + toString() + "]" );
        }
      }

    } catch ( SQLException e ) {
      throw new KettleDatabaseException( "Error performing rollback on connection", e );
    }
  }

  /**
   * Prepare inserting values into a table, using the fields & values in a Row
   *
   * @param rowMeta The row metadata to determine which values need to be inserted
   * @param table   The name of the table in which we want to insert rows
   * @throws KettleDatabaseException if something went wrong.
   */
  public void prepareInsert( RowMetaInterface rowMeta, String tableName ) throws KettleDatabaseException {
    prepareInsert( rowMeta, null, tableName );
  }

  /**
   * Prepare inserting values into a table, using the fields & values in a Row
   *
   * @param rowMeta    The metadata row to determine which values need to be inserted
   * @param schemaName The name of the schema in which we want to insert rows
   * @param tableName  The name of the table in which we want to insert rows
   * @throws KettleDatabaseException if something went wrong.
   */
  public void prepareInsert( RowMetaInterface rowMeta, String schemaName, String tableName )
    throws KettleDatabaseException {
    if ( rowMeta.size() == 0 ) {
      throw new KettleDatabaseException( "No fields in row, can't insert!" );
    }

    String ins = getInsertStatement( schemaName, tableName, rowMeta );

    if ( log.isDetailed() ) {
      log.logDetailed( "Preparing statement: " + Const.CR + ins );
    }
    prepStatementInsert = prepareSQL( ins );
  }

  /**
   * Prepare a statement to be executed on the database. (does not return generated keys)
   *
   * @param sql The SQL to be prepared
   * @return The PreparedStatement object.
   * @throws KettleDatabaseException
   */
  public PreparedStatement prepareSQL( String sql ) throws KettleDatabaseException {
    return prepareSQL( sql, false );
  }

  /**
   * Prepare a statement to be executed on the database.
   *
   * @param sql        The SQL to be prepared
   * @param returnKeys set to true if you want to return generated keys from an insert statement
   * @return The PreparedStatement object.
   * @throws KettleDatabaseException
   */
  public PreparedStatement prepareSQL( String sql, boolean returnKeys ) throws KettleDatabaseException {
    DatabaseInterface databaseInterface = databaseMeta.getDatabaseInterface();
    boolean supportsAutoGeneratedKeys = databaseInterface.supportsAutoGeneratedKeys();

    try {
      if ( returnKeys && supportsAutoGeneratedKeys ) {
        return connection.prepareStatement( databaseMeta.stripCR( sql ), Statement.RETURN_GENERATED_KEYS );
      } else {
        return connection.prepareStatement( databaseMeta.stripCR( sql ) );
      }
    } catch ( SQLException ex ) {
      throw new KettleDatabaseException( "Couldn't prepare statement:" + Const.CR + sql, ex );
    }
  }

  public void closeLookup() throws KettleDatabaseException {
    closePreparedStatement( pstmt );
    pstmt = null;
  }

  public void closePreparedStatement( PreparedStatement ps ) throws KettleDatabaseException {
    if ( ps != null ) {
      try {
        ps.close();
      } catch ( SQLException e ) {
        throw new KettleDatabaseException( "Error closing prepared statement", e );
      }
    }
  }

  public void closeInsert() throws KettleDatabaseException {
    if ( prepStatementInsert != null ) {
      try {
        prepStatementInsert.close();
        prepStatementInsert = null;
      } catch ( SQLException e ) {
        throw new KettleDatabaseException( "Error closing insert prepared statement.", e );
      }
    }
  }

  public void closeUpdate() throws KettleDatabaseException {
    if ( prepStatementUpdate != null ) {
      try {
        prepStatementUpdate.close();
        prepStatementUpdate = null;
      } catch ( SQLException e ) {
        throw new KettleDatabaseException( "Error closing update prepared statement.", e );
      }
    }
  }

  public void setValues( RowMetaInterface rowMeta, Object[] data ) throws KettleDatabaseException {
    setValues( rowMeta, data, pstmt );
  }

  public void setValues( RowMetaAndData row ) throws KettleDatabaseException {
    setValues( row.getRowMeta(), row.getData() );
  }

  public void setValuesInsert( RowMetaInterface rowMeta, Object[] data ) throws KettleDatabaseException {
    setValues( rowMeta, data, prepStatementInsert );
  }

  public void setValuesInsert( RowMetaAndData row ) throws KettleDatabaseException {
    setValues( row.getRowMeta(), row.getData(), prepStatementInsert );
  }

  public void setValuesUpdate( RowMetaInterface rowMeta, Object[] data ) throws KettleDatabaseException {
    setValues( rowMeta, data, prepStatementUpdate );
  }

  public void setValuesLookup( RowMetaInterface rowMeta, Object[] data ) throws KettleDatabaseException {
    setValues( rowMeta, data, prepStatementLookup );
  }

  public void setProcValues( RowMetaInterface rowMeta, Object[] data, int[] argnrs, String[] argdir, boolean result )
    throws KettleDatabaseException {
    int pos;

    if ( result ) {
      pos = 2;
    } else {
      pos = 1;
    }

    for ( int i = 0; i < argnrs.length; i++ ) {
      if ( argdir[ i ].equalsIgnoreCase( "IN" ) || argdir[ i ].equalsIgnoreCase( "INOUT" ) ) {
        ValueMetaInterface valueMeta = rowMeta.getValueMeta( argnrs[ i ] );
        Object value = data[ argnrs[ i ] ];

        setValue( cstmt, valueMeta, value, pos );
        pos++;
      } else {
        pos++; // next parameter when OUT
      }
    }
  }

  public void setValue( PreparedStatement ps, ValueMetaInterface v, Object object, int pos )
    throws KettleDatabaseException {

    v.setPreparedStatementValue( databaseMeta, ps, pos, object );

  }

  public void setValues( RowMetaAndData row, PreparedStatement ps ) throws KettleDatabaseException {
    setValues( row.getRowMeta(), row.getData(), ps );
  }

  public void setValues( RowMetaInterface rowMeta, Object[] data, PreparedStatement ps )
    throws KettleDatabaseException {
    // now set the values in the row!
    for ( int i = 0; i < rowMeta.size(); i++ ) {
      ValueMetaInterface v = rowMeta.getValueMeta( i );
      Object object = data[ i ];

      try {
        setValue( ps, v, object, i + 1 );
      } catch ( KettleDatabaseException e ) {
        throw new KettleDatabaseException( "offending row : " + rowMeta, e );
      }
    }
  }

  /**
   * Sets the values of the preparedStatement pstmt.
   *
   * @param rowMeta
   * @param data
   */
  public void setValues( RowMetaInterface rowMeta, Object[] data, PreparedStatement ps, int ignoreThisValueIndex )
    throws KettleDatabaseException {
    // now set the values in the row!
    int index = 0;
    for ( int i = 0; i < rowMeta.size(); i++ ) {
      if ( i != ignoreThisValueIndex ) {
        ValueMetaInterface v = rowMeta.getValueMeta( i );
        Object object = data[ i ];

        try {
          setValue( ps, v, object, index + 1 );
          index++;
        } catch ( KettleDatabaseException e ) {
          throw new KettleDatabaseException( "offending row : " + rowMeta, e );
        }
      }
    }
  }

  /**
   * @param ps The prepared insert statement to use
   * @return The generated keys in auto-increment fields
   * @throws KettleDatabaseException in case something goes wrong retrieving the keys.
   */
  public RowMetaAndData getGeneratedKeys( PreparedStatement ps ) throws KettleDatabaseException {
    ResultSet keys = null;
    try {
      keys = ps.getGeneratedKeys(); // 1 row of keys
      ResultSetMetaData resultSetMetaData = keys.getMetaData();
      if ( resultSetMetaData == null ) {
        resultSetMetaData = ps.getMetaData();
      }
      RowMetaInterface rowMeta;
      if ( resultSetMetaData == null ) {
        rowMeta = new RowMeta();
        rowMeta.addValueMeta( new ValueMetaInteger( "ai-key" ) );
      } else {
        rowMeta = getRowInfo( resultSetMetaData, false, false );
      }

      return new RowMetaAndData( rowMeta, getRow( keys, resultSetMetaData, rowMeta ) );
    } catch ( Exception ex ) {
      throw new KettleDatabaseException( "Unable to retrieve key(s) from auto-increment field(s)", ex );
    } finally {
      if ( keys != null ) {
        try {
          keys.close();
        } catch ( SQLException e ) {
          throw new KettleDatabaseException( "Unable to close resultset of auto-generated keys", e );
        }
      }
    }
  }

  public Long getNextSequenceValue( String sequenceName, String keyfield ) throws KettleDatabaseException {
    return getNextSequenceValue( null, sequenceName, keyfield );
  }

  public Long getNextSequenceValue( String schemaName, String sequenceName, String keyfield )
    throws KettleDatabaseException {
    Long retval = null;

    String schemaSequence = databaseMeta.getQuotedSchemaTableCombination( schemaName, sequenceName );

    try {
      if ( pstmt_seq == null ) {
        pstmt_seq =
          connection.prepareStatement( databaseMeta.getSeqNextvalSQL( databaseMeta.stripCR( schemaSequence ) ) );
      }
      ResultSet rs = null;
      try {
        rs = pstmt_seq.executeQuery();
        if ( rs.next() ) {
          retval = Long.valueOf( rs.getLong( 1 ) );
        }
      } finally {
        if ( rs != null ) {
          rs.close();
        }
      }
    } catch ( SQLException ex ) {
      throw new KettleDatabaseException( "Unable to get next value for sequence : " + schemaSequence, ex );
    }

    return retval;
  }

  public void insertRow( String tableName, RowMetaInterface fields, Object[] data ) throws KettleDatabaseException {
    insertRow( null, tableName, fields, data );
  }

  public void insertRow( String schemaName, String tableName, RowMetaInterface fields, Object[] data )
    throws KettleDatabaseException {
    prepareInsert( fields, schemaName, tableName );
    setValuesInsert( fields, data );
    insertRow();
    closeInsert();
  }

  public String getInsertStatement( String tableName, RowMetaInterface fields ) {
    return getInsertStatement( null, tableName, fields );
  }

  public String getInsertStatement( String schemaName, String tableName, RowMetaInterface fields ) {
    StringBuilder ins = new StringBuilder( 128 );

    String schemaTable = databaseMeta.getQuotedSchemaTableCombination( schemaName, tableName );
    ins.append( "INSERT INTO " ).append( schemaTable ).append( " (" );

    // now add the names in the row:
    for ( int i = 0; i < fields.size(); i++ ) {
      if ( i > 0 ) {
        ins.append( ", " );
      }
      String name = fields.getValueMeta( i ).getName();
      ins.append( databaseMeta.quoteField( name ) );
    }
    ins.append( ") VALUES (" );

    // Add placeholders...
    for ( int i = 0; i < fields.size(); i++ ) {
      if ( i > 0 ) {
        ins.append( ", " );
      }
      ins.append( " ?" );
    }
    ins.append( ')' );

    return ins.toString();
  }

  public void insertRow() throws KettleDatabaseException {
    insertRow( prepStatementInsert );
  }

  public void insertRow( boolean batch ) throws KettleDatabaseException {
    insertRow( prepStatementInsert, batch );
  }

  public void updateRow() throws KettleDatabaseException {
    insertRow( prepStatementUpdate );
  }

  public void insertRow( PreparedStatement ps ) throws KettleDatabaseException {
    insertRow( ps, false );
  }

  /**
   * Insert a row into the database using a prepared statement that has all values set.
   *
   * @param ps    The prepared statement
   * @param batch True if you want to use batch inserts (size = commit size)
   * @return true if the rows are safe: if batch of rows was sent to the database OR if a commit was done.
   * @throws KettleDatabaseException
   */
  public boolean insertRow( PreparedStatement ps, boolean batch ) throws KettleDatabaseException {
    return insertRow( ps, batch, true );
  }

  public boolean getUseBatchInsert( boolean batch ) throws KettleDatabaseException {
    try {
      return batch && getDatabaseMetaData().supportsBatchUpdates() && databaseMeta.supportsBatchUpdates()
        && Utils.isEmpty( connectionGroup );
    } catch ( SQLException e ) {
      throw createKettleDatabaseBatchException( "Error determining whether to use batch", e );
    }
  }

  /**
   * Insert a row into the database using a prepared statement that has all values set.
   *
   * @param ps           The prepared statement
   * @param batch        True if you want to use batch inserts (size = commit size)
   * @param handleCommit True if you want to handle the commit here after the commit size (False e.g. in case the step
   *                     handles this, see TableOutput)
   * @return true if the rows are safe: if batch of rows was sent to the database OR if a commit was done.
   * @throws KettleDatabaseException
   */
  public boolean insertRow( PreparedStatement ps, boolean batch, boolean handleCommit ) throws KettleDatabaseException {
    String debug = "insertRow start";
    boolean rowsAreSafe = false;
    boolean isBatchUpdate = false;

    try {
      // Unique connections and Batch inserts don't mix when you want to roll
      // back on certain databases.
      // That's why we disable the batch insert in that case.
      //
      boolean useBatchInsert = getUseBatchInsert( batch );

      //
      // Add support for batch inserts...
      //
      if ( !isAutoCommit() ) {
        if ( useBatchInsert ) {
          debug = "insertRow add batch";
          ps.addBatch(); // Add the batch, but don't forget to run the batch
        } else {
          debug = "insertRow exec update";
          ps.executeUpdate();
        }
      } else {
        ps.executeUpdate();
      }

      written++;

      if ( handleCommit ) { // some steps handle the commit themselves (see e.g.
        // TableOutput step)
        if ( !isAutoCommit() && ( written % commitsize ) == 0 ) {
          if ( useBatchInsert ) {
            isBatchUpdate = true;
            debug = "insertRow executeBatch commit";
            ps.executeBatch();
            commit();
            ps.clearBatch();
          } else {
            debug = "insertRow normal commit";
            commit();
          }
          written = 0;
          rowsAreSafe = true;
        }
      }

      return rowsAreSafe;
    } catch ( BatchUpdateException ex ) {
      throw createKettleDatabaseBatchException( "Error updating batch", ex );
    } catch ( SQLException ex ) {
      if ( isBatchUpdate ) {
        throw createKettleDatabaseBatchException( "Error updating batch", ex );
      } else {
        throw new KettleDatabaseException( "Error inserting/updating row", ex );
      }
    } catch ( Exception e ) {
      // System.out.println("Unexpected exception in ["+debug+"] : "+e.getMessage());
      throw new KettleDatabaseException( "Unexpected error inserting/updating row in part [" + debug + "]", e );
    }
  }

  /**
   * Clears batch of insert prepared statement
   *
   * @throws KettleDatabaseException
   * @deprecated
   */
  @Deprecated
  public void clearInsertBatch() throws KettleDatabaseException {
    clearBatch( prepStatementInsert );
  }

  public void clearBatch( PreparedStatement preparedStatement ) throws KettleDatabaseException {
    try {
      preparedStatement.clearBatch();
    } catch ( SQLException e ) {
      throw new KettleDatabaseException( "Unable to clear batch for prepared statement", e );
    }
  }

  public void executeAndClearBatch( PreparedStatement preparedStatement ) throws KettleDatabaseException {
    try {
      if ( written > 0 && getDatabaseMetaData().supportsBatchUpdates() ) {
        preparedStatement.executeBatch();
      }

      written = 0;
      preparedStatement.clearBatch();
    } catch ( SQLException e ) {
      throw new KettleDatabaseException( "Unable to clear batch for prepared statement", e );
    }
  }

  public void insertFinished( boolean batch ) throws KettleDatabaseException {
    insertFinished( prepStatementInsert, batch );
    prepStatementInsert = null;
  }

  /**
   * Close the passed prepared statement. This object's "written" property is passed to the method that does the execute
   * and commit.
   *
   * @param ps
   * @param batch
   * @throws KettleDatabaseException
   */
  public void emptyAndCommit( PreparedStatement ps, boolean batch ) throws KettleDatabaseException {
    emptyAndCommit( ps, batch, written );
  }

  /**
   * Close the prepared statement of the insert statement.
   *
   * @param ps             The prepared statement to empty and close.
   * @param batch          true if you are using batch processing
   * @param psBatchCounter The number of rows on the batch queue
   * @throws KettleDatabaseException
   */
  public void emptyAndCommit( PreparedStatement ps, boolean batch, int batchCounter ) throws KettleDatabaseException {
    boolean isBatchUpdate = false;
    try {
      if ( ps != null ) {
        if ( !isAutoCommit() ) {
          // Execute the batch or just perform a commit.
          if ( batch && getDatabaseMetaData().supportsBatchUpdates() && batchCounter > 0 ) {
            // The problem with the batch counters is that you can't just
            // execute the current batch.
            // Certain databases have a problem if you execute the batch and if
            // there are no statements in it.
            // You can't just catch the exception either because you would have
            // to roll back on certain databases before you can then continue to
            // do anything.
            // That leaves the task of keeping track of the number of rows up to
            // our responsibility.
            isBatchUpdate = true;
            ps.executeBatch();
            commit();
            ps.clearBatch();
          } else {
            commit();
          }
        }

        // Let's not forget to close the prepared statement.
        //
        ps.close();
      }
    } catch ( BatchUpdateException ex ) {
      throw createKettleDatabaseBatchException( "Error updating batch", ex );
    } catch ( SQLException ex ) {
      if ( isBatchUpdate ) {
        throw createKettleDatabaseBatchException( "Error updating batch", ex );
      } else {
        throw new KettleDatabaseException( "Unable to empty ps and commit connection.", ex );
      }
    }
  }

  public static KettleDatabaseBatchException createKettleDatabaseBatchException( String message, SQLException ex ) {
    KettleDatabaseBatchException kdbe = new KettleDatabaseBatchException( message, ex );
    if ( ex instanceof BatchUpdateException ) {
      kdbe.setUpdateCounts( ( (BatchUpdateException) ex ).getUpdateCounts() );
    } else {
      // Null update count forces rollback of batch
      kdbe.setUpdateCounts( null );
    }
    List<Exception> exceptions = new ArrayList<Exception>();
    SQLException nextException = ex.getNextException();
    SQLException oldException = null;

    // This construction is specifically done for some JDBC drivers, these
    // drivers
    // always return the same exception on getNextException() (and thus go
    // into an infinite loop).
    // So it's not "equals" but != (comments from Sven Boden).
    while ( ( nextException != null ) && ( oldException != nextException ) ) {
      exceptions.add( nextException );
      oldException = nextException;
      nextException = nextException.getNextException();
    }
    kdbe.setExceptionsList( exceptions );
    return kdbe;
  }

  /**
   * Close the prepared statement of the insert statement.
   *
   * @param ps             The prepared statement to empty and close.
   * @param batch          true if you are using batch processing (typically true for this method)
   * @param psBatchCounter The number of rows on the batch queue
   * @throws KettleDatabaseException
   * @deprecated use emptyAndCommit() instead (pass in the number of rows left in the batch)
   */
  @Deprecated
  public void insertFinished( PreparedStatement ps, boolean batch ) throws KettleDatabaseException {
    boolean isBatchUpdate = false;
    try {
      if ( ps != null ) {
        if ( !isAutoCommit() ) {
          // Execute the batch or just perform a commit.
          if ( batch && getDatabaseMetaData().supportsBatchUpdates() ) {
            // The problem with the batch counters is that you can't just
            // execute the current batch.
            // Certain databases have a problem if you execute the batch and if
            // there are no statements in it.
            // You can't just catch the exception either because you would have
            // to roll back on certain databases before you can then continue to
            // do anything.
            // That leaves the task of keeping track of the number of rows up to
            // our responsibility.
            isBatchUpdate = true;
            ps.executeBatch();
            commit();
          } else {
            commit();
          }
        }

        // Let's not forget to close the prepared statement.
        //
        ps.close();
      }
    } catch ( BatchUpdateException ex ) {
      throw createKettleDatabaseBatchException( "Error updating batch", ex );
    } catch ( SQLException ex ) {
      if ( isBatchUpdate ) {
        throw createKettleDatabaseBatchException( "Error updating batch", ex );
      } else {
        throw new KettleDatabaseException( "Unable to commit connection after having inserted rows.", ex );
      }
    }
  }

  /**
   * Execute an SQL statement on the database connection (has to be open)
   *
   * @param sql The SQL to execute
   * @return a Result object indicating the number of lines read, deleted, inserted, updated, ...
   * @throws KettleDatabaseException in case anything goes wrong.
   */
  public Result execStatement( String sql ) throws KettleDatabaseException {
    return execStatement( sql, null, null );
  }

  public Result execStatement( String rawsql, RowMetaInterface params, Object[] data ) throws KettleDatabaseException {
    Result result = new Result();

    // Replace existing code with a class that removes comments from the raw
    // SQL.
    // The SqlCommentScrubber respects single-quoted strings, so if a
    // double-dash or a multiline comment appears
    // in a single-quoted string, it will be treated as a string instead of
    // comments.
    String sql = databaseMeta.getDatabaseInterface().createSqlScriptParser().removeComments( rawsql ).trim();
    try {
      boolean resultSet;
      int count;
      if ( params != null ) {
        PreparedStatement prep_stmt = connection.prepareStatement( databaseMeta.stripCR( sql ) );
        setValues( params, data, prep_stmt ); // set the parameters!
        resultSet = prep_stmt.execute();
        count = prep_stmt.getUpdateCount();
        prep_stmt.close();
      } else {
        String sqlStripped = databaseMeta.stripCR( sql );
        // log.logDetailed("Executing SQL Statement: ["+sqlStripped+"]");
        Statement stmt = connection.createStatement();
        resultSet = stmt.execute( sqlStripped );
        count = stmt.getUpdateCount();
        stmt.close();
      }
      String upperSql = sql.toUpperCase();
      if ( !resultSet ) {
        // if the result is a resultset, we don't do anything with it!
        // You should have called something else!
        // log.logDetailed("What to do with ResultSet??? (count="+count+")");
        if ( count > 0 ) {
          if ( upperSql.startsWith( "INSERT" ) ) {
            result.setNrLinesOutput( count );
          } else if ( upperSql.startsWith( "UPDATE" ) ) {
            result.setNrLinesUpdated( count );
          } else if ( upperSql.startsWith( "DELETE" ) ) {
            result.setNrLinesDeleted( count );
          }
        }
      }

      // See if a cache needs to be cleared...
      if ( upperSql.startsWith( "ALTER TABLE" )
        || upperSql.startsWith( "DROP TABLE" ) || upperSql.startsWith( "CREATE TABLE" ) ) {
        DBCache.getInstance().clear( databaseMeta.getName() );
      }
    } catch ( SQLException ex ) {
      throw new KettleDatabaseException( "Couldn't execute SQL: " + sql + Const.CR, ex );
    } catch ( Exception e ) {
      throw new KettleDatabaseException( "Unexpected error executing SQL: " + Const.CR, e );
    }

    return result;
  }

  /**
   * Execute a series of SQL statements, separated by ;
   * <p/>
   * We are already connected...
   * <p/>
   * Multiple statements have to be split into parts We use the ";" to separate statements...
   * <p/>
   * We keep the results in Result object from Jobs
   *
   * @param script The SQL script to be execute
   * @return A result with counts of the number or records updates, inserted, deleted or read.
   * @throws KettleDatabaseException In case an error occurs
   */
  public Result execStatements( String script ) throws KettleDatabaseException {
    return execStatements( script, null, null );
  }

  /**
   * Execute a series of SQL statements, separated by ;
   * <p/>
   * We are already connected...
   * <p/>
   * Multiple statements have to be split into parts We use the ";" to separate statements...
   * <p/>
   * We keep the results in Result object from Jobs
   *
   * @param script The SQL script to be execute
   * @param params Parameters Meta
   * @param data   Parameters value
   * @return A result with counts of the number or records updates, inserted, deleted or read.
   * @throws KettleDatabaseException In case an error occurs
   */
  public Result execStatements( String script, RowMetaInterface params, Object[] data ) throws KettleDatabaseException {
    Result result = new Result();

    SqlScriptParser sqlScriptParser = databaseMeta.getDatabaseInterface().createSqlScriptParser();
    List<String> statements = sqlScriptParser.split( script );
    int nrstats = 0;

    if ( statements != null ) {
      for ( String stat : statements ) {
        // Deleting all the single-line and multi-line comments from the string
        stat = sqlScriptParser.removeComments( stat );

        if ( !Const.onlySpaces( stat ) ) {
          String sql = Const.trim( stat );
          if ( sql.toUpperCase().startsWith( "SELECT" ) ) {
            // A Query
            if ( log.isDetailed() ) {
              log.logDetailed( "launch SELECT statement: " + Const.CR + sql );
            }

            nrstats++;
            ResultSet rs = null;
            try {
              rs = openQuery( sql, params, data );
              if ( rs != null ) {
                Object[] row = getRow( rs );
                while ( row != null ) {
                  result.setNrLinesRead( result.getNrLinesRead() + 1 );
                  if ( log.isDetailed() ) {
                    log.logDetailed( rowMeta.getString( row ) );
                  }
                  row = getRow( rs );
                }

              } else {
                if ( log.isDebug() ) {
                  log.logDebug( "Error executing query: " + Const.CR + sql );
                }
              }
            } catch ( KettleValueException e ) {
              throw new KettleDatabaseException( e ); // just pass the error
              // upwards.
            } finally {
              try {
                if ( rs != null ) {
                  rs.close();
                }
              } catch ( SQLException ex ) {
                if ( log.isDebug() ) {
                  log.logDebug( "Error closing query: " + Const.CR + sql );
                }
              }
            }
          } else {
            // any kind of statement
            if ( log.isDetailed() ) {
              log.logDetailed( "launch DDL statement: " + Const.CR + sql );
            }

            // A DDL statement
            nrstats++;
            Result res = execStatement( sql, params, data );
            result.add( res );
          }
        }
      }
    }

    if ( log.isDetailed() ) {
      log.logDetailed( nrstats + " statement" + ( nrstats == 1 ? "" : "s" ) + " executed" );
    }

    return result;
  }

  public ResultSet openQuery( String sql ) throws KettleDatabaseException {
    return openQuery( sql, null, null );
  }

  /**
   * Open a query on the database with a set of parameters stored in a Kettle Row
   *
   * @param sql    The SQL to launch with question marks (?) as placeholders for the parameters
   * @param params The parameters or null if no parameters are used.
   * @return A JDBC ResultSet
   * @throws KettleDatabaseException when something goes wrong with the query.
   * @data the parameter data to open the query with
   */
  public ResultSet openQuery( String sql, RowMetaInterface params, Object[] data ) throws KettleDatabaseException {
    return openQuery( sql, params, data, ResultSet.FETCH_FORWARD );
  }

  public ResultSet openQuery( String sql, RowMetaInterface params, Object[] data, int fetch_mode )
    throws KettleDatabaseException {
    return openQuery( sql, params, data, fetch_mode, false );
  }

  public ResultSet openQuery( String sql, RowMetaInterface params, Object[] data, int fetch_mode,
                              boolean lazyConversion ) throws KettleDatabaseException {
    ResultSet res;

    // Create a Statement
    try {
      log.snap( Metrics.METRIC_DATABASE_OPEN_QUERY_START, databaseMeta.getName() );
      if ( params != null ) {
        log.snap( Metrics.METRIC_DATABASE_PREPARE_SQL_START, databaseMeta.getName() );
        pstmt =
          connection.prepareStatement(
            databaseMeta.stripCR( sql ), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY );
        log.snap( Metrics.METRIC_DATABASE_PREPARE_SQL_STOP, databaseMeta.getName() );

        log.snap( Metrics.METRIC_DATABASE_SQL_VALUES_START, databaseMeta.getName() );
        setValues( params, data ); // set the dates etc!
        log.snap( Metrics.METRIC_DATABASE_SQL_VALUES_STOP, databaseMeta.getName() );

        if ( canWeSetFetchSize( pstmt ) ) {
          int maxRows = pstmt.getMaxRows();
          int fs = Const.FETCH_SIZE <= maxRows ? maxRows : Const.FETCH_SIZE;
          if ( databaseMeta.isMySQLVariant() ) {
            setMysqlFetchSize( pstmt, fs, maxRows );
          } else {
            pstmt.setFetchSize( fs );
          }

          pstmt.setFetchDirection( fetch_mode );
        }

        if ( rowlimit > 0 && databaseMeta.supportsSetMaxRows() ) {
          pstmt.setMaxRows( rowlimit );
        }

        log.snap( Metrics.METRIC_DATABASE_EXECUTE_SQL_START, databaseMeta.getName() );
        res = pstmt.executeQuery();
        log.snap( Metrics.METRIC_DATABASE_EXECUTE_SQL_STOP, databaseMeta.getName() );
      } else {
        log.snap( Metrics.METRIC_DATABASE_CREATE_SQL_START, databaseMeta.getName() );
        sel_stmt = connection.createStatement();
        log.snap( Metrics.METRIC_DATABASE_CREATE_SQL_STOP, databaseMeta.getName() );
        if ( canWeSetFetchSize( sel_stmt ) ) {
          int fs = Const.FETCH_SIZE <= sel_stmt.getMaxRows() ? sel_stmt.getMaxRows() : Const.FETCH_SIZE;
          if ( databaseMeta.getDatabaseInterface().isMySQLVariant()
            && databaseMeta.isStreamingResults() ) {
            sel_stmt.setFetchSize( Integer.MIN_VALUE );
          } else {
            sel_stmt.setFetchSize( fs );
          }
          sel_stmt.setFetchDirection( fetch_mode );
        }
        if ( rowlimit > 0 && databaseMeta.supportsSetMaxRows() ) {
          sel_stmt.setMaxRows( rowlimit );
        }

        log.snap( Metrics.METRIC_DATABASE_EXECUTE_SQL_START, databaseMeta.getName() );
        res = sel_stmt.executeQuery( databaseMeta.stripCR( sql ) );
        log.snap( Metrics.METRIC_DATABASE_EXECUTE_SQL_STOP, databaseMeta.getName() );
      }

      // MySQL Hack only. It seems too much for the cursor type of operation on
      // MySQL, to have another cursor opened
      // to get the length of a String field. So, on MySQL, we ingore the length
      // of Strings in result rows.
      //
      rowMeta = getRowInfo( res.getMetaData(), databaseMeta.isMySQLVariant(), lazyConversion );
    } catch ( SQLException ex ) {
      throw new KettleDatabaseException( "An error occurred executing SQL: " + Const.CR + sql, ex );
    } catch ( Exception e ) {
      throw new KettleDatabaseException( "An error occurred executing SQL:" + Const.CR + sql, e );
    } finally {
      log.snap( Metrics.METRIC_DATABASE_OPEN_QUERY_STOP, databaseMeta.getName() );
    }

    return res;
  }

  private boolean canWeSetFetchSize( Statement statement ) throws SQLException {
    return databaseMeta.isFetchSizeSupported()
      && ( statement.getMaxRows() > 0
      || databaseMeta.getDatabaseInterface() instanceof PostgreSQLDatabaseMeta
      || ( databaseMeta.isMySQLVariant() && databaseMeta.isStreamingResults() ) );
  }

  public ResultSet openQuery( PreparedStatement ps, RowMetaInterface params, Object[] data )
    throws KettleDatabaseException {
    ResultSet res;

    // Create a Statement
    try {
      log.snap( Metrics.METRIC_DATABASE_OPEN_QUERY_START, databaseMeta.getName() );

      log.snap( Metrics.METRIC_DATABASE_SQL_VALUES_START, databaseMeta.getName() );
      setValues( params, data, ps ); // set the parameters!
      log.snap( Metrics.METRIC_DATABASE_SQL_VALUES_STOP, databaseMeta.getName() );

      if ( canWeSetFetchSize( ps ) ) {
        int maxRows = ps.getMaxRows();
        int fs = Const.FETCH_SIZE <= maxRows ? maxRows : Const.FETCH_SIZE;
        // mysql have some restriction on fetch size assignment
        if ( databaseMeta.isMySQLVariant() ) {
          setMysqlFetchSize( ps, fs, maxRows );
        } else {
          // other databases seems not.
          ps.setFetchSize( fs );
        }

        ps.setFetchDirection( ResultSet.FETCH_FORWARD );
      }

      if ( rowlimit > 0 && databaseMeta.supportsSetMaxRows() ) {
        ps.setMaxRows( rowlimit );
      }

      log.snap( Metrics.METRIC_DATABASE_EXECUTE_SQL_START, databaseMeta.getName() );
      res = ps.executeQuery();
      log.snap( Metrics.METRIC_DATABASE_EXECUTE_SQL_STOP, databaseMeta.getName() );

      // MySQL Hack only. It seems too much for the cursor type of operation on
      // MySQL, to have another cursor opened
      // to get the length of a String field. So, on MySQL, we ignore the length
      // of Strings in result rows.
      //
      log.snap( Metrics.METRIC_DATABASE_GET_ROW_META_START, databaseMeta.getName() );
      rowMeta = getRowInfo( res.getMetaData(), databaseMeta.isMySQLVariant(), false );
      log.snap( Metrics.METRIC_DATABASE_GET_ROW_META_STOP, databaseMeta.getName() );
    } catch ( SQLException ex ) {
      throw new KettleDatabaseException( "ERROR executing query", ex );
    } catch ( Exception e ) {
      throw new KettleDatabaseException( "ERROR executing query", e );
    } finally {
      log.snap( Metrics.METRIC_DATABASE_OPEN_QUERY_STOP, databaseMeta.getName() );
    }

    return res;
  }

  void setMysqlFetchSize( PreparedStatement ps, int fs, int getMaxRows ) throws SQLException, KettleDatabaseException {
    if ( databaseMeta.isStreamingResults() && getDatabaseMetaData().getDriverMajorVersion() == 3 ) {
      ps.setFetchSize( Integer.MIN_VALUE );
    } else if ( fs <= getMaxRows ) {
      // PDI-11373 do not set fetch size more than max rows can returns
      ps.setFetchSize( fs );
    }
  }

  public RowMetaInterface getTableFields( String tablename ) throws KettleDatabaseException {
    return getQueryFields( databaseMeta.getSQLQueryFields( tablename ), false );
  }

  public RowMetaInterface getQueryFields( String sql, boolean param ) throws KettleDatabaseException {
    return getQueryFields( sql, param, null, null );
  }

  /**
   * See if the table specified exists by reading
   *
   * @param tablename
   *          The name of the table to check.<br>
   *          This is supposed to be the properly quoted name of the table or the complete schema-table name
   *          combination.
   * @return true if the table exists, false if it doesn't.
   */
  public boolean checkTableExists( String tablename ) throws KettleDatabaseException {
    try {
      if ( log.isDebug() ) {
        log.logDebug( "Checking if table [" + tablename + "] exists!" );
      }
      // Just try to read from the table.
      String sql = databaseMeta.getSQLTableExists( tablename );
      try {
        getOneRow( sql );
        return true;
      } catch ( KettleDatabaseException e ) {
        return false;
      }
    } catch ( Exception e ) {
      throw new KettleDatabaseException( "Unable to check if table [" + tablename + "] exists on connection [" + databaseMeta.getName() + "]", e );
    }
  }

  /**
   * See if the table specified exists by getting db metadata.
   *
   * @param tablename
   *          The name of the table to check.<br>
   *          This is supposed to be the properly quoted name of the table or the complete schema-table name
   *          combination.
   * @return true if the table exists, false if it doesn't.
   * @throws KettleDatabaseException
   */
  public boolean checkTableExistsByDbMeta( String shema, String tablename ) throws KettleDatabaseException {
    boolean isTableExist = false;
    if ( log.isDebug() ) {
      log.logDebug( BaseMessages.getString( PKG, "Database.Info.CheckingIfTableExistsInDbMetaData", tablename ) );
    }
    try ( ResultSet resTables = getTableMetaData( shema, tablename ) ) {
      while ( resTables.next() ) {
        String resTableName = resTables.getString( TABLES_META_DATA_TABLE_NAME );
        if ( tablename.equalsIgnoreCase( resTableName ) ) {
          if ( log.isDebug() ) {
            log.logDebug( BaseMessages.getString( PKG, "Database.Info.TableFound", tablename ) );
          }
          isTableExist = true;
          break;
        }
      }
    } catch ( SQLException e ) {
      throw new KettleDatabaseException( BaseMessages.getString( PKG, "Database.Error.UnableToCheckExistingTable", tablename, databaseMeta.getName() ), e );
    }
    return isTableExist;
  }

  /**
   * Retrieves the table description matching the schema and table name.
   *
   * @param shema
   *          the schema name pattern
   * @param table
   *          the table name pattern
   * @return table description row set
   * @throws KettleDatabaseException
   *           if DatabaseMetaData is null or some database error occurs
   */
  private ResultSet getTableMetaData( String schema, String table ) throws KettleDatabaseException {
    ResultSet tables = null;
    if ( getDatabaseMetaData() == null ) {
      throw new KettleDatabaseException( BaseMessages.getString( PKG, "Database.Error.UnableToGetDbMeta" ) );
    }
    try {
      tables = getDatabaseMetaData().getTables( null, schema, table, TABLE_TYPES_TO_GET );
    } catch ( SQLException e ) {
      throw new KettleDatabaseException( BaseMessages.getString( PKG, "Database.Error.UnableToGetTableNames" ), e );
    }
    if ( tables == null ) {
      throw new KettleDatabaseException( BaseMessages.getString( PKG, "Database.Error.UnableToGetTableNames" ) );
    }
    return tables;
  }

  /**
   * See if the column specified exists by reading
   *
   * @param columnname The name of the column to check.
   * @param tablename  The name of the table to check.<br> This is supposed to be the properly quoted name of the table
   *                   or the complete schema-table name combination.
   * @return true if the table exists, false if it doesn't.
   */
  public boolean checkColumnExists( String columnname, String tablename ) throws KettleDatabaseException {
    try {
      if ( log.isDebug() ) {
        log.logDebug( "Checking if column [" + columnname + "] exists in table [" + tablename + "] !" );
      }

      // Just try to read from the table.
      String sql = databaseMeta.getSQLColumnExists( columnname, tablename );

      try {
        getOneRow( sql );
        return true;
      } catch ( KettleDatabaseException e ) {
        return false;
      }
    } catch ( Exception e ) {
      throw new KettleDatabaseException( "Unable to check if column ["
        + columnname + "] exists in table [" + tablename + "] on connection [" + databaseMeta.getName() + "]", e );
    }
  }

  /**
   * Check whether the sequence exists, Oracle only!
   *
   * @param sequenceName The name of the sequence
   * @return true if the sequence exists.
   */
  public boolean checkSequenceExists( String sequenceName ) throws KettleDatabaseException {
    return checkSequenceExists( null, sequenceName );
  }

  /**
   * Check whether the sequence exists, Oracle only!
   *
   * @param sequenceName The name of the sequence
   * @return true if the sequence exists.
   */
  public boolean checkSequenceExists( String schemaName, String sequenceName ) throws KettleDatabaseException {
    boolean retval = false;

    if ( !databaseMeta.supportsSequences() ) {
      return retval;
    }

    String schemaSequence = databaseMeta.getQuotedSchemaTableCombination( schemaName, sequenceName );
    try {
      //
      // Get the info from the data dictionary...
      //
      String sql = databaseMeta.getSQLSequenceExists( schemaSequence );
      ResultSet res = openQuery( sql );
      if ( res != null ) {
        Object[] row = getRow( res );
        if ( row != null ) {
          retval = true;
        }
        closeQuery( res );
      }
    } catch ( Exception e ) {
      throw new KettleDatabaseException( "Unexpected error checking whether or not sequence ["
        + schemaSequence + "] exists", e );
    }

    return retval;
  }

  /**
   * Check if an index on certain fields in a table exists.
   *
   * @param tableName  The table on which the index is checked
   * @param idx_fields The fields on which the indexe is checked
   * @return True if the index exists
   */
  public boolean checkIndexExists( String tableName, String[] idx_fields ) throws KettleDatabaseException {
    return checkIndexExists( null, tableName, idx_fields );
  }

  /**
   * Check if an index on certain fields in a table exists.
   *
   * @param tablename  The table on which the index is checked
   * @param idx_fields The fields on which the indexe is checked
   * @return True if the index exists
   */
  public boolean checkIndexExists( String schemaName, String tableName, String[] idx_fields )
    throws KettleDatabaseException {
    String tablename = databaseMeta.getQuotedSchemaTableCombination( schemaName, tableName );
    if ( !checkTableExists( tablename ) ) {
      return false;
    }

    if ( log.isDebug() ) {
      log.logDebug( "CheckIndexExists() tablename = " + tablename + " type = " + databaseMeta.getPluginId() );
    }

    return databaseMeta.getDatabaseInterface().checkIndexExists( this, schemaName, tableName, idx_fields );
  }

  public String getCreateIndexStatement( String tablename, String indexname, String[] idx_fields, boolean tk,
                                         boolean unique, boolean bitmap, boolean semi_colon ) {
    return getCreateIndexStatement( null, tablename, indexname, idx_fields, tk, unique, bitmap, semi_colon );
  }

  public String getCreateIndexStatement( String schemaname, String tablename, String indexname,
                                         String[] idx_fields, boolean tk, boolean unique, boolean bitmap,
                                         boolean semi_colon ) {
    String cr_index = "";
    DatabaseInterface databaseInterface = databaseMeta.getDatabaseInterface();

    // Exasol does not support explicit handling of indexes
    if ( databaseInterface instanceof Exasol4DatabaseMeta ) {
      return "";
    }

    cr_index += "CREATE ";

    if ( unique || ( tk && databaseInterface instanceof SybaseDatabaseMeta ) ) {
      cr_index += "UNIQUE ";
    }

    if ( bitmap && databaseMeta.supportsBitmapIndex() ) {
      cr_index += "BITMAP ";
    }

    cr_index += "INDEX " + databaseMeta.quoteField( indexname ) + " ";
    cr_index += "ON ";
    // assume table has already been quoted (and possibly includes schema)
    cr_index += tablename;
    cr_index += "(";
    for ( int i = 0; i < idx_fields.length; i++ ) {
      if ( i > 0 ) {
        cr_index += ", ";
      }
      cr_index += databaseMeta.quoteField( idx_fields[ i ] );
    }
    cr_index += ")" + Const.CR;

    cr_index += databaseInterface.getIndexTablespaceDDL( variables, databaseMeta );

    if ( semi_colon ) {
      cr_index += ";" + Const.CR;
    }

    return cr_index;
  }

  public String getCreateSequenceStatement( String sequence, long start_at, long increment_by, long max_value,
                                            boolean semi_colon ) {
    return getCreateSequenceStatement(
      null, sequence, Long.toString( start_at ), Long.toString( increment_by ), Long.toString( max_value ),
      semi_colon );
  }

  public String getCreateSequenceStatement( String sequence, String start_at, String increment_by,
                                            String max_value, boolean semi_colon ) {
    return getCreateSequenceStatement( null, sequence, start_at, increment_by, max_value, semi_colon );
  }

  public String getCreateSequenceStatement( String schemaName, String sequence, long start_at, long increment_by,
                                            long max_value, boolean semi_colon ) {
    return getCreateSequenceStatement( schemaName, sequence, Long.toString( start_at ), Long
      .toString( increment_by ), Long.toString( max_value ), semi_colon );
  }

  public String getCreateSequenceStatement( String schemaName, String sequenceName, String start_at,
                                            String increment_by, String max_value, boolean semi_colon ) {
    String cr_seq = "";

    if ( Utils.isEmpty( sequenceName ) ) {
      return cr_seq;
    }

    if ( databaseMeta.supportsSequences() ) {
      String schemaSequence = databaseMeta.getQuotedSchemaTableCombination( schemaName, sequenceName );
      cr_seq += "CREATE SEQUENCE " + schemaSequence + " " + Const.CR; // Works
      // for
      // both
      // Oracle
      // and
      // PostgreSQL
      // :-)
      cr_seq += "START WITH " + start_at + " " + Const.CR;
      cr_seq += "INCREMENT BY " + increment_by + " " + Const.CR;
      if ( max_value != null ) {
        // "-1" means there is no maxvalue, must be handles different by DB2 /
        // AS400
        //
        if ( databaseMeta.supportsSequenceNoMaxValueOption() && max_value.trim().equals( "-1" ) ) {
          DatabaseInterface databaseInterface = databaseMeta.getDatabaseInterface();
          cr_seq += databaseInterface.getSequenceNoMaxValueOption() + Const.CR;
        } else {
          // set the max value
          cr_seq += "MAXVALUE " + max_value + Const.CR;
        }
      }

      if ( semi_colon ) {
        cr_seq += ";" + Const.CR;
      }
    }

    return cr_seq;
  }

  public RowMetaInterface getQueryFields( String sql, boolean param, RowMetaInterface inform, Object[] data )
    throws KettleDatabaseException {
    RowMetaInterface fields;
    DBCache dbcache = DBCache.getInstance();

    DBCacheEntry entry = null;

    // Check the cache first!
    //
    if ( dbcache != null ) {
      entry = new DBCacheEntry( databaseMeta.getName(), sql );
      fields = dbcache.get( entry );
      if ( fields != null ) {
        return fields;
      }
    }
    if ( connection == null ) {
      return null; // Cache test without connect.
    }

    // No cache entry found

    // The new method of retrieving the query fields fails on Oracle because
    // they failed to implement the getMetaData method on a prepared statement.
    // (!!!)
    // Even recent drivers like 10.2 fail because of it.
    //
    // There might be other databases that don't support it (we have no
    // knowledge of this at the time of writing).
    // If we discover other RDBMSs, we will create an interface for it.
    // For now, we just try to get the field layout on the re-bound in the
    // exception block below.
    //
    try {
      if ( databaseMeta.supportsPreparedStatementMetadataRetrieval() ) {
        // On with the regular program.
        //
        fields = getQueryFieldsFromPreparedStatement( sql );
      } else {
        fields = getQueryFieldsFromDatabaseMetaData();
      }
    } catch ( Exception e ) {
      /*
       * databaseMeta.getDatabaseType()==DatabaseMeta.TYPE_DATABASE_SYBASEIQ ) {
       */
      fields = getQueryFieldsFallback( sql, param, inform, data );
    }

    // Store in cache!!
    if ( dbcache != null && entry != null ) {
      if ( fields != null ) {
        dbcache.put( entry, fields );
      }
    }

    return fields;
  }

  public RowMetaInterface getQueryFieldsFromPreparedStatement( String sql ) throws Exception {
    PreparedStatement preparedStatement = null;
    try {
      preparedStatement =
          connection.prepareStatement( databaseMeta.stripCR( sql ), ResultSet.TYPE_FORWARD_ONLY,
              ResultSet.CONCUR_READ_ONLY );
      preparedStatement.setMaxRows( 1 );
      ResultSetMetaData rsmd = preparedStatement.getMetaData();
      return getRowInfo( rsmd, false, false );
    } catch ( Exception e ) {
      throw new Exception( e );
    } finally {
      if ( preparedStatement != null ) {
        try {
          preparedStatement.close();
        } catch ( SQLException e ) {
          throw new KettleDatabaseException( "Unable to close prepared statement after determining SQL layout", e );
        }
      }
    }
  }

  public RowMetaInterface getQueryFieldsFromDatabaseMetaData() throws Exception {

    ResultSet columns = connection.getMetaData().getColumns( "", "", databaseMeta.getName(), "" );
    RowMetaInterface rowMeta = new RowMeta();
    while ( columns.next() ) {
      ValueMetaInterface valueMeta = null;
      String name = columns.getString( "COLUMN_NAME" );
      String type = columns.getString( "SOURCE_DATA_TYPE" );
      int size = columns.getInt( "COLUMN_SIZE" );
      if ( type.equals( "Integer" ) || type.equals( "Long" ) ) {
        valueMeta = new ValueMetaInteger();
      } else if ( type.equals( "BigDecimal" ) || type.equals( "BigNumber" ) ) {
        valueMeta = new ValueMetaBigNumber();
      } else if ( type.equals( "Double" ) || type.equals( "Number" ) ) {
        valueMeta = new ValueMetaNumber();
      } else if ( type.equals( "String" ) ) {
        valueMeta = new ValueMetaString();
      } else if ( type.equals( "Date" ) ) {
        valueMeta = new ValueMetaDate();
      } else if ( type.equals( "Boolean" ) ) {
        valueMeta = new ValueMetaBoolean();
      } else if ( type.equals( "Binary" ) ) {
        valueMeta = new ValueMetaBinary();
      } else if ( type.equals( "Timestamp" ) ) {
        valueMeta = new ValueMetaTimestamp();
      } else if ( type.equals( "Internet Address" ) ) {
        valueMeta = new ValueMetaInternetAddress();
      }
      if ( valueMeta != null ) {
        valueMeta.setName( name );
        valueMeta.setComments( name );
        valueMeta.setLength( size );
        valueMeta.setOriginalColumnTypeName( type );

        valueMeta.setConversionMask( columns.getString( "SOURCE_MASK" ) );
        valueMeta.setDecimalSymbol( columns.getString( "SOURCE_DECIMAL_SYMBOL" ) );
        valueMeta.setGroupingSymbol( columns.getString( "SOURCE_GROUPING_SYMBOL" ) );
        valueMeta.setCurrencySymbol( columns.getString( "SOURCE_CURRENCY_SYMBOL" ) );

        rowMeta.addValueMeta( valueMeta );
      } else {
        log.logBasic( "Database.getQueryFields() ValueMetaInterface mapping not resolved for the column " + name );
        rowMeta = null;
        break;
      }
    }
    if ( rowMeta != null && !rowMeta.isEmpty() ) {
      return rowMeta;
    } else {
      throw new Exception( "Error in Database.getQueryFields()" );
    }
  }

  public RowMetaInterface getQueryFieldsFallback( String sql, boolean param, RowMetaInterface inform,
                                                   Object[] data ) throws KettleDatabaseException {
    RowMetaInterface fields;

    try {
      if ( ( inform == null
        // Hack for MSSQL jtds 1.2 when using xxx NOT IN yyy we have to use a
        // prepared statement (see BugID 3214)
        && databaseMeta.getDatabaseInterface() instanceof MSSQLServerDatabaseMeta )
        || databaseMeta.getDatabaseInterface().supportsResultSetMetadataRetrievalOnly() ) {
        sel_stmt = connection.createStatement( ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY );
        try {
          if ( databaseMeta.isFetchSizeSupported() && sel_stmt.getMaxRows() >= 1 ) {
            if ( databaseMeta.getDatabaseInterface().isMySQLVariant() ) {
              sel_stmt.setFetchSize( Integer.MIN_VALUE );
            } else {
              sel_stmt.setFetchSize( 1 );
            }
          }
          if ( databaseMeta.supportsSetMaxRows() ) {
            sel_stmt.setMaxRows( 1 );
          }

          ResultSet r = sel_stmt.executeQuery( databaseMeta.stripCR( sql ) );
          try {
            fields = getRowInfo( r.getMetaData(), false, false );
          } finally { // avoid leaking resources
            r.close();
          }
        } finally { // avoid leaking resources
          sel_stmt.close();
          sel_stmt = null;
        }
      } else {
        PreparedStatement ps = connection.prepareStatement( databaseMeta.stripCR( sql ) );
        try {
          if ( param ) {
            RowMetaInterface par = inform;

            if ( par == null || par.isEmpty() ) {
              par = getParameterMetaData( ps );
            }

            if ( par == null || par.isEmpty() ) {
              par = getParameterMetaData( sql, inform, data );
            }

            setValues( par, data, ps );
          }
          ResultSet r = ps.executeQuery();
          try {
            //
            // See PDI-14893
            // If we're in this private fallback method, it's because the databasemeta returns false for
            // supportsPreparedStatementMetadataRetrieval() or because we got an exception trying to do
            // it the other way. In either case, there is no reason for us to ever try getting the prepared
            // statement's metadata. The right answer is to directly get the resultset metadata.
            //
            // ResultSetMetaData metadata = ps.getMetaData();
            // If the PreparedStatement can't get us the metadata, try using the ResultSet's metadata
            // if ( metadata == null ) {
            //  metadata = r.getMetaData();
            // }
            ResultSetMetaData metadata = r.getMetaData();
            fields = getRowInfo( metadata, false, false );
          } finally { // should always use a try/finally to avoid leaks
            r.close();
          }
        } finally { // should always use a try/finally to avoid leaks
          ps.close();
        }
      }
    } catch ( Exception ex ) {
      throw new KettleDatabaseException( "Couldn't get field info from [" + sql + "]" + Const.CR, ex );
    }

    return fields;
  }

  public void closeQuery( ResultSet res ) throws KettleDatabaseException {
    // close everything involved in the query!
    try {
      if ( res != null ) {
        res.close();
      }
      if ( sel_stmt != null ) {
        sel_stmt.close();
        sel_stmt = null;
      }
      if ( pstmt != null ) {
        pstmt.close();
        pstmt = null;
      }
    } catch ( SQLException ex ) {
      throw new KettleDatabaseException( "Couldn't close query: resultset or prepared statements", ex );
    }
  }

  /**
   * Build the row using ResultSetMetaData rsmd
   *
   * @param rm             The resultset metadata to inquire
   * @param ignoreLength   true if you want to ignore the length (workaround for MySQL bug/problem)
   * @param lazyConversion true if lazy conversion needs to be enabled where possible
   */
  private RowMetaInterface getRowInfo( ResultSetMetaData rm, boolean ignoreLength, boolean lazyConversion )
    throws KettleDatabaseException {
    try {
      log.snap( Metrics.METRIC_DATABASE_GET_ROW_META_START, databaseMeta.getName() );

      if ( rm == null ) {
        throw new KettleDatabaseException( "No result set metadata available to retrieve row metadata!" );
      }

      RowMetaInterface rowMeta = new RowMeta();

      try {
        int nrcols = rm.getColumnCount();
        for ( int i = 1; i <= nrcols; i++ ) {
          ValueMetaInterface valueMeta = getValueFromSQLType( rm, i, ignoreLength, lazyConversion );
          rowMeta.addValueMeta( valueMeta );
        }
        return rowMeta;
      } catch ( SQLException ex ) {
        throw new KettleDatabaseException( "Error getting row information from database: ", ex );
      }
    } finally {
      log.snap( Metrics.METRIC_DATABASE_GET_ROW_META_STOP, databaseMeta.getName() );
    }
  }

  private ValueMetaInterface getValueFromSQLType( ResultSetMetaData rm, int i, boolean ignoreLength,
                                                  boolean lazyConversion )
    throws KettleDatabaseException, SQLException {
    // TODO If we do lazy conversion, we need to find out about the encoding
    //

    // Extract the name from the result set meta data...
    //
    String name;
    if ( databaseMeta.isMySQLVariant() && getDatabaseMetaData().getDriverMajorVersion() > 3 ) {
      name = new String( rm.getColumnLabel( i ) );
    } else {
      name = new String( rm.getColumnName( i ) );
    }

    // Check the name, sometimes it's empty.
    //
    if ( Utils.isEmpty( name ) || Const.onlySpaces( name ) ) {
      name = "Field" + ( i + 1 );
    }

    // Ask all the value meta types if they want to handle the SQL type.
    // The first to reply something gets the job...
    //
    ValueMetaInterface valueMeta = null;
    for ( ValueMetaInterface valueMetaClass : valueMetaPluginClasses ) {
      ValueMetaInterface v =
        valueMetaClass.getValueFromSQLType( databaseMeta, name, rm, i, ignoreLength, lazyConversion );
      if ( v != null ) {
        valueMeta = v;
        break;
      }
    }

    if ( valueMeta != null ) {
      return valueMeta;
    }

    throw new KettleDatabaseException( "Unable to handle database column '"
      + name + "', on column index " + i + " : not a handled data type" );
  }

  public boolean absolute( ResultSet rs, int position ) throws KettleDatabaseException {
    try {
      return rs.absolute( position );
    } catch ( SQLException e ) {
      throw new KettleDatabaseException( "Unable to move resultset to position " + position, e );
    }
  }

  public boolean relative( ResultSet rs, int rows ) throws KettleDatabaseException {
    try {
      return rs.relative( rows );
    } catch ( SQLException e ) {
      throw new KettleDatabaseException( "Unable to move the resultset forward " + rows + " rows", e );
    }
  }

  public void afterLast( ResultSet rs ) throws KettleDatabaseException {
    try {
      rs.afterLast();
    } catch ( SQLException e ) {
      throw new KettleDatabaseException( "Unable to move resultset to after the last position", e );
    }
  }

  public void first( ResultSet rs ) throws KettleDatabaseException {
    try {
      rs.first();
    } catch ( SQLException e ) {
      throw new KettleDatabaseException( "Unable to move resultset to the first position", e );
    }
  }

  /**
   * Get a row from the resultset. Do not use lazy conversion
   *
   * @param rs The resultset to get the row from
   * @return one row or null if no row was found on the resultset or if an error occurred.
   */
  public Object[] getRow( ResultSet rs ) throws KettleDatabaseException {
    return getRow( rs, false );
  }

  /**
   * Get a row from the resultset.
   *
   * @param rs             The resultset to get the row from
   * @param lazyConversion set to true if strings need to have lazy conversion enabled
   * @return one row or null if no row was found on the resultset or if an error occurred.
   */
  public Object[] getRow( ResultSet rs, boolean lazyConversion ) throws KettleDatabaseException {
    if ( rowMeta == null ) {
      ResultSetMetaData rsmd = null;
      try {
        rsmd = rs.getMetaData();
      } catch ( SQLException e ) {
        throw new KettleDatabaseException( "Unable to retrieve metadata from resultset", e );
      }

      rowMeta = getRowInfo( rsmd, false, lazyConversion );
    }

    return getRow( rs, null, rowMeta );
  }

  /**
   * Get a row from the resultset.
   *
   * @param rs The resultset to get the row from
   * @return one row or null if no row was found on the resultset or if an error occurred.
   */
  public Object[] getRow( ResultSet rs, ResultSetMetaData dummy, RowMetaInterface rowInfo )
    throws KettleDatabaseException {
    long startTime = System.currentTimeMillis();

    try {

      int nrcols = rowInfo.size();
      Object[] data = RowDataUtil.allocateRowData( nrcols );

      if ( rs.next() ) {
        for ( int i = 0; i < nrcols; i++ ) {
          ValueMetaInterface val = rowInfo.getValueMeta( i );

          data[ i ] = databaseMeta.getValueFromResultSet( rs, val, i );
        }
      } else {
        data = null;
      }

      return data;
    } catch ( Exception ex ) {
      throw new KettleDatabaseException( "Couldn't get row from result set", ex );
    } finally {
      if ( log.isGatheringMetrics() ) {
        long time = System.currentTimeMillis() - startTime;
        log.snap( Metrics.METRIC_DATABASE_GET_ROW_SUM_TIME, databaseMeta.getName(), time );
        log.snap( Metrics.METRIC_DATABASE_GET_ROW_MIN_TIME, databaseMeta.getName(), time );
        log.snap( Metrics.METRIC_DATABASE_GET_ROW_MAX_TIME, databaseMeta.getName(), time );
        log.snap( Metrics.METRIC_DATABASE_GET_ROW_COUNT, databaseMeta.getName() );
      }
    }
  }

  public void printSQLException( SQLException ex ) {
    log.logError( "==> SQLException: " );
    while ( ex != null ) {
      log.logError( "Message:   " + ex.getMessage() );
      log.logError( "SQLState:  " + ex.getSQLState() );
      log.logError( "ErrorCode: " + ex.getErrorCode() );
      ex = ex.getNextException();
      log.logError( "" );
    }
  }

  public void setLookup( String table, String[] codes, String[] condition, String[] gets, String[] rename,
                         String orderby ) throws KettleDatabaseException {
    setLookup( table, codes, condition, gets, rename, orderby, false );
  }

  public void setLookup( String schema, String table, String[] codes, String[] condition, String[] gets,
                         String[] rename, String orderby ) throws KettleDatabaseException {
    setLookup( schema, table, codes, condition, gets, rename, orderby, false );
  }

  public void setLookup( String tableName, String[] codes, String[] condition, String[] gets, String[] rename,
                         String orderby, boolean checkForMultipleResults ) throws KettleDatabaseException {
    setLookup( null, tableName, codes, condition, gets, rename, orderby, checkForMultipleResults );
  }

  // Lookup certain fields in a table
  public void setLookup( String schemaName, String tableName, String[] codes, String[] condition, String[] gets,
                         String[] rename, String orderby, boolean checkForMultipleResults )
    throws KettleDatabaseException {
    try {
      log.snap( Metrics.METRIC_DATABASE_SET_LOOKUP_START, databaseMeta.getName() );

      String table = databaseMeta.getQuotedSchemaTableCombination( schemaName, tableName );

      String sql = "SELECT ";

      for ( int i = 0; i < gets.length; i++ ) {
        if ( i != 0 ) {
          sql += ", ";
        }
        sql += databaseMeta.quoteField( gets[ i ] );
        if ( rename != null && rename[ i ] != null && !gets[ i ].equalsIgnoreCase( rename[ i ] ) ) {
          sql += " AS " + databaseMeta.quoteField( rename[ i ] );
        }
      }

      sql += " FROM " + table + " WHERE ";

      for ( int i = 0; i < codes.length; i++ ) {
        if ( i != 0 ) {
          sql += " AND ";
        }
        sql += databaseMeta.quoteField( codes[ i ] );
        if ( "BETWEEN".equalsIgnoreCase( condition[ i ] ) ) {
          sql += " BETWEEN ? AND ? ";
        } else if ( "IS NULL".equalsIgnoreCase( condition[ i ] ) || "IS NOT NULL".equalsIgnoreCase( condition[ i ] ) ) {
          sql += " " + condition[ i ] + " ";
        } else {
          sql += " " + condition[ i ] + " ? ";
        }
      }

      if ( orderby != null && orderby.length() != 0 ) {
        sql += " ORDER BY " + orderby;
      }

      try {
        if ( log.isDetailed() ) {
          log.logDetailed( "Setting preparedStatement to [" + sql + "]" );
        }
        prepStatementLookup = connection.prepareStatement( databaseMeta.stripCR( sql ) );
        if ( !checkForMultipleResults && databaseMeta.supportsSetMaxRows() ) {
          prepStatementLookup.setMaxRows( 1 ); // alywas get only 1 line back!
        }
      } catch ( SQLException ex ) {
        throw new KettleDatabaseException( "Unable to prepare statement for update [" + sql + "]", ex );
      }
    } finally {
      log.snap( Metrics.METRIC_DATABASE_SET_LOOKUP_STOP, databaseMeta.getName() );
    }
  }

  public boolean prepareUpdate( String table, String[] codes, String[] condition, String[] sets ) {
    return prepareUpdate( null, table, codes, condition, sets );
  }

  // Lookup certain fields in a table
  public boolean prepareUpdate( String schemaName, String tableName, String[] codes, String[] condition,
                                String[] sets ) {
    try {
      log.snap( Metrics.METRIC_DATABASE_PREPARE_UPDATE_START, databaseMeta.getName() );

      StringBuilder sql = new StringBuilder( 128 );

      String schemaTable = databaseMeta.getQuotedSchemaTableCombination( schemaName, tableName );

      sql.append( "UPDATE " ).append( schemaTable ).append( Const.CR ).append( "SET " );

      for ( int i = 0; i < sets.length; i++ ) {
        if ( i != 0 ) {
          sql.append( ",   " );
        }
        sql.append( databaseMeta.quoteField( sets[ i ] ) );
        sql.append( " = ?" ).append( Const.CR );
      }

      sql.append( "WHERE " );

      for ( int i = 0; i < codes.length; i++ ) {
        if ( i != 0 ) {
          sql.append( "AND   " );
        }
        sql.append( databaseMeta.quoteField( codes[ i ] ) );
        if ( "BETWEEN".equalsIgnoreCase( condition[ i ] ) ) {
          sql.append( " BETWEEN ? AND ? " );
        } else if ( "IS NULL".equalsIgnoreCase( condition[ i ] ) || "IS NOT NULL".equalsIgnoreCase( condition[ i ] ) ) {
          sql.append( ' ' ).append( condition[ i ] ).append( ' ' );
        } else {
          sql.append( ' ' ).append( condition[ i ] ).append( " ? " );
        }
      }

      try {
        String s = sql.toString();
        if ( log.isDetailed() ) {
          log.logDetailed( "Setting update preparedStatement to [" + s + "]" );
        }
        prepStatementUpdate = connection.prepareStatement( databaseMeta.stripCR( s ) );
      } catch ( SQLException ex ) {
        printSQLException( ex );
        return false;
      }

      return true;
    } finally {
      log.snap( Metrics.METRIC_DATABASE_PREPARE_UPDATE_STOP, databaseMeta.getName() );
    }
  }

  /**
   * Prepare a delete statement by giving it the tablename, fields and conditions to work with.
   *
   * @param table     The table-name to delete in
   * @param codes
   * @param condition
   * @return true when everything went OK, false when something went wrong.
   */
  public boolean prepareDelete( String table, String[] codes, String[] condition ) {
    return prepareDelete( null, table, codes, condition );
  }

  /**
   * Prepare a delete statement by giving it the tablename, fields and conditions to work with.
   *
   * @param schemaName the schema-name to delete in
   * @param tableName  The table-name to delete in
   * @param codes
   * @param condition
   * @return true when everything went OK, false when something went wrong.
   */
  public boolean prepareDelete( String schemaName, String tableName, String[] codes, String[] condition ) {
    try {
      log.snap( Metrics.METRIC_DATABASE_PREPARE_DELETE_START, databaseMeta.getName() );

      String sql;

      String table = databaseMeta.getQuotedSchemaTableCombination( schemaName, tableName );
      sql = "DELETE FROM " + table + Const.CR;
      sql += "WHERE ";

      for ( int i = 0; i < codes.length; i++ ) {
        if ( i != 0 ) {
          sql += "AND   ";
        }
        sql += codes[ i ];
        if ( "BETWEEN".equalsIgnoreCase( condition[ i ] ) ) {
          sql += " BETWEEN ? AND ? ";
        } else if ( "IS NULL".equalsIgnoreCase( condition[ i ] ) || "IS NOT NULL".equalsIgnoreCase( condition[ i ] ) ) {
          sql += " " + condition[ i ] + " ";
        } else {
          sql += " " + condition[ i ] + " ? ";
        }
      }

      try {
        if ( log.isDetailed() ) {
          log.logDetailed( "Setting update preparedStatement to [" + sql + "]" );
        }
        prepStatementUpdate = connection.prepareStatement( databaseMeta.stripCR( sql ) );
      } catch ( SQLException ex ) {
        printSQLException( ex );
        return false;
      }

      return true;
    } finally {
      log.snap( Metrics.METRIC_DATABASE_PREPARE_DELETE_STOP, databaseMeta.getName() );
    }
  }

  public void setProcLookup( String proc, String[] arg, String[] argdir, int[] argtype, String returnvalue,
                             int returntype ) throws KettleDatabaseException {
    try {
      log.snap( Metrics.METRIC_DATABASE_PREPARE_DBPROC_START, databaseMeta.getName() );
      String sql;
      int pos = 0;

      sql = "{ ";
      if ( returnvalue != null && returnvalue.length() != 0 ) {
        sql += "? = ";
      }
      sql += "call " + proc + " ";

      if ( arg.length > 0 ) {
        sql += "(";
      }

      for ( int i = 0; i < arg.length; i++ ) {
        if ( i != 0 ) {
          sql += ", ";
        }
        sql += " ?";
      }

      if ( arg.length > 0 ) {
        sql += ")";
      }

      sql += "}";

      try {
        if ( log.isDetailed() ) {
          log.logDetailed( "DBA setting callableStatement to [" + sql + "]" );
        }
        cstmt = connection.prepareCall( sql );
        pos = 1;
        if ( !Utils.isEmpty( returnvalue ) ) {
          switch ( returntype ) {
            case ValueMetaInterface.TYPE_NUMBER:
              cstmt.registerOutParameter( pos, java.sql.Types.DOUBLE );
              break;
            case ValueMetaInterface.TYPE_BIGNUMBER:
              cstmt.registerOutParameter( pos, java.sql.Types.DECIMAL );
              break;
            case ValueMetaInterface.TYPE_INTEGER:
              cstmt.registerOutParameter( pos, java.sql.Types.BIGINT );
              break;
            case ValueMetaInterface.TYPE_STRING:
              cstmt.registerOutParameter( pos, java.sql.Types.VARCHAR );
              break;
            case ValueMetaInterface.TYPE_DATE:
              cstmt.registerOutParameter( pos, java.sql.Types.TIMESTAMP );
              break;
            case ValueMetaInterface.TYPE_BOOLEAN:
              cstmt.registerOutParameter( pos, java.sql.Types.BOOLEAN );
              break;
            default:
              break;
          }
          pos++;
        }
        for ( int i = 0; i < arg.length; i++ ) {
          if ( argdir[ i ].equalsIgnoreCase( "OUT" ) || argdir[ i ].equalsIgnoreCase( "INOUT" ) ) {
            switch ( argtype[ i ] ) {
              case ValueMetaInterface.TYPE_NUMBER:
                cstmt.registerOutParameter( i + pos, java.sql.Types.DOUBLE );
                break;
              case ValueMetaInterface.TYPE_BIGNUMBER:
                cstmt.registerOutParameter( i + pos, java.sql.Types.DECIMAL );
                break;
              case ValueMetaInterface.TYPE_INTEGER:
                cstmt.registerOutParameter( i + pos, java.sql.Types.BIGINT );
                break;
              case ValueMetaInterface.TYPE_STRING:
                cstmt.registerOutParameter( i + pos, java.sql.Types.VARCHAR );
                break;
              case ValueMetaInterface.TYPE_DATE:
                cstmt.registerOutParameter( i + pos, java.sql.Types.TIMESTAMP );
                break;
              case ValueMetaInterface.TYPE_BOOLEAN:
                cstmt.registerOutParameter( i + pos, java.sql.Types.BOOLEAN );
                break;
              default:
                break;
            }
          }
        }
      } catch ( SQLException ex ) {
        throw new KettleDatabaseException( "Unable to prepare database procedure call", ex );
      }
    } finally {
      log.snap( Metrics.METRIC_DATABASE_PREPARE_DBPROC_STOP, databaseMeta.getName() );
    }

  }

  public Object[] getLookup() throws KettleDatabaseException {
    return getLookup( prepStatementLookup, false );
  }

  public Object[] getLookup( boolean failOnMultipleResults ) throws KettleDatabaseException {
    return getLookup( failOnMultipleResults, false );
  }

  public Object[] getLookup( boolean failOnMultipleResults, boolean lazyConversion ) throws KettleDatabaseException {
    return getLookup( prepStatementLookup, failOnMultipleResults, lazyConversion );
  }

  public Object[] getLookup( PreparedStatement ps ) throws KettleDatabaseException {
    // we assume this is external PreparedStatement and we may need to re-create rowMeta
    // so we just reset it to null and it will be re-created on processRow call
    rowMeta = null;
    return getLookup( ps, false );
  }

  public Object[] getLookup( PreparedStatement ps, boolean failOnMultipleResults ) throws KettleDatabaseException {
    return getLookup( ps, failOnMultipleResults, false );
  }

  public Object[] getLookup( PreparedStatement ps, boolean failOnMultipleResults, boolean lazyConversion )
    throws KettleDatabaseException {
    ResultSet res = null;
    try {
      log.snap( Metrics.METRIC_DATABASE_GET_LOOKUP_START, databaseMeta.getName() );
      res = ps.executeQuery();

      Object[] ret = getRow( res, lazyConversion );

      if ( failOnMultipleResults ) {
        if ( ret != null && res.next() ) {
          // if the previous row was null, there's no reason to try res.next()
          // again.
          // on DB2 this will even cause an exception (because of the buggy DB2
          // JDBC driver).
          throw new KettleDatabaseException(
            "Only 1 row was expected as a result of a lookup, and at least 2 were found!" );
        }
      }
      return ret;
    } catch ( SQLException ex ) {
      throw new KettleDatabaseException( "Error looking up row in database", ex );
    } finally {
      try {
        if ( res != null ) {
          res.close(); // close resultset!
        }
      } catch ( SQLException e ) {
        throw new KettleDatabaseException( "Unable to close resultset after looking up data", e );
      } finally {
        log.snap( Metrics.METRIC_DATABASE_GET_LOOKUP_STOP, databaseMeta.getName() );
      }
    }
  }

  public DatabaseMetaData getDatabaseMetaData() throws KettleDatabaseException {
    if ( dbmd == null ) {
      try {
        log.snap( Metrics.METRIC_DATABASE_GET_DBMETA_START, databaseMeta.getName() );
        dbmd = connection.getMetaData(); // Only get the metadata once!
      } catch ( Exception e ) {
        throw new KettleDatabaseException( "Unable to get database metadata from this database connection", e );
      } finally {
        log.snap( Metrics.METRIC_DATABASE_GET_DBMETA_STOP, databaseMeta.getName() );
      }
    }
    return dbmd;
  }

  public String getDDL( String tablename, RowMetaInterface fields ) throws KettleDatabaseException {
    return getDDL( tablename, fields, null, false, null, true );
  }

  public String getDDL( String tablename, RowMetaInterface fields, String tk, boolean use_autoinc, String pk )
    throws KettleDatabaseException {
    return getDDL( tablename, fields, tk, use_autoinc, pk, true );
  }

  public String getDDL( String tableName, RowMetaInterface fields, String tk, boolean use_autoinc, String pk,
                        boolean semicolon ) throws KettleDatabaseException {
    String retval;

    // First, check for reserved SQL in the input row r...
    databaseMeta.quoteReservedWords( fields );
    String quotedTk = tk != null ? databaseMeta.quoteField( tk ) : null;

    if ( checkTableExists( tableName ) ) {
      retval = getAlterTableStatement( tableName, fields, quotedTk, use_autoinc, pk, semicolon );
    } else {
      retval = getCreateTableStatement( tableName, fields, quotedTk, use_autoinc, pk, semicolon );
    }

    return retval;
  }

  /**
   * Generates SQL
   *
   * @param tableName   the table name or schema/table combination: this needs to be quoted properly in advance.
   * @param fields      the fields
   * @param tk          the name of the technical key field
   * @param use_autoinc true if we need to use auto-increment fields for a primary key
   * @param pk          the name of the primary/technical key field
   * @param semicolon   append semicolon to the statement
   * @return the SQL needed to create the specified table and fields.
   */
  public String getCreateTableStatement( String tableName, RowMetaInterface fields, String tk,
                                         boolean use_autoinc, String pk, boolean semicolon ) {
    StringBuilder retval = new StringBuilder();
    DatabaseInterface databaseInterface = databaseMeta.getDatabaseInterface();
    retval.append( databaseInterface.getCreateTableStatement() );

    retval.append( tableName + Const.CR );
    retval.append( "(" ).append( Const.CR );
    for ( int i = 0; i < fields.size(); i++ ) {
      if ( i > 0 ) {
        retval.append( ", " );
      } else {
        retval.append( "  " );
      }

      ValueMetaInterface v = fields.getValueMeta( i );
      retval.append( databaseMeta.getFieldDefinition( v, tk, pk, use_autoinc ) );
    }
    // At the end, before the closing of the statement, we might need to add
    // some constraints...
    // Technical keys
    if ( tk != null ) {
      if ( databaseMeta.requiresCreateTablePrimaryKeyAppend() ) {
        retval.append( ", PRIMARY KEY (" ).append( tk ).append( ")" ).append( Const.CR );
      }
    }

    // Primary keys
    if ( pk != null ) {
      if ( databaseMeta.requiresCreateTablePrimaryKeyAppend() ) {
        retval.append( ", PRIMARY KEY (" ).append( pk ).append( ")" ).append( Const.CR );
      }
    }
    retval.append( ")" ).append( Const.CR );

    retval.append( databaseMeta.getDatabaseInterface().getDataTablespaceDDL( variables, databaseMeta ) );

    if ( pk == null && tk == null && databaseMeta.getDatabaseInterface() instanceof NeoviewDatabaseMeta ) {
      retval.append( "NO PARTITION" ); // use this as a default when no pk/tk is
      // there, otherwise you get an error
    }

    if ( semicolon ) {
      retval.append( ";" );
    }

    return retval.toString();
  }

  public String getAlterTableStatement( String tableName, RowMetaInterface fields, String tk, boolean use_autoinc,
                                        String pk, boolean semicolon ) throws KettleDatabaseException {
    String retval = "";

    // Get the fields that are in the table now:
    RowMetaInterface tabFields = getTableFields( tableName );

    // Don't forget to quote these as well...
    databaseMeta.quoteReservedWords( tabFields );

    // Find the missing fields
    RowMetaInterface missing = new RowMeta();
    for ( int i = 0; i < fields.size(); i++ ) {
      ValueMetaInterface v = fields.getValueMeta( i );
      // Not found?
      if ( tabFields.searchValueMeta( v.getName() ) == null ) {
        missing.addValueMeta( v ); // nope --> Missing!
      }
    }

    if ( missing.size() != 0 ) {
      for ( int i = 0; i < missing.size(); i++ ) {
        ValueMetaInterface v = missing.getValueMeta( i );
        retval += databaseMeta.getAddColumnStatement( tableName, v, tk, use_autoinc, pk, true );
      }
    }

    // Find the surplus fields
    RowMetaInterface surplus = new RowMeta();
    for ( int i = 0; i < tabFields.size(); i++ ) {
      ValueMetaInterface v = tabFields.getValueMeta( i );
      // Found in table, not in input ?
      if ( fields.searchValueMeta( v.getName() ) == null ) {
        surplus.addValueMeta( v ); // yes --> surplus!
      }
    }

    if ( surplus.size() != 0 ) {
      for ( int i = 0; i < surplus.size(); i++ ) {
        ValueMetaInterface v = surplus.getValueMeta( i );
        retval += databaseMeta.getDropColumnStatement( tableName, v, tk, use_autoinc, pk, true );
      }
    }

    //
    // OK, see if there are fields for which we need to modify the type...
    // (length, precision)
    //
    RowMetaInterface modify = new RowMeta();
    for ( int i = 0; i < fields.size(); i++ ) {
      ValueMetaInterface desiredField = fields.getValueMeta( i );
      ValueMetaInterface currentField = tabFields.searchValueMeta( desiredField.getName() );
      if ( desiredField != null && currentField != null ) {
        String desiredDDL = databaseMeta.getFieldDefinition( desiredField, tk, pk, use_autoinc );
        String currentDDL = databaseMeta.getFieldDefinition( currentField, tk, pk, use_autoinc );

        boolean mod = !desiredDDL.equalsIgnoreCase( currentDDL );
        if ( mod ) {
          modify.addValueMeta( desiredField );
        }
      }
    }

    if ( modify.size() > 0 ) {
      for ( int i = 0; i < modify.size(); i++ ) {
        ValueMetaInterface v = modify.getValueMeta( i );
        retval += databaseMeta.getModifyColumnStatement( tableName, v, tk, use_autoinc, pk, true );
      }
    }

    return retval;
  }

  public void truncateTable( String tablename ) throws KettleDatabaseException {
    if ( Utils.isEmpty( connectionGroup ) ) {
      String truncateStatement = databaseMeta.getTruncateTableStatement( null, tablename );
      if ( truncateStatement == null ) {
        throw new KettleDatabaseException( "Truncate table not supported by "
          + databaseMeta.getDatabaseInterface().getPluginName() );
      }
      execStatement( truncateStatement );
    } else {
      execStatement( "DELETE FROM " + databaseMeta.quoteField( tablename ) );
    }
  }

  public void truncateTable( String schema, String tablename ) throws KettleDatabaseException {
    if ( Utils.isEmpty( connectionGroup ) ) {
      String truncateStatement = databaseMeta.getTruncateTableStatement( schema, tablename );
      if ( truncateStatement == null ) {
        throw new KettleDatabaseException( "Truncate table not supported by "
          + databaseMeta.getDatabaseInterface().getPluginName() );
      }
      execStatement( truncateStatement );
    } else {
      execStatement( "DELETE FROM " + databaseMeta.getQuotedSchemaTableCombination( schema, tablename ) );
    }
  }

  /**
   * Execute a query and return at most one row from the resultset
   *
   * @param sql The SQL for the query
   * @return one Row with data or null if nothing was found.
   */
  public RowMetaAndData getOneRow( String sql ) throws KettleDatabaseException {
    ResultSet rs = openQuery( sql );
    if ( rs != null ) {
      Object[] row = getRow( rs ); // One row only;

      try {
        rs.close();
      } catch ( Exception e ) {
        throw new KettleDatabaseException( "Unable to close resultset", e );
      }

      if ( pstmt != null ) {
        try {
          pstmt.close();
        } catch ( Exception e ) {
          throw new KettleDatabaseException( "Unable to close prepared statement pstmt", e );
        }
        pstmt = null;
      }
      if ( sel_stmt != null ) {
        try {
          sel_stmt.close();
        } catch ( Exception e ) {
          throw new KettleDatabaseException( "Unable to close prepared statement sel_stmt", e );
        }
        sel_stmt = null;
      }
      return new RowMetaAndData( rowMeta, row );
    } else {
      throw new KettleDatabaseException( "error opening resultset for query: " + sql );
    }
  }

  public RowMeta getMetaFromRow( Object[] row, ResultSetMetaData md ) throws SQLException, KettleDatabaseException {
    RowMeta meta = new RowMeta();

    for ( int i = 0; i < md.getColumnCount(); i++ ) {
      ValueMetaInterface valueMeta = getValueFromSQLType( md, i + 1, true, false );
      meta.addValueMeta( valueMeta );
    }

    return meta;
  }

  public RowMetaAndData getOneRow( String sql, RowMetaInterface param, Object[] data ) throws KettleDatabaseException {
    ResultSet rs = openQuery( sql, param, data );
    if ( rs != null ) {
      Object[] row = getRow( rs ); // One value: a number;

      rowMeta = null;
      RowMeta tmpMeta = null;
      try {

        ResultSetMetaData md = rs.getMetaData();
        tmpMeta = getMetaFromRow( row, md );

      } catch ( Exception e ) {
        e.printStackTrace();
      } finally {
        try {
          rs.close();
        } catch ( Exception e ) {
          throw new KettleDatabaseException( "Unable to close resultset", e );
        }

        if ( pstmt != null ) {
          try {
            pstmt.close();
          } catch ( Exception e ) {
            throw new KettleDatabaseException( "Unable to close prepared statement pstmt", e );
          }
          pstmt = null;
        }
        if ( sel_stmt != null ) {
          try {
            sel_stmt.close();
          } catch ( Exception e ) {
            throw new KettleDatabaseException( "Unable to close prepared statement sel_stmt", e );
          }
          sel_stmt = null;
        }

      }

      return new RowMetaAndData( tmpMeta, row );
    } else {
      return null;
    }
  }

  public RowMetaInterface getParameterMetaData( PreparedStatement ps ) {
    RowMetaInterface par = new RowMeta();
    try {
      ParameterMetaData pmd = ps.getParameterMetaData();
      for ( int i = 1; i <= pmd.getParameterCount(); i++ ) {
        String name = "par" + i;
        int sqltype = pmd.getParameterType( i );
        int length = pmd.getPrecision( i );
        int precision = pmd.getScale( i );
        ValueMetaInterface val;

        switch ( sqltype ) {
          case java.sql.Types.CHAR:
          case java.sql.Types.VARCHAR:
            val = new ValueMetaString( name );
            break;
          case java.sql.Types.BIGINT:
          case java.sql.Types.INTEGER:
          case java.sql.Types.NUMERIC:
          case java.sql.Types.SMALLINT:
          case java.sql.Types.TINYINT:
            val = new ValueMetaInteger( name );
            break;
          case java.sql.Types.DECIMAL:
          case java.sql.Types.DOUBLE:
          case java.sql.Types.FLOAT:
          case java.sql.Types.REAL:
            val = new ValueMetaNumber( name );
            break;
          case java.sql.Types.DATE:
          case java.sql.Types.TIME:
          case java.sql.Types.TIMESTAMP:
            val = new ValueMetaDate( name );
            break;
          case java.sql.Types.BOOLEAN:
          case java.sql.Types.BIT:
            val = new ValueMetaBoolean( name );
            break;
          default:
            val = new ValueMetaNone( name );
            break;
        }

        if ( val.isNumeric() && ( length > 18 || precision > 18 ) ) {
          val = new ValueMetaBigNumber( name );
        }

        par.addValueMeta( val );
      }
    } catch ( AbstractMethodError e ) {
      // Oops: probably the database or JDBC doesn't support it.
      return null;
    } catch ( SQLException e ) {
      return null;
    } catch ( Exception e ) {
      return null;
    }

    return par;
  }

  public int countParameters( String sql ) {
    int q = 0;
    boolean quote_opened = false;
    boolean dquote_opened = false;

    for ( int x = 0; x < sql.length(); x++ ) {
      char c = sql.charAt( x );

      switch ( c ) {
        case '\'':
          quote_opened = !quote_opened;
          break;
        case '"':
          dquote_opened = !dquote_opened;
          break;
        case '?':
          if ( !quote_opened && !dquote_opened ) {
            q++;
          }
          break;
        default:
          break;
      }
    }

    return q;
  }

  // Get the fields back from an SQL query
  public RowMetaInterface getParameterMetaData( String sql, RowMetaInterface inform, Object[] data ) {
    // The database couldn't handle it: try manually!
    int q = countParameters( sql );

    RowMetaInterface par = new RowMeta();

    if ( inform != null && q == inform.size() ) {
      for ( int i = 0; i < q; i++ ) {
        ValueMetaInterface inf = inform.getValueMeta( i );
        ValueMetaInterface v = inf.clone();
        par.addValueMeta( v );
      }
    } else {
      for ( int i = 0; i < q; i++ ) {
        ValueMetaInterface v = new ValueMetaNumber( "name" + i );
        par.addValueMeta( v );
      }
    }

    return par;
  }

  public void writeLogRecord( LogTableCoreInterface logTable, LogStatus status, Object subject, Object parent )
    throws KettleDatabaseException {
    try {
      RowMetaAndData logRecord = logTable.getLogRecord( status, subject, parent );
      if ( logRecord == null ) {
        return;
      }

      boolean update = ( logTable.getKeyField() != null ) && !status.equals( LogStatus.START );
      String schemaTable =
        databaseMeta.getQuotedSchemaTableCombination(
          environmentSubstitute( logTable.getActualSchemaName() ), environmentSubstitute( logTable
            .getActualTableName() ) );
      RowMetaInterface rowMeta = logRecord.getRowMeta();
      Object[] rowData = logRecord.getData();

      if ( update ) {
        RowMetaInterface updateRowMeta = new RowMeta();
        Object[] updateRowData = new Object[ rowMeta.size() ];
        ValueMetaInterface keyValueMeta = rowMeta.getValueMeta( 0 );
        StringBuilder sqlBuff = new StringBuilder( 250 );
        sqlBuff.append( "UPDATE " ).append( schemaTable ).append( " SET " );

        for ( int i = 1; i < rowMeta.size(); i++ ) { // Without ID_JOB or ID_BATCH
          ValueMetaInterface valueMeta = rowMeta.getValueMeta( i );
          if ( i > 1 ) {
            sqlBuff.append( ", " );
          }
          sqlBuff.append( databaseMeta.quoteField( valueMeta.getName() ) ).append( "=? " );

          updateRowMeta.addValueMeta( valueMeta );
          updateRowData[ i - 1 ] = rowData[ i ];
        }
        sqlBuff.append( "WHERE " ).append( databaseMeta.quoteField( keyValueMeta.getName() ) ).append( "=? " );

        updateRowMeta.addValueMeta( keyValueMeta );
        updateRowData[ rowMeta.size() - 1 ] = rowData[ 0 ];

        String sql = sqlBuff.toString();
        execStatement( sql, updateRowMeta, updateRowData );

      } else {

        insertRow( environmentSubstitute( logTable.getActualSchemaName() ), environmentSubstitute( logTable
          .getActualTableName() ), logRecord.getRowMeta(), logRecord.getData() );

      }
    } catch ( Exception e ) {
      DatabaseLogExceptionFactory.getExceptionStrategy( logTable, e )
        .registerException( log, e, PKG, "Database.Error.WriteLogTable",
          environmentSubstitute( logTable.getActualTableName() ) );
    }
  }

  public void cleanupLogRecords( LogTableCoreInterface logTable ) throws KettleDatabaseException {
    double timeout = Const.toDouble( Const.trim( environmentSubstitute( logTable.getTimeoutInDays() ) ), 0.0 );
    if ( timeout < 0.000001 ) {
      // The timeout has to be at least a few seconds, otherwise we don't
      // bother
      return;
    }

    String schemaTable =
      databaseMeta.getQuotedSchemaTableCombination( environmentSubstitute( logTable.getActualSchemaName() ),
        environmentSubstitute( logTable.getActualTableName() ) );

    if ( schemaTable.isEmpty() ) {
      //we can't process without table name
      DatabaseLogExceptionFactory.getExceptionStrategy( logTable )
        .registerException( log, PKG, "DatabaseMeta.Error.LogTableNameNotFound" );
    }

    LogTableField logField = logTable.getLogDateField();
    if ( logField == null ) {
      //can't stand without logField
      DatabaseLogExceptionFactory.getExceptionStrategy( logTable )
        .registerException( log, PKG, "Database.Exception.LogTimeoutDefinedOnTableWithoutLogField" );
    }

    String sql =
      "DELETE FROM " + schemaTable + " WHERE " + databaseMeta.quoteField( logField.getFieldName() ) + " < ?";
    long now = System.currentTimeMillis();
    long limit = now - Math.round( timeout * 24 * 60 * 60 * 1000 );
    RowMetaAndData row = new RowMetaAndData();
    row.addValue( logField.getFieldName(), ValueMetaInterface.TYPE_DATE, new Date( limit ) );

    try {
      //fire database
      execStatement( sql, row.getRowMeta(), row.getData() );
    } catch ( Exception e ) {
      DatabaseLogExceptionFactory.getExceptionStrategy( logTable )
        .registerException( log, PKG, "Database.Exception.UnableToCleanUpOlderRecordsFromLogTable",
          environmentSubstitute( logTable.getActualTableName() ) );
    }
  }

  public Object[] getLastLogDate( String logtable, String name, boolean job, LogStatus status )
    throws KettleDatabaseException {
    Object[] row = null;

    String jobtrans = job ? databaseMeta.quoteField( "JOBNAME" ) : databaseMeta.quoteField( "TRANSNAME" );

    String sql = "";
    sql +=
      " SELECT "
        + databaseMeta.quoteField( "ENDDATE" ) + ", " + databaseMeta.quoteField( "DEPDATE" ) + ", "
        + databaseMeta.quoteField( "STARTDATE" );
    sql += " FROM " + logtable;
    sql += " WHERE  " + databaseMeta.quoteField( "ERRORS" ) + "    = 0";
    sql += " AND    " + databaseMeta.quoteField( "STATUS" ) + "    = 'end'";
    sql += " AND    " + jobtrans + " = ?";
    sql +=
      " ORDER BY "
        + databaseMeta.quoteField( "LOGDATE" ) + " DESC, " + databaseMeta.quoteField( "ENDDATE" ) + " DESC";

    try {
      pstmt = connection.prepareStatement( databaseMeta.stripCR( sql ) );

      RowMetaInterface r = new RowMeta();
      r.addValueMeta( new ValueMetaString( "TRANSNAME" ) );
      setValues( r, new Object[] { name } );

      ResultSet res = pstmt.executeQuery();
      if ( res != null ) {
        rowMeta = getRowInfo( res.getMetaData(), false, false );
        row = getRow( res );
        res.close();
      }
      pstmt.close();
      pstmt = null;
    } catch ( SQLException ex ) {
      throw new KettleDatabaseException( "Unable to obtain last logdate from table " + logtable, ex );
    }

    return row;
  }

  public synchronized Long getNextValue( Hashtable<String, Counter> counters, String tableName, String val_key )
    throws KettleDatabaseException {
    return getNextValue( counters, null, tableName, val_key );
  }

  public synchronized Long getNextValue( Hashtable<String, Counter> counters, String schemaName, String tableName,
                                         String val_key ) throws KettleDatabaseException {
    Long nextValue = null;

    String schemaTable = databaseMeta.getQuotedSchemaTableCombination( schemaName, tableName );

    String lookup = schemaTable + "." + databaseMeta.quoteField( val_key );

    // Try to find the previous sequence value...
    Counter counter = null;
    if ( counters != null ) {
      counter = counters.get( lookup );
    }

    if ( counter == null ) {
      RowMetaAndData rmad =
        getOneRow( "SELECT MAX(" + databaseMeta.quoteField( val_key ) + ") FROM " + schemaTable );
      if ( rmad != null ) {
        long previous;
        try {
          Long tmp = rmad.getRowMeta().getInteger( rmad.getData(), 0 );

          // A "select max(x)" on a table with no matching rows will return
          // null.
          if ( tmp != null ) {
            previous = tmp.longValue();
          } else {
            previous = 0L;
          }
        } catch ( KettleValueException e ) {
          throw new KettleDatabaseException(
            "Error getting the first long value from the max value returned from table : " + schemaTable );
        }
        counter = new Counter( previous + 1, 1 );
        nextValue = Long.valueOf( counter.next() );
        if ( counters != null ) {
          counters.put( lookup, counter );
        }
      } else {
        throw new KettleDatabaseException( "Couldn't find maximum key value from table " + schemaTable );
      }
    } else {
      nextValue = Long.valueOf( counter.next() );
    }

    return nextValue;
  }

  @Override
  public String toString() {
    if ( databaseMeta != null ) {
      return databaseMeta.getName();
    } else {
      return "-";
    }
  }

  public boolean isSystemTable( String table_name ) {
    return databaseMeta.isSystemTable( table_name );
  }

  /**
   * Reads the result of an SQL query into an ArrayList
   *
   * @param sql   The SQL to launch
   * @param limit <=0 means unlimited, otherwise this specifies the maximum number of rows read.
   * @return An ArrayList of rows.
   * @throws KettleDatabaseException if something goes wrong.
   */
  public List<Object[]> getRows( String sql, int limit ) throws KettleDatabaseException {
    return getRows( sql, limit, null );
  }

  /**
   * Reads the result of an SQL query into an ArrayList
   *
   * @param sql     The SQL to launch
   * @param limit   <=0 means unlimited, otherwise this specifies the maximum number of rows read.
   * @param monitor The progress monitor to update while getting the rows.
   * @return An ArrayList of rows.
   * @throws KettleDatabaseException if something goes wrong.
   */
  public List<Object[]> getRows( String sql, int limit, ProgressMonitorListener monitor )
    throws KettleDatabaseException {

    return getRows( sql, null, null, ResultSet.FETCH_FORWARD, false, limit, monitor );
  }

  /**
   * Reads the result of an SQL query into an ArrayList.
   *
   * @param sql            The SQL to launch
   * @param params         The types of any parameters to be passed to the query
   * @param data           The values of any parameters to be passed to the query
   * @param fetch_mode     The fetch mode for the query (ResultSet.FETCH_FORWARD, e.g.)
   * @param lazyConversion Whether to perform lazy conversion of the values
   * @param limit          <=0 means unlimited, otherwise this specifies the maximum number of rows read.
   * @param monitor        The progress monitor to update while getting the rows.
   * @return An ArrayList of rows.
   * @throws KettleDatabaseException if something goes wrong.
   */
  public List<Object[]> getRows( String sql, RowMetaInterface params, Object[] data, int fetch_mode,
                                 boolean lazyConversion, int limit, ProgressMonitorListener monitor )
    throws KettleDatabaseException {
    if ( monitor != null ) {
      monitor.setTaskName( "Opening query..." );
    }
    ResultSet rset = openQuery( sql, params, data, fetch_mode, lazyConversion );

    return getRows( rset, limit, monitor );
  }

  /**
   * Reads the result of a ResultSet into an ArrayList
   *
   * @param rset    the ResultSet to read out
   * @param limit   <=0 means unlimited, otherwise this specifies the maximum number of rows read.
   * @param monitor The progress monitor to update while getting the rows.
   * @return An ArrayList of rows.
   * @throws KettleDatabaseException if something goes wrong.
   */
  public List<Object[]> getRows( ResultSet rset, int limit, ProgressMonitorListener monitor )
    throws KettleDatabaseException {
    try {
      List<Object[]> result = new ArrayList<Object[]>();
      boolean stop = false;
      int i = 0;

      if ( rset != null ) {
        if ( monitor != null && limit > 0 ) {
          monitor.beginTask( "Reading rows...", limit );
        }
        while ( ( limit <= 0 || i < limit ) && !stop ) {
          Object[] row = getRow( rset );
          if ( row != null ) {
            result.add( row );
            i++;
          } else {
            stop = true;
          }
          if ( monitor != null && limit > 0 ) {
            monitor.worked( 1 );
          }
          if ( monitor != null && monitor.isCanceled() ) {
            break;
          }
        }
        closeQuery( rset );
        if ( monitor != null ) {
          monitor.done();
        }
      }

      return result;
    } catch ( Exception e ) {
      throw new KettleDatabaseException( "Unable to get list of rows from ResultSet : ", e );
    }
  }

  public List<Object[]> getFirstRows( String table_name, int limit ) throws KettleDatabaseException {
    return getFirstRows( table_name, limit, null );
  }

  /**
   * Get the first rows from a table (for preview)
   *
   * @param table_name The table name (or schema/table combination): this needs to be quoted properly
   * @param limit      limit <=0 means unlimited, otherwise this specifies the maximum number of rows read.
   * @param monitor    The progress monitor to update while getting the rows.
   * @return An ArrayList of rows.
   * @throws KettleDatabaseException in case something goes wrong
   */
  public List<Object[]> getFirstRows( String table_name, int limit, ProgressMonitorListener monitor )
    throws KettleDatabaseException {
    String sql = "SELECT";
    if ( databaseMeta.getDatabaseInterface() instanceof NeoviewDatabaseMeta ) {
      sql += " [FIRST " + limit + "]";
    } else if ( databaseMeta.getDatabaseInterface() instanceof SybaseIQDatabaseMeta ) {
      // improve support for Sybase IQ
      sql += " TOP " + limit + " ";
    }
    sql += " * FROM " + table_name;

    if ( limit > 0 ) {
      sql += databaseMeta.getLimitClause( limit );
    }

    return getRows( sql, limit, monitor );
  }

  public RowMetaInterface getReturnRowMeta() {
    return rowMeta;
  }

  public String[] getTableTypes() throws KettleDatabaseException {
    try {
      ArrayList<String> types = new ArrayList<String>();

      ResultSet rstt = getDatabaseMetaData().getTableTypes();
      while ( rstt.next() ) {
        String ttype = rstt.getString( "TABLE_TYPE" );
        types.add( ttype );
      }

      return types.toArray( new String[ types.size() ] );
    } catch ( SQLException e ) {
      throw new KettleDatabaseException( "Unable to get table types from database!", e );
    }
  }

  public String[] getTablenames() throws KettleDatabaseException {
    return getTablenames( false );
  }

  public String[] getTablenames( boolean includeSchema ) throws KettleDatabaseException {
    return getTablenames( null, includeSchema );
  }

  public String[] getTablenames( String schemanamein, boolean includeSchema ) throws KettleDatabaseException {
    Map<String, Collection<String>> tableMap = getTableMap( schemanamein );
    List<String> res = new ArrayList<String>();
    for ( String schema : tableMap.keySet() ) {
      Collection<String> tables = tableMap.get( schema );
      for ( String table : tables ) {
        if ( includeSchema ) {
          res.add( databaseMeta.getQuotedSchemaTableCombination( schema, table ) );
        } else {
          res.add( databaseMeta.getQuotedSchemaTableCombination( null, table ) );
        }
      }
    }
    return res.toArray( new String[ res.size() ] );
  }

  public Map<String, Collection<String>> getTableMap() throws KettleDatabaseException {
    return getTableMap( null );
  }

  public Map<String, Collection<String>> getTableMap( String schemanamein ) throws KettleDatabaseException {
    String schemaname = schemanamein;
    if ( schemaname == null ) {
      if ( databaseMeta.useSchemaNameForTableList() ) {
        schemaname = environmentSubstitute( databaseMeta.getUsername() ).toUpperCase();
      }
    }
    Map<String, Collection<String>> tableMap = new HashMap<String, Collection<String>>();
    ResultSet alltables = null;
    try {
      alltables = getDatabaseMetaData().getTables( null, schemaname, null, databaseMeta.getTableTypes() );
      while ( alltables.next() ) {
        // due to PDI-743 with ODBC and MS SQL Server the order is changed and
        // try/catch included for safety
        String cat = "";
        try {
          cat = alltables.getString( "TABLE_CAT" );
        } catch ( Exception e ) {
          // ignore
          if ( log.isDebug() ) {
            log.logDebug( "Error getting tables for field TABLE_CAT (ignored): " + e.toString() );
          }
        }

        String schema = "";
        try {
          schema = alltables.getString( "TABLE_SCHEM" );
        } catch ( Exception e ) {
          // ignore
          if ( log.isDebug() ) {
            log.logDebug( "Error getting tables for field TABLE_SCHEM (ignored): " + e.toString() );
          }
        }

        if ( Utils.isEmpty( schema ) ) {
          schema = cat;
        }

        String table = alltables.getString( TABLES_META_DATA_TABLE_NAME );

        if ( log.isRowLevel() ) {
          log.logRowlevel( toString(), "got table from meta-data: "
            + databaseMeta.getQuotedSchemaTableCombination( schema, table ) );
        }
        multimapPut( schema, table, tableMap );
      }
    } catch ( SQLException e ) {
      log.logError( "Error getting tablenames from schema [" + schemaname + "]" );
    } finally {
      try {
        if ( alltables != null ) {
          alltables.close();
        }
      } catch ( SQLException e ) {
        throw new KettleDatabaseException( "Error closing resultset after getting views from schema ["
          + schemaname + "]", e );
      }
    }

    if ( log.isDetailed() ) {
      log.logDetailed( "read :" + multimapSize( tableMap ) + " table names from db meta-data." );
    }

    return tableMap;
  }

  public String[] getViews() throws KettleDatabaseException {
    return getViews( false );
  }

  public String[] getViews( boolean includeSchema ) throws KettleDatabaseException {
    return getViews( null, includeSchema );
  }

  public String[] getViews( String schemanamein, boolean includeSchema ) throws KettleDatabaseException {
    Map<String, Collection<String>> viewMap = getViewMap( schemanamein );
    List<String> res = new ArrayList<String>();
    for ( String schema : viewMap.keySet() ) {
      Collection<String> views = viewMap.get( schema );
      for ( String view : views ) {
        if ( includeSchema ) {
          res.add( databaseMeta.getQuotedSchemaTableCombination( schema, view ) );
        } else {
          res.add( view );
        }
      }
    }
    return res.toArray( new String[ res.size() ] );
  }

  public Map<String, Collection<String>> getViewMap() throws KettleDatabaseException {
    return getViewMap( null );
  }

  public Map<String, Collection<String>> getViewMap( String schemanamein ) throws KettleDatabaseException {
    if ( !databaseMeta.supportsViews() ) {
      return Collections.emptyMap();
    }

    String schemaname = schemanamein;
    if ( schemaname == null ) {
      if ( databaseMeta.useSchemaNameForTableList() ) {
        schemaname = environmentSubstitute( databaseMeta.getUsername() ).toUpperCase();
      }
    }

    Map<String, Collection<String>> viewMap = new HashMap<String, Collection<String>>();
    ResultSet allviews = null;
    try {
      allviews = getDatabaseMetaData().getTables( null, schemaname, null, databaseMeta.getViewTypes() );
      while ( allviews.next() ) {
        // due to PDI-743 with ODBC and MS SQL Server the order is changed and
        // try/catch included for safety
        String cat = "";
        try {
          cat = allviews.getString( "TABLE_CAT" );
        } catch ( Exception e ) {
          // ignore
          if ( log.isDebug() ) {
            log.logDebug( "Error getting views for field TABLE_CAT (ignored): " + e.toString() );
          }
        }

        String schema = "";
        try {
          schema = allviews.getString( "TABLE_SCHEM" );
        } catch ( Exception e ) {
          // ignore
          if ( log.isDebug() ) {
            log.logDebug( "Error getting views for field TABLE_SCHEM (ignored): " + e.toString() );
          }
        }

        if ( Utils.isEmpty( schema ) ) {
          schema = cat;
        }

        String table = allviews.getString( TABLES_META_DATA_TABLE_NAME );

        if ( log.isRowLevel() ) {
          log.logRowlevel( toString(), "got view from meta-data: "
            + databaseMeta.getQuotedSchemaTableCombination( schema, table ) );
        }
        multimapPut( schema, table, viewMap );
      }
    } catch ( SQLException e ) {
      throw new KettleDatabaseException( "Error getting views from schema [" + schemaname + "]", e );
    } finally {
      try {
        if ( allviews != null ) {
          allviews.close();
        }
      } catch ( SQLException e ) {
        throw new KettleDatabaseException( "Error closing resultset after getting views from schema ["
          + schemaname + "]", e );
      }
    }

    if ( log.isDetailed() ) {
      log.logDetailed( "read :" + multimapSize( viewMap ) + " views from db meta-data." );
    }

    return viewMap;
  }

  public String[] getSynonyms() throws KettleDatabaseException {
    return getSynonyms( false );
  }

  public String[] getSynonyms( boolean includeSchema ) throws KettleDatabaseException {
    return getSynonyms( null, includeSchema );
  }

  public String[] getSynonyms( String schemanamein, boolean includeSchema ) throws KettleDatabaseException {
    Map<String, Collection<String>> synonymMap = getSynonymMap( schemanamein );
    List<String> res = new ArrayList<String>();
    for ( String schema : synonymMap.keySet() ) {
      Collection<String> synonyms = synonymMap.get( schema );
      for ( String synonym : synonyms ) {
        if ( includeSchema ) {
          res.add( databaseMeta.getQuotedSchemaTableCombination( schema, synonym ) );
        } else {
          res.add( synonym );
        }
      }
    }
    return res.toArray( new String[ res.size() ] );
  }

  public Map<String, Collection<String>> getSynonymMap() throws KettleDatabaseException {
    return getSynonymMap( null );
  }

  public Map<String, Collection<String>> getSynonymMap( String schemanamein ) throws KettleDatabaseException {
    if ( !databaseMeta.supportsSynonyms() ) {
      return Collections.emptyMap();
    }

    String schemaname = schemanamein;
    if ( schemaname == null ) {
      if ( databaseMeta.useSchemaNameForTableList() ) {
        schemaname = environmentSubstitute( databaseMeta.getUsername() ).toUpperCase();
      }
    }
    Map<String, Collection<String>> synonymMap = new HashMap<String, Collection<String>>();
    // ArrayList<String> names = new ArrayList<String>();
    ResultSet alltables = null;
    try {
      alltables = getDatabaseMetaData().getTables( null, schemaname, null, databaseMeta.getSynonymTypes() );
      while ( alltables.next() ) {
        // due to PDI-743 with ODBC and MS SQL Server the order is changed and
        // try/catch included for safety
        String cat = "";
        try {
          cat = alltables.getString( "TABLE_CAT" );
        } catch ( Exception e ) {
          // ignore
          if ( log.isDebug() ) {
            log.logDebug( "Error getting synonyms for field TABLE_CAT (ignored): " + e.toString() );
          }
        }

        String schema = "";
        try {
          schema = alltables.getString( "TABLE_SCHEM" );
        } catch ( Exception e ) {
          // ignore
          if ( log.isDebug() ) {
            log.logDebug( "Error getting synonyms for field TABLE_SCHEM (ignored): " + e.toString() );
          }
        }

        if ( Utils.isEmpty( schema ) ) {
          schema = cat;
        }

        String table = alltables.getString( TABLES_META_DATA_TABLE_NAME );

        if ( log.isRowLevel() ) {
          log.logRowlevel( toString(), "got synonym from meta-data: "
            + databaseMeta.getQuotedSchemaTableCombination( schema, table ) );
        }
        multimapPut( schema, table, synonymMap );
      }
    } catch ( SQLException e ) {
      throw new KettleDatabaseException( "Error getting synonyms from schema [" + schemaname + "]", e );
    } finally {
      try {
        if ( alltables != null ) {
          alltables.close();
        }
      } catch ( SQLException e ) {
        throw new KettleDatabaseException( "Error closing resultset after getting synonyms from schema ["
          + schemaname + "]", e );
      }
    }

    if ( log.isDetailed() ) {
      log.logDetailed( "read :" + multimapSize( synonymMap ) + " synonyms from db meta-data." );
    }

    return synonymMap;
  }

  private <K, V> void multimapPut( final K key, final V value, final Map<K, Collection<V>> map ) {
    Collection<V> valueCollection = map.get( key );
    if ( valueCollection == null ) {
      valueCollection = new HashSet<V>();
    }
    valueCollection.add( value );
    map.put( key, valueCollection );
  }

  private <K, V> int multimapSize( final Map<K, Collection<V>> map ) {
    int count = 0;
    for ( Collection<V> valueCollection : map.values() ) {
      count += valueCollection.size();
    }
    return count;
  }

  public String[] getSchemas() throws KettleDatabaseException {
    ArrayList<String> catalogList = new ArrayList<String>();
    ResultSet catalogResultSet = null;
    try {
      catalogResultSet = getDatabaseMetaData().getSchemas();
      // Grab all the catalog names and put them in an array list
      while ( catalogResultSet != null && catalogResultSet.next() ) {
        catalogList.add( catalogResultSet.getString( 1 ) );
      }
    } catch ( SQLException e ) {
      throw new KettleDatabaseException( "Error getting schemas!", e );
    } finally {
      try {
        if ( catalogResultSet != null ) {
          catalogResultSet.close();
        }
      } catch ( SQLException e ) {
        throw new KettleDatabaseException( "Error closing resultset after getting schemas!", e );
      }
    }

    if ( log.isDetailed() ) {
      log.logDetailed( "read :" + catalogList.size() + " schemas from db meta-data." );
    }

    return catalogList.toArray( new String[ catalogList.size() ] );
  }

  public String[] getCatalogs() throws KettleDatabaseException {
    ArrayList<String> catalogList = new ArrayList<String>();
    ResultSet catalogResultSet = null;
    try {
      catalogResultSet = getDatabaseMetaData().getCatalogs();
      // Grab all the catalog names and put them in an array list
      while ( catalogResultSet != null && catalogResultSet.next() ) {
        catalogList.add( catalogResultSet.getString( 1 ) );
      }
    } catch ( SQLException e ) {
      throw new KettleDatabaseException( "Error getting catalogs!", e );
    } finally {
      try {
        if ( catalogResultSet != null ) {
          catalogResultSet.close();
        }
      } catch ( SQLException e ) {
        throw new KettleDatabaseException( "Error closing resultset after getting catalogs!", e );
      }
    }

    if ( log.isDetailed() ) {
      log.logDetailed( "read :" + catalogList.size() + " catalogs from db meta-data." );
    }

    return catalogList.toArray( new String[ catalogList.size() ] );
  }

  public String[] getProcedures() throws KettleDatabaseException {
    String sql = databaseMeta.getSQLListOfProcedures();
    if ( sql != null ) {
      // System.out.println("SQL= "+sql);
      List<Object[]> procs = getRows( sql, 1000 );
      // System.out.println("Found "+procs.size()+" rows");
      String[] str = new String[ procs.size() ];
      for ( int i = 0; i < procs.size(); i++ ) {
        str[ i ] = procs.get( i )[ 0 ].toString();
      }
      return str;
    } else {
      ResultSet rs = null;
      try {
        DatabaseMetaData dbmd = getDatabaseMetaData();
        rs = dbmd.getProcedures( null, null, null );
        List<Object[]> rows = getRows( rs, 0, null );
        String[] result = new String[ rows.size() ];
        for ( int i = 0; i < rows.size(); i++ ) {
          Object[] row = rows.get( i );
          String procCatalog = rowMeta.getString( row, "PROCEDURE_CAT", null );
          String procSchema = rowMeta.getString( row, "PROCEDURE_SCHEM", null );
          String procName = rowMeta.getString( row, "PROCEDURE_NAME", "" );

          StringBuilder name = new StringBuilder( "" );
          if ( procCatalog != null ) {
            name.append( procCatalog ).append( "." );
          }
          if ( procSchema != null ) {
            name.append( procSchema ).append( "." );
          }

          name.append( procName );

          result[ i ] = name.toString();
        }
        return result;
      } catch ( Exception e ) {
        throw new KettleDatabaseException( "Unable to get list of procedures from database meta-data: ", e );
      } finally {
        if ( rs != null ) {
          try {
            rs.close();
          } catch ( Exception e ) {
            // ignore the error.
          }
        }
      }
    }
  }

  public boolean isAutoCommit() {
    return commitsize <= 0;
  }

  /**
   * @return Returns the databaseMeta.
   */
  public DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  /**
   * Lock a tables in the database for write operations
   *
   * @param tableNames The tables to lock. These need to be the appropriately quoted fully qualified (schema+table)
   *                   names.
   * @throws KettleDatabaseException
   */
  public void lockTables( String[] tableNames ) throws KettleDatabaseException {
    if ( Utils.isEmpty( tableNames ) ) {
      return;
    }

    // Get the SQL to lock the (quoted) tables
    //
    String sql = databaseMeta.getSQLLockTables( tableNames );
    if ( sql != null ) {
      execStatements( sql );
    }
  }

  /**
   * Unlock certain tables in the database for write operations
   *
   * @param tableNames The tables to unlock
   * @throws KettleDatabaseException
   */
  public void unlockTables( String[] tableNames ) throws KettleDatabaseException {
    if ( Utils.isEmpty( tableNames ) ) {
      return;
    }

    // Quote table names too...
    //
    String[] quotedTableNames = new String[ tableNames.length ];
    for ( int i = 0; i < tableNames.length; i++ ) {
      quotedTableNames[ i ] = databaseMeta.getQuotedSchemaTableCombination( null, tableNames[ i ] );
    }

    // Get the SQL to unlock the (quoted) tables
    //
    String sql = databaseMeta.getSQLUnlockTables( quotedTableNames );
    if ( sql != null ) {
      execStatement( sql );
    }
  }

  /**
   * @return the opened
   */
  public int getOpened() {
    return opened;
  }

  /**
   * @param opened the opened to set
   */
  public synchronized void setOpened( int opened ) {
    this.opened = opened;
  }

  /**
   * @return the connectionGroup
   */
  public String getConnectionGroup() {
    return connectionGroup;
  }

  /**
   * @param connectionGroup the connectionGroup to set
   */
  public void setConnectionGroup( String connectionGroup ) {
    this.connectionGroup = connectionGroup;
  }

  /**
   * @return the partitionId
   */
  public String getPartitionId() {
    return partitionId;
  }

  /**
   * @param partitionId the partitionId to set
   */
  public void setPartitionId( String partitionId ) {
    this.partitionId = partitionId;
  }

  /**
   * @return the copy
   */
  public int getCopy() {
    return copy;
  }

  /**
   * @param copy the copy to set
   */
  public synchronized void setCopy( int copy ) {
    this.copy = copy;
  }

  @Override
  public void copyVariablesFrom( VariableSpace space ) {
    variables.copyVariablesFrom( space );
  }

  @Override
  public String environmentSubstitute( String aString ) {
    return variables.environmentSubstitute( aString );
  }

  @Override
  public String[] environmentSubstitute( String[] aString ) {
    return variables.environmentSubstitute( aString );
  }

  @Override
  public String fieldSubstitute( String aString, RowMetaInterface rowMeta, Object[] rowData )
    throws KettleValueException {
    return variables.fieldSubstitute( aString, rowMeta, rowData );
  }

  @Override
  public VariableSpace getParentVariableSpace() {
    return variables.getParentVariableSpace();
  }

  @Override
  public void setParentVariableSpace( VariableSpace parent ) {
    variables.setParentVariableSpace( parent );
  }

  @Override
  public String getVariable( String variableName, String defaultValue ) {
    return variables.getVariable( variableName, defaultValue );
  }

  @Override
  public String getVariable( String variableName ) {
    return variables.getVariable( variableName );
  }

  @Override
  public boolean getBooleanValueOfVariable( String variableName, boolean defaultValue ) {
    if ( !Utils.isEmpty( variableName ) ) {
      String value = environmentSubstitute( variableName );
      if ( !Utils.isEmpty( value ) ) {
        return ValueMetaBase.convertStringToBoolean( value );
      }
    }
    return defaultValue;
  }

  @Override
  public void initializeVariablesFrom( VariableSpace parent ) {
    variables.initializeVariablesFrom( parent );
  }

  @Override
  public String[] listVariables() {
    return variables.listVariables();
  }

  @Override
  public void setVariable( String variableName, String variableValue ) {
    variables.setVariable( variableName, variableValue );
  }

  @Override
  public void shareVariablesWith( VariableSpace space ) {
    variables = space;

    // Also share the variables with the meta data object
    // Make sure it's not the databaseMeta object itself. We would get an
    // infinite loop in that case.
    //
    if ( space != databaseMeta ) {
      databaseMeta.shareVariablesWith( space );
    }
  }

  @Override
  public void injectVariables( Map<String, String> prop ) {
    variables.injectVariables( prop );
  }

  public RowMetaAndData callProcedure( String[] arg, String[] argdir, int[] argtype, String resultname,
                                       int resulttype ) throws KettleDatabaseException {
    RowMetaAndData ret;
    try {
      boolean moreResults = cstmt.execute();
      ret = new RowMetaAndData();
      int pos = 1;
      if ( resultname != null && resultname.length() != 0 ) {
        ValueMeta vMeta = new ValueMeta( resultname, resulttype );
        Object v = null;
        switch ( resulttype ) {
          case ValueMetaInterface.TYPE_BOOLEAN:
            v = Boolean.valueOf( cstmt.getBoolean( pos ) );
            break;
          case ValueMetaInterface.TYPE_NUMBER:
            v = new Double( cstmt.getDouble( pos ) );
            break;
          case ValueMetaInterface.TYPE_BIGNUMBER:
            v = cstmt.getBigDecimal( pos );
            break;
          case ValueMetaInterface.TYPE_INTEGER:
            v = Long.valueOf( cstmt.getLong( pos ) );
            break;
          case ValueMetaInterface.TYPE_STRING:
            v = cstmt.getString( pos );
            break;
          case ValueMetaInterface.TYPE_BINARY:
            if ( databaseMeta.supportsGetBlob() ) {
              Blob blob = cstmt.getBlob( pos );
              if ( blob != null ) {
                v = blob.getBytes( 1L, (int) blob.length() );
              } else {
                v = null;
              }
            } else {
              v = cstmt.getBytes( pos );
            }
            break;
          case ValueMetaInterface.TYPE_DATE:
            if ( databaseMeta.supportsTimeStampToDateConversion() ) {
              v = cstmt.getTimestamp( pos );
            } else {
              v = cstmt.getDate( pos );
            }
            break;
          default:
            break;
        }
        ret.addValue( vMeta, v );
        pos++;
      }
      for ( int i = 0; i < arg.length; i++ ) {
        if ( argdir[ i ].equalsIgnoreCase( "OUT" ) || argdir[ i ].equalsIgnoreCase( "INOUT" ) ) {
          ValueMetaInterface vMeta = ValueMetaFactory.createValueMeta( arg[ i ], argtype[ i ] );
          Object v = null;
          switch ( argtype[ i ] ) {
            case ValueMetaInterface.TYPE_BOOLEAN:
              v = Boolean.valueOf( cstmt.getBoolean( pos + i ) );
              break;
            case ValueMetaInterface.TYPE_NUMBER:
              v = new Double( cstmt.getDouble( pos + i ) );
              break;
            case ValueMetaInterface.TYPE_BIGNUMBER:
              v = cstmt.getBigDecimal( pos + i );
              break;
            case ValueMetaInterface.TYPE_INTEGER:
              v = Long.valueOf( cstmt.getLong( pos + i ) );
              break;
            case ValueMetaInterface.TYPE_STRING:
              v = cstmt.getString( pos + i );
              break;
            case ValueMetaInterface.TYPE_BINARY:
              if ( databaseMeta.supportsGetBlob() ) {
                Blob blob = cstmt.getBlob( pos + i );
                if ( blob != null ) {
                  v = blob.getBytes( 1L, (int) blob.length() );
                } else {
                  v = null;
                }
              } else {
                v = cstmt.getBytes( pos + i );
              }
              break;
            case ValueMetaInterface.TYPE_DATE:
              if ( databaseMeta.supportsTimeStampToDateConversion() ) {
                v = cstmt.getTimestamp( pos + i );
              } else {
                v = cstmt.getDate( pos + i );
              }
              break;
            default:
              break;
          }
          ret.addValue( vMeta, v );
        }
      }
      ResultSet rs = null;
      int updateCount = -1;

      // CHE: Iterate through the result sets and update counts
      // to receive all error messages from within the stored procedure.
      // This is only the first step to ensure that the stored procedure
      // is properly executed. A future extension would be to return all
      // result sets and update counts properly.

      do {
        rs = null;
        try {
          // Save the result set
          if ( moreResults ) {
            rs = cstmt.getResultSet();

          } else {
            // Save the update count if it is available (> -1)
            updateCount = cstmt.getUpdateCount();

          }

          moreResults = cstmt.getMoreResults();

        } finally {
          if ( rs != null ) {
            rs.close();
            rs = null;
          }
        }

      } while ( moreResults || ( updateCount > -1 ) );

      return ret;
    } catch ( Exception ex ) {
      throw new KettleDatabaseException( "Unable to call procedure", ex );
    }

  }

  public void closeProcedureStatement() throws KettleDatabaseException {
    // CHE: close the callable statement involved in the stored
    // procedure call!
    try {
      if ( cstmt != null ) {
        cstmt.close();
        cstmt = null;
      }
    } catch ( SQLException ex ) {
      throw new KettleDatabaseException( BaseMessages.getString(
        PKG, "Database.Exception.ErrorClosingCallableStatement" ), ex );
    }
  }

  /**
   * Return SQL CREATION statement for a Table
   *
   * @param tableName The table to create
   * @throws KettleDatabaseException
   */

  public String getDDLCreationTable( String tableName, RowMetaInterface fields ) throws KettleDatabaseException {
    String retval;

    // First, check for reserved SQL in the input row r...
    databaseMeta.quoteReservedWords( fields );
    String quotedTk = databaseMeta.quoteField( null );
    retval = getCreateTableStatement( tableName, fields, quotedTk, false, null, true );

    return retval;
  }

  /**
   * Return SQL TRUNCATE statement for a Table
   *
   * @param schema              The schema
   * @param tableNameWithSchema The table to create
   * @throws KettleDatabaseException
   */
  public String getDDLTruncateTable( String schema, String tablename ) throws KettleDatabaseException {
    if ( Utils.isEmpty( connectionGroup ) ) {
      String truncateStatement = databaseMeta.getTruncateTableStatement( schema, tablename );
      if ( truncateStatement == null ) {
        throw new KettleDatabaseException( "Truncate table not supported by "
          + databaseMeta.getDatabaseInterface().getPluginName() );
      }
      return truncateStatement;
    } else {
      return ( "DELETE FROM " + databaseMeta.getQuotedSchemaTableCombination( schema, tablename ) );
    }
  }

  /**
   * Return SQL statement (INSERT INTO TableName ...
   *
   * @param schemaName tableName The schema
   * @param tableName
   * @param fields
   * @param dateFormat date format of field
   * @throws KettleDatabaseException
   */

  public String getSQLOutput( String schemaName, String tableName, RowMetaInterface fields, Object[] r,
                              String dateFormat ) throws KettleDatabaseException {
    StringBuilder ins = new StringBuilder( 128 );

    try {
      String schemaTable = databaseMeta.getQuotedSchemaTableCombination( schemaName, tableName );
      ins.append( "INSERT INTO " ).append( schemaTable ).append( '(' );

      // now add the names in the row:
      for ( int i = 0; i < fields.size(); i++ ) {
        if ( i > 0 ) {
          ins.append( ", " );
        }
        String name = fields.getValueMeta( i ).getName();
        ins.append( databaseMeta.quoteField( name ) );

      }
      ins.append( ") VALUES (" );

      java.text.SimpleDateFormat[] fieldDateFormatters = new java.text.SimpleDateFormat[ fields.size() ];

      // new add values ...
      for ( int i = 0; i < fields.size(); i++ ) {
        ValueMetaInterface valueMeta = fields.getValueMeta( i );
        Object valueData = r[ i ];

        if ( i > 0 ) {
          ins.append( "," );
        }

        // Check for null values...
        //
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
              // This also adds the single quotes around the string (thanks to
              // PostgreSQL)
              //
              string = databaseMeta.quoteSQLString( string );
              ins.append( string );
              break;
            case ValueMetaInterface.TYPE_DATE:
              Date date = fields.getDate( r, i );

              if ( Utils.isEmpty( dateFormat ) ) {
                if ( databaseMeta.getDatabaseInterface() instanceof OracleDatabaseMeta ) {
                  if ( fieldDateFormatters[ i ] == null ) {
                    fieldDateFormatters[ i ] = new java.text.SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );
                  }
                  ins.append( "TO_DATE('" ).append( fieldDateFormatters[ i ].format( date ) ).append(
                    "', 'YYYY/MM/DD HH24:MI:SS')" );
                } else {
                  ins.append( "'" + fields.getString( r, i ) + "'" );
                }
              } else {
                try {
                  java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat( dateFormat );
                  ins.append( "'" + formatter.format( fields.getDate( r, i ) ) + "'" );
                } catch ( Exception e ) {
                  throw new KettleDatabaseException( "Error : ", e );
                }
              }
              break;
            default:
              ins.append( fields.getString( r, i ) );
              break;
          }
        }

      }
      ins.append( ')' );
    } catch ( Exception e ) {
      throw new KettleDatabaseException( e );
    }
    return ins.toString();
  }

  public Savepoint setSavepoint() throws KettleDatabaseException {
    try {
      return connection.setSavepoint();
    } catch ( SQLException e ) {
      throw new KettleDatabaseException(
        BaseMessages.getString( PKG, "Database.Exception.UnableToSetSavepoint" ), e );
    }
  }

  public Savepoint setSavepoint( String savePointName ) throws KettleDatabaseException {
    try {
      return connection.setSavepoint( savePointName );
    } catch ( SQLException e ) {
      throw new KettleDatabaseException( BaseMessages.getString(
        PKG, "Database.Exception.UnableToSetSavepointName", savePointName ), e );
    }
  }

  public void releaseSavepoint( Savepoint savepoint ) throws KettleDatabaseException {
    try {
      connection.releaseSavepoint( savepoint );
    } catch ( SQLException e ) {
      throw new KettleDatabaseException( BaseMessages.getString(
        PKG, "Database.Exception.UnableToReleaseSavepoint" ), e );
    }
  }

  public void rollback( Savepoint savepoint ) throws KettleDatabaseException {
    try {
      connection.rollback( savepoint );
    } catch ( SQLException e ) {
      throw new KettleDatabaseException( BaseMessages.getString(
        PKG, "Database.Exception.UnableToRollbackToSavepoint" ), e );
    }
  }

  public Object getParentObject() {
    return parentLoggingObject;
  }

  /**
   * Return primary key column names ...
   *
   * @param tablename
   * @throws KettleDatabaseException
   */
  public String[] getPrimaryKeyColumnNames( String tablename ) throws KettleDatabaseException {
    List<String> names = new ArrayList<String>();
    ResultSet allkeys = null;
    try {
      allkeys = getDatabaseMetaData().getPrimaryKeys( null, null, tablename );
      while ( allkeys.next() ) {
        String keyname = allkeys.getString( "PK_NAME" );
        String col_name = allkeys.getString( "COLUMN_NAME" );
        if ( !names.contains( col_name ) ) {
          names.add( col_name );
        }
        if ( log.isRowLevel() ) {
          log.logRowlevel( toString(), "getting key : " + keyname + " on column " + col_name );
        }
      }
    } catch ( SQLException e ) {
      log.logError( toString(), "Error getting primary keys columns from table [" + tablename + "]" );
    } finally {
      try {
        if ( allkeys != null ) {
          allkeys.close();
        }
      } catch ( SQLException e ) {
        throw new KettleDatabaseException( "Error closing connection while searching primary keys in table ["
          + tablename + "]", e );
      }
    }
    return names.toArray( new String[ names.size() ] );
  }

  /**
   * Return all sequence names from connection
   *
   * @return The sequences name list.
   * @throws KettleDatabaseException
   */
  public String[] getSequences() throws KettleDatabaseException {
    if ( databaseMeta.supportsSequences() ) {
      String sql = databaseMeta.getSQLListOfSequences();
      if ( sql != null ) {
        List<Object[]> seqs = getRows( sql, 0 );
        String[] str = new String[ seqs.size() ];
        for ( int i = 0; i < seqs.size(); i++ ) {
          str[ i ] = seqs.get( i )[ 0 ].toString();
        }
        return str;
      }
    } else {
      throw new KettleDatabaseException( "Sequences are only available for Oracle databases." );
    }
    return null;
  }

  @Override
  public String getFilename() {
    return null;
  }

  @Override
  public String getLogChannelId() {
    return log.getLogChannelId();
  }

  @Override
  public String getObjectName() {
    return databaseMeta.getName();
  }

  @Override
  public String getObjectCopy() {
    return null;
  }

  @Override
  public ObjectId getObjectId() {
    return databaseMeta.getObjectId();
  }

  @Override
  public ObjectRevision getObjectRevision() {
    return databaseMeta.getObjectRevision();
  }

  @Override
  public LoggingObjectType getObjectType() {
    return LoggingObjectType.DATABASE;
  }

  @Override
  public LoggingObjectInterface getParent() {
    return parentLoggingObject;
  }

  @Override
  public RepositoryDirectory getRepositoryDirectory() {
    return null;
  }

  @Override
  public LogLevel getLogLevel() {
    return logLevel;
  }

  public void setLogLevel( LogLevel logLevel ) {
    this.logLevel = logLevel;
    log.setLogLevel( logLevel );
  }

  /**
   * @return the carteObjectId
   */
  @Override
  public String getContainerObjectId() {
    return containerObjectId;
  }

  /**
   * @param containerObjectId the execution container Object id to set
   */
  public void setContainerObjectId( String containerObjectId ) {
    this.containerObjectId = containerObjectId;
  }

  /**
   * Stub
   */
  @Override
  public Date getRegistrationDate() {
    return null;
  }

  /**
   * @return the nrExecutedCommits
   */
  public int getNrExecutedCommits() {
    return nrExecutedCommits;
  }

  /**
   * @param nrExecutedCommits the nrExecutedCommits to set
   */
  public void setNrExecutedCommits( int nrExecutedCommits ) {
    this.nrExecutedCommits = nrExecutedCommits;
  }

  /**
   * Execute an SQL statement inside a file on the database connection (has to be open)
   *
   * @param sql The file that contains SQL to execute
   * @return a Result object indicating the number of lines read, deleted, inserted, updated, ...
   * @throws KettleDatabaseException in case anything goes wrong.
   * @sendSinglestatement send one statement
   */
  public Result execStatementsFromFile( String filename, boolean sendSinglestatement ) throws KettleException {
    FileObject sqlFile = null;
    InputStream is = null;
    InputStreamReader bis = null;
    try {
      if ( Utils.isEmpty( filename ) ) {
        throw new KettleException( "Filename is missing!" );
      }
      sqlFile = KettleVFS.getFileObject( filename );
      if ( !sqlFile.exists() ) {
        throw new KettleException( "We can not find file [" + filename + "]!" );
      }

      is = KettleVFS.getInputStream( sqlFile );
      bis = new InputStreamReader( new BufferedInputStream( is, 500 ) );
      StringBuilder lineStringBuilder = new StringBuilder( 256 );
      lineStringBuilder.setLength( 0 );

      BufferedReader buff = new BufferedReader( bis );
      String sLine = null;
      String sql = Const.CR;

      while ( ( sLine = buff.readLine() ) != null ) {
        if ( Utils.isEmpty( sLine ) ) {
          sql = sql + Const.CR;
        } else {
          sql = sql + Const.CR + sLine;
        }
      }

      if ( sendSinglestatement ) {
        return execStatement( sql );
      } else {
        return execStatements( sql );
      }

    } catch ( Exception e ) {
      throw new KettleException( e );
    } finally {
      try {
        if ( sqlFile != null ) {
          sqlFile.close();
        }
        if ( is != null ) {
          is.close();
        }
        if ( bis != null ) {
          bis.close();
        }
      } catch ( Exception e ) {
        // Ignore
      }
    }
  }

  @Override
  public boolean isGatheringMetrics() {
    return log != null && log.isGatheringMetrics();
  }

  @Override
  public void setGatheringMetrics( boolean gatheringMetrics ) {
    if ( log != null ) {
      log.setGatheringMetrics( gatheringMetrics );
    }
  }

  @Override
  public boolean isForcingSeparateLogging() {
    return log != null && log.isForcingSeparateLogging();
  }

  @Override
  public void setForcingSeparateLogging( boolean forcingSeparateLogging ) {
    if ( log != null ) {
      log.setForcingSeparateLogging( forcingSeparateLogging );
    }
  }
}
