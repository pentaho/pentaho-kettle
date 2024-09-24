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
public class EqIndexTest extends IndexTestBase<EqIndex> {

  @Parameterized.Parameters
  public static List<Object[]> createSampleData() {
    return IndexTestBase.createSampleData();
  }

  public EqIndexTest( Long[][] rows ) {
    super( EqIndex.class, rows );
  }

  @Override
  void doAssertMatches( BitSet candidates, long lookupValue, long actualValue ) {
    if ( lookupValue != actualValue ) {
      fail( String.format( "Expected to find [%d] among %s, but got [%d]", lookupValue, candidates, actualValue ) );
    }
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
    testFindsCorrectly( 1, 1 );
  }

  @Override
  public void lookupFor_Two() {
    testFindsCorrectly( 2, 2 );
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
