/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.scriptvalues_mod;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ScriptableObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ScriptValuesAddedFunctionsTest {

  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void testTruncDate() {
    Date dateBase = new Date( 118, Calendar.FEBRUARY, 15, 11, 11, 11 ); // 2018-02-15 11:11:11
    Calendar c = Calendar.getInstance();
    c.set( 2011, Calendar.NOVEMBER, 11, 11, 11, 11 ); // 2011-11-11 11:11:11
    c.set( Calendar.MILLISECOND, 11 );

    Date rtn = null;
    Calendar c2 = Calendar.getInstance();
    rtn = ScriptValuesAddedFunctions.truncDate( dateBase, 5 );
    c2.setTime( rtn );
    Assert.assertEquals( Calendar.JANUARY, c2.get( Calendar.MONTH ) );
    rtn = ScriptValuesAddedFunctions.truncDate( dateBase, 4 );
    c2.setTime( rtn );
    Assert.assertEquals( 1, c2.get( Calendar.DAY_OF_MONTH ) );
    rtn = ScriptValuesAddedFunctions.truncDate( dateBase, 3 );
    c2.setTime( rtn );
    Assert.assertEquals( 0, c2.get( Calendar.HOUR_OF_DAY ) );
    rtn = ScriptValuesAddedFunctions.truncDate( dateBase, 2 );
    c2.setTime( rtn );
    Assert.assertEquals( 0, c2.get( Calendar.MINUTE ) );
    rtn = ScriptValuesAddedFunctions.truncDate( dateBase, 1 );
    c2.setTime( rtn );
    Assert.assertEquals( 0, c2.get( Calendar.SECOND ) );
    rtn = ScriptValuesAddedFunctions.truncDate( dateBase, 0 );
    c2.setTime( rtn );
    Assert.assertEquals( 0, c2.get( Calendar.MILLISECOND ) );
    try {
      ScriptValuesAddedFunctions.truncDate( rtn, 6 ); // Should throw exception
      Assert.fail( "Expected exception - passed in level > 5 to truncDate" );
    } catch ( Exception expected ) {
      // Should get here
    }
    try {
      ScriptValuesAddedFunctions.truncDate( rtn, -7 ); // Should throw exception
      Assert.fail( "Expected exception - passed in level < 0  to truncDate" );
    } catch ( Exception expected ) {
      // Should get here
    }
  }

  @Test
  public void testDateDiff_DefaultDST_Forward() {
    System.setProperty( Const.KETTLE_DATEDIFF_DST_AWARE, Const.KETTLE_DATEDIFF_DST_AWARE_DEFAULT );

    doDateDiff_Forward( 3.0 );
  }

  @Test
  public void testDateDiff_AwareDST_Forward() {
    System.setProperty( Const.KETTLE_DATEDIFF_DST_AWARE, "Y" );

    doDateDiff_Forward( 2.0 );
  }

  @Test
  public void testDateDiff_IgnoreDST_Forward() {
    System.setProperty( Const.KETTLE_DATEDIFF_DST_AWARE, Const.KETTLE_DATEDIFF_DST_AWARE_DEFAULT );

    doDateDiff_Forward( 3.0 );
  }

  @Test
  public void testDateDiff_DefaultDST_Backward() {
    System.setProperty( Const.KETTLE_DATEDIFF_DST_AWARE, Const.KETTLE_DATEDIFF_DST_AWARE_DEFAULT );

    doDateDiff_Backward( 3.0 );
  }

  @Test
  public void testDateDiff_AwareDST_Backward() {
    System.setProperty( Const.KETTLE_DATEDIFF_DST_AWARE, "Y" );

    doDateDiff_Backward( 4.0 );
  }

  @Test
  public void testDateDiff_IgnoreDST_Backward() {
    System.setProperty( Const.KETTLE_DATEDIFF_DST_AWARE, Const.KETTLE_DATEDIFF_DST_AWARE_DEFAULT );

    doDateDiff_Backward( 3.0 );
  }

  private void doDateDiff_Forward( Double expectedResult ) {
    // Specify the Timezone to be used: for London, Summer time will come into effect in the 28th of March, 2021
    // At 1:00:00, clocks move forward 1 hour.
    TimeZone.setDefault( TimeZone.getTimeZone( "Europe/London" ) );

    ScriptableObject jsScope = ContextFactory.getGlobal().enterContext().initStandardObjects();

    // The dates
    Calendar cal = Calendar.getInstance( TimeZone.getDefault(), Locale.UK );
    cal.set( 2021, Calendar.MARCH, 28, 0, 30, 0 );
    Object d1 = Context.javaToJS( cal.getTime(), jsScope );
    cal.set( 2021, Calendar.MARCH, 28, 3, 30, 0 );
    Object d2 = Context.javaToJS( cal.getTime(), jsScope );

    // First, try "d1 - d2"
    Object[] jsArgs = { d1, d2, "hh" };

    Object diff = ScriptValuesAddedFunctions.dateDiff( null, null, jsArgs, null );

    Assert.assertNotNull( diff );
    Assert.assertEquals( expectedResult, diff );

    // And, then, try "d2 - d1"
    jsArgs = new Object[] { d2, d1, "hh" };

    diff = ScriptValuesAddedFunctions.dateDiff( null, null, jsArgs, null );

    Assert.assertNotNull( diff );
    Assert.assertEquals( -expectedResult, diff );
  }

  private void doDateDiff_Backward( Double expectedResult ) {
    // Specify the Timezone to be used: for London, Winter time will come into effect in the 31st of October, 2021
    // At 2:00:00, clocks move backward 1 hour.
    TimeZone.setDefault( TimeZone.getTimeZone( "Europe/London" ) );

    ScriptableObject jsScope = ContextFactory.getGlobal().enterContext().initStandardObjects();

    // The dates
    Calendar cal = Calendar.getInstance( TimeZone.getDefault(), Locale.UK );
    cal.set( 2021, Calendar.OCTOBER, 31, 0, 30, 0 );
    Object d1 = Context.javaToJS( cal.getTime(), jsScope );
    cal.set( 2021, Calendar.OCTOBER, 31, 3, 30, 0 );
    Object d2 = Context.javaToJS( cal.getTime(), jsScope );

    // First, try "d1 - d2"
    Object[] jsArgs = { d1, d2, "hh" };

    Object diff = ScriptValuesAddedFunctions.dateDiff( null, null, jsArgs, null );

    Assert.assertNotNull( diff );
    Assert.assertEquals( expectedResult, diff );

    // And, then, try "d2 - d1"
    jsArgs = new Object[] { d2, d1, "hh" };

    diff = ScriptValuesAddedFunctions.dateDiff( null, null, jsArgs, null );

    Assert.assertNotNull( diff );
    Assert.assertEquals( -expectedResult, diff );
  }
}
