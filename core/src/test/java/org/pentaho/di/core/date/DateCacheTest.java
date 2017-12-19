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

package org.pentaho.di.core.date;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Test;

public class DateCacheTest {

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
