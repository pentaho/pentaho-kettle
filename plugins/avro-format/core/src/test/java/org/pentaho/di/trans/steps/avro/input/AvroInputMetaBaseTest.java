/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2022-2024 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.avro.input;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.avro.AvroSpec;
import org.pentaho.di.trans.steps.named.cluster.NamedClusterEmbedManager;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class AvroInputMetaBaseTest {

  @Mock
  private AvroInputField field;

  @Mock
  private Repository rep;

  @Mock
  private IMetaStore metaStore;

  @Mock
  private ObjectId id_transformation;

  @Mock
  private ObjectId id_step;

  @Mock
  private List<DatabaseMeta> databases;

  @Mock
  private NamedClusterEmbedManager embedManager;

  private AvroInputMetaBase meta;

  private static final String FILE_NAME_VALID_PATH = "path/to/file";

  private VariableSpace variableSpace;

  @Before
  public void setUp() throws KettlePluginException {
    when( field.getAvroType() ).thenReturn( AvroSpec.DataType.STRING );
    meta = spy( new AvroInputMetaBase() {
      @Override
      public StepDataInterface getStepData() {
        return null;
      }

      @Override
      public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
                                    TransMeta transMeta, Trans trans ) {
        return null;
      }
    } );

    TransMeta parentTransMeta = mock( TransMeta.class );
    doReturn( embedManager ).when( parentTransMeta ).getNamedClusterEmbedManager();

    StepMeta parentStepMeta = mock( StepMeta.class );
    doReturn( parentTransMeta ).when( parentStepMeta ).getParentTransMeta();

    meta.setParentStepMeta( parentStepMeta );
    variableSpace = mock( VariableSpace.class );
  }

  @Test
  public void testGetXML() throws KettleStepException {
    when( field.getPentahoFieldName() ).thenReturn( "SampleName" );
    meta.setInputFields( Arrays.asList( field ) );

    assertNotNull( meta.getXML() );
    verify( meta, atLeastOnce() ).getDataLocation();
    verify( meta ).getSchemaLocation();

    verify( field ).getAvroFieldName();
    verify( field, times( 3 ) ).getPentahoFieldName();
    verify( field ).getTypeDesc();
    verify( embedManager ).registerUrl( nullable( String.class ) );
  }

  @Test
  public void testSaveRep() throws KettleException {
    meta.setInputFields( Arrays.asList( field ) );

    meta.saveRep( rep, metaStore, id_transformation, id_step );
    verify( meta ).getDataLocation();
    verify( meta ).getSchemaLocation();

    verify( field ).getAvroFieldName();
    verify( field ).getPentahoFieldName();
    verify( field ).getTypeDesc();
  }

  @Test
  public void testLoadXML()
    throws KettleException, URISyntaxException, SAXException, IOException, ParserConfigurationException {
    URL resource = getClass().getClassLoader()
      .getResource( getClass().getPackage().getName().replace( ".", "/" ) + "/AvroInput.xml" );
    Path path = Paths.get( resource.toURI() );
    Node node = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( Files.newInputStream( path ) )
      .getDocumentElement();
    meta.loadXML( node, databases, metaStore );
    assertEquals( "SampleFileName", meta.getDataLocation() );
    assertEquals( "SampleSchemaFileName", meta.getSchemaLocation() );

    AvroInputField field = meta.getInputFields()[ 0 ];
    assertEquals( "SampleName", field.getPentahoFieldName() );
    assertEquals( "SamplePath", field.getAvroFieldName() );
    assertEquals( "string", field.getAvroType().getType() );
  }

}
