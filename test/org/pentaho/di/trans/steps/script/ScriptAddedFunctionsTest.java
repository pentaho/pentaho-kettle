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
