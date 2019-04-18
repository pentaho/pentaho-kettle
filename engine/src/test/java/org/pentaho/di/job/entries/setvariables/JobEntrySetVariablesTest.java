/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.job.entries.setvariables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class JobEntrySetVariablesTest {
  private Job job;
  private JobEntrySetVariables entry;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleLogStore.init();
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    job = new Job( null, new JobMeta() );
    entry = new JobEntrySetVariables();
    job.getJobMeta().addJobEntry( new JobEntryCopy( entry ) );
    entry.setParentJob( job );
    job.setStopped( false );
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testASCIIText() throws Exception {
    // properties file with native2ascii
    entry.setFilename( "src/test/resources/org/pentaho/di/job/entries/setvariables/ASCIIText.properties" );
    entry.setReplaceVars( true );
    Result result = entry.execute( new Result(), 0 );
    assertTrue( "Result should be true", result.getResult() );
    assertEquals( "日本語", entry.getVariable( "Japanese" ) );
    assertEquals( "English", entry.getVariable( "English" ) );
    assertEquals( "中文", entry.getVariable( "Chinese" ) );
  }

  @Test
  public void testUTF8Text() throws Exception {
    // properties files without native2ascii
    entry.setFilename( "src/test/resources/org/pentaho/di/job/entries/setvariables/UTF8Text.properties" );
    entry.setReplaceVars( true );
    Result result = entry.execute( new Result(), 0 );
    assertTrue( "Result should be true", result.getResult() );
    assertEquals( "日本語", entry.getVariable( "Japanese" ) );
    assertEquals( "English", entry.getVariable( "English" ) );
    assertEquals( "中文", entry.getVariable( "Chinese" ) );
  }
  @Test
  public void testInputStreamClosed() throws Exception {
    // properties files without native2ascii
    String propertiesFilename = "src/test/resources/org/pentaho/di/job/entries/setvariables/UTF8Text.properties";
    entry.setFilename( propertiesFilename );
    entry.setReplaceVars( true );
    Result result = entry.execute( new Result(), 0 );
    assertTrue( "Result should be true", result.getResult() );
    RandomAccessFile fos = null;
    try {
      File file = new File( propertiesFilename );
      if ( file.exists() ) {
        fos = new RandomAccessFile( file, "rw" );
      }
    } catch ( FileNotFoundException | SecurityException e ) {
      fail( "the file with properties should be unallocated" );
    } finally {
      if ( fos != null ) {
        fos.close();
      }
    }
  }

  @Test
  public void testParentJobVariablesExecutingFilePropertiesThatChangesVariablesAndParameters() throws Exception {
    entry.setReplaceVars( true );
    entry.setFileVariableType( 1 );

    Job parentJob = entry.getParentJob();

    parentJob.addParameterDefinition( "parentParam", "", "" );
    parentJob.setParameterValue( "parentParam", "parentValue" );
    parentJob.setVariable( "parentParam", "parentValue" );

    entry.setFilename( "src/test/resources/org/pentaho/di/job/entries/setvariables/configurationA.properties" );
    Result result = entry.execute( new Result(), 0 );
    assertTrue( "Result should be true", result.getResult() );
    assertEquals( "a", parentJob.getVariable( "propertyFile" ) );
    assertEquals( "a", parentJob.getVariable( "dynamicProperty" ) );
    assertEquals( "parentValue", parentJob.getVariable( "parentParam" ) );


    entry.setFilename( "src/test/resources/org/pentaho/di/job/entries/setvariables/configurationB.properties" );
    result = entry.execute( new Result(), 0 );
    assertTrue( "Result should be true", result.getResult() );
    assertEquals( "b", parentJob.getVariable( "propertyFile" ) );
    assertEquals( "new", parentJob.getVariable( "newProperty" ) );
    assertEquals( "haha", parentJob.getVariable( "parentParam" ) );
    assertEquals( "static", parentJob.getVariable( "staticProperty" ) );
    assertEquals( "", parentJob.getVariable( "dynamicProperty" ) );

    entry.setFilename( "src/test/resources/org/pentaho/di/job/entries/setvariables/configurationA.properties" );
    result = entry.execute( new Result(), 0 );
    assertTrue( "Result should be true", result.getResult() );
    assertEquals( "a", parentJob.getVariable( "propertyFile" ) );
    assertEquals( "", parentJob.getVariable( "newProperty" ) );
    assertEquals( "parentValue", parentJob.getVariable( "parentParam" ) );
    assertEquals( "", parentJob.getVariable( "staticProperty" ) );
    assertEquals( "a", parentJob.getVariable( "dynamicProperty" ) );


    entry.setFilename( "src/test/resources/org/pentaho/di/job/entries/setvariables/configurationB.properties" );
    result = entry.execute( new Result(), 0 );
    assertTrue( "Result should be true", result.getResult() );
    assertEquals( "b", parentJob.getVariable( "propertyFile" ) );
    assertEquals( "new", parentJob.getVariable( "newProperty" ) );
    assertEquals( "haha", parentJob.getVariable( "parentParam" ) );
    assertEquals( "static", parentJob.getVariable( "staticProperty" ) );
    assertEquals( "", parentJob.getVariable( "dynamicProperty" ) );
  }

  @Test
  public void testJobEntrySetVariablesExecute_VARIABLE_TYPE_JVM_NullVariable() throws Exception {
    List<DatabaseMeta> databases = mock( List.class );
    List<SlaveServer> slaveServers = mock( List.class );
    Repository repository = mock( Repository.class );
    IMetaStore metaStore = mock( IMetaStore.class );
    entry.loadXML( getEntryNode( "nullVariable", null, "JVM" ), databases, slaveServers, repository, metaStore );
    Result result = entry.execute( new Result(), 0 );
    assertTrue( "Result should be true", result.getResult() );
    assertNull( System.getProperty( "nullVariable" )  );
  }

  @Test
  public void testJobEntrySetVariablesExecute_VARIABLE_TYPE_CURRENT_JOB_NullVariable() throws Exception {
    List<DatabaseMeta> databases = mock( List.class );
    List<SlaveServer> slaveServers = mock( List.class );
    Repository repository = mock( Repository.class );
    IMetaStore metaStore = mock( IMetaStore.class );
    entry.loadXML( getEntryNode( "nullVariable", null, "CURRENT_JOB" ), databases, slaveServers, repository, metaStore );
    Result result = entry.execute( new Result(), 0 );
    assertTrue( "Result should be true", result.getResult() );
    assertNull( entry.getVariable( "nullVariable" )  );
  }

  @Test
  public void testJobEntrySetVariablesExecute_VARIABLE_TYPE_JVM_VariableNotNull() throws Exception {
    List<DatabaseMeta> databases = mock( List.class );
    List<SlaveServer> slaveServers = mock( List.class );
    Repository repository = mock( Repository.class );
    IMetaStore metaStore = mock( IMetaStore.class );
    entry.loadXML( getEntryNode( "variableNotNull", "someValue", "JVM" ), databases, slaveServers, repository, metaStore );
    assertNull( System.getProperty( "variableNotNull" )  );
    Result result = entry.execute( new Result(), 0 );
    assertTrue( "Result should be true", result.getResult() );
    assertEquals( "someValue", System.getProperty( "variableNotNull" ) );
  }

  @Test
  public void testJobEntrySetVariablesExecute_VARIABLE_TYPE_CURRENT_JOB_VariableNotNull() throws Exception {
    List<DatabaseMeta> databases = mock( List.class );
    List<SlaveServer> slaveServers = mock( List.class );
    Repository repository = mock( Repository.class );
    IMetaStore metaStore = mock( IMetaStore.class );
    entry.loadXML( getEntryNode( "variableNotNull", "someValue", "CURRENT_JOB" ), databases, slaveServers, repository, metaStore );
    assertNull( System.getProperty( "variableNotNull" )  );
    Result result = entry.execute( new Result(), 0 );
    assertTrue( "Result should be true", result.getResult() );
    assertEquals( "someValue", entry.getVariable( "variableNotNull" ) );
  }

  //prepare xml for use
  public Node getEntryNode( String variable_name, String variable_value, String variable_type )
    throws ParserConfigurationException, SAXException, IOException {
    StringBuilder sb = new StringBuilder();
    sb.append( XMLHandler.openTag( "job" ) );
    sb.append( "      " ).append( XMLHandler.openTag( "fields" ) );
    sb.append( "      " ).append( XMLHandler.openTag( "field" ) );
    sb.append( "      " ).append( XMLHandler.addTagValue( "variable_name", variable_name ) );
    if ( variable_value != null ) {
      sb.append( "      " ).append( XMLHandler.addTagValue( "variable_value", variable_value ) );
    }
    if ( variable_type != null ) {
      sb.append( "          " ).append(
        XMLHandler.addTagValue( "variable_type", variable_type ) );
    }
    sb.append( "      " ).append( XMLHandler.closeTag( "field" ) );
    sb.append( "      " ).append( XMLHandler.closeTag( "fields" ) );
    sb.append( XMLHandler.closeTag( "job" ) );

    InputStream stream = new ByteArrayInputStream( sb.toString().getBytes( StandardCharsets.UTF_8 ) );
    DocumentBuilder db;
    Document doc;
    db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    doc = db.parse( stream );
    Node entryNode = doc.getFirstChild();
    return entryNode;
  }

}
