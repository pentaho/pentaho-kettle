/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.constant;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class ConstantIT {

  @Mock private LogChannelInterfaceFactory logChannelFactory;
  @Mock private LogChannelInterface logChannel;

  @Before
  public void setUp() throws KettleException {
    KettleEnvironment.init();
    KettleLogStore.setLogChannelInterfaceFactory( logChannelFactory );
    when( logChannelFactory.create( any(), any() ) ).thenReturn( logChannel );
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
  public void constantFieldsAddedOnceWithCorrectFormatting() throws KettleException {
    String path = getClass().getResource( "/constants-test.ktr" ).getPath();
    TransMeta constantTrans = new TransMeta( path, new Variables() );
    Trans trans = new Trans( constantTrans );
    trans.prepareExecution( new String[] {} );
    RowStepCollector collector = new RowStepCollector();
    trans.getSteps().get( 1 ).step.addRowListener( collector );
    List<RowMetaAndData> rowsWritten = collector.getRowsWritten();
    trans.setPreview( true );
    trans.startThreads();
    trans.waitUntilFinished();
    assertEquals( 1, rowsWritten.size() );
    Object[] data = rowsWritten.get( 0 ).getData();
    RowMetaInterface rowMeta = rowsWritten.get( 0 ).getRowMeta();
    assertEquals( 7, rowMeta.size() );
    assertEquals( "a", data[0] );
    assertEquals( "88.00", rowMeta.getValueMeta( 1 ).getString( data[1] ) );
    assertEquals( "b", data[2] );
    assertEquals( "c", data[3] );
    assertEquals( "d", data[4] );
    assertEquals( "e", data[5] );
    assertEquals( "1,234.568", rowMeta.getValueMeta( 6 ).getString( data[6] ) );
  }
}
