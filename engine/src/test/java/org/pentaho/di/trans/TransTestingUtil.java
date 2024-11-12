/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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
