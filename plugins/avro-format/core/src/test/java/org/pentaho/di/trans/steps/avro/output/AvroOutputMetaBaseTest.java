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
package org.pentaho.di.trans.steps.avro.output;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
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
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;

/**
 * Created by rmansoor on 4/8/2018.
 */
@RunWith( MockitoJUnitRunner.class )
public class AvroOutputMetaBaseTest {
  private static final String VALID_SINGLE_FILE_URI_REGEX =
      "^hdfs://username:password@cluster.host.internal:8020/user/out/avro_test/output_[0-9]+_[0-9]+.avro";
  private static final String VALID_PART_FILE_URI_REGEX =
      "^hdfs://username:password@cluster.host.internal:8020/user/out/avro_test_[0-9]+_[0-9]+/";

  private AvroOutputMetaBase metaBase;

  @Mock
  private IMetaStore metaStore;

  @Mock
  private List<DatabaseMeta> databases;

  @Mock
  private NamedClusterEmbedManager embedManager;

  @Before
  public void setUp() throws Exception {

    metaBase = spy( new AvroOutputMetaBase() {
      @Override
      public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
          TransMeta transMeta,
          Trans trans ) {
        return null;
      }

      @Override public StepDataInterface getStepData() {
        return null;
      }
    } );

    TransMeta parentTransMeta = mock( TransMeta.class );
    doReturn( embedManager ).when( parentTransMeta ).getNamedClusterEmbedManager();

    StepMeta parentStepMeta = mock( StepMeta.class );
    doReturn( parentTransMeta ).when( parentStepMeta ).getParentTransMeta();

    metaBase.setParentStepMeta( parentStepMeta );
  }

  @Test
  public void setCompressionType() {
    metaBase.setCompressionType( "snappy" );
    Assert.assertEquals( metaBase.getCompressionType(), AvroOutputMetaBase.CompressionType.SNAPPY.toString() );
    metaBase.setCompressionType( "Snappy" );
    Assert.assertEquals( metaBase.getCompressionType(), AvroOutputMetaBase.CompressionType.SNAPPY.toString() );
    metaBase.setCompressionType( "SNAPPY" );
    Assert.assertEquals( metaBase.getCompressionType(), AvroOutputMetaBase.CompressionType.SNAPPY.toString() );
    metaBase.setCompressionType( "deflate" );
    Assert.assertEquals( metaBase.getCompressionType(), AvroOutputMetaBase.CompressionType.DEFLATE.toString() );
    metaBase.setCompressionType( "Deflate" );
    Assert.assertEquals( metaBase.getCompressionType(), AvroOutputMetaBase.CompressionType.DEFLATE.toString() );
    metaBase.setCompressionType( "DEFLATE" );
    Assert.assertEquals( metaBase.getCompressionType(), AvroOutputMetaBase.CompressionType.DEFLATE.toString() );
    metaBase.setCompressionType( "DEFLATE124" );
    Assert.assertEquals( metaBase.getCompressionType(), AvroOutputMetaBase.CompressionType.NONE.toString() );
    metaBase.setCompressionType( "None" );
    Assert.assertEquals( metaBase.getCompressionType(), AvroOutputMetaBase.CompressionType.NONE.toString() );
    metaBase.setCompressionType( "NONE" );
    Assert.assertEquals( metaBase.getCompressionType(), AvroOutputMetaBase.CompressionType.NONE.toString() );
  }

  @Test
  public void kettleConstructFilename() throws Exception {
    loadStepMeta( "/AvroOutput.xml" );
    String fileName = metaBase.getFilename();
    String constructedFileName = metaBase.constructOutputFilename( fileName );

    assertTrue( constructedFileName.matches( VALID_SINGLE_FILE_URI_REGEX ) );
  }

  @Test
  public void getXmlTest() {
    metaBase.getXML();
    verify( embedManager ).registerUrl( nullable( String.class ) );
  }

  private void loadStepMeta( String resourceFile )
      throws URISyntaxException, ParserConfigurationException, IOException, KettleXMLException, SAXException {
    URL resource = getClass().getClassLoader()
        .getResource( getClass().getPackage().getName().replace( ".", "/" ) + resourceFile );
    Path path = Paths.get( resource.toURI() );
    Node node = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( Files.newInputStream( path ) )
        .getDocumentElement();
    metaBase.loadXML( node, databases, metaStore );
  }
}
