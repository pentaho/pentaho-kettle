/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.jsoninput.analyzer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.jsoninput.JsonInput;
import org.pentaho.di.trans.steps.jsoninput.JsonInputMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseObjectFactory;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class JsonInputAnalyzerTest {

  @Mock
  private JsonInputAnalyzer analyzerMock;
  private JsonInputAnalyzer analyzer;

  @Mock
  private JsonInput mockJsonInput;
  @Mock
  private JsonInputMeta meta;
  @Mock
  private StepMeta mockStepMeta;
  @Mock
  private TransMeta transMeta;
  @Mock
  private RowMetaInterface mockRowMetaInterface;
  @Mock
  private IMetaverseBuilder mockBuilder;
  @Mock
  private INamespace mockNamespace;

  private IMetaverseObjectFactory mockFactory;

  @Before
  public void setUp() throws Exception {

    mockFactory = new MetaverseObjectFactory();
    when( mockBuilder.getMetaverseObjectFactory() ).thenReturn( mockFactory );
    when( mockNamespace.getParentNamespace() ).thenReturn( mockNamespace );

    analyzer = new JsonInputAnalyzer() {
      @Override
      public Set<String> getInputStepNames( final JsonInputMeta meta, final String fieldName ) {
        Set<String> inputFieldStepNames = new HashSet<>();
        inputFieldStepNames.add( "previousStep" );
        return inputFieldStepNames;
      }
    };
    analyzer.setMetaverseBuilder( mockBuilder );

    when( mockJsonInput.getStepMetaInterface() ).thenReturn( meta );
    when( mockJsonInput.getStepMeta() ).thenReturn( mockStepMeta );
    when( mockStepMeta.getStepMetaInterface() ).thenReturn( meta );
  }

  @Test
  public void testGetUsedFields_fileNameFromField() throws Exception {
    when( meta.isAcceptingFilenames() ).thenReturn( true );
    when( meta.getAcceptingField() ).thenReturn( "filename" );
    Set<String> stepNames = new HashSet<>();
    stepNames.add( "previousStep" );

    Set<StepField> usedFields = analyzer.getUsedFields( meta );
    assertNotNull( usedFields );
    assertEquals( 1, usedFields.size() );
    StepField used = usedFields.iterator().next();
    assertEquals( "previousStep", used.getStepName() );
    assertEquals( "filename", used.getFieldName() );
  }

  @Test
  public void testGetUsedFields_isNotAcceptingFilenames() throws Exception {
    when( meta.isAcceptingFilenames() ).thenReturn( false );
    when( meta.getAcceptingField() ).thenReturn( "filename" );
    Set<StepField> usedFields = analyzerMock.getUsedFields( meta );
    assertNotNull( usedFields );
    assertEquals( 0, usedFields.size() );
  }

  @Test
  public void testGetResourceInputNodeType() throws Exception {
    assertEquals( DictionaryConst.NODE_TYPE_JSON_FIELD, analyzer.getResourceInputNodeType() );
  }

  @Test
  public void testGetResourceOutputNodeType() throws Exception {
    assertNull( analyzer.getResourceOutputNodeType() );
  }

  @Test
  public void testIsOutput() throws Exception {
    assertFalse( analyzer.isOutput() );
  }

  @Test
  public void testIsInput() throws Exception {
    assertTrue( analyzer.isInput() );
  }

  @Test
  public void testGetSupportedSteps() {
    JsonInputAnalyzer analyzer = new JsonInputAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( JsonInputMeta.class ) );
  }

  @Test
  public void testJsonInputExternalResourceConsumer() throws Exception {
    JsonInputExternalResourceConsumer consumer = new JsonInputExternalResourceConsumer();

    StepMeta spyMeta = spy( new StepMeta( "test", meta ) );

    when( meta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( transMeta );
    when( meta.getFileName() ).thenReturn( null );
    when( meta.isAcceptingFilenames() ).thenReturn( false );
    when( meta.getFilePaths( false ) ).thenReturn( new String[]{ "/path/to/file1" , "/another/path/to/file2" } );

    assertFalse( consumer.isDataDriven( meta ) );

    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromMeta( meta );
    assertTrue( resources.isEmpty() );

    when( meta.writesToFile() ).thenReturn( true );
    resources = consumer.getResourcesFromMeta( meta );
    assertFalse( resources.isEmpty() );
    assertEquals( 2, resources.size() );

    when( meta.isAcceptingFilenames() ).thenReturn( true );
    assertTrue( consumer.isDataDriven( meta ) );
    assertTrue( consumer.getResourcesFromMeta( meta ).isEmpty() );
    when( mockJsonInput.environmentSubstitute( Mockito.any( String.class ) ) ).thenReturn( "/path/to/row/file" );

    when( mockJsonInput.getStepMetaInterface() ).thenReturn( meta );
    resources = consumer.getResourcesFromRow( mockJsonInput, mockRowMetaInterface, new String[] { "id", "name" } );
    assertFalse( resources.isEmpty() );
    assertEquals( 1, resources.size() );

    when( mockRowMetaInterface.getString( Mockito.any( Object[].class ), Mockito.anyString(), Mockito.anyString() ) )
      .thenThrow( KettleException.class );
    resources = consumer.getResourcesFromRow( mockJsonInput, mockRowMetaInterface, new String[] { "id", "name" } );
    assertTrue( resources.isEmpty() );

    assertEquals( JsonInputMeta.class, consumer.getMetaClass() );
  }
}
