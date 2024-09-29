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

import java.util.BitSet;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * @author Andrey Khayrutdinov
 */
@RunWith( Parameterized.class )
public class GtIndexTest extends IndexTestBase<GtIndex> {

  @Parameterized.Parameters
  public static List<Object[]> createSampleData() {
    return IndexTestBase.createSampleData();
  }

  public GtIndexTest( Long[][] rows ) {
    super( GtIndex.class, rows );
  }

  @Override
  void doAssertMatches( BitSet candidates, long lookupValue, long actualValue ) {
    if ( !( actualValue > lookupValue ) ) {
      fail( String.format( "All found values are expected to be greater than [%d], but got [%d] among %s",
        lookupValue, actualValue, candidates ) );
    }
  }


  @Override
  public void lookupFor_MinusOne() {
    // should be all
    testFindsCorrectly( -1, 5 );
  }

  @Override
  public void lookupFor_Zero() {
    // should be [1, 2, 2, 3]
    testFindsCorrectly( 0, 4 );
  }

  @Override
  public void lookupFor_One() {
    testFindsCorrectly( 1, 3 );
  }

  @Override
  public void lookupFor_Two() {
    testFindsCorrectly( 1, 1 );
  }

  @Override
  public void lookupFor_Three() {
    testFindsNothing( 3 );
  }

  @Override
  public void lookupFor_Hundred() {
    testFindsNothing( 100 );
  }
}
