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
public class LeIndexTest extends IndexTestBase<GtIndex> {

  @Parameterized.Parameters
  public static List<Object[]> createSampleData() {
    return IndexTestBase.createSampleData();
  }

  public LeIndexTest( Long[][] rows ) {
    super( GtIndex.class, rows );
  }

  @Override
  void doAssertMatches( BitSet candidates, long lookupValue, long actualValue ) {
    if ( !( actualValue <= lookupValue ) ) {
      fail( String.format( "All found values are expected to be less than [%d] or equal to it, but got [%d] among %s",
        lookupValue, actualValue, candidates ) );
    }
  }

  @Override
  GtIndex createIndexInstance( int column, ValueMetaInterface meta, int rowsAmount ) throws Exception {
    return (GtIndex) GtIndex.lessOrEqualCache( column, meta, rowsAmount );
  }


  @Override
  public void lookupFor_MinusOne() {
    testFindsNothing( -1 );
  }

  @Override
  public void lookupFor_Zero() {
    testFindsCorrectly( 0, 1 );
  }

  @Override
  public void lookupFor_One() {
    testFindsCorrectly( 1, 2 );
  }

  @Override
  public void lookupFor_Two() {
    testFindsCorrectly( 2, 4 );
  }

  @Override
  public void lookupFor_Three() {
    testFindsCorrectly( 3, 5 );
  }

  @Override
  public void lookupFor_Hundred() {
    testFindsCorrectly( 100, 5 );
  }
}
