/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.dbcp2.BasicDataSource;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.util.DatabaseUtil;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;

import javax.sql.DataSource;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

public class ConnectionPoolUtil {

  public static final String DEFAULT_AUTO_COMMIT = "defaultAutoCommit";
  public static final String DEFAULT_READ_ONLY = "defaultReadOnly";
  public static final String DEFAULT_TRANSACTION_ISOLATION = "defaultTransactionIsolation";
  public static final String DEFAULT_CATALOG = "defaultCatalog";
  public static final String INITIAL_SIZE = "initialSize";
  public static final String MAX_ACTIVE = "maxActive";
  public static final String MAX_IDLE = "maxIdle";
  public static final String MIN_IDLE = "minIdle";
  public static final String MAX_WAIT = "maxWait";
  public static final String VALIDATION_QUERY = "validationQuery";
  public static final String TEST_ON_BORROW = "testOnBorrow";
  public static final String TEST_ON_RETURN = "testOnReturn";
  public static final String TEST_WHILE_IDLE = "testWhileIdle";
  public static final String TIME_BETWEEN_EVICTION_RUNS_MILLIS = "timeBetweenEvictionRunsMillis";
  public static final String POOL_PREPARED_STATEMENTS = "poolPreparedStatements";
  public static final String MAX_OPEN_PREPARED_STATEMENTS = "maxOpenPreparedStatements";
  public static final String ACCESS_TO_UNDERLYING_CONNECTION_ALLOWED = "accessToUnderlyingConnectionAllowed";
  public static final String REMOVE_ABANDONED = "removeAbandoned";
  public static final String REMOVE_ABANDONED_TIMEOUT = "removeAbandonedTimeout";
  public static final String LOG_ABANDONED = "logAbandoned";
  private static Class<?> PKG = Database.class; // for i18n purposes, needed by Translator2!!

  private static ConcurrentMap<String, BasicDataSource> dataSources = new ConcurrentHashMap<String, BasicDataSource>();
  private static Map<String, Properties> dataSourcesAttributesMap = new HashMap<>();

  // PDI-12947
  private static final ReentrantLock lock = new ReentrantLock();

  public static final int defaultInitialNrOfConnections = 5;
  public static final int defaultMaximumNrOfConnections = 10;

  private static boolean isDataSourceRegistered( DatabaseMeta dbMeta, String partitionId )
    throws KettleDatabaseException {
    try {
      String name = getDataSourceName( dbMeta, partitionId );
      return dataSources.containsKey( name ) && hasOldConfig( dbMeta, partitionId );
    } catch ( Exception e ) {
      throw new KettleDatabaseException( BaseMessages.getString( PKG,
          "Database.UnableToCheckIfConnectionPoolExists.Exception" ), e );
    }
  }

  public static DataSource getDataSource( LogChannelInterface log, DatabaseMeta dbMeta, String partitionId ) throws KettleDatabaseException {
    int initialSize = dbMeta.getInitialPoolSize();
    int maximumSize = dbMeta.getMaximumPoolSize();

    lock.lock();
    try {
      if ( !isDataSourceRegistered( dbMeta, partitionId ) ) {
        addPoolableDataSource( log, dbMeta, partitionId, initialSize, maximumSize );
      }
    } finally {
      lock.unlock();
    }
    return dataSources.get( getDataSourceName( dbMeta, partitionId ) );
  }

  public static DataSource removeDataSource( String name ) {
    return dataSources.remove( name );
  }

  /**
   * @deprecated (Please use {@getDataSource(LogChannelInterface, DatabaseMeta, String) getDataSource} on init the step and then just get the connection when needed)
   */
  @Deprecated
  public static Connection getConnection( LogChannelInterface log, DatabaseMeta dbMeta, String partitionId )
    throws Exception {

    DataSource ds = getDataSource( log, dbMeta, partitionId );
    return ds.getConnection();
  }

  // BACKLOG-674
  public static String getDataSourceName( DatabaseMeta dbMeta, String partitionId ) {

    String name = dbMeta.environmentSubstitute( Const.NVL( dbMeta.getName(), "" ) );
    String username = dbMeta.environmentSubstitute( Const.NVL( dbMeta.getUsername(), "" ) );
    String password = dbMeta.environmentSubstitute( Const.NVL( dbMeta.getPassword(), "" ) );
    String preferredSchema = dbMeta.environmentSubstitute( Const.NVL( dbMeta.getPreferredSchemaName(), "" ) );
    String database = dbMeta.environmentSubstitute( Const.NVL( dbMeta.getDatabaseName(), "" ) );
    String hostname = dbMeta.environmentSubstitute( Const.NVL( dbMeta.getHostname(), "" ) );
    String port = dbMeta.environmentSubstitute( Const.NVL( dbMeta.getDatabasePortNumberString(), "" ) );

    return name + username + password + preferredSchema + database + hostname + port + Const.NVL( partitionId, "" );
  }

  /**
   * Replace Kettle variables/parameters with its values
   *
   * @param properties
   * @param databaseMeta
   * @return new object of type Properties with resolved variables/parameters
   */
  private static Properties environmentSubstitute( Properties properties, DatabaseMeta databaseMeta ) {
    Iterator<Object> iterator = properties.keySet().iterator();
    while ( iterator.hasNext() ) {
      String key = (String) iterator.next();
      String value = properties.getProperty( key );
      properties.put( key, databaseMeta.environmentSubstitute( value ) );
    }
    return properties;
  }

  @VisibleForTesting
  static void configureDataSource( BasicDataSource ds, DatabaseMeta databaseMeta, String partitionId,
      int initialSize, int maximumSize ) throws KettleDatabaseException {
    // substitute variables and populate pool properties; add credentials
    Properties connectionPoolProperties = new Properties( databaseMeta.getConnectionPoolingProperties() );
    connectionPoolProperties = environmentSubstitute( connectionPoolProperties, databaseMeta );
    setPoolProperties( ds, connectionPoolProperties, initialSize, maximumSize );
    setCredentials( ds, databaseMeta, partitionId );

    // add url/driver class
    String url = databaseMeta.environmentSubstitute( databaseMeta.getURL( partitionId ) );
    ds.setUrl( url );
    String clazz = databaseMeta.getDriverClass();
    if ( databaseMeta.getDatabaseInterface() != null ) {
      ds.setDriverClassLoader( databaseMeta.getDatabaseInterface().getClass().getClassLoader() );
    }
    ds.setDriverClassName( clazz );
    dataSourcesAttributesMap.put( getDataSourceName( databaseMeta, partitionId ), databaseMeta.getAttributes() );
  }

  private static void setCredentials( BasicDataSource ds, DatabaseMeta databaseMeta, String partitionId )
    throws KettleDatabaseException {

    String userName = databaseMeta.environmentSubstitute( databaseMeta.getUsername() );
    String password = databaseMeta.environmentSubstitute( databaseMeta.getPassword() );
    password = Encr.decryptPasswordOptionallyEncrypted( password );

    ds.addConnectionProperty( "user", Const.NVL( userName, "" ) );
    ds.addConnectionProperty( "password", Const.NVL( password, "" ) );
  }

  @SuppressWarnings( "deprecation" )
  private static void setPoolProperties( BasicDataSource ds, Properties properties, int initialSize, int maxSize ) {
    ds.setInitialSize( initialSize );
    ds.setMaxTotal( maxSize );

    String value = properties.getProperty( DEFAULT_AUTO_COMMIT );
    if ( !Utils.isEmpty( value ) ) {
      ds.setDefaultAutoCommit( Boolean.valueOf( value ) );
    }

    value = properties.getProperty( DEFAULT_READ_ONLY );
    if ( !Utils.isEmpty( value ) ) {
      ds.setDefaultReadOnly( Boolean.valueOf( value ) );
    }

    value = properties.getProperty( DEFAULT_TRANSACTION_ISOLATION );
    if ( !Utils.isEmpty( value ) ) {
      ds.setDefaultTransactionIsolation( Integer.valueOf( value ) );
    }

    value = properties.getProperty( DEFAULT_CATALOG );
    if ( !Utils.isEmpty( value ) ) {
      ds.setDefaultCatalog( value );
    }

    value = properties.getProperty( INITIAL_SIZE );
    if ( !Utils.isEmpty( value ) ) {
      ds.setInitialSize( Integer.valueOf( value ) );
    }

    value = properties.getProperty( MAX_ACTIVE );
    if ( !Utils.isEmpty( value ) ) {
      ds.setMaxTotal( Integer.valueOf( value ) );
    }

    value = properties.getProperty( MAX_IDLE );
    if ( !Utils.isEmpty( value ) ) {
      ds.setMaxIdle( Integer.valueOf( value ) );
    }

    value = properties.getProperty( MIN_IDLE );
    if ( !Utils.isEmpty( value ) ) {
      ds.setMinIdle( Integer.valueOf( value ) );
    }

    value = properties.getProperty( MAX_WAIT );
    if ( !Utils.isEmpty( value ) ) {
      ds.setMaxWaitMillis( Long.valueOf( value ) );
    }

    value = properties.getProperty( VALIDATION_QUERY );
    if ( !Utils.isEmpty( value ) ) {
      ds.setValidationQuery( value );
    }

    value = properties.getProperty( TEST_ON_BORROW );
    if ( !Utils.isEmpty( value ) ) {
      ds.setTestOnBorrow( Boolean.valueOf( value ) );
    }

    value = properties.getProperty( TEST_ON_RETURN );
    if ( !Utils.isEmpty( value ) ) {
      ds.setTestOnReturn( Boolean.valueOf( value ) );
    }

    value = properties.getProperty( TEST_WHILE_IDLE );
    if ( !Utils.isEmpty( value ) ) {
      ds.setTestWhileIdle( Boolean.valueOf( value ) );
    }

    value = properties.getProperty( TIME_BETWEEN_EVICTION_RUNS_MILLIS );
    if ( !Utils.isEmpty( value ) ) {
      ds.setTimeBetweenEvictionRunsMillis( Long.valueOf( value ) );
    }

    value = properties.getProperty( POOL_PREPARED_STATEMENTS );
    if ( !Utils.isEmpty( value ) ) {
      ds.setPoolPreparedStatements( Boolean.valueOf( value ) );
    }

    value = properties.getProperty( MAX_OPEN_PREPARED_STATEMENTS );
    if ( !Utils.isEmpty( value ) ) {
      ds.setMaxOpenPreparedStatements( Integer.valueOf( value ) );
    }

    value = properties.getProperty( ACCESS_TO_UNDERLYING_CONNECTION_ALLOWED );
    if ( !Utils.isEmpty( value ) ) {
      ds.setAccessToUnderlyingConnectionAllowed( Boolean.valueOf( value ) );
    }

    value = properties.getProperty( REMOVE_ABANDONED );
    if ( !Utils.isEmpty( value ) ) {
      ds.setRemoveAbandonedOnBorrow( Boolean.valueOf( value ) );
    }

    value = properties.getProperty( REMOVE_ABANDONED_TIMEOUT );
    if ( !Utils.isEmpty( value ) ) {
      ds.setRemoveAbandonedTimeout( Integer.valueOf( value ) );
    }

    value = properties.getProperty( LOG_ABANDONED );
    if ( !Utils.isEmpty( value ) ) {
      ds.setLogAbandoned( Boolean.valueOf( value ) );
    }

  }

  /**
   * This method verifies that it's possible to get connection fron a datasource
   *
   * @param ds
   * @throws KettleDatabaseException
   */
  private static void testDataSource( DataSource ds ) throws KettleDatabaseException {
    Connection conn = null;
    try {
      conn = ds.getConnection();
    } catch ( Throwable e ) {
      throw new KettleDatabaseException( BaseMessages.getString( PKG,
          "Database.UnableToPreLoadConnectionToConnectionPool.Exception" ), e );
    } finally {
      DatabaseUtil.closeSilently( conn );
    }
  }

  /**
   * This methods adds a new data source to cache
   *
   * @param log
   * @param databaseMeta
   * @param partitionId
   * @param initialSize
   * @param maximumSize
   * @throws KettleDatabaseException
   */
  private static void addPoolableDataSource( LogChannelInterface log, DatabaseMeta databaseMeta, String partitionId,
      int initialSize, int maximumSize ) throws KettleDatabaseException {
    if ( log.isBasic() ) {
      log.logBasic( BaseMessages.getString( PKG, "Database.CreatingConnectionPool", databaseMeta.getName() ) );
    }

    BasicDataSource ds = new BasicDataSource();
    configureDataSource( ds, databaseMeta, partitionId, initialSize, maximumSize );
    // check if datasource is valid
    testDataSource( ds );
    // register data source
    dataSources.put( getDataSourceName( databaseMeta, partitionId ), ds );

    if ( log.isBasic() ) {
      log.logBasic( BaseMessages.getString( PKG, "Database.CreatedConnectionPool", databaseMeta.getName() ) );
    }
  }

  protected static String buildPoolName( DatabaseMeta dbMeta, String partitionId ) {
    return dbMeta.getName() + Const.NVL( dbMeta.getDatabaseName(), "" )
        + Const.NVL( dbMeta.getHostname(),  ""  ) + Const.NVL( dbMeta.getDatabasePortNumberString(),  ""  )
        + Const.NVL( partitionId, "" );
  }

  public static boolean hasOldConfig( DatabaseMeta dbMeta, String partitionId ) {
    return dbMeta.getAttributes().equals( dataSourcesAttributesMap.get( getDataSourceName( dbMeta, partitionId ) ) );
  }

}
