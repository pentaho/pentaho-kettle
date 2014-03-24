/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
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
package org.pentaho.di.core.row.value;


import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;

import java.sql.PreparedStatement;
import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * User: Dzmitry Stsiapanau Date: 3/24/14 Time: 1:30 PM
 */
public class ValueMetaTimestampTest {
  @Test
  public void testSetPreparedStatementValue() throws Exception {
    ValueMetaTimestamp vm = new ValueMetaTimestamp();
    PreparedStatement ps = mock( PreparedStatement.class );
    doAnswer( new Answer() {
      @Override public Object answer( InvocationOnMock invocationOnMock ) throws Throwable {
        Object ts = invocationOnMock.getArguments()[ 1 ];
        return ts.toString();
      }
    } ).when( ps ).setTimestamp( anyInt(), (Timestamp) anyObject() );

    try {
      vm.setPreparedStatementValue( mock( DatabaseMeta.class ), ps, 0, null );
    } catch ( KettleDatabaseException ex ) {
      fail( "Check PDI-11547" );
    }

  }

  @Test
  public void testCompare() throws Exception {
    ValueMetaTimestamp vm = new ValueMetaTimestamp();
    Timestamp earlier = Timestamp.valueOf( "2012-12-12 12:12:12.121212" );
    Timestamp later = Timestamp.valueOf( "2013-12-12 12:12:12.121212" );
    assertEquals( vm.compare( null, null ), 0 );
    assertEquals( vm.compare( null, earlier ), -1 );
    assertEquals( vm.compare( earlier, null ), 1 );
    assertEquals( vm.compare( earlier, earlier ), 0 );
    assertEquals( vm.compare( earlier, later ), -1 );
    assertEquals( vm.compare( later, earlier ), 1 );
  }
}
