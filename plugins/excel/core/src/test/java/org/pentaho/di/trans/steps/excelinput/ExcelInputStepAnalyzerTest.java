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

package org.pentaho.di.trans.steps.excelinput;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.ClonableStepAnalyzerTest;
import org.pentaho.di.trans.steps.MetaverseTestUtils;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.StepField;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class ExcelInputStepAnalyzerTest extends ClonableStepAnalyzerTest {

  private ExcelInputStepAnalyzer analyzer;

  @Mock ExcelInputMeta meta;
  @Mock INamespace mockNamespace;
  @Mock TransMeta transMeta;
  @Mock RowMetaInterface rmi;
  @Mock ExcelInput excelInput;

  IComponentDescriptor descriptor;
  ExcelInputExternalResourceConsumer consumer;

  public static String[] ROW = new String[] { "id", "name" };

  @Before
  public void setUp() throws Exception {
    lenient().when( mockNamespace.getParentNamespace() ).thenReturn( mockNamespace );
    descriptor = new MetaverseComponentDescriptor( "test", DictionaryConst.NODE_TYPE_TRANS_STEP, mockNamespace );
    analyzer = spy( new ExcelInputStepAnalyzer() );
    analyzer.setDescriptor( descriptor );
    analyzer.setObjectFactory( MetaverseTestUtils.getMetaverseObjectFactory() );
    consumer = new ExcelInputExternalResourceConsumer();
  }


  @Test
  public void testGetUsedFields_fileNameFromField() throws Exception {
    lenient().when( meta.isAcceptingFilenames() ).thenReturn( true );
    lenient().when( meta.getAcceptingField() ).thenReturn( "filename" );
    lenient().when( meta.getAcceptingStepName() ).thenReturn( "previousStep" );
    Set<StepField> usedFields = analyzer.getUsedFields( meta );
    assertNotNull( usedFields );
    assertEquals( 1, usedFields.size() );
    StepField used = usedFields.iterator().next();
    assertEquals( "previousStep", used.getStepName() );
    assertEquals( "filename", used.getFieldName() );
  }

  @Test
  public void testGetUsedFields_isNotAcceptingFilenames() throws Exception {
    lenient().when( meta.isAcceptingFilenames() ).thenReturn( false );
    lenient().when( meta.getAcceptingField() ).thenReturn( "filename" );
    lenient().when( meta.getAcceptingStepName() ).thenReturn( "previousStep" );
    Set<StepField> usedFields = analyzer.getUsedFields( meta );
    assertNotNull( usedFields );
    assertEquals( 0, usedFields.size() );
  }

  @Test
  public void testGetUsedFields_isAcceptingFilenamesButNoStepName() throws Exception {
    lenient().when( meta.isAcceptingFilenames() ).thenReturn( true );
    lenient().when( meta.getAcceptingField() ).thenReturn( "filename" );
    lenient().when( meta.getAcceptingStepName() ).thenReturn( null );
    Set<StepField> usedFields = analyzer.getUsedFields( meta );
    assertNotNull( usedFields );
    assertEquals( 0, usedFields.size() );
  }

  @Test
  public void testGetResourceInputNodeType() throws Exception {
    assertEquals( DictionaryConst.NODE_TYPE_FILE_FIELD, analyzer.getResourceInputNodeType() );
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
    ExcelInputStepAnalyzer analyzer = new ExcelInputStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( ExcelInputMeta.class ) );
  }

  @Test
  public void resourcesFromMetaGotSuccessfully() throws Exception {
    StepMeta spyMeta = spy( new StepMeta( "test", meta ) );

    when( meta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( transMeta );
    lenient().when( meta.getFileName() ).thenReturn( null );
    when( meta.isAcceptingFilenames() ).thenReturn( false );
    String[] filePaths = { "/path/to/file1", "/another/path/to/file2" };
    when( meta.getFilePaths( Mockito.any( VariableSpace.class ) ) ).thenReturn( filePaths );

    assertFalse( consumer.isDataDriven( meta ) );
    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromMeta( meta );
    assertEquals( 2, resources.size() );
  }

  @Test
  public void resourcesFromRowGotSuccessfully() throws Exception {
    when( meta.isAcceptingFilenames() ).thenReturn( true );

    assertTrue( consumer.isDataDriven( meta ) );
    assertTrue( consumer.getResourcesFromMeta( meta ).isEmpty() );
    when( rmi.getString( Mockito.any( Object[].class ), Mockito.any(), Mockito.any() ) )
      .thenReturn( "/path/to/row/file" );
    when( excelInput.getStepMetaInterface() ).thenReturn( meta );

    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromRow( excelInput, rmi, ROW );

    assertEquals( 1, resources.size() );
  }

  @Test
  public void resourcesFromRowGotSuccessfullyWhenExceptionThrown() throws Exception {
    when( excelInput.getStepMetaInterface() ).thenReturn( meta );
    when( rmi.getString( Mockito.any( Object[].class ), Mockito.any(), Mockito.any() ) )
      .thenThrow( KettleValueException.class );

    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromRow( excelInput, rmi, ROW );

    assertTrue( resources.isEmpty() );
    assertEquals( ExcelInputMeta.class, consumer.getMetaClass() );
  }

  @Test
  public void resourcesFromRowGotSuccessfullyWhenStepInputMetaInterfaceIsNull() throws Exception {
    StepMeta mockedStepMeta = mock( StepMeta.class );
    when( excelInput.getStepMetaInterface() ).thenReturn( null );
    when( excelInput.getStepMeta() ).thenReturn( mockedStepMeta );
    when( mockedStepMeta.getStepMetaInterface() ).thenReturn( new ExcelInputMeta() );
    when( rmi.getString( Mockito.any( Object[].class ), Mockito.any(), Mockito.any() ) )
      .thenReturn( "/path/to/row/file" );

    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromRow( excelInput, rmi, ROW );

    assertEquals( 1, resources.size() );
  }

  @Override
  protected IClonableStepAnalyzer newInstance() {
    return new ExcelInputStepAnalyzer();
  }
}
