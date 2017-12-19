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
package org.pentaho.di.core;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class TimedRowTest {
  @Test
  public void testClass() {
    final long time = 1447691729119L;
    final Date date = new Date( time );
    final Object[] data = new Object[] { "value1", "value2", null };
    TimedRow row = new TimedRow( date, data );
    assertSame( data, row.getRow() );
    assertSame( date, row.getLogDate() );
    assertEquals( time, row.getLogtime() );
    assertEquals( "value1, value2, null", row.toString() );
    row.setRow( null );
    assertNull( row.getRow() );
    row.setLogDate( null );
    assertNull( row.getLogDate() );
    assertEquals( 0L, row.getLogtime() );

    row = new TimedRow( data );
    assertSame( data, row.getRow() );
    assertNotSame( date, row.getLogDate() );
  }
}
