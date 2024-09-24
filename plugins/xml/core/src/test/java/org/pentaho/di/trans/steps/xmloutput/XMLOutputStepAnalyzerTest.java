/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
 *
 * ******************************************************************************
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
 */

package org.pentaho.di.trans.steps.xmloutput;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IComponentDescriptor;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseNode;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseComponentDescriptor;
import org.pentaho.metaverse.api.MetaverseObjectFactory;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class XMLOutputStepAnalyzerTest {

  private XMLOutputStepAnalyzer analyzer;

  @Mock
  private XMLOutput mockXMLOutput;

  @Mock
  private XMLOutputMeta meta;

  @Mock
  private XMLOutputData data;

  @Mock
  IMetaverseNode node;
  @Mock
  IMetaverseBuilder mockBuilder;
  @Mock
  private INamespace mockNamespace;
  @Mock
  private StepMeta parentStepMeta;
  @Mock
  private TransMeta mockTransMeta;

  IComponentDescriptor descriptor;

  static MetaverseObjectFactory metaverseObjectFactory = new MetaverseObjectFactory();

  @Before
  public void setUp() throws Exception {
    analyzer = spy( new XMLOutputStepAnalyzer() );
    analyzer.setMetaverseBuilder( mockBuilder );
    analyzer.setBaseStepMeta( meta );
    analyzer.setRootNode( node );
    analyzer.setParentTransMeta( mockTransMeta );
    analyzer.setParentStepMeta( parentStepMeta );

    descriptor = new MetaverseComponentDescriptor( "test", DictionaryConst.NODE_TYPE_TRANS_STEP, mockNamespace );
    analyzer.setDescriptor( descriptor );
    analyzer.setObjectFactory( metaverseObjectFactory );

    lenient().when( mockXMLOutput.getStepDataInterface() ).thenReturn( data );
    lenient().when( mockXMLOutput.getStepMeta() ).thenReturn( parentStepMeta );

    lenient().when( meta.getParentStepMeta() ).thenReturn( parentStepMeta );
    lenient().when( parentStepMeta.getStepMetaInterface() ).thenReturn( meta );
    lenient().when( parentStepMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    lenient().when( parentStepMeta.getName() ).thenReturn( "test" );
    lenient().when( parentStepMeta.getStepID() ).thenReturn( "XmlOutputStep" );
  }

  @Test
  public void testCustomAnalyze() throws Exception {
    when( meta.getMainElement() ).thenReturn( "main" );
    when( meta.getRepeatElement() ).thenReturn( "repeat" );
    analyzer.customAnalyze( meta, node );

    verify( node ).setProperty( "parentnode", "main" );
    verify( node ).setProperty( "rownode", "repeat" );

  }

  @Test
  public void testGetSupportedSteps() {
    XMLOutputStepAnalyzer analyzer = new XMLOutputStepAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( XMLOutputMeta.class ) );
  }

  @Test
  public void testGetOutputResourceFields() throws Exception {
    XMLField[] outputFields = new XMLField[2];
    XMLField field1 = mock( XMLField.class );
    XMLField field2 = mock( XMLField.class );
    outputFields[0] = field1;
    outputFields[1] = field2;

    when( field1.getFieldName() ).thenReturn( "field1" );
    when( field2.getFieldName() ).thenReturn( "field2" );

    when( meta.getOutputFields() ).thenReturn( outputFields );

    Set<String> outputResourceFields = analyzer.getOutputResourceFields( meta );

    assertEquals( outputFields.length, outputResourceFields.size() );
    for ( XMLField outputField : outputFields ) {
      assertTrue( outputResourceFields.contains( outputField.getFieldName() ) );
    }
  }

  @Test
  public void testXMLOutputExternalResourceConsumer() throws Exception {
    XMLOutputExternalResourceConsumer consumer = new XMLOutputExternalResourceConsumer();

    StepMeta meta = new StepMeta( "test", this.meta );
    StepMeta spyMeta = spy( meta );

    when( this.meta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( mockTransMeta );
    String[] filePaths = { "/path/to/file1", "/another/path/to/file2" };
    when( this.meta.getFiles( Mockito.any( VariableSpace.class ) ) ).thenReturn( filePaths );

    assertFalse( consumer.isDataDriven( this.meta ) );
    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromMeta( this.meta );
    assertFalse( resources.isEmpty() );
    assertEquals( 2, resources.size() );

    lenient().when( this.meta.getExtension() ).thenReturn( "txt" );

    assertEquals( XMLOutputMeta.class, consumer.getMetaClass() );
  }

  @Test
  public void testCreateResourceNode() throws Exception {
    IExternalResourceInfo res = mock( IExternalResourceInfo.class );
    when( res.getName() ).thenReturn( "file:///Users/home/tmp/xyz.xml" );
    IMetaverseNode resourceNode = analyzer.createResourceNode( res );
    assertNotNull( resourceNode );
    assertEquals( DictionaryConst.NODE_TYPE_FILE, resourceNode.getType() );
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
  public void testGetUsedFields() throws Exception {
    assertNull( analyzer.getUsedFields( meta ) );
  }
}

