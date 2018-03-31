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

package org.pentaho.di.core.database.util;

import com.mysql.jdbc.MysqlDataTruncation;
import com.mysql.jdbc.PacketTooBigException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mariadb.jdbc.internal.stream.MaxAllowedPacketException;

import static org.junit.Assert.assertEquals;

import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.MariaDBDatabaseMeta;
import org.pentaho.di.core.database.MySQLDatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.logging.LogTableCoreInterface;

import static org.mockito.Mockito.*;

public class DatabaseLogExceptionFactoryTest {

  private LogTableCoreInterface logTable;
  private final String SUPPRESSABLE = "org.pentaho.di.core.database.util.DatabaseLogExceptionFactory$SuppressBehaviour";
  private final String THROWABLE = "org.pentaho.di.core.database.util.DatabaseLogExceptionFactory$ThrowableBehaviour";
  private final String
    SUPPRESSABLE_WITH_SHORT_MESSAGE =
    "org.pentaho.di.core.database.util.DatabaseLogExceptionFactory$SuppressableWithShortMessage";
  private final String PROPERTY_VALUE_TRUE = "Y";

  @Before public void setUp() {
    logTable = mock( LogTableCoreInterface.class );
    System.clearProperty( DatabaseLogExceptionFactory.KETTLE_GLOBAL_PROP_NAME );
  }

  @After public void tearDown() {
    System.clearProperty( DatabaseLogExceptionFactory.KETTLE_GLOBAL_PROP_NAME );
  }

  @Test public void testGetExceptionStrategyWithoutException() {
    LogExceptionBehaviourInterface exceptionStrategy = DatabaseLogExceptionFactory.getExceptionStrategy( logTable );
    String strategyName = exceptionStrategy.getClass().getName();
    assertEquals( SUPPRESSABLE, strategyName );
  }

  @Test public void testGetExceptionStrategyWithoutExceptionPropSetY() {
    System.setProperty( DatabaseLogExceptionFactory.KETTLE_GLOBAL_PROP_NAME, PROPERTY_VALUE_TRUE );
    LogExceptionBehaviourInterface exceptionStrategy = DatabaseLogExceptionFactory.getExceptionStrategy( logTable );
    String strategyName = exceptionStrategy.getClass().getName();
    assertEquals( THROWABLE, strategyName );
  }

  @Test public void testExceptionStrategyWithException() {
    LogExceptionBehaviourInterface
      exceptionStrategy =
      DatabaseLogExceptionFactory.getExceptionStrategy( logTable, new Exception() );
    String strategyName = exceptionStrategy.getClass().getName();
    assertEquals( SUPPRESSABLE, strategyName );
  }

  @Test public void testGetExceptionStrategyWithExceptionPropSetY() {
    System.setProperty( DatabaseLogExceptionFactory.KETTLE_GLOBAL_PROP_NAME, PROPERTY_VALUE_TRUE );
    LogExceptionBehaviourInterface
      exceptionStrategy =
      DatabaseLogExceptionFactory.getExceptionStrategy( logTable, new Exception() );
    String strategyName = exceptionStrategy.getClass().getName();
    assertEquals( THROWABLE, strategyName );
  }

  /**
   * PDI-5153
   * Test that in case of PacketTooBigException exception there will be no stack trace in log
   */
  @Test public void testExceptionStrategyWithPacketTooBigException() {
    DatabaseMeta databaseMeta = mock( DatabaseMeta.class );
    DatabaseInterface databaseInterface = new MySQLDatabaseMeta();
    PacketTooBigException e = new PacketTooBigException();

    when( logTable.getDatabaseMeta() ).thenReturn( databaseMeta );
    when( databaseMeta.getDatabaseInterface() ).thenReturn( databaseInterface );

    LogExceptionBehaviourInterface
      exceptionStrategy =
      DatabaseLogExceptionFactory.getExceptionStrategy( logTable, new KettleDatabaseException( e ) );
    String strategyName = exceptionStrategy.getClass().getName();
    assertEquals( SUPPRESSABLE_WITH_SHORT_MESSAGE, strategyName );
  }

  /**
   * PDI-5153
   * Test that in case of MaxAllowedPacketException exception there will be no stack trace in log (MariaDB)
   */
  @Test public void testExceptionStrategyWithMaxAllowedPacketException() {
    DatabaseMeta databaseMeta = mock( DatabaseMeta.class );
    DatabaseInterface databaseInterface = new MariaDBDatabaseMeta();
    MaxAllowedPacketException e = new MaxAllowedPacketException();

    when( logTable.getDatabaseMeta() ).thenReturn( databaseMeta );
    when( databaseMeta.getDatabaseInterface() ).thenReturn( databaseInterface );

    LogExceptionBehaviourInterface
      exceptionStrategy =
      DatabaseLogExceptionFactory.getExceptionStrategy( logTable, new KettleDatabaseException( e ) );
    String strategyName = exceptionStrategy.getClass().getName();
    assertEquals( SUPPRESSABLE_WITH_SHORT_MESSAGE, strategyName );
  }


  /**
   * PDI-5153
   * Test that in case of MysqlDataTruncation exception there will be no stack trace in log
   */
  @Test public void testExceptionStrategyWithMysqlDataTruncationException() {
    DatabaseMeta databaseMeta = mock( DatabaseMeta.class );
    DatabaseInterface databaseInterface = new MySQLDatabaseMeta();
    MysqlDataTruncation e = new MysqlDataTruncation();

    when( logTable.getDatabaseMeta() ).thenReturn( databaseMeta );
    when( databaseMeta.getDatabaseInterface() ).thenReturn( databaseInterface );

    LogExceptionBehaviourInterface
      exceptionStrategy =
      DatabaseLogExceptionFactory.getExceptionStrategy( logTable, new KettleDatabaseException( e ) );
    String strategyName = exceptionStrategy.getClass().getName();
    assertEquals( SUPPRESSABLE_WITH_SHORT_MESSAGE, strategyName );
  }

  /**
   * Property value has priority
   */
  @Test public void testExceptionStrategyWithPacketTooBigExceptionPropSetY() {
    System.setProperty( DatabaseLogExceptionFactory.KETTLE_GLOBAL_PROP_NAME, PROPERTY_VALUE_TRUE );

    DatabaseMeta databaseMeta = mock( DatabaseMeta.class );
    DatabaseInterface databaseInterface = new MySQLDatabaseMeta();
    PacketTooBigException e = new PacketTooBigException();

    when( logTable.getDatabaseMeta() ).thenReturn( databaseMeta );
    when( databaseMeta.getDatabaseInterface() ).thenReturn( databaseInterface );

    LogExceptionBehaviourInterface
      exceptionStrategy =
      DatabaseLogExceptionFactory.getExceptionStrategy( logTable, new KettleDatabaseException( e ) );
    String strategyName = exceptionStrategy.getClass().getName();
    assertEquals( THROWABLE, strategyName );
  }
}
