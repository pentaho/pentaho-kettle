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


package org.pentaho.di.core.date;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;

public class DateCacheTest {

  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();

  @SuppressWarnings( "deprecation" )
  @Test
  public void testDateCache() {
    DateCache cache = new DateCache();
    cache.populate( "yyyy-MM-dd", 2016, 2016 );
    assertEquals( 366, cache.getSize() ); // Leap year
    assertEquals( Calendar.FEBRUARY, cache.lookupDate( "2016-02-29" ).getMonth() );
    assertEquals( 29, cache.lookupDate( "2016-02-29" ).getDate() );
    assertEquals( ( 2016 - 1900 ), cache.lookupDate( "2016-02-29" ).getYear() );
  }
}
