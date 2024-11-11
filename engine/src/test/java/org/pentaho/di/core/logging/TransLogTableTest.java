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

package org.pentaho.di.core.logging;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.pentaho.di.trans.step.StepMeta;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TransLogTableTest {
  @Mock
  VariableSpace mockSpace;
  @Mock
  HasDatabasesInterface mockDatabasesInterface;
  @Mock
  List< StepMeta > mockSteps;

  TransLogTable transLogTable;

  @Before
  public void setup() {
    transLogTable = TransLogTable.getDefault( mockSpace, mockDatabasesInterface, mockSteps );
  }

  @Test
  public void getRecommendedIndexes() {
    List<RowMetaInterface> indexes = transLogTable.getRecommendedIndexes();
    String[] expected = new String[]{ "TRANSNAME", "LOGDATE" };
    assertTrue( "No indicies present", indexes.size() > 0 );
    boolean found = false;
    for ( RowMetaInterface rowMeta : indexes ) {
      if ( Arrays.equals( rowMeta.getFieldNames(), expected ) ) {
        found = true;
        break;
      }
    }
    if ( !found ) {
      fail( "Could not find index with " + Arrays.toString( expected ) );
    }
  }
}
