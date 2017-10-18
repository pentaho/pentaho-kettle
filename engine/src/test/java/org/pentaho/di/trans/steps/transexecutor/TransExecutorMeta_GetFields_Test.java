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

package org.pentaho.di.trans.steps.transexecutor;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaPluginType;
import org.pentaho.di.trans.step.StepMeta;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TransExecutorMeta_GetFields_Test {

  private TransExecutorMeta meta;

  private StepMeta executionResult;
  private StepMeta resultFiles;
  private StepMeta outputRows;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    PluginRegistry.addPluginType( ValueMetaPluginType.getInstance() );
    PluginRegistry.init( true );
  }

  @Before
  public void setUp() {
    executionResult = mock( StepMeta.class );
    resultFiles = mock( StepMeta.class );
    outputRows = mock( StepMeta.class );

    meta = new TransExecutorMeta();
    meta.setExecutionResultTargetStepMeta( executionResult );
    meta.setResultFilesTargetStepMeta( resultFiles );
    meta.setOutputRowsSourceStepMeta( outputRows );

    meta.setExecutionTimeField( "executionTime" );
    meta.setExecutionResultField( "true" );
    meta.setExecutionNrErrorsField( "1" );

    meta.setResultFilesFileNameField( "resultFileName" );

    meta.setOutputRowsField( new String[] { "outputRow" } );
    meta.setOutputRowsType( new int[] { 0 } );
    meta.setOutputRowsLength( new int[] { 0 } );
    meta.setOutputRowsPrecision( new int[] { 0 } );

    meta = spy( meta );

    StepMeta parent = mock( StepMeta.class );
    doReturn( parent ).when( meta ).getParentStepMeta();
    when( parent.getName() ).thenReturn( "parent step" );

  }

  @Test
  public void getFieldsForExecutionResults() throws Exception {
    RowMetaInterface mock = invokeGetFieldsWith( executionResult );
    verify( mock, times( 3 ) ).addValueMeta( any( ValueMetaInterface.class ) );
  }

  @Test
  public void getFieldsForResultFiles() throws Exception {
    RowMetaInterface mock = invokeGetFieldsWith( resultFiles );
    verify( mock ).addValueMeta( any( ValueMetaInterface.class ) );
  }

  @Test
  public void getFieldsForInternalTransformationOutputRows() throws Exception {
    RowMetaInterface mock = invokeGetFieldsWith( outputRows );
    verify( mock ).addValueMeta( any( ValueMetaInterface.class ) );
  }

  private RowMetaInterface invokeGetFieldsWith( StepMeta stepMeta ) throws Exception {
    RowMetaInterface rowMeta = mock( RowMetaInterface.class );
    meta.getFields( rowMeta, "", null, stepMeta, null, null, null );
    return rowMeta;
  }
}
