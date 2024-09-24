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

package org.pentaho.di.trans.steps.dimensionlookup;

import java.sql.Timestamp;
import java.util.Date;

import org.junit.Test;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaTimestamp;
import org.pentaho.di.core.util.Assert;

public class DimensionCacheTest {

  @Test
  public void testCompareDateInterval() {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaTimestamp( "DATE_FROM" ) );
    rowMeta.addValueMeta( new ValueMetaTimestamp( "DATE_TO" ) );
    int[] keyIndexes = new int[] {};
    int fromDateIndex = 0;
    int toDateIndex = 1;
    DimensionCache dc = new DimensionCache( rowMeta, keyIndexes, fromDateIndex, toDateIndex );

    long t0 = 1425300000000L; // (3/2/15 4:40 PM)
    final Date D1 = new Timestamp( t0 );
    final Date D2 = new Timestamp( t0 + 3600000L );
    final Date D3 = new Timestamp( t0 + 3600000L * 2 );
    final Date D4 = new Timestamp( t0 + 3600000L * 3 );
    final Date D5 = new Timestamp( t0 + 3600000L * 4 );

    // [PDI-13508] NPE in DimensionCache class after update to Java 1.7u76
    // fix prevents NullPointerException in the combinations marked "NPE"

    assertCompareDateInterval( dc, null, null, null, null, 0 );
    assertCompareDateInterval( dc, null, null, D1, null, -1 );

    assertCompareDateInterval( dc, D2, null, null, null, 1 );
    assertCompareDateInterval( dc, D2, null, D1, null, 1 );
    assertCompareDateInterval( dc, D2, null, D2, null, 0 );
    assertCompareDateInterval( dc, D2, null, D3, null, -1 );

    assertCompareDateInterval( dc, D2, D4, null, null, 1 ); // NPE
    assertCompareDateInterval( dc, D2, D4, D1, null, 1 );
    assertCompareDateInterval( dc, D2, D4, D2, null, 0 );
    assertCompareDateInterval( dc, D2, D4, D3, null, 0 );
    assertCompareDateInterval( dc, D2, D4, D4, null, -1 );
    assertCompareDateInterval( dc, D2, D4, D5, null, -1 );

    assertCompareDateInterval( dc, null, D4, null, null, 0 ); // NPE
    assertCompareDateInterval( dc, null, D4, D3, null, 0 );
    assertCompareDateInterval( dc, null, D4, D4, null, -1 ); // NPE
    assertCompareDateInterval( dc, null, D4, D5, null, -1 ); // NPE
  }

  private static void assertCompareDateInterval( DimensionCache dc, Object from1, Object to1, Object from2, Object to2,
      int expectedValue ) {

    final int actualValue = dc.compare( new Object[] { from1, to1 }, new Object[] { from2, to2 } );

    boolean success = ( expectedValue == 0 && actualValue == 0 ) //
        || ( expectedValue < 0 && actualValue < 0 ) //
        || ( expectedValue > 0 && actualValue > 0 );
    Assert.assertTrue( success, "{0} expected, {1} actual. compare( [({2}), ({3})], [({4}), ({5})] )", //
        expectedValue, actualValue, from1, to1, from2, to2 );
  }

}
