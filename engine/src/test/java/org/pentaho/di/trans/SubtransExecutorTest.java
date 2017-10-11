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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.core.logging.LoggingObject;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.steps.transexecutor.TransExecutorData;
import org.pentaho.di.trans.steps.transexecutor.TransExecutorParameters;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@RunWith( MockitoJUnitRunner.class )
public class SubtransExecutorTest {
  @Mock private LogChannelInterfaceFactory logChannelFactory;
  @Mock private LogChannel logChannel;

  @Before
  public void setUp() throws Exception {
    KettleLogStore.setLogChannelInterfaceFactory( this.logChannelFactory );
    Mockito.when( this.logChannelFactory.create( any(), any() ) ).thenReturn( this.logChannel );
  }

  @BeforeClass
  public static void init() throws Exception {
    KettleClientEnvironment.init();
    PluginRegistry.addPluginType( StepPluginType.getInstance() );
    PluginRegistry.init();
    if ( !Props.isInitialized() ) {
      Props.init( 0 );
    }
  }

  @Test
  public void testRunningZeroRowsIsEmptyOptional() throws Exception {
    SubtransExecutor subtransExecutor = new SubtransExecutor( null, null, false, null, null );
    Optional<Result> execute = subtransExecutor.execute( Collections.emptyList() );
    assertFalse( execute.isPresent() );
  }

  @Test
  public void testRunsATrans() throws Exception {
    TransMeta parentMeta =
      new TransMeta( this.getClass().getResource( "subtrans-executor-parent.ktr" ).getPath(), new Variables() );
    TransMeta subMeta =
      new TransMeta( this.getClass().getResource( "subtrans-executor-sub.ktr" ).getPath(), new Variables() );
    LoggingObjectInterface loggingObject = new LoggingObject( "anything" );
    Trans parentTrans = new Trans( parentMeta, loggingObject );
    SubtransExecutor subtransExecutor =
      new SubtransExecutor( parentTrans, subMeta, true, new TransExecutorData(), new TransExecutorParameters() );
    RowMetaInterface rowMeta = parentMeta.getStepFields( "Data Grid" );
    List<RowMetaAndData> rows = Arrays.asList(
      new RowMetaAndData( rowMeta, "Pentaho", 1L ),
      new RowMetaAndData( rowMeta, "Pentaho", 2L ),
      new RowMetaAndData( rowMeta, "Pentaho", 3L ),
      new RowMetaAndData( rowMeta, "Pentaho", 4L ) );
    subtransExecutor.execute( rows );
    verify( this.logChannel )
      .logBasic(
        "\n"
          + "------------> Linenr 1------------------------------\n"
          + "name = Pentaho\n"
          + "sum = 10\n"
          + "\n"
          + "===================="
      );
  }

}
