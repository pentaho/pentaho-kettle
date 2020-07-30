/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.jsonoutput.analyzer;

import com.google.common.cache.CacheBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.jsonoutput.JsonOutput;
import org.pentaho.di.trans.steps.jsonoutput.JsonOutputMeta;
import org.pentaho.dictionary.DictionaryConst;
import org.pentaho.metaverse.api.IMetaverseBuilder;
import org.pentaho.metaverse.api.IMetaverseObjectFactory;
import org.pentaho.metaverse.api.INamespace;
import org.pentaho.metaverse.api.MetaverseObjectFactory;
import org.pentaho.metaverse.api.analyzer.kettle.ExternalResourceCache;
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import org.powermock.reflect.Whitebox;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class JsonOutputAnalyzerTest {

  private JsonOutputAnalyzer analyzer;

  @Mock
  private JsonOutput mockJsonOutput;
  @Mock
  private JsonOutputMeta meta;
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

  @BeforeClass
  public static void init() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {

    mockFactory = new MetaverseObjectFactory();
    when( mockBuilder.getMetaverseObjectFactory() ).thenReturn( mockFactory );
    when( mockNamespace.getParentNamespace() ).thenReturn( mockNamespace );

    analyzer = new JsonOutputAnalyzer() {};
    analyzer.setMetaverseBuilder( mockBuilder );

    when( mockJsonOutput.getStepMetaInterface() ).thenReturn( meta );
    when( mockJsonOutput.getStepMeta() ).thenReturn( mockStepMeta );
    when( mockStepMeta.getStepMetaInterface() ).thenReturn( meta );
  }

  @Test
  public void testGetResourceOutputNodeType() throws Exception {
    assertEquals( DictionaryConst.NODE_TYPE_FILE_FIELD, analyzer.getResourceOutputNodeType() );
  }

  @Test
  public void testGetResourceInputNodeType() throws Exception {
    assertNull( analyzer.getResourceInputNodeType() );
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
  public void testGetSupportedSteps() {
    JsonOutputAnalyzer analyzer = new JsonOutputAnalyzer();
    Set<Class<? extends BaseStepMeta>> types = analyzer.getSupportedSteps();
    assertNotNull( types );
    assertEquals( types.size(), 1 );
    assertTrue( types.contains( JsonOutputMeta.class ) );
  }

  @Test
  public void testJsonOutputExternalResourceConsumer() throws Exception {
    JsonOutputExternalResourceConsumer consumer = new JsonOutputExternalResourceConsumer();

    assertEquals( JsonOutputMeta.class, consumer.getMetaClass() );

    StepMeta spyMeta = spy( new StepMeta( "test", meta ) );

    when( meta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( transMeta );
    when( meta.getFileName() ).thenReturn( null );
    String[] outputFilePaths = new String[]{ "/path/to/file1", "/another/path/to/file2"};
    when( meta.getFilePaths( true ) ).thenReturn( outputFilePaths );
    when( meta.getFilePaths( false ) ).thenReturn( outputFilePaths );
    when( meta.writesToFile() ).thenReturn( false );

    assertFalse( consumer.isDataDriven( meta ) );

    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromMeta( meta );
    assertTrue( resources.isEmpty() );

    when( meta.writesToFile() ).thenReturn( true );
    resources = consumer.getResourcesFromMeta( meta );
    assertFalse( resources.isEmpty() );
    assertTrue( resources.stream().anyMatch( eri -> eri.getName().endsWith( outputFilePaths[0] ) ) );
    assertTrue( resources.stream().anyMatch( eri -> eri.getName().endsWith( outputFilePaths[1] ) ) );
  }

  @Test
  public void testCloneAnalyzer() {
    final JsonOutputAnalyzer analyzer = new JsonOutputAnalyzer();
    // verify that cloneAnalyzer returns an instance that is different from the original
    assertNotEquals( analyzer, analyzer.cloneAnalyzer() );
  }
  
  @Test
  public void testNewInstance(){
    JsonOutputAnalyzer analyzer = new JsonOutputAnalyzer();
    assertTrue( analyzer.newInstance().getClass().equals(JsonOutputAnalyzer.class));
  }
}
