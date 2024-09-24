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

package org.pentaho.di.trans.steps.script;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

public class ScriptAddedFunctionsTest {

  @Test
  public void testTruncDate() {
    Date dateBase = new Date( 118, Calendar.FEBRUARY, 15, 11, 11, 11 ); // 2018-02-15 11:11:11
    Calendar c = Calendar.getInstance();
    c.set( 2011, Calendar.NOVEMBER, 11, 11, 11, 11 ); // 2011-11-11 11:11:11
    c.set( Calendar.MILLISECOND, 11 );

    Date rtn = null;
    Calendar c2 = Calendar.getInstance();
    rtn = ScriptAddedFunctions.truncDate( dateBase, 5 );
    c2.setTime( rtn );
    Assert.assertEquals( Calendar.JANUARY, c2.get( Calendar.MONTH ) );
    rtn = ScriptAddedFunctions.truncDate( dateBase, 4 );
    c2.setTime( rtn );
    Assert.assertEquals( 1, c2.get( Calendar.DAY_OF_MONTH ) );
    rtn = ScriptAddedFunctions.truncDate( dateBase, 3 );
    c2.setTime( rtn );
    Assert.assertEquals( 0, c2.get( Calendar.HOUR_OF_DAY ) );
    rtn = ScriptAddedFunctions.truncDate( dateBase, 2 );
    c2.setTime( rtn );
    Assert.assertEquals( 0, c2.get( Calendar.MINUTE ) );
    rtn = ScriptAddedFunctions.truncDate( dateBase, 1 );
    c2.setTime( rtn );
    Assert.assertEquals( 0, c2.get( Calendar.SECOND ) );
    rtn = ScriptAddedFunctions.truncDate( dateBase, 0 );
    c2.setTime( rtn );
    Assert.assertEquals( 0, c2.get( Calendar.MILLISECOND ) );
    try {
      ScriptAddedFunctions.truncDate( rtn, 6 ); // Should throw exception
      Assert.fail( "Expected exception - passed in level > 5 to truncDate" );
    } catch ( Exception expected ) {
      // Should get here
    }
    try {
      ScriptAddedFunctions.truncDate( rtn, -7 ); // Should throw exception
      Assert.fail( "Expected exception - passed in level < 0  to truncDate" );
    } catch ( Exception expected ) {
      // Should get here
    }
  }

}
