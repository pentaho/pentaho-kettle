/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
import org.junit.runner.RunWith;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
@PrepareForTest( { KettleLogStore.class, Utils.class, Const.class } )
public class BaseLogTableTest {

  @Test
  public void testRemoveChannelFromBufferCallInGetLogBufferInFirstJobExecution() {
    StringBuffer sb = new StringBuffer( "" );
    LoggingBuffer lb = mock( LoggingBuffer.class );
    doReturn( sb ).when( lb ).getBuffer( anyString(), anyBoolean() );

    mockStatic( KettleLogStore.class );
    mockStatic( Utils.class );
    mockStatic( Const.class );
    when( KettleLogStore.getAppender() ).thenReturn( lb );

    BaseLogTable baseLogTable = mock( BaseLogTable.class );
    doCallRealMethod().when( baseLogTable ).getLogBuffer( any( VariableSpace.class ), anyString(), any( LogStatus.class ), anyString() );

    VariableSpace vs = mock( VariableSpace.class );

    String s1 = baseLogTable.getLogBuffer( vs, "1", LogStatus.START, null );
    String s2 = baseLogTable.getLogBuffer( vs, "1", LogStatus.END, null );

    assertEquals( Const.CR + "START" + Const.CR, s1 );
    assertEquals( Const.CR + "START" + Const.CR, s1 + Const.CR + "END" + Const.CR, s2 );

    verify( lb, times( 1 ) ).removeChannelFromBuffer( "1" );
  }

  @Test
  public void testRemoveChannelFromBufferCallInGetLogBufferInRecursiveJobExecution() {
    StringBuffer sb = new StringBuffer( "Event previously executed for the same Job" );
    LoggingBuffer lb = mock( LoggingBuffer.class );
    doReturn( sb ).when( lb ).getBuffer( anyString(), anyBoolean() );

    mockStatic( KettleLogStore.class );
    mockStatic( Utils.class );
    mockStatic( Const.class );
    when( KettleLogStore.getAppender() ).thenReturn( lb );

    BaseLogTable baseLogTable = mock( BaseLogTable.class );
    doCallRealMethod().when( baseLogTable ).getLogBuffer( any( VariableSpace.class ), anyString(), any( LogStatus.class ), anyString() );

    VariableSpace vs = mock( VariableSpace.class );

    String s1 = baseLogTable.getLogBuffer( vs, "1", LogStatus.START, null );
    String s2 = baseLogTable.getLogBuffer( vs, "1", LogStatus.END, null );

    //removeChannelFromBuffer function is void - need to simulate the behaviour here
    s1 = s1.replace( "Event previously executed for the same Job", "" );
    s2 = s2.replace( "Event previously executed for the same Job", "" );


    assertEquals( Const.CR + "START" + Const.CR, s1 );
    assertEquals( Const.CR + "START" + Const.CR, s1 + Const.CR + "END" + Const.CR, s2 );

    verify( lb, times( 1 ) ).removeChannelFromBuffer( "1" );

  }

}
