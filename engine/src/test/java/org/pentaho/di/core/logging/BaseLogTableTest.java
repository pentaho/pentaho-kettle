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
import org.mockito.MockedStatic;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;

public class BaseLogTableTest {

  @Test
  public void testSecondJobExecutionDoesNotReturnLogFromFirstExecution() throws Exception {
    try ( MockedStatic<KettleLogStore> kettleLogStoreMockedStatic = mockStatic( KettleLogStore.class );
          MockedStatic<LoggingRegistry> loggingRegistryMockedStatic = mockStatic( LoggingRegistry.class ) ) {

      LoggingBuffer lb = spy( new LoggingBuffer( 10 ) );
      kettleLogStoreMockedStatic.when( KettleLogStore::getAppender ).thenReturn( lb );
      doCallRealMethod().when( lb ).getBuffer( anyString(), anyBoolean(), anyInt() );

      LoggingRegistry lr = mock( LoggingRegistry.class );
      loggingRegistryMockedStatic.when( LoggingRegistry::getInstance ).thenReturn( lr );
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
      BufferLine firstBufferLine = new BufferLine( kLE1 );
      Field bufferSequenceNum = BufferLine.class.getDeclaredField( "sequence" );
      ReflectionUtils.makeAccessible( bufferSequenceNum );
      int startingBufferSequence =
        ( (AtomicInteger) ReflectionUtils.getField( bufferSequenceNum, firstBufferLine ) ).intValue();
      addToBuffer( bl, firstBufferLine );

      VariableSpace vs = mock( VariableSpace.class );

      BaseLogTable baseLogTable = new BaseLogTableTestImpl( vs, null, "", "", "" );

      String s1 = baseLogTable.getLogBuffer( vs, "1", LogStatus.START, "", startingBufferSequence );
      assertTrue( s1.contains( "First Job Execution Logging Event" ) );

      KettleLoggingEvent kLE2 = spy( KettleLoggingEvent.class );
      LogMessage lm2 = new LogMessage( "Second Job Execution Logging Event", "1", LogLevel.BASIC );
      kLE2.setMessage( lm2 );
      addToBuffer( bl, new BufferLine( kLE2 ) );

      String s2 = baseLogTable.getLogBuffer( vs, "1", LogStatus.START, "", startingBufferSequence + 1 );
      assertFalse( s2.contains( "First Job Execution Logging Event" ) );
      assertTrue( s2.contains( "Second Job Execution Logging Event" ) );
    }
  }

  private void addToBuffer( ConcurrentSkipListMap bl, BufferLine bufferLine ) {
    bl.put( bufferLine.getNr(), bufferLine );
  }

  // this may not be essential but it's easier to debug than a mocked abstract class
  class BaseLogTableTestImpl extends BaseLogTable {

    public BaseLogTableTestImpl( VariableSpace space, HasDatabasesInterface databasesInterface,
                                 String connectionName, String schemaName, String tableName ) {
      super( space, databasesInterface, connectionName, schemaName, tableName );
    }

    @Override public String getLogTableCode() {
      return "";
    }

    @Override public String getConnectionNameVariable() {
      return "";
    }

    @Override public String getSchemaNameVariable() {
      return "";
    }

    @Override public String getTableNameVariable() {
      return "";
    }
  }

}
