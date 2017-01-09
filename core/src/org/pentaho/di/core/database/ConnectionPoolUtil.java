/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;

public class ConnectionPoolUtil {
  private static Class<?> PKG = Database.class; // for i18n purposes, needed by Translator2!!

  private static final ReentrantLock lock = new ReentrantLock();

  private static PoolingDriver pd = initPoolingDriver();

  private static PoolingDriver initPoolingDriver() {
  //for avoid lock  http://jira.pentaho.com/browse/PDI-12948
    synchronized ( DriverManager.class ) {
      return new PoolingDriver();
    }
  }

  public static final int defaultInitialNrOfConnections = 5;
  public static final int defaultMaximumNrOfConnections = 10;

  private static boolean isPoolRegistered( DatabaseMeta dbMeta, String partitionId ) throws KettleDatabaseException {
    try {
      String name = buildPoolName( dbMeta, partitionId );
      return Const.indexOfString( name, pd.getPoolNames() ) >= 0;
    } catch ( Exception e ) {
      throw new KettleDatabaseException( BaseMessages.getString( PKG,
          "Database.UnableToCheckIfConnectionPoolExists.Exception" ), e );
    }
  }

  private static void createPool( LogChannelInterface log, DatabaseMeta databaseMeta, String partitionId,
      int initialSize, int maximumSize ) throws KettleDatabaseException {
    log.logBasic( BaseMessages.getString( PKG, "Database.CreatingConnectionPool", databaseMeta.getName() ) );
    GenericObjectPool gpool = new GenericObjectPool();

    gpool.setMaxIdle( -1 );
    gpool.setWhenExhaustedAction( GenericObjectPool.WHEN_EXHAUSTED_GROW );
    gpool.setMaxActive( maximumSize );

    String clazz = databaseMeta.getDriverClass();
    try {
      Class.forName( clazz ).newInstance();
    } catch ( Exception e ) {
      throw new KettleDatabaseException( BaseMessages.getString( PKG,
          "Database.UnableToLoadConnectionPoolDriver.Exception", databaseMeta.getName(), clazz ), e );
    }

    String url;
    String userName;
    String password;

    try {
      url = databaseMeta.environmentSubstitute( databaseMeta.getURL( partitionId ) );
      userName = databaseMeta.environmentSubstitute( databaseMeta.getUsername() );
      password = databaseMeta.environmentSubstitute( databaseMeta.getPassword() );
    } catch ( RuntimeException e ) {
      url = databaseMeta.getURL( partitionId );
      userName = databaseMeta.getUsername();
      password = databaseMeta.getPassword();
    }

    password = Encr.decryptPasswordOptionallyEncrypted( password );
    // Get the list of pool properties
    Properties originalProperties = databaseMeta.getConnectionPoolingProperties();
    
    // add config to pool
    GenericObjectPool.Config config = createGenericObjectPoolConfig(originalProperties, databaseMeta, maximumSize);
    gpool.setConfig(config);
	
    // Add user/pass
    originalProperties.setProperty( "user", Const.NVL( userName, "" ) );
    originalProperties.setProperty( "password", Const.NVL( password, "" ) );

    // Now, replace the environment variables in there...
    Properties properties = new Properties();
    Iterator<Object> iterator = originalProperties.keySet().iterator();
    while ( iterator.hasNext() ) {
      String key = (String) iterator.next();
      String value = originalProperties.getProperty( key );
      properties.put( key, databaseMeta.environmentSubstitute( value ) );
    }

    // Create factory using these properties.
    //
    ConnectionFactory cf = new DriverManagerConnectionFactory( url, properties );

    new PoolableConnectionFactory( cf, gpool, null, null, false, false );

    for ( int i = 0; i < initialSize; i++ ) {
      try {
        gpool.addObject();
      } catch ( Exception e ) {
        throw new KettleDatabaseException( BaseMessages.getString( PKG,
            "Database.UnableToPreLoadConnectionToConnectionPool.Exception" ), e );
      }
    }

    pd.registerPool( buildPoolName( databaseMeta, partitionId ), gpool );

    log.logBasic( BaseMessages.getString( PKG, "Database.CreatedConnectionPool", databaseMeta.getName() ) );
  }

  public static Connection getConnection( LogChannelInterface log, DatabaseMeta dbMeta,
      String partitionId ) throws Exception {
    return getConnection( log, dbMeta, partitionId, dbMeta.getInitialPoolSize(), dbMeta.getMaximumPoolSize() );
  }

  public static Connection getConnection( LogChannelInterface log, DatabaseMeta dbMeta, String partitionId,
      int initialSize, int maximumSize ) throws Exception {
    lock.lock();
    try {
      if ( !isPoolRegistered( dbMeta, partitionId ) ) {
        createPool( log, dbMeta, partitionId, initialSize, maximumSize );
      }
    } finally {
      lock.unlock();
    }

    return DriverManager.getConnection( "jdbc:apache:commons:dbcp:" + buildPoolName( dbMeta, partitionId ) );
  }

  protected static String buildPoolName( DatabaseMeta dbMeta, String partitionId ) {
    return dbMeta.getName() + Const.NVL( dbMeta.getDatabaseName(), "" )
        + Const.NVL( dbMeta.getHostname(),  ""  ) + Const.NVL( dbMeta.getDatabasePortNumberString(),  ""  )
        + Const.NVL( partitionId, "" );
  }
  
  
  private static GenericObjectPool.Config createGenericObjectPoolConfig(Properties properties, DatabaseMeta databaseMeta, Integer maxActive){
  	GenericObjectPool.Config config = new GenericObjectPool.Config();
  	
    String _maxIdle = properties.getProperty("maxIdle");
    config.maxIdle = (_maxIdle == null ? GenericObjectPool.DEFAULT_MAX_IDLE : Integer.parseInt(_maxIdle));

    String _minIdle = properties.getProperty("minIdle");
    config.minIdle = (_minIdle == null ? GenericObjectPool.DEFAULT_MIN_IDLE : Integer.parseInt(_minIdle));

    String _maxActive = properties.getProperty("maxActive");
    config.maxActive = (_maxActive == null ? (maxActive == null ? GenericObjectPool.DEFAULT_MAX_ACTIVE : maxActive): Integer.parseInt(_maxActive));

    String _maxWait = properties.getProperty("maxWait");
    config.maxWait = (_maxWait == null ? GenericObjectPool.DEFAULT_MAX_WAIT : Integer.parseInt(_maxWait));

    String _minEvictableIdleTimeMillis = properties.getProperty("minEvictableIdleTimeMillis");
    config.minEvictableIdleTimeMillis = (_minEvictableIdleTimeMillis == null ? GenericObjectPool.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS : Integer.parseInt(_minEvictableIdleTimeMillis));

    String _numTestsPerEvictionRun = properties.getProperty("numTestsPerEvictionRun");
    config.numTestsPerEvictionRun = (_numTestsPerEvictionRun == null ? GenericObjectPool.DEFAULT_NUM_TESTS_PER_EVICTION_RUN : Integer.parseInt(_numTestsPerEvictionRun));

    String _softMinEvictableIdleTimeMillis = properties.getProperty("softMinEvictableIdleTimeMillis");
    config.softMinEvictableIdleTimeMillis = (_softMinEvictableIdleTimeMillis == null ? GenericObjectPool.DEFAULT_SOFT_MIN_EVICTABLE_IDLE_TIME_MILLIS : Integer.parseInt(_softMinEvictableIdleTimeMillis));

    String _testOnBorrow = properties.getProperty("testOnBorrow");
    config.testOnBorrow = (_testOnBorrow == null ? GenericObjectPool.DEFAULT_TEST_ON_BORROW : Boolean.parseBoolean(_testOnBorrow));

    String _testOnReturn = properties.getProperty("testOnReturn");
    config.testOnReturn = (_testOnReturn == null ? GenericObjectPool.DEFAULT_TEST_ON_RETURN : Boolean.parseBoolean(_testOnReturn));

    String _testWhileIdle = properties.getProperty("testWhileIdle");
    config.testWhileIdle = (_testWhileIdle == null ? GenericObjectPool.DEFAULT_TEST_WHILE_IDLE : Boolean.parseBoolean(_testWhileIdle));

    String _timeBetweenEvictionRunsMillis = properties.getProperty("timeBetweenEvictionRunsMillis");
    config.timeBetweenEvictionRunsMillis = (_timeBetweenEvictionRunsMillis == null ? GenericObjectPool.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS : Integer.parseInt(_timeBetweenEvictionRunsMillis));

    String _whenExhaustedAction = properties.getProperty("whenExhaustedAction");
    config.whenExhaustedAction = (_whenExhaustedAction == null ? GenericObjectPool.DEFAULT_WHEN_EXHAUSTED_ACTION : Byte.parseByte(_whenExhaustedAction));

    return config;
    }
  }

}
