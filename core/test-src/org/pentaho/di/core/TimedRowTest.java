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
