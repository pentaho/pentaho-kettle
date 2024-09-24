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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This is a base class for several similar cases. All of them are checking how indexes work with the same tuple of data:
 * [0, 1, 2, 2, 3]. Since the data set is known, each subclass show implement tests for the following values:
 * <ul>
 *   <li>-1</li>
 *   <li>0</li>
 *   <li>1</li>
 *   <li>2</li>
 *   <li>3</li>
 *   <li>100</li>
 * </ul>
 *
 * @author Andrey Khayrutdinov
 */
public abstract class IndexTestBase<T extends Index> {

  private static Long[][] toMatrix( long... values ) {
    Long[][] result = new Long[ values.length ][];
    for ( int i = 0; i < values.length; i++ ) {
      result[ i ] = new Long[] { values[ i ] };
    }
    return result;
  }

  static List<Object[]> createSampleData() {
    // sorted, reversely sorted, and shuffled data
    return Arrays.asList(
      new Object[] { toMatrix( 0, 1, 2, 2, 3 ) },
      new Object[] { toMatrix( 3, 2, 2, 1, 0 ) },
      new Object[] { toMatrix( 1, 3, 2, 0, 2 ) }
    );
  }


  final Long[][] rows;
  private final Class<T> clazz;

  T index;
  SearchingContext context;

  public IndexTestBase( Class<T> clazz, Long[][] rows ) {
    this.rows = rows;
    this.clazz = clazz;
  }

  @Before
  public void setUp() throws Exception {
    index = createIndexInstance( 0, new ValueMetaInteger(), 5 );
    index.performIndexingOf( rows );

    context = new SearchingContext();
    context.init( 5 );
  }

  T createIndexInstance( int column, ValueMetaInterface meta, int rowsAmount ) throws Exception {
    return clazz
      .getDeclaredConstructor( int.class, ValueMetaInterface.class, int.class )
      .newInstance( column, meta, rowsAmount );
  }

  @After
  public void tearDown() {
    index = null;
    context = null;
  }


  void testFindsNothing( long value ) {
    assertFalse( context.isEmpty() );
    index.applyRestrictionsTo( context, new ValueMetaInteger(), value );
    assertTrue( "Expected not to find anything matching " + value, context.isEmpty() );
  }


  void testFindsCorrectly( long lookupValue, int expectedAmount ) {
    assertFalse( context.isEmpty() );
    index.applyRestrictionsTo( context, new ValueMetaInteger(), lookupValue );

    assertFalse( "Expected to find something", context.isEmpty() );

    BitSet actual = context.getCandidates();
    int cnt = expectedAmount;
    int lastSetBit = 0;
    while ( cnt > 0 ) {
      lastSetBit = actual.nextSetBit( lastSetBit );
      if ( lastSetBit < 0 ) {
        fail( "Expected to find " + expectedAmount + " values, but got: " + actual.toString() );
      }

      doAssertMatches( actual, lookupValue, rows[ lastSetBit ][ 0 ] );

      lastSetBit++;
      cnt--;
    }
  }

  abstract void doAssertMatches( BitSet candidates, long lookupValue, long actualValue );

  @Test
  public abstract void lookupFor_MinusOne();

  @Test
  public abstract void lookupFor_Zero();

  @Test
  public abstract void lookupFor_One();

  @Test
  public abstract void lookupFor_Two();

  @Test
  public abstract void lookupFor_Three();

  @Test
  public abstract void lookupFor_Hundred();
}
