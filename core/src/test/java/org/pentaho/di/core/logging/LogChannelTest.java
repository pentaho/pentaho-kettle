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

package org.pentaho.di.core.logging;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.pentaho.di.core.util.Utils;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.mockito.Mockito.times;
import static org.powermock.reflect.Whitebox.getInternalState;
import static org.powermock.reflect.Whitebox.setInternalState;

@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
@PrepareForTest( {DefaultLogLevel.class, LoggingRegistry.class, LogLevel.class, KettleLogStore.class, Utils.class} )
public class LogChannelTest {

  private LogChannel logChannel;
  private String logChannelSubject = "pdi";
  private String channelId = "1234-5678-abcd-efgh";

  private LogLevel logLevel;
  private LogMessageInterface logMsgInterface;
  private LogChannelFileWriterBuffer logChFileWriterBuffer;

  @Before
  public void setUp() throws Exception {
    LogLevel logLevelStatic = PowerMockito.mock( LogLevel.class );
    setInternalState( logLevelStatic, "name", "Basic" );
    setInternalState( logLevelStatic, "ordinal", 3 );

    PowerMockito.mockStatic( DefaultLogLevel.class );
    when( DefaultLogLevel.getLogLevel() ).thenReturn( LogLevel.BASIC );

    logChFileWriterBuffer = mock( LogChannelFileWriterBuffer.class );

    LoggingRegistry regInstance = mock( LoggingRegistry.class );
    Mockito.when( regInstance.registerLoggingSource( logChannelSubject ) ).thenReturn( channelId );
    Mockito.when( regInstance.getLogChannelFileWriterBuffer( channelId ) ).thenReturn( logChFileWriterBuffer );

    PowerMockito.mockStatic( LoggingRegistry.class );
    when( LoggingRegistry.getInstance() ).thenReturn( regInstance );

    logLevel = PowerMockito.mock( LogLevel.class );
    setInternalState( logLevel, "name", "Basic" );
    setInternalState( logLevel, "ordinal", 3 );

    logMsgInterface = mock( LogMessageInterface.class );
    Mockito.when( logMsgInterface.getLevel() ).thenReturn( logLevel );

    logChannel = new LogChannel( logChannelSubject );
  }

  @Test
  public void testPrintlnWithNullLogChannelFileWriterBuffer() {
    when( logLevel.isVisible( any( LogLevel.class ) ) ).thenReturn( true );

    LoggingBuffer loggingBuffer = mock( LoggingBuffer.class );
    PowerMockito.mockStatic( KettleLogStore.class );
    when( KettleLogStore.getAppender() ).thenReturn( loggingBuffer );

    logChannel.println( logMsgInterface, LogLevel.BASIC );
    verify( logChFileWriterBuffer, times( 1 ) ).addEvent( any( KettleLoggingEvent.class ) );
    verify( loggingBuffer, times( 1 ) ).addLogggingEvent( any( KettleLoggingEvent.class ) );
  }

  @Test
  public void testPrintlnLogNotVisible() {
    when( logLevel.isVisible( any( LogLevel.class ) ) ).thenReturn( false );
    logChannel.println( logMsgInterface, LogLevel.BASIC );
    verify( logChFileWriterBuffer, times( 0 ) ).addEvent( any( KettleLoggingEvent.class ) );
  }

  @Test
  public void testPrintMessageFiltered() {
    LogLevel logLevelFil = PowerMockito.mock( LogLevel.class );
    setInternalState( logLevelFil, "name", "Error" );
    setInternalState( logLevelFil, "ordinal", 1 );
    when( logLevelFil.isError() ).thenReturn( false );

    LogMessageInterface logMsgInterfaceFil = mock( LogMessageInterface.class );
    Mockito.when( logMsgInterfaceFil.getLevel() ).thenReturn( logLevelFil );
    Mockito.when( logMsgInterfaceFil.toString() ).thenReturn( "a" );

    PowerMockito.mockStatic( Utils.class );
    when( Utils.isEmpty( anyString() ) ).thenReturn( false );

    when( logLevelFil.isVisible( any( LogLevel.class ) ) ).thenReturn( true );
    logChannel.setFilter( "b" );
    logChannel.println( logMsgInterfaceFil, LogLevel.BASIC );
    verify( logChFileWriterBuffer, times( 0 ) ).addEvent( any( KettleLoggingEvent.class ) );
  }

  @Test
  public void testGetHooks() {
    LogChannel logChannel = new LogChannel();
    LoggingObjectInterface loggingObjectInterface = mock( LoggingObjectInterface.class );
    logChannel.setHooks( loggingObjectInterface );

    assertEquals( loggingObjectInterface, getInternalState( logChannel, "logChannelHooks" ) );
  }

  @Test
  public void testSetHooks() {
    LogChannel logChannel = new LogChannel();
    LoggingObjectInterface loggingObjectInterface = mock( LoggingObjectInterface.class );
    setInternalState( logChannel, "logChannelHooks", loggingObjectInterface );

    assertEquals( loggingObjectInterface, logChannel.getHooks() );
  }

  @Test
  public void testLogChannelConstructorWithParentObject() {
    LoggingObjectInterface parentObject = mock( LoggingObjectInterface.class );
    String subject = UUID.randomUUID().toString();
    LogChannel logChannel = new LogChannel( subject, parentObject );

    assertEquals( parentObject, getInternalState( logChannel, "logChannelHooks" ) );
  }

  @Test
  public void testPrintlnHooksCall()  {
    LoggingObjectInterface parentObject = mock( LoggingObjectInterface.class );
    String subject = UUID.randomUUID().toString();
    LogMessageInterface logMessageInterface = mock( LogMessageInterface.class );
    LogLevel logLevel = mock( LogLevel.class );
    LoggingBuffer loggingBuffer = mock( LoggingBuffer.class );

    when( logMessageInterface.getLevel() ).thenReturn( LogLevel.BASIC );
    when( logLevel.getLevel() ).thenReturn( LogLevel.BASIC.getLevel() );
    PowerMockito.mockStatic( KettleLogStore.class );
    when( KettleLogStore.getAppender() ).thenReturn( loggingBuffer );

    LogChannel logChannel = spy( new LogChannel( subject, parentObject ) );
    logChannel.println( logMessageInterface, logLevel );

    verify( logChannel, times( 1 ) ).callAfterLog();
    verify( logChannel, times( 1 ) ).callBeforeLog();
  }


}
