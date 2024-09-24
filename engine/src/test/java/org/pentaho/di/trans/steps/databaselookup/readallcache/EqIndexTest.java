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
