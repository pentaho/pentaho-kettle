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
package org.pentaho.di.core.logging.log4j;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.KettleLoggingEvent;
import org.pentaho.di.core.logging.LogLevel;

import static org.mockito.Mockito.*;

public class Log4jLoggingTest {

  private Logger logger;

  private Log4jLoggingPluginWithMockedLogger log4jPlugin;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleLogStore.init();
  }

  @Before
  public void before() {
    logger = mock( Logger.class );
    log4jPlugin = new Log4jLoggingPluginWithMockedLogger( logger );
    log4jPlugin.init();
  }

  @After
  public void after() {
    log4jPlugin.dispose();
  }

  @Test
  public void eventAddedError() {
    log4jPlugin.eventAdded( new KettleLoggingEvent( "ERROR_TEST_MESSAGE", 0L, LogLevel.ERROR ) );
    verify( logger ).log( Level.ERROR, "ERROR_TEST_MESSAGE" );
  }

  @Test
  public void eventAddedDebug() {
    log4jPlugin.eventAdded( new KettleLoggingEvent( "DEBUG_TEST_MESSAGE", 0L, LogLevel.DEBUG ) );
    verify( logger ).log( Level.DEBUG, "DEBUG_TEST_MESSAGE" );
  }

  @Test
  public void eventAddedDetailed() {
    log4jPlugin.eventAdded( new KettleLoggingEvent( "DETAILED_TEST_MESSAGE", 0L, LogLevel.DETAILED ) );
    verify( logger ).log( Level.INFO, "DETAILED_TEST_MESSAGE" );
  }

  @Test
  public void eventAddedRowLevel() {
    log4jPlugin.eventAdded( new KettleLoggingEvent( "ROWLEVEL_TEST_MESSAGE", 0L, LogLevel.ROWLEVEL ) );
    verify( logger ).log( Level.DEBUG, "ROWLEVEL_TEST_MESSAGE" );
  }

  @Test
  public void eventAddedBasic() {
    log4jPlugin.eventAdded( new KettleLoggingEvent( "BASIC_TEST_MESSAGE", 0L, LogLevel.BASIC ) );
    verify( logger ).log( Level.INFO, "BASIC_TEST_MESSAGE" );
  }

  @Test
  public void eventAddedMinimal() {
    log4jPlugin.eventAdded( new KettleLoggingEvent( "MINIMAL_TEST_MESSAGE", 0L, LogLevel.MINIMAL ) );
    verify( logger ).log( Level.INFO, "MINIMAL_TEST_MESSAGE" );
  }

  private static final class Log4jLoggingPluginWithMockedLogger extends Log4jLogging {

    private final Logger logger;

    Log4jLoggingPluginWithMockedLogger( Logger logger ) {
      this.logger = logger;
    }

    @Override
    Logger createLogger( String loggerName ) {
      return logger;
    }

  }

}
