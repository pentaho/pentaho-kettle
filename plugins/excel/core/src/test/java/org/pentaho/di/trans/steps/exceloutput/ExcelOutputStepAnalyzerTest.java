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

package org.pentaho.di.trans.steps.exceloutput;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.ClonableStepAnalyzerTest;
import org.pentaho.di.trans.steps.MetaverseTestUtils;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.analyzer.kettle.step.IClonableStepAnalyzer;
import org.pentaho.metaverse.api.analyzer.kettle.step.StepNodes;
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
public class ExcelOutputStepAnalyzerTest extends ClonableStepAnalyzerTest {

  private ExcelOutputStepAnalyzer analyzer;

  @Mock ExcelOutputMeta meta;
  @Mock ExcelOutputData data;
  @Mock ExcelOutput step;
  @Mock IMetaverseNode node;
  @Mock INamespace mockNamespace;
  @Mock TransMeta transMeta;
  @Mock RowMetaInterface rmi;

  IComponentDescriptor descriptor;
  StepNodes inputs;

  @Before
  public void setUp() throws Exception {
    when( mockNamespace.getParentNamespace() ).thenReturn( mockNamespace );
    descriptor = new MetaverseComponentDescriptor( "test", DictionaryConst.NODE_TYPE_TRANS_STEP, mockNamespace );

    analyzer = spy( new ExcelOutputStepAnalyzer() );
    analyzer.setDescriptor( descriptor );
    analyzer.setObjectFactory( MetaverseTestUtils.getMetaverseObjectFactory() );

    inputs = new StepNodes();
    inputs.addNode( "previousStep", "first", node );
    inputs.addNode( "previousStep", "last", node );
    inputs.addNode( "previousStep", "age", node );
    inputs.addNode( "previousStep", "filename", node );
    lenient().doReturn( inputs ).when( analyzer ).getInputs();
  }

  @Test
  public void testGetResourceInputNodeType() throws Exception {
    assertNull( analyzer.getResourceInputNodeType() );
  }

  @Test
  public void testGetResourceOutputNodeType() throws Exception {
    assertEquals( DictionaryConst.NODE_TYPE_FILE_FIELD, analyzer.getResourceOutputNodeType() );
  }

  @Test
  public void testIsOutput() throws Exception {
    assertTrue( analyzer.isOutput() );
  }

  @Test
  public void testIsInput() throws Exception {
    assertFalse( analyzer.isInput() );
  }

  @Test
  public void testCreateResourceNode() throws Exception {
    IExternalResourceInfo res = mock( IExternalResourceInfo.class );
    when( res.getName() ).thenReturn( "file:///Users/home/tmp/xyz.ktr" );
    IMetaverseNode resourceNode = analyzer.createResourceNode( res );
    assertNotNull( resourceNode );
    assertEquals( DictionaryConst.NODE_TYPE_FILE, resourceNode.getType() );
  }

  @Test
  public void testGetUsedFields() throws Exception {
    assertNull( analyzer.getUsedFields( meta ) );
  }

  @Test
  public void testGetSupportedSteps() {
    ExcelOutputStepAnalyzer analyzer = new ExcelOutputStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( ExcelOutputMeta.class ) );
  }

  @Test
  public void testExcelOutputExternalResourceConsumer() throws Exception {
    ExcelOutputExternalResourceConsumer consumer = new ExcelOutputExternalResourceConsumer();

    StepMeta meta = new StepMeta( "test", this.meta );
    StepMeta spyMeta = spy( meta );

    when( this.meta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( transMeta );
    lenient().when( this.meta.getFileName() ).thenReturn( null );
    String[] filePaths = { "/path/to/file1", "/another/path/to/file2" };
    when( this.meta.getFiles( Mockito.any( VariableSpace.class ) ) ).thenReturn( filePaths );

    assertFalse( consumer.isDataDriven( this.meta ) );
    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromMeta( this.meta );
    assertFalse( resources.isEmpty() );
    assertEquals( 2, resources.size() );


    lenient().when( this.meta.getExtension() ).thenReturn( "xls" );

    assertFalse( consumer.getResourcesFromMeta( this.meta ).isEmpty() );

    data.realFilename = "/path/to/row/file";
    when( step.buildFilename() )
      .thenAnswer( new Answer<String>() {
        @Override
        public String answer( InvocationOnMock invocation ) throws Throwable {
          return ( data.realFilename + ".xls" );
        }
      } );

    resources = consumer.getResourcesFromRow( step, rmi, new String[]{ "id", "name" } );
    assertFalse( resources.isEmpty() );
    assertEquals( 1, resources.size() );

    resources = consumer.getResourcesFromRow( step, rmi, new String[]{ "id", "name" } );
    assertFalse( resources.isEmpty() );

    assertEquals( ExcelOutputMeta.class, consumer.getMetaClass() );
  }

  @Test
  public void testGetOutputResourceFields() throws Exception {
    ExcelField[] outputFields = new ExcelField[2];
    ExcelField field1 = mock( ExcelField.class );
    ExcelField field2 = mock( ExcelField.class );
    outputFields[0] = field1;
    outputFields[1] = field2;

    when( field1.getName() ).thenReturn( "field1" );
    when( field2.getName() ).thenReturn( "field2" );

    when( meta.getOutputFields() ).thenReturn( outputFields );

    Set<String> outputResourceFields = analyzer.getOutputResourceFields( meta );

    assertEquals( outputFields.length, outputResourceFields.size() );
    for ( ExcelField outputField : outputFields ) {
      assertTrue( outputResourceFields.contains( outputField.getName() ) );
    }
  }

  @Override protected IClonableStepAnalyzer newInstance() {
    return new ExcelOutputStepAnalyzer();
  }
}
