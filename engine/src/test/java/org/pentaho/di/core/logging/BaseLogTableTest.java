/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class BaseLogTableTest {

  @Test
  public void testSecondJobExecutionDoesNotReturnLogFromFirstExecution() throws Exception {
    mockStatic( KettleLogStore.class );
    mockStatic( LoggingRegistry.class );
    mockStatic( Utils.class );
    mockStatic( Const.class );

    LoggingBuffer lb = spy( new LoggingBuffer( 10 ) );
    when( KettleLogStore.getAppender() ).thenReturn( lb );
    doCallRealMethod().when( lb ).getBuffer( anyString(), anyBoolean(), anyInt() );

    LoggingRegistry lr = mock( LoggingRegistry.class );
    when( LoggingRegistry.getInstance() ).thenReturn( lr );
    doReturn( List.of( "1" ) ).when( lr ).getLogChannelChildren( anyString() );

    Field privateLoggingRegistryField = LoggingBuffer.class.getDeclaredField( "loggingRegistry" );
    privateLoggingRegistryField.setAccessible( true );
    ReflectionUtils.setField( privateLoggingRegistryField, lb, lr );

    ConcurrentSkipListMap<Integer, BufferLine> bl = new ConcurrentSkipListMap<>();
    Field privateLBufferField = LoggingBuffer.class.getDeclaredField( "buffer" );
    privateLBufferField.setAccessible( true );
    ReflectionUtils.setField( privateLBufferField, lb, bl );

    KettleLoggingEvent kLE1 = spy( KettleLoggingEvent.class );
    LogMessage lm = new LogMessage( "First Job Execution Logging Event", "1", LogLevel.BASIC );
    kLE1.setMessage( lm );
    addToBuffer( bl, new BufferLine( kLE1 ) );

    BaseLogTable baseLogTable = mock( BaseLogTable.class );
    doCallRealMethod().when( baseLogTable ).getLogBuffer( any( VariableSpace.class ), anyString(), any( LogStatus.class ), anyString(), anyInt() );

    VariableSpace vs = mock( VariableSpace.class );

    String s1 = baseLogTable.getLogBuffer( vs, "1", LogStatus.START, "", 1 );
    assertTrue( s1.contains( "First Job Execution Logging Event" ) );

    KettleLoggingEvent kLE2 = spy( KettleLoggingEvent.class );
    LogMessage lm2 = new LogMessage( "Second Job Execution Logging Event", "1", LogLevel.BASIC );
    kLE2.setMessage( lm2 );
    addToBuffer( bl, new BufferLine( kLE2 ) );

    String s2 = baseLogTable.getLogBuffer( vs, "1", LogStatus.START, "", 2 );
    assertFalse( s2.contains( "First Job Execution Logging Event" ) );
    assertTrue( s2.contains( "Second Job Execution Logging Event" ) );
  }

  private void addToBuffer( ConcurrentSkipListMap bl, BufferLine bufferLine ) {
    bl.put( bufferLine.getNr(), bufferLine );
  }

}
