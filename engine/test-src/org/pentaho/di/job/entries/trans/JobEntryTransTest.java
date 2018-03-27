/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2018 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 */

package org.pentaho.di.job.entries.trans;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
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

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.job.Job;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class JobEntryTransTest {
  private final String JOB_ENTRY_TRANS_NAME = "JobEntryTransName";
  private final String JOB_ENTRY_FILE_NAME = "JobEntryFileName";
  private final String JOB_ENTRY_FILE_DIRECTORY = "JobEntryFileDirectory";
  private final String JOB_ENTRY_DESCRIPTION = "JobEntryDescription";

  //prepare xml for use
  public Node getEntryNode( boolean includeTransname, ObjectLocationSpecificationMethod method )
    throws ParserConfigurationException, SAXException, IOException {
    JobEntryTrans jobEntryTrans = getJobEntryTrans();
    jobEntryTrans.setDescription( JOB_ENTRY_DESCRIPTION );
    jobEntryTrans.setFileName( JOB_ENTRY_FILE_NAME );
    jobEntryTrans.setDirectory( JOB_ENTRY_FILE_DIRECTORY );
    if ( includeTransname ) {
      jobEntryTrans.setTransname( JOB_ENTRY_FILE_NAME );
    }
    if ( method != null ) {
      jobEntryTrans.setSpecificationMethod( method );
    }
    String string = "<job>" + jobEntryTrans.getXML() + "</job>";
    InputStream stream = new ByteArrayInputStream( string.getBytes( StandardCharsets.UTF_8 ) );
    DocumentBuilder db;
    Document doc;
    db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    doc = db.parse( stream );
    Node entryNode = doc.getFirstChild();
    return entryNode;
  }

  private JobEntryTrans getJobEntryTrans() {
    JobEntryTrans jobEntryTrans = new JobEntryTrans( JOB_ENTRY_TRANS_NAME );
    return jobEntryTrans;
  }

  @SuppressWarnings( "unchecked" )
  private void testJobEntry( Repository rep, boolean includeJobName, ObjectLocationSpecificationMethod method,
      ObjectLocationSpecificationMethod expectedMethod )
    throws KettleXMLException, ParserConfigurationException, SAXException, IOException {
    List<DatabaseMeta> databases = mock( List.class );
    List<SlaveServer> slaveServers = mock( List.class );
    IMetaStore metaStore = mock( IMetaStore.class );
    JobEntryTrans jobEntryTrans = getJobEntryTrans();
    jobEntryTrans.loadXML( getEntryNode( includeJobName, method ), databases, slaveServers, rep, metaStore );
    assertEquals( "If we connect to repository then we use rep_name method",
        expectedMethod, jobEntryTrans.getSpecificationMethod() );
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
  public void testExecute_result_false_get_transMeta_exception() throws KettleException {
    JobEntryTrans jobEntryTrans = spy( new JobEntryTrans( JOB_ENTRY_TRANS_NAME ) );
    jobEntryTrans.setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
    jobEntryTrans.setParentJob( mock( Job.class ) );
    jobEntryTrans.setLogLevel( LogLevel.NOTHING );
    doThrow( new KettleException( "Error while loading transformation" ) ).when( jobEntryTrans ).getTransMeta( any(
        Repository.class ), any( IMetaStore.class ), any( VariableSpace.class ) );
    Result result = mock( Result.class );

    jobEntryTrans.execute( result, 1 );
    verify( result ).setResult( false );
  }

  @Test
  public void testExportResources() throws KettleException {
    JobEntryTrans jobEntryTrans = spy( getJobEntryTrans() );
    TransMeta transMeta = mock( TransMeta.class );

    String testName = "test";

    doReturn( transMeta ).when( jobEntryTrans ).getTransMeta( any( Repository.class ),
            any( VariableSpace.class ) );
    when( transMeta.exportResources( any( TransMeta.class ), any( Map.class ), any( ResourceNamingInterface.class ),
            any( Repository.class ), any( IMetaStore.class ) ) ).thenReturn( testName );

    jobEntryTrans.exportResources( null, null, null, null, null );

    verify( transMeta ).setFilename( "${" + Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY + "}/" + testName );
    verify( jobEntryTrans ).setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );
  }

  @Test
  public void testGetTransMeta() throws KettleException {
    String param1 = "param1";
    String param2 = "param2";
    String param3 = "param3";
    String parentValue1 = "parentValue1";
    String parentValue2 = "parentValue2";
    String childValue3 = "childValue3";

    JobEntryTrans jobEntryTrans = spy( getJobEntryTrans() );
    Repository rep = Mockito.mock( Repository.class );

    TransMeta meta = new TransMeta();
    meta.setVariable( param2, "childValue2 should be override" );
    meta.setVariable( param3, childValue3 );

    Mockito.doReturn( meta ).when( rep )
      .loadTransformation( Mockito.eq( "test.ktr" ), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyBoolean(),
        Mockito.anyObject() );

    VariableSpace parentSpace = new Variables();
    parentSpace.setVariable( param1, parentValue1 );
    parentSpace.setVariable( param2, parentValue2 );

    jobEntryTrans.setFileName( "/home/admin/test.ktr" );

    Mockito.doNothing().when( jobEntryTrans ).logBasic( Mockito.anyString() );
    jobEntryTrans.setSpecificationMethod( ObjectLocationSpecificationMethod.FILENAME );

    TransMeta transMeta;
    jobEntryTrans.setPassingAllParameters( false );
    transMeta = jobEntryTrans.getTransMeta( rep, null, parentSpace );
    Assert.assertEquals( null, transMeta.getVariable( param1 ) );
    Assert.assertEquals( parentValue2, transMeta.getVariable( param2 ) );
    Assert.assertEquals( childValue3, transMeta.getVariable( param3 ) );

    jobEntryTrans.setPassingAllParameters( true );
    transMeta = jobEntryTrans.getTransMeta( rep, null, parentSpace );
    Assert.assertEquals( parentValue1, transMeta.getVariable( param1 ) );
    Assert.assertEquals( parentValue2, transMeta.getVariable( param2 ) );
    Assert.assertEquals( childValue3, transMeta.getVariable( param3 ) );
  }
}
