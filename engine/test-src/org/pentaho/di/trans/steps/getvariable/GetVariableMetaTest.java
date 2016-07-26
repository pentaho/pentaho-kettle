/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.getvariable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaTimestamp;
import org.pentaho.di.core.variables.Variables;

public class GetVariableMetaTest {

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init();
    PluginRegistry.init( true );
  }

  @Test
  public void testGetValueMetaPlugin() throws KettleStepException {
    GetVariableMeta meta = new GetVariableMeta();
    meta.setDefault();

    meta.setFieldName( new String[] { "outputField" } );
    meta.setFieldType( new int[] { ValueMetaInterface.TYPE_TIMESTAMP } );
    meta.setVariableString( new String[] { String.valueOf( 2000000L ) } );
    meta.setFieldLength( new int[] { 100 } );
    meta.setFieldPrecision( new int[] { 100 } );
    meta.setFieldFormat( new String[] { "" } );
    meta.setGroup( new String[] { "" } );
    meta.setDecimal( new String[] { "" } );
    meta.setCurrency( new String[] { "" } );
    meta.setTrimType( new int[] { 100 } );

    RowMetaInterface rowMeta = new RowMeta();
    meta.getFields( rowMeta, "stepName", null, null, new Variables(), null, null );

    assertNotNull( rowMeta );
    assertEquals( 1, rowMeta.size() );
    assertEquals( "outputField", rowMeta.getFieldNames()[0] );
    assertEquals( ValueMetaInterface.TYPE_TIMESTAMP, rowMeta.getValueMeta( 0 ).getType() );
    assertTrue( rowMeta.getValueMeta( 0 ) instanceof ValueMetaTimestamp );
  }
}
