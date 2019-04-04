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

package org.pentaho.di.trans.steps.databaselookup.readallcache;

import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Andrey Khayrutdinov
 */
public class SearchingContextTest {

  @Test
  public void returnsClearWorkingSet() {
    SearchingContext ctx = new SearchingContext();
    ctx.init( 4 );
    ctx.getWorkingSet().set( 1 );
    assertEquals( "Should return cleared object", -1, ctx.getWorkingSet().nextSetBit( 0 ) );
  }

  @Test
  public void intersectionDetectsBecomingEmpty() {

    SearchingContext ctx = new SearchingContext();
    ctx.init( 4 );

    BitSet set = ctx.getWorkingSet();
    set.set( 1 );
    set.set( 2 );
    ctx.intersect( set, false );
    assertFalse( ctx.isEmpty() );

    set = ctx.getWorkingSet();
    ctx.intersect( set, false );
    assertTrue( ctx.isEmpty() );
  }
}
