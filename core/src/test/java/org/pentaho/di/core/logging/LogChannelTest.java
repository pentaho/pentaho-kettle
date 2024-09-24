/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.core.logging;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.util.Utils;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class LogChannelTest {

  private LogChannel logChannel;
  private String logChannelSubject = "pdi";
  private String channelId = "1234-5678-abcd-efgh";

  private LogLevel logLevel;
  private LogMessageInterface logMsgInterface;
  private LogChannelFileWriterBuffer logChFileWriterBuffer;
  private MockedStatic<LoggingRegistry> loggingRegistryMockedStatic;
  private MockedStatic<KettleLogStore> kettleLogStoreMockedStatic;
  //private MockedStatic<DefaultLogLevel> defaultLogLevelMockedStatic;

  @Before
  public void setUp() throws Exception {
    DefaultLogLevel.setLogLevel( LogLevel.BASIC );

    logChFileWriterBuffer = mock( LogChannelFileWriterBuffer.class );

    LoggingRegistry regInstance = mock( LoggingRegistry.class );
    when( regInstance.registerLoggingSource( logChannelSubject ) ).thenReturn( channelId );
    when( regInstance.getLogChannelFileWriterBuffer( channelId ) ).thenReturn( logChFileWriterBuffer );

    loggingRegistryMockedStatic = Mockito.mockStatic( LoggingRegistry.class );
    loggingRegistryMockedStatic.when( LoggingRegistry::getInstance ).thenReturn( regInstance );

    kettleLogStoreMockedStatic = Mockito.mockStatic( KettleLogStore.class );

    logLevel = LogLevel.BASIC;
    logMsgInterface = mock( LogMessageInterface.class );
    when( logMsgInterface.getLevel() ).thenReturn( logLevel );

    logChannel = new LogChannel( logChannelSubject );
  }

  @After
  public void cleanup() {
    Mockito.validateMockitoUsage();
    loggingRegistryMockedStatic.close();
    kettleLogStoreMockedStatic.close();
  }

  @Test
  public void testPrintlnWithNullLogChannelFileWriterBuffer() {
    LoggingBuffer loggingBuffer = mock( LoggingBuffer.class );
    kettleLogStoreMockedStatic.when( KettleLogStore::getAppender ).thenReturn( loggingBuffer );

    logChannel.println( logMsgInterface, LogLevel.BASIC );
    verify( logChFileWriterBuffer, times( 1 ) ).addEvent( any( KettleLoggingEvent.class ) );
    verify( loggingBuffer, times( 1 ) ).addLogggingEvent( any( KettleLoggingEvent.class ) );
  }

  @Test
  public void testPrintlnLogNotVisible() {
    when( logMsgInterface.getLevel() ).thenReturn( LogLevel.DETAILED );
    logChannel.println( logMsgInterface, LogLevel.BASIC );
    verify( logChFileWriterBuffer, times( 0 ) ).addEvent( any( KettleLoggingEvent.class ) );
  }

  @Test
  public void testPrintMessageFiltered() {
    LogMessageInterface logMsgInterfaceFil = mock( LogMessageInterface.class );
    when( logMsgInterfaceFil.getLevel() ).thenReturn( LogLevel.BASIC );
    when( logMsgInterfaceFil.toString() ).thenReturn( "a" );

    try ( MockedStatic<Utils> utilsMockedStatic = Mockito.mockStatic( Utils.class ) ) {
      utilsMockedStatic.when( () -> Utils.isEmpty( anyString() ) ).thenReturn( false );

      logChannel.setFilter( "b" );
      logChannel.println( logMsgInterfaceFil, LogLevel.BASIC );
      verify( logChFileWriterBuffer, times( 0 ) ).addEvent( any( KettleLoggingEvent.class ) );
    }
  }

  @Test
  public void testGetHooks() {
    LogChannel logChannel = new LogChannel();
    LoggingObjectInterface loggingObjectInterface = mock( LoggingObjectInterface.class );
    logChannel.setHooks( loggingObjectInterface );

    assertEquals( loggingObjectInterface, logChannel.getHooks() );
  }

  @Test
  public void testLogChannelConstructorWithParentObject() {
    LoggingObjectInterface parentObject = mock( LoggingObjectInterface.class );
    String subject = UUID.randomUUID().toString();
    LogChannel logChannel = new LogChannel( subject, parentObject );

    assertEquals( parentObject, logChannel.getHooks() );
  }

  @Test
  public void testPrintlnHooksCall()  {
    LoggingObjectInterface parentObject = mock( LoggingObjectInterface.class );
    String subject = UUID.randomUUID().toString();
    LogMessageInterface logMessageInterface = mock( LogMessageInterface.class );
    LoggingBuffer loggingBuffer = mock( LoggingBuffer.class );

    when( logMessageInterface.getLevel() ).thenReturn( LogLevel.BASIC );
    when( logMessageInterface.getSubject() ).thenReturn( subject );
    kettleLogStoreMockedStatic.when( KettleLogStore::getAppender ).thenReturn( loggingBuffer );

    LogChannel logChannel = new LogChannel( subject, parentObject );
    logChannel.setFilter( "" );
    logChannel.println( logMessageInterface, LogLevel.BASIC );

    verify( parentObject, times( 1 ) ).callAfterLog();
    verify( parentObject, times( 1 ) ).callBeforeLog();
  }


}
