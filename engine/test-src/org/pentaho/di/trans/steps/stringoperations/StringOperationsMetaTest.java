/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.stringoperations;

import org.junit.Test;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * User: Dzmitry Stsiapanau Date: 2/3/14 Time: 5:41 PM
 */
public class StringOperationsMetaTest {
  @Test
  public void testGetFields() throws Exception {
    StringOperationsMeta meta = new StringOperationsMeta();
    meta.allocate( 1 );
    meta.setFieldInStream( new String[] { "field1" } );

    RowMetaInterface rowMetaInterface = new RowMeta();
    ValueMetaInterface valueMeta = new ValueMeta( "field1", ValueMeta.TYPE_STRING );
    valueMeta.setStorageMetadata( new ValueMeta( "field1", ValueMeta.TYPE_STRING ) );
    valueMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );
    rowMetaInterface.addValueMeta( valueMeta );

    VariableSpace space = mock( VariableSpace.class );
    meta.getFields( rowMetaInterface, "STRING_OPERATIONS", null, null, space, null, null );
    RowMetaInterface expectedRowMeta = new RowMeta();
    expectedRowMeta.addValueMeta( new ValueMeta( "field1", ValueMeta.TYPE_STRING ) );
    assertEquals( expectedRowMeta.toString(), rowMetaInterface.toString() );
  }
}
