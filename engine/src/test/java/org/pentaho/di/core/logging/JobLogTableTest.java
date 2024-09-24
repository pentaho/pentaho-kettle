/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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