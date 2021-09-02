/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2019-2020 by Hitachi Vantara : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */
package org.pentaho.di.trans.ael.websocket.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelFileWriterBuffer;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LogMessageInterface;
import org.pentaho.di.core.logging.LoggingRegistry;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.engine.api.remote.StopMessage;
import org.pentaho.di.trans.ael.websocket.DaemonMessagesClientEndpoint;
import org.pentaho.di.trans.ael.websocket.TransWebSocketEngineAdapter;
import org.pentaho.di.trans.ael.websocket.exception.MessageEventHandlerExecutionException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.spy;

@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
@PrepareForTest( {DefaultLogLevel.class, LoggingRegistry.class, LogLevel.class, KettleLogStore.class, Utils.class} )
public class StopMessageEventHandlerTest {
  @Mock
  TransWebSocketEngineAdapter transWebSocketEngineAdapter;

  @Mock
  DaemonMessagesClientEndpoint daemonMessagesClientEndpoint;

  private LogChannel logChannel;
  private String logChannelSubject = "pdi";
  private String channelId = "1234-5678-abcd-efgh";

  private LogLevel logLevel;
  private LogMessageInterface logMsgInterface;
  private LogChannelFileWriterBuffer logChFileWriterBuffer;

  private AtomicInteger errors = new AtomicInteger( 1 );
  private CountDownLatch transFinishSignal = new CountDownLatch( 1 );
  private boolean cancelling = false;
  private String id;

  private StopMessageEventHandler handlerUnderTest;

  @Before
  public void setUp() throws Exception {
    KettleLogStore.init();
    this.id = UUID.randomUUID().toString();
    LogLevel logLevelStatic = PowerMockito.mock( LogLevel.class );
    Whitebox.setInternalState( logLevelStatic, "name", "Basic" );
    Whitebox.setInternalState( logLevelStatic, "ordinal", 3 );

    PowerMockito.mockStatic( DefaultLogLevel.class );
    PowerMockito.when( DefaultLogLevel.getLogLevel() ).thenReturn( LogLevel.BASIC );

    logChFileWriterBuffer = Mockito.mock( LogChannelFileWriterBuffer.class );

    LoggingRegistry regInstance = Mockito.mock( LoggingRegistry.class );
    Mockito.when( regInstance.registerLoggingSource( logChannelSubject ) ).thenReturn( channelId );
    Mockito.when( regInstance.getLogChannelFileWriterBuffer( channelId ) ).thenReturn( logChFileWriterBuffer );

    PowerMockito.mockStatic( LoggingRegistry.class );
    PowerMockito.when( LoggingRegistry.getInstance() ).thenReturn( regInstance );

    logLevel = PowerMockito.mock( LogLevel.class );
    Whitebox.setInternalState( logLevel, "name", "Basic" );
    Whitebox.setInternalState( logLevel, "ordinal", 3 );

    logMsgInterface = Mockito.mock( LogMessageInterface.class );
    Mockito.when( logMsgInterface.getLevel() ).thenReturn( logLevel );

    logChannel = spy( new LogChannel( logChannelSubject ) );
    handlerUnderTest = new StopMessageEventHandler( logChannel,
        errors,
        transFinishSignal,
        transWebSocketEngineAdapter,
        cancelling );

    when( transWebSocketEngineAdapter.getDaemonEndpoint() ).thenReturn( daemonMessagesClientEndpoint );
  }

  @Test
  public void stopMessageWithFailureTest() throws MessageEventHandlerExecutionException {
    StopMessage stubStopMessage = StopMessage.builder()
        .reasonPhrase( "Stub Fail Message" )
        .result( StopMessage.Status.FAILED )
        .requestUUID( this.id )
        .build();

    handlerUnderTest.execute( stubStopMessage );

    verify( logChannel, times( 2 ) ).logError( anyString() );
    verify( logChannel, times( 1 ) )
        .logError( "Please check with cluster administrator." );
  }

  @Test
  public void stopMessageWithSessionKilledTest() throws MessageEventHandlerExecutionException {
    StopMessage stubStopMessage = StopMessage.builder()
        .reasonPhrase( "Stub Fail Message" )
        .result( StopMessage.Status.SESSION_KILLED )
        .requestUUID( this.id )
        .build();

    handlerUnderTest.execute( stubStopMessage );

    verify( logChannel, times( 1 ) ).logError( anyString() );
  }

  @Test
  public void stopMessageWithSuccessTest() throws MessageEventHandlerExecutionException {
    StopMessage stubStopMessage = StopMessage.builder()
        .reasonPhrase( "Stub Fail Message" )
        .result( StopMessage.Status.SUCCESS )
        .requestUUID( this.id )
        .build();

    handlerUnderTest.execute( stubStopMessage );

    verify( logChannel, times( 1 ) ).logBasic( anyString() );
  }

}
