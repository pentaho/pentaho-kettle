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
package org.pentaho.di.core.logging;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.TimeZone;

import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Log4jKettleLayoutTest {

  private TimeZone defaultTimeZone;

  @Before
  public void before() {
    defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault( TimeZone.getTimeZone( "UTC" ) );
  }

  @After
  public void after() {
    TimeZone.setDefault( defaultTimeZone );
  }

  @Test
  public void formatNonLogMessage_without_add_time() {
    Log4jKettleLayout layout = new Log4jKettleLayout( false );
    String actualResult = layout.format( mock( LoggingEvent.class ) );
    assertThat( actualResult, equalTo( "<null>" ) );
  }

  @Test
  public void formatNonLogMessage_with_add_time() {
    Log4jKettleLayout layout = new Log4jKettleLayout( true );
    String actualResult = layout.format( mock( LoggingEvent.class ) );
    assertThat( actualResult, equalTo( "1970/01/01 00:00:00 - <null>" ) );
  }

  @Test
  public void formatLogMessage_without_add_time() {
    Log4jKettleLayout layout = new Log4jKettleLayout( false );
    LoggingEvent loggingEvent = mock( LoggingEvent.class );
    LogMessage logMessage = new LogMessage( "TEST_MESSAGE", "TEST_ID", LogLevel.BASIC );
    when( loggingEvent.getMessage() ).thenReturn( logMessage );
    String actualResult = layout.format( loggingEvent );
    assertThat( actualResult, equalTo( "TEST_MESSAGE" ) );
  }

  @Test
  public void formatLogMessage_with_add_time() {
    Log4jKettleLayout layout = new Log4jKettleLayout( true );
    LoggingEvent loggingEvent = mock( LoggingEvent.class );
    LogMessage logMessage = new LogMessage( "TEST_MESSAGE", "TEST_ID", LogLevel.BASIC );
    when( loggingEvent.getMessage() ).thenReturn( logMessage );
    String actualResult = layout.format( loggingEvent );
    assertThat( actualResult, equalTo( "1970/01/01 00:00:00 - TEST_MESSAGE" ) );
  }

}
