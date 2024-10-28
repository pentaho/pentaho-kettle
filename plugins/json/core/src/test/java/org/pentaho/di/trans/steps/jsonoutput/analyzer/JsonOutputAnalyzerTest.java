/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.jsonoutput.analyzer;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.bowl.DefaultBowl;
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
import org.pentaho.metaverse.api.model.IExternalResourceInfo;

import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
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
    lenient().when( mockNamespace.getParentNamespace() ).thenReturn( mockNamespace );

    analyzer = new JsonOutputAnalyzer() {};
    analyzer.setMetaverseBuilder( mockBuilder );

    when( meta.getParentStepMeta() ).thenReturn( mockStepMeta );
    when( transMeta.getBowl() ).thenReturn( DefaultBowl.getInstance() );

    lenient().when( mockJsonOutput.getStepMetaInterface() ).thenReturn( meta );
    lenient().when( mockJsonOutput.getStepMeta() ).thenReturn( mockStepMeta );
    lenient().when( mockStepMeta.getStepMetaInterface() ).thenReturn( meta );
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

  @Ignore
  @Test
  public void testJsonOutputExternalResourceConsumer() throws Exception {
    JsonOutputExternalResourceConsumer consumer = new JsonOutputExternalResourceConsumer();

    assertEquals( JsonOutputMeta.class, consumer.getMetaClass() );

    StepMeta spyMeta = spy( new StepMeta( "test", meta ) );

    when( meta.getParentStepMeta() ).thenReturn( spyMeta );
    when( spyMeta.getParentTransMeta() ).thenReturn( transMeta );
    String[] outputFilePaths = new String[]{ "/path/to/file1", "/another/path/to/file2"};
    when( meta.getFilePaths( anyBoolean() ) ).thenReturn( outputFilePaths );
    when( meta.writesToFile() ).thenReturn( false );

    assertFalse( consumer.isDataDriven( meta ) );

    Collection<IExternalResourceInfo> resources = consumer.getResourcesFromMeta( DefaultBowl.getInstance(), meta );
    assertTrue( resources.isEmpty() );

    when( meta.writesToFile() ).thenReturn( true );
    resources = consumer.getResourcesFromMeta( DefaultBowl.getInstance(), meta );
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
