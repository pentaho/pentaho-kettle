/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.job.entries.job;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doReturn;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class JobEntryJobTest {

  private final String JOB_ENTRY_JOB_NAME = "JobEntryJobName";
  private final String JOB_ENTRY_FILE_NAME = "JobEntryFileName";
  private final String JOB_ENTRY_FILE_DIRECTORY = "JobEntryFileDirectory";
  private final String JOB_ENTRY_DESCRIPTION = "JobEntryDescription";

  //prepare xml for use
  public Node getEntryNode( boolean includeJobname, ObjectLocationSpecificationMethod method )
    throws ParserConfigurationException, SAXException, IOException {
    JobEntryJob jobEntryJob = getJobEntryJob();
    jobEntryJob.setDescription( JOB_ENTRY_DESCRIPTION );
    jobEntryJob.setFileName( JOB_ENTRY_FILE_NAME );
    jobEntryJob.setDirectory( JOB_ENTRY_FILE_DIRECTORY );
    if ( includeJobname ) {
      jobEntryJob.setJobName( JOB_ENTRY_FILE_NAME );
    }
    if ( method != null ) {
      jobEntryJob.setSpecificationMethod( method );
    }
    String string = "<job>" + jobEntryJob.getXML() + "</job>";
    InputStream stream = new ByteArrayInputStream( string.getBytes( StandardCharsets.UTF_8 ) );
    DocumentBuilder db;
    Document doc;
    db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    doc = db.parse( stream );
    Node entryNode = doc.getFirstChild();
    return entryNode;
  }

  private JobEntryJob getJobEntryJob() {
    JobEntryJob jobEntryJob = new JobEntryJob( JOB_ENTRY_JOB_NAME );
    return jobEntryJob;
  }

  @SuppressWarnings( "unchecked" )
  private void testJobEntry( Repository rep, boolean includeJobName, ObjectLocationSpecificationMethod method,
      ObjectLocationSpecificationMethod expectedMethod )
    throws KettleXMLException, ParserConfigurationException, SAXException, IOException {
    List<DatabaseMeta> databases = mock( List.class );
    List<SlaveServer> slaveServers = mock( List.class );
    IMetaStore metaStore = mock( IMetaStore.class );
    JobEntryJob jobEntryJob = getJobEntryJob();
    jobEntryJob.loadXML( getEntryNode( includeJobName, method ), databases, slaveServers, rep, metaStore );
    assertEquals( "If we connect to repository then we use rep_name method",
        expectedMethod, jobEntryJob.getSpecificationMethod() );
  }

  /**
   * BACKLOG-179 - Exporting/Importing Jobs breaks Transformation specification when using "Specify by reference"
   * 
   * Test checks that we choose different {@link ObjectLocationSpecificationMethod} when connection to
   * {@link Repository} and disconnected. 
   * 
   * <b>Important!</b> You must rewrite test when change import logic
   * 
   * @throws KettleXMLException
   * @throws IOException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  @Test
  public void testChooseSpecMethodByRepositoryConnectionStatus()
    throws KettleXMLException, ParserConfigurationException, SAXException, IOException {
    Repository rep = mock( Repository.class );
    when( rep.isConnected() ).thenReturn( true );

    // 000
    // not connected, no jobname, no method
    testJobEntry( null, false, null, ObjectLocationSpecificationMethod.FILENAME );

    // 001
    // not connected, no jobname, REPOSITORY_BY_REFERENCE method
    testJobEntry( null, false, ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE, ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );
    // not connected, no jobname, REPOSITORY_BY_NAME method
    testJobEntry( null, false, ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    // not connected, no jobname, FILENAME method
    testJobEntry( null, false, ObjectLocationSpecificationMethod.FILENAME, ObjectLocationSpecificationMethod.FILENAME );

    // 010
    // not connected, jobname, no method
    testJobEntry( null, true, null, ObjectLocationSpecificationMethod.FILENAME );

    // 011
    // not connected, jobname, REPOSITORY_BY_REFERENCE method
    testJobEntry( null, true, ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE, ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );
    // not connected, jobname, REPOSITORY_BY_NAME method
    testJobEntry( null, true, ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    // not connected, jobname, FILENAME method
    testJobEntry( null, true, ObjectLocationSpecificationMethod.FILENAME, ObjectLocationSpecificationMethod.FILENAME );

    // 100
    // connected, no jobname, no method
    testJobEntry( rep, false, null, ObjectLocationSpecificationMethod.FILENAME );

    // 101
    // connected, no jobname, REPOSITORY_BY_REFERENCE method
    testJobEntry( rep, false, ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE, ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE );
    // connected, no jobname, REPOSITORY_BY_NAME method
    testJobEntry( rep, false, ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    // connected, no jobname, FILENAME method
    testJobEntry( rep, false, ObjectLocationSpecificationMethod.FILENAME, ObjectLocationSpecificationMethod.FILENAME );

    // 110  
    // connected, jobname, no method
    testJobEntry( rep, true, null, ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );

    // 111
    // connected, jobname, REPOSITORY_BY_REFERENCE method
    testJobEntry( rep, true, ObjectLocationSpecificationMethod.REPOSITORY_BY_REFERENCE, ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    // connected, jobname, REPOSITORY_BY_NAME method
    testJobEntry( rep, true, ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME, ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
    // connected, jobname, FILENAME method    
    testJobEntry( rep, true, ObjectLocationSpecificationMethod.FILENAME, ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
  }

  @Test
  public void testCurrDirListener() throws Exception {
    JobMeta meta = mock( JobMeta.class );
    JobEntryJob jej = getJobEntryJob();
    jej.setParentJobMeta( null );
    jej.setParentJobMeta( meta );
    jej.setParentJobMeta( null );
    verify( meta, times( 1 ) ).addCurrentDirectoryChangedListener( any() );
    verify( meta, times( 1 ) ).removeCurrentDirectoryChangedListener( any() );
  }

  @Test
  public void testExportResources() throws Exception {
    JobEntryJob jobEntryJob = spy( getJobEntryJob() );
    JobMeta jobMeta = mock( JobMeta.class );

    String testName = "test";

    doReturn( jobMeta ).when( jobEntryJob ).getJobMeta( any( Repository.class ),
            any( IMetaStore.class ), any( VariableSpace.class ) );
    when( jobMeta.exportResources( any( JobMeta.class ), any( Map.class ), any( ResourceNamingInterface.class ),
            any( Repository.class ), any( IMetaStore.class ) ) ).thenReturn( testName );

    jobEntryJob.exportResources( null, null, null, null, null );

    verify( jobMeta ).setFilename( "${" + Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY + "}/" + testName );
    verify( jobEntryJob ).setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
  }
}