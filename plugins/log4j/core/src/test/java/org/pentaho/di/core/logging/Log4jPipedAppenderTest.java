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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PipedOutputStream;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.Const;

public class Log4jPipedAppenderTest extends Log4jAppenderTest {

  private Log4jPipedAppender log4jPipedAppender;

  @Before
  public void before() {
    log4jPipedAppender = new Log4jPipedAppender();
  }

  @Test
  public void setPipedOutputStream() {
    PipedOutputStream pipedOutputStream = new PipedOutputStream();
    log4jPipedAppender.setPipedOutputStream( pipedOutputStream );
    assertThat( log4jPipedAppender.getPipedOutputStream(), is( pipedOutputStream ) );
  }

  @Test
  public void doAppend() throws IOException {
    PipedOutputStream pipedOutputStream = mock( PipedOutputStream.class );
    LoggingEvent loggingEvent = mock( LoggingEvent.class );
    Layout testLayout = mock( Layout.class );
    when( testLayout.format( loggingEvent ) ).thenReturn( "LOG_TEST_LINE" );
    log4jPipedAppender.setLayout( testLayout );
    log4jPipedAppender.setPipedOutputStream( pipedOutputStream );
    log4jPipedAppender.doAppend( loggingEvent );
    verify( pipedOutputStream ).write( ( "LOG_TEST_LINE" + Const.CR ).getBytes() );
  }

  @Test
  public void close() throws IOException {
    PipedOutputStream pipedOutputStream = mock( PipedOutputStream.class );
    log4jPipedAppender.setPipedOutputStream( pipedOutputStream );
    log4jPipedAppender.close();
    verify( pipedOutputStream ).close();
  }

  @Override
  public Appender getAppender() {
    return log4jPipedAppender;
  }

}
