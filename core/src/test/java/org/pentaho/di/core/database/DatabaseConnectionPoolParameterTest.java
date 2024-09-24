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

package org.pentaho.di.core.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;

public class DatabaseConnectionPoolParameterTest {

  @Test
  public void testGetRowList() {
    List<RowMetaAndData> result = DatabaseConnectionPoolParameter.getRowList( BaseDatabaseMeta.poolingParameters,
      "myTitleParameter", "myTitleDefaultValue", "myTitleDescription" );

    assertNotNull( result );
    for ( RowMetaAndData rmd : result ) {
      assertEquals( 3, rmd.getRowMeta().size() );
      assertEquals( "myTitleParameter", rmd.getRowMeta().getValueMeta( 0 ).getName() );
      assertEquals( ValueMetaInterface.TYPE_STRING, rmd.getRowMeta().getValueMeta( 0 ).getType() );
      assertEquals( "myTitleDefaultValue", rmd.getRowMeta().getValueMeta( 1 ).getName() );
      assertEquals( ValueMetaInterface.TYPE_STRING, rmd.getRowMeta().getValueMeta( 1 ).getType() );
      assertEquals( "myTitleDescription", rmd.getRowMeta().getValueMeta( 2 ).getName() );
      assertEquals( ValueMetaInterface.TYPE_STRING, rmd.getRowMeta().getValueMeta( 2 ).getType() );
    }
  }
}
