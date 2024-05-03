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

package org.pentaho.di.trans.streaming.common;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.streaming.api.StreamSource;
import org.pentaho.di.trans.streaming.api.StreamWindow;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith ( MockitoJUnitRunner.class )
public class BaseStreamStepTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  private BaseStreamStep baseStreamStep;

  @Mock BaseStreamStepMeta meta;
  @Mock BaseStreamStepMeta metaWithVariables;
  @Mock StepDataInterface stepData;
  @Mock StreamSource<List<Object>> streamSource;
  @Mock StreamWindow<List<Object>, Result> streamWindow;
  @Mock LogChannelInterfaceFactory logChannelFactory;
  @Mock LogChannelInterface logChannel;
  @Mock private StepMeta parentStepMeta;

  @Rule public TemporaryFolder folder = new TemporaryFolder();

  @Before
  public void setUp() throws KettleException {
    KettleLogStore.setLogChannelInterfaceFactory( logChannelFactory );
    when( logChannelFactory.create( any(), any() ) ).thenReturn( logChannel );

    StepMeta stepMeta = new StepMeta( "BaseStreamStep", meta );

    TransMeta transMeta = new TransMeta();
    transMeta.addStep( stepMeta );
    Trans trans = new Trans( transMeta );

    baseStreamStep = new BaseStreamStep( stepMeta, stepData, 1, transMeta, trans );
    baseStreamStep.source = streamSource;
    baseStreamStep.window = streamWindow;
    baseStreamStep.setParentVariableSpace( new Variables() );

    StepMetaDataCombi stepMetaDataCombi = new StepMetaDataCombi();
    stepMetaDataCombi.step = baseStreamStep;
    stepMetaDataCombi.data = stepData;
    stepMetaDataCombi.stepMeta = stepMeta;
    stepMetaDataCombi.meta = meta;

    trans.prepareExecution( new String[ 0 ] );
    trans.getSteps().add( stepMetaDataCombi );
  }

  @Test
  public void testInitMissingFilename() {


    when( meta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.FILENAME );
    assertFalse( baseStreamStep.init( meta, stepData ) );
    verify( logChannel ).logError( contains( "Unable to load transformation " ), any( KettleException.class ) );
  }


  @Test
  public void testInitFilenameSubstitution() throws IOException {
    // verifies that filename resolution uses the parents ${Internal.Entry.Current.Directory}.
    // Necessary since the Current.Directory may change when running non-locally.
    // Variables should all be set in variableizedStepMeta after init, with the caveat that
    // the substrans location must be set using the parents Current.Directory.
    File testFile = File.createTempFile( "testInitFilenameSubstitution", ".ktr",
      folder.getRoot() );
    try ( PrintWriter pw = new PrintWriter( testFile ) ) {
      // empty subtrans definition
      pw.write( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<transformation/>" );
    }

    when( meta.getParentStepMeta() ).thenReturn( parentStepMeta );
    when( meta.withVariables( baseStreamStep ) ).thenReturn( metaWithVariables );
    baseStreamStep.getParentVariableSpace()
      .setVariable( "Internal.Entry.Current.Directory",
        testFile.getParentFile().getAbsolutePath() );

    when( meta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.FILENAME );
    when( meta.getFileName() ).thenReturn( "${Internal.Entry.Current.Directory}/" + testFile.getName() );

    assertTrue( baseStreamStep.init( meta, stepData ) );
    assertThat( baseStreamStep.variablizedStepMeta, equalTo( metaWithVariables ) );
  }

  @Test
  public void testStop() throws KettleException {
    Result result = new Result();
    result.setSafeStop( false );
    result.setRows( Collections.emptyList() );
    when( streamWindow.buffer( any() ) ).thenReturn( Collections.singletonList( result ) );

    baseStreamStep.processRow( meta, stepData );
    assertFalse( baseStreamStep.isSafeStopped() );
    verify( streamSource ).close();
  }

  @Test
  public void testSafeStop() throws KettleException {
    Result result = new Result();
    result.setSafeStop( true );
    when( streamWindow.buffer( any() ) ).thenReturn( Collections.singletonList( result ) );

    baseStreamStep.processRow( meta, stepData );
    assertTrue( baseStreamStep.isSafeStopped() );
    verify( streamSource, times( 2 ) ).close();
  }

  @Test
  public void testAlwaysCloses() throws KettleException {
    when( streamWindow.buffer( any() ) ).thenThrow( new IllegalStateException( "run for your life!!!" ) );
    try {
      baseStreamStep.processRow( meta, stepData );
    } catch ( IllegalStateException ignored ) {
    }
    verify( streamSource ).close();
  }

  @Test
  public void testPrefetchCount() throws IOException {
    File testFile = File.createTempFile( "testInitFilenameSubstitution", ".ktr",
      folder.getRoot() );
    try ( PrintWriter pw = new PrintWriter( testFile ) ) {
      // empty subtrans definition
      pw.write( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<transformation/>" );
    }

    when( meta.withVariables( baseStreamStep ) ).thenReturn( metaWithVariables );

    baseStreamStep.getParentVariableSpace()
      .setVariable( "Internal.Entry.Current.Directory",
        testFile.getParentFile().getAbsolutePath() );
    when( meta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.FILENAME );
    when( meta.getFileName() ).thenReturn( "${Internal.Entry.Current.Directory}/" + testFile.getName() );

    baseStreamStep.init( meta, stepData );

    when( metaWithVariables.getPrefetchCount() ).thenReturn( "100000" );
    assertEquals( 100000, baseStreamStep.getPrefetchCount() );

    //Max int
    when( metaWithVariables.getPrefetchCount() ).thenReturn( "2147483647" );
    assertEquals( 2147483647, baseStreamStep.getPrefetchCount() );

    //Max int + 1
    //Should return default
    when( metaWithVariables.getPrefetchCount() ).thenReturn( "2147483648" );
    assertEquals( 100000, baseStreamStep.getPrefetchCount() );

    //Later validation will catch an issue with this being 0
    when( metaWithVariables.getPrefetchCount() ).thenReturn( "0" );
    assertEquals( 0, baseStreamStep.getPrefetchCount() );

    //Later validation will catch an issue with this being negative
    when( metaWithVariables.getPrefetchCount() ).thenReturn( "-1" );
    assertEquals( -1, baseStreamStep.getPrefetchCount() );

    when( metaWithVariables.getPrefetchCount() ).thenReturn( "" );
    assertEquals( 100000, baseStreamStep.getPrefetchCount() );
  }
}
