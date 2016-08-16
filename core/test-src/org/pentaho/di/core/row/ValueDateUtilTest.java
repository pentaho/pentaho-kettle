/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.row;

import org.junit.After;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.value.ValueMetaDate;

import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;


public class ValueDateUtilTest {

  @After
  public void tearDown() {
    System.clearProperty( Const.KETTLE_COMPATIBILITY_CALCULATION_TIMEZONE_DECOMPOSITION );
  }

  @Test
  public void shouldCalculateHourOfDayUsingValueMetasTimeZoneByDefault() throws KettleValueException {
    Calendar date = Calendar.getInstance();
    date.setTimeInMillis( 1454313600000L ); // 2016-07-01 08:00:00 UTC
    ValueMetaInterface valueMetaDate = new ValueMetaDate();
    valueMetaDate.setDateFormatTimeZone( TimeZone.getTimeZone( "CET" ) ); // UTC +1
    long offsetCET = (long) TimeZone.getTimeZone( "CET" ).getRawOffset() / 3600000;

    Object hourOfDayCET = ValueDataUtil.hourOfDay( valueMetaDate, date.getTime() );

    assertEquals( 8L + offsetCET, hourOfDayCET );
  }

  @Test
  public void shouldCalculateHourOfDayUsingLocalTimeZoneIfPropertyIsSet() throws KettleValueException {
    Calendar date = Calendar.getInstance();
    date.setTimeInMillis( 1454313600000L ); // 2016-07-01 08:00:00 UTC
    ValueMetaInterface valueMetaDate = new ValueMetaDate();
    valueMetaDate.setDateFormatTimeZone( TimeZone.getTimeZone( "CET" ) ); // UTC +1
    long offsetLocal = (long) TimeZone.getDefault().getRawOffset() / 3600000;
    System.setProperty( Const.KETTLE_COMPATIBILITY_CALCULATION_TIMEZONE_DECOMPOSITION, "true" );

    Object hourOfDayLocal = ValueDataUtil.hourOfDay( valueMetaDate, date.getTime() );

    assertEquals( 8L + offsetLocal, hourOfDayLocal );
  }
}
