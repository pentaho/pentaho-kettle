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

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class JobLogTableTest {
  @Mock
  VariableSpace mockSpace;
  @Mock
  HasDatabasesInterface mockDatabasesInterface;

  JobLogTable jobLogTable;

  @Before
  public void setup() {
    jobLogTable = JobLogTable.getDefault( mockSpace, mockDatabasesInterface );
  }

  @Test
  public void getRecommendedIndexes() {
    List<RowMetaInterface> indexes = jobLogTable.getRecommendedIndexes();
    String[] expected = new String[]{ "JOBNAME", "LOGDATE" };
    assertTrue( "No indicies present", indexes.size() > 0 );
    boolean found = false;
    for ( RowMetaInterface rowMeta : indexes ) {
      if ( Arrays.equals( rowMeta.getFieldNames(), expected ) ) {
        found = true;
        break;
      }
    }
    if ( !found ) {
      fail( "Cound not find index with " + Arrays.toString( expected ) );
    }
  }
}