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


package org.pentaho.di.trans.steps.databaselookup.readallcache;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.pentaho.di.core.row.ValueMetaInterface;

import java.util.BitSet;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * @author Andrey Khayrutdinov
 */
@RunWith( Parameterized.class )
public class GeIndexTest extends IndexTestBase<LtIndex> {

  @Parameterized.Parameters
  public static List<Object[]> createSampleData() {
    return IndexTestBase.createSampleData();
  }

  public GeIndexTest( Long[][] rows ) {
    super( LtIndex.class, rows );
  }

  @Override
  void doAssertMatches( BitSet candidates, long lookupValue, long actualValue ) {
    if ( !( actualValue >= lookupValue ) ) {
      fail( String.format( "All found values are expected to be greater than [%d] or equal to it, but got [%d] among %s",
        lookupValue, actualValue, candidates ) );
    }
  }

  @Override
  LtIndex createIndexInstance( int column, ValueMetaInterface meta, int rowsAmount ) throws Exception {
    return (LtIndex) LtIndex.greaterOrEqualCache( column, meta, rowsAmount );
  }


  @Override
  public void lookupFor_MinusOne() {
    testFindsCorrectly( -1, 5 );
  }

  @Override
  public void lookupFor_Zero() {
    testFindsCorrectly( 0, 5 );
  }

  @Override
  public void lookupFor_One() {
    testFindsCorrectly( 1, 4 );
  }

  @Override
  public void lookupFor_Two() {
    testFindsCorrectly( 2, 3 );
  }

  @Override
  public void lookupFor_Three() {
    testFindsCorrectly( 3, 1 );
  }

  @Override
  public void lookupFor_Hundred() {
    testFindsNothing( 100 );
  }
}
