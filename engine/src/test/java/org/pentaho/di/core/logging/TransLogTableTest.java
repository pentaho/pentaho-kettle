/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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
