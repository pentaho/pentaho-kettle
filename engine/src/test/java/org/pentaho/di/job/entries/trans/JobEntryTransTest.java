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

package org.pentaho.di.job.entries.trans;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.named.cluster.NamedClusterEmbedManager;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.test.util.InternalState;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    JobMeta mockJobMeta = mock( JobMeta.class );
    jobEntryTrans.setParentJobMeta( mockJobMeta );
    jobEntryTrans.setLogLevel( LogLevel.NOTHING );
    doThrow( new KettleException( "Error while loading transformation" ) ).when( jobEntryTrans ).getTransMeta( any(
        Repository.class ), any( IMetaStore.class ), any( VariableSpace.class ) );
    Result result = mock( Result.class );

    jobEntryTrans.execute( result, 1 );
    verify( result ).setResult( false );
  }

  @Test
  public void testCurrDirListener() throws Exception {
    JobMeta meta = mock( JobMeta.class );
    JobEntryTrans jet = getJobEntryTrans();
    jet.setParentJobMeta( meta );
    jet.setParentJobMeta( null );
    verify( meta, times( 1 ) ).addCurrentDirectoryChangedListener( any() );
    verify( meta, times( 1 ) ).removeCurrentDirectoryChangedListener( any() );
  }

  @Ignore( "Test can't be properly mocked" )
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
  public void testPrepareFieldNamesParameters() throws UnknownParamException {
    // array of params
    String[] parameterNames = new String[2];
    parameterNames[0] = "param1";
    parameterNames[1] = "param2";

    // array of fieldNames params
    String[] parameterFieldNames = new String[1];
    parameterFieldNames[0] = "StreamParam1";

    // array of parameterValues params
    String[] parameterValues = new String[2];
    parameterValues[1] = "ValueParam2";


    JobEntryTrans jet = new JobEntryTrans();
    VariableSpace variableSpace = new Variables();
    jet.copyVariablesFrom( variableSpace );

    //at this point StreamColumnNameParams are already inserted in namedParams
    NamedParams namedParam = Mockito.mock( NamedParamsDefault.class );
    Mockito.doReturn( "value1" ).when( namedParam ).getParameterValue(  "param1" );
    Mockito.doReturn( "value2" ).when( namedParam ).getParameterValue(  "param2" );

    jet.prepareFieldNamesParameters( parameterNames, parameterFieldNames, parameterValues, namedParam, jet );

    Assert.assertEquals( "value1", jet.getVariable( "param1" ) );
    Assert.assertEquals( null, jet.getVariable( "param2" ) );
  }

  @Test
  public void testPrepareFieldNamesParametersWithNulls() throws UnknownParamException {
    //NOTE: this only tests the prepareFieldNamesParameters function not all variable substitution logic
    // array of params
    String[] parameterNames = new String[7];
    parameterNames[0] = "param1";
    parameterNames[1] = "param2";
    parameterNames[2] = "param3";
    parameterNames[3] = "param4";
    parameterNames[4] = "param5";
    parameterNames[5] = "param6";
    parameterNames[6] = "param7";

    // array of fieldNames params
    String[] parameterFieldNames = new String[7];
    parameterFieldNames[0] = null;
    parameterFieldNames[2] = "ValueParam3";
    parameterFieldNames[3] = "FieldValueParam4";
    parameterFieldNames[4] = "FieldValueParam5";
    parameterFieldNames[6] = "FieldValueParam7";

    // array of parameterValues params
    String[] parameterValues = new String[7];
    parameterValues[1] = "ValueParam2";
    parameterValues[3] = "";
    parameterValues[4] = "StaticValueParam5";
    parameterValues[5] = "StaticValueParam6";


    JobEntryTrans jet = new JobEntryTrans();
    VariableSpace variableSpace = new Variables();
    jet.copyVariablesFrom( variableSpace );

    jet.setVariable( "param6", "someDummyPreviousValue6" );
    jet.setVariable( "param7", "someDummyPreviousValue7" );

    //at this point StreamColumnNameParams are already inserted in namedParams
    NamedParams namedParam = Mockito.mock( NamedParamsDefault.class );
    Mockito.doReturn( "value1" ).when( namedParam ).getParameterValue(  "param1" );
    Mockito.doReturn( "value2" ).when( namedParam ).getParameterValue(  "param2" );
    Mockito.doReturn( "value3" ).when( namedParam ).getParameterValue(  "param3" );
    Mockito.doReturn( "value4" ).when( namedParam ).getParameterValue(  "param4" );
    Mockito.doReturn( "value5" ).when( namedParam ).getParameterValue(  "param5" );
    Mockito.doReturn( "" ).when( namedParam ).getParameterValue(  "param6" );
    Mockito.doReturn( "" ).when( namedParam ).getParameterValue(  "param7" );

    jet.prepareFieldNamesParameters( parameterNames, parameterFieldNames, parameterValues, namedParam, jet );
    // "param1" has parameterFieldName value = null and no parameterValues defined so it should be null
    Assert.assertEquals( null, jet.getVariable( "param1" ) );
    // "param2" has only parameterValues defined and no parameterFieldName value so it should be null
    Assert.assertEquals( null, jet.getVariable( "param2" ) );
    // "param3" has only the parameterFieldName defined so it should return the mocked value
    Assert.assertEquals( "value3", jet.getVariable( "param3" ) );
    // "param4" has parameterFieldName and also an empty parameterValues defined so it should return the mocked value
    Assert.assertEquals( "value4", jet.getVariable( "param4" ) );
    // "param5" has parameterFieldName and also parameterValues defined with a not empty value so it should return null
    Assert.assertEquals( null, jet.getVariable( "param5" ) );
    // "param6" only has a parameterValues defined with a not empty value and has a previous value on it ( someDummyPreviousValue6 )
    // so it should keep "someDummyPreviousValue6" since there is no parameterFieldNames definition
    Assert.assertEquals( "someDummyPreviousValue6", jet.getVariable( "param6" ) );
    // "param7" only has a parameterFieldNames defined and has a previous value on it ( someDummyPreviousValue7 )
    // so it should update to the new value mocked = "" even it is a blank value - PDI-18227
    Assert.assertEquals( "", jet.getVariable( "param7" ) );
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
    JobMeta parentJobMeta = spy( new JobMeta() );
    when( parentJobMeta.getNamedClusterEmbedManager() ).thenReturn( mock( NamedClusterEmbedManager.class ) );
    jobEntryTrans.setParentJobMeta( parentJobMeta);

    Repository rep = Mockito.mock( Repository.class );
    RepositoryDirectory repositoryDirectory = Mockito.mock( RepositoryDirectory.class );
    RepositoryDirectoryInterface repositoryDirectoryInterface = Mockito.mock( RepositoryDirectoryInterface.class );
    Mockito.doReturn( repositoryDirectoryInterface ).when( rep ).loadRepositoryDirectoryTree();
    Mockito.doReturn( repositoryDirectory ).when( repositoryDirectoryInterface ).findDirectory( "/home/admin" );

    TransMeta meta = new TransMeta();
    meta.setVariable( param2, "childValue2 should be override" );
    meta.setVariable( param3, childValue3 );

    Mockito.doReturn( meta ).when( rep )
      .loadTransformation( Mockito.eq( "test.ktr" ), Mockito.any(), Mockito.any(), Mockito.anyBoolean(),
        Mockito.any() );

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

  @Test
  public void testGetTransMetaRepo() throws KettleException {
    String param1 = "dir";
    String param2 = "file";
    String parentValue1 = "/home/admin";
    String parentValue2 = "test";

    JobEntryTrans jobEntryTrans = spy( getJobEntryTrans() );
    JobMeta parentJobMeta = spy( new JobMeta() );
    when( parentJobMeta.getNamedClusterEmbedManager() ).thenReturn( mock( NamedClusterEmbedManager.class ) );
    jobEntryTrans.setParentJobMeta( parentJobMeta);

    Repository rep = Mockito.mock( Repository.class );
    RepositoryDirectory repositoryDirectory = Mockito.mock( RepositoryDirectory.class );
    RepositoryDirectoryInterface repositoryDirectoryInterface = Mockito.mock( RepositoryDirectoryInterface.class );
    Mockito.doReturn( repositoryDirectoryInterface ).when( rep ).loadRepositoryDirectoryTree();
    Mockito.doReturn( repositoryDirectory ).when( repositoryDirectoryInterface ).findDirectory( parentValue1 );

    TransMeta meta = new TransMeta();
    meta.setVariable( param2, "childValue2 should be override" );

    Mockito.doReturn( meta ).when( rep )
            .loadTransformation( Mockito.eq( "test" ), Mockito.any(), Mockito.any(), Mockito.anyBoolean(),
                    Mockito.any() );

    VariableSpace parentSpace = new Variables();
    parentSpace.setVariable( param1, parentValue1 );
    parentSpace.setVariable( param2, parentValue2 );

    jobEntryTrans.setDirectory( "${dir}" );
    jobEntryTrans.setTransname( "${file}" );

    Mockito.doNothing().when( jobEntryTrans ).logBasic( Mockito.anyString() );
    jobEntryTrans.setSpecificationMethod( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );

    TransMeta transMeta;
    jobEntryTrans.setPassingAllParameters( false );
    transMeta = jobEntryTrans.getTransMeta( rep, null, parentSpace );
    Assert.assertEquals( null, transMeta.getVariable( param1 ) );
    Assert.assertEquals( parentValue2, transMeta.getVariable( param2 ) );

    jobEntryTrans.setPassingAllParameters( true );
    transMeta = jobEntryTrans.getTransMeta( rep, null, parentSpace );
    Assert.assertEquals( parentValue1, transMeta.getVariable( param1 ) );
    Assert.assertEquals( parentValue2, transMeta.getVariable( param2 ) );
  }

  @Test
  public void updateResultTest() {
    JobEntryTrans jobEntryTrans = spy( getJobEntryTrans() );
    Trans transMock = mock( Trans.class );
    InternalState.setInternalState( jobEntryTrans, "trans", transMock );
    //Transformation returns result with 5 rows
    when( transMock.getResult() ).thenReturn( generateDummyResult( 5 ) );
    //Previous result has 3 rows
    Result resultToBeUpdated = generateDummyResult( 3 );
    //Update the result
    jobEntryTrans.updateResult( resultToBeUpdated );
    //Result should have 5 number of rows since the trans result has 5 rows (meaning 5 rows were returned)
    assertEquals( resultToBeUpdated.getRows().size(), 5 );
  }

  @Test
  public void updateResultTestWithZeroRows() {
    JobEntryTrans jobEntryTrans = spy( getJobEntryTrans() );
    Trans transMock = mock( Trans.class );
    InternalState.setInternalState( jobEntryTrans, "trans", transMock );
    //Transformation returns result with 0 rows
    when( transMock.getResult() ).thenReturn( generateDummyResult( 0 ) );
    //Previous result has 3 rows
    Result resultToBeUpdated = generateDummyResult( 3 );
    //Update the result
    jobEntryTrans.updateResult( resultToBeUpdated );
    //Result should have 3 number of rows since the trans result has no rows (meaning nothing was done)
    assertEquals( resultToBeUpdated.getRows().size(), 3 );
  }

  @Test
  public void updateResultTestWithZeroRowsForced() {
    JobEntryTrans jobEntryTrans = spy( getJobEntryTrans() );
    Trans transMock = mock( Trans.class );
    InternalState.setInternalState( jobEntryTrans, "trans", transMock );
    //Transformation returns result with 0 rows
    when( transMock.getResult() ).thenReturn( generateDummyResult( 0 ) );
    //Previous result has 3 rows
    Result resultToBeUpdated = generateDummyResult( 3 );
    //Result Set Was updated
    when( transMock.isResultRowsSet() ).thenReturn( true );
    //Update the result
    jobEntryTrans.updateResult( resultToBeUpdated );
    //Result should have zero number of rows since the result rows has no rows but the result set was in fact updated
    //(meaning something was done that resulted in zero rows.
    assertEquals( resultToBeUpdated.getRows().size(), 0 );
  }

  private void updateResultTest( int previousRowsResult, int newRowsResult ) {

  }

  private Result generateDummyResult( int nRows ) {
    Result result = new Result();
    List<RowMetaAndData> rows = new ArrayList<>();
    for ( int i = 0; i < nRows; ++i ) {
      rows.add( new RowMetaAndData() );
    }
    result.setRows( rows );
    return result;
  }
}
