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

package org.pentaho.di.trans;

import org.pentaho.di.core.BlockingRowSet;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMetaInterface;

import java.util.ArrayList;
import java.util.List;

import java.util.Collections;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @author Andrey Khayrutdinov
 */
public class TransTestingUtil {

  public static List<Object[]> execute( BaseStep step,
                                        StepMetaInterface meta,
                                        StepDataInterface data,
                                        int expectedRowsAmount,
                                        boolean checkIsDone ) throws Exception {
    RowSet output = new BlockingRowSet( Math.max( 1, expectedRowsAmount ) );
    step.setOutputRowSets( Collections.singletonList( output ) );

    int i = 0;
    List<Object[]> result = new ArrayList<>( expectedRowsAmount );
    while ( step.processRow( meta, data ) && i < expectedRowsAmount ) {
      Object[] row = output.getRowImmediate();
      assertNotNull( Integer.toString( i ), row );
      result.add( row );

      i++;
    }
    assertEquals( "The amount of executions should be equal to expected", expectedRowsAmount, i );
    if ( checkIsDone ) {
      assertTrue( output.isDone() );
    }

    return result;
  }


  public static void assertResult( Object[] expectedRow, Object[] actualRow ) {
    assertRow( 0, expectedRow, actualRow );
  }

  public static void assertResult( List<Object[]> expected, List<Object[]> actual ) {
    assertEquals( expected.size(), actual.size() );
    for ( int i = 0; i < expected.size(); i++ ) {
      Object[] expectedRow = expected.get( i );
      Object[] actualRow = actual.get( i );
      assertRow( i, expectedRow, actualRow );
    }
  }

  private static void assertRow( int index, Object[] expected, Object[] actual ) {
    assertNotNull( actual );

    boolean sizeCondition = ( expected.length <= actual.length );
    if ( !sizeCondition ) {
      fail(
        String.format( "Row [%d]: expected.length=[%d]; actual.length=[%d]", index, expected.length, actual.length ) );
    }

    int i = 0;
    while ( i < expected.length ) {
      assertEquals( String.format( "[%d][%d]", index, i ), expected[ i ], actual[ i ] );
      i++;
    }
    while ( i < actual.length ) {
      assertNull( actual[ i ] );
      i++;
    }
  }
}
