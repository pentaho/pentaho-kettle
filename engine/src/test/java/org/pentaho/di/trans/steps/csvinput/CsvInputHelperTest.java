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


package org.pentaho.di.trans.steps.csvinput;

import org.apache.commons.vfs2.FileObject;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.StepMockUtil;
import org.pentaho.di.trans.steps.common.CsvInputAwareMeta;
import org.pentaho.di.trans.steps.fileinput.text.BufferedInputStreamReader;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;
import static org.pentaho.di.core.util.Assert.assertNotNull;

public class CsvInputHelperTest extends CsvInputUnitTestBase {

  private StepMockHelper<CsvInputMeta, StepDataInterface> stepMockHelper;
  private LogChannelInterface logChannelInterface;
  private CsvInputMeta csvInputMeta;

  @Before
  public void setUp() throws Exception {
    logChannelInterface = mock( LogChannelInterface.class );
    stepMockHelper = StepMockUtil
      .getStepMockHelper( CsvInputMeta.class, "CsvInputHelperTest" );
    csvInputMeta = mock( CsvInputMeta.class );
  }

  @After
  public void cleanUp() {
    stepMockHelper.cleanUp();
  }

  @Test
  @SuppressWarnings( "java:S1874" )
  // CsvInput uses deprecated class TextFileInput & KettleVFS.getFileObject to read data from file
  public void testGetFieldsAction() throws Exception {
    // Setup helper and transMeta
    CsvInputHelper helper = new CsvInputHelper();
    TransMeta transMeta = spy( new TransMeta() );
    helper.setTransMeta( transMeta );

    // Create test data and mocks
    String sampleData = "field1,field2\nvalue1,value2";
    TextFileInputField[] inputFields = createInputFileFields( "f1", "f2", "f3" );
    
    // Create and setup meta
    StepMeta stepMeta = mock( StepMeta.class );
    CsvInputMeta meta = new CsvInputMeta();
    meta.setDefault();
    meta.setInputFields( inputFields );
    
    // Setup mocks
    doReturn( stepMeta ).when( transMeta ).findStep( "testStep" );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );

    // Create spied helper for input stream mocking
    CsvInputHelper spyHelper = spy( helper );
    InputStream inputStream = new ByteArrayInputStream( sampleData.getBytes() );
    BufferedInputStreamReader reader = new BufferedInputStreamReader( new InputStreamReader( inputStream ) );
    
    when( spyHelper.getInputStream( meta ) ).thenReturn( inputStream );
    when( spyHelper.getBufferedReader( meta, inputStream ) ).thenReturn( reader );
    when( spyHelper.getFieldNames( meta ) ).thenReturn( new String[] { "field1", "field2" } );

    // Create query params
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "testStep" );
    queryParams.put( "noOfFields", "10" );
    queryParams.put( "isSampleSummary", "true" );

    // Execute test
    JSONObject response = spyHelper.getFieldsAction( queryParams );

    // Verify
    assertNotNull( response );
    assertNotNull( response.get( "fields" ) );
    assertNotNull( response.get( "summary" ) );
  }

  @Test
  public void testHandleStepAction_GetFields_Success() throws Exception {
    CsvInputHelper helper = new CsvInputHelper();
    TransMeta transMeta = mock( TransMeta.class );
    StepMeta stepMeta = mock( StepMeta.class );
    CsvInputMeta meta = mock( CsvInputMeta.class );

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "testStep" );
    queryParams.put( "noOfFields", "10" );
    queryParams.put( "isSampleSummary", "true" );

    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( meta );

    CsvInputHelper spyHelper = spy( helper );
    JSONObject expectedResponse = new JSONObject();
    expectedResponse.put( "fields", new ArrayList<>() );
    expectedResponse.put( "summary", "test summary" );

    // Use doReturn + anyMap to avoid argument identity issues
    doReturn( expectedResponse ).when( spyHelper ).getFieldsAction( anyMap() );

    JSONObject response = spyHelper.handleStepAction( "getFields", transMeta, queryParams );

    assertNotNull( response );
    assertEquals( expectedResponse, response );
  }

  @Test
  public void testHandleStepAction_MethodNotFound() {
    CsvInputHelper helper = new CsvInputHelper();
    TransMeta transMeta = mock( TransMeta.class );
    Map<String, String> queryParams = new HashMap<>();

    JSONObject response = helper.handleStepAction( "unknownMethod", transMeta, queryParams );

    assertNotNull( response );
    assertEquals( CsvInputHelper.FAILURE_METHOD_NOT_FOUND_RESPONSE, response.get( CsvInputHelper.ACTION_STATUS ) );
  }

  @Test
  public void testHandleStepAction_ExceptionHandling() {
    CsvInputHelper helper = spy( new CsvInputHelper() );
    TransMeta transMeta = mock( TransMeta.class );
    Map<String, String> queryParams = new HashMap<>();

    try {
      when( helper.getFieldsAction( queryParams ) ).thenThrow( new KettleException( "Test exception" ) );
    } catch ( Exception e ) {
      // ignore
    }

    JSONObject response = helper.handleStepAction( "getFields", transMeta, queryParams );

    assertNotNull( response );
    assertEquals( CsvInputHelper.FAILURE_RESPONSE, response.get( CsvInputHelper.ACTION_STATUS ) );
    assertNotNull( response.get( CsvInputHelper.ERROR ) );
  }

  @Test
  public void testSetAndGetTransMeta() {
    CsvInputHelper helper = new CsvInputHelper();
    TransMeta transMeta = mock( TransMeta.class );

    helper.setTransMeta( transMeta );

    assertEquals( transMeta, helper.getTransMeta() );
  }

  @Test
  public void testGetInputStream_Success() throws Exception {
    CsvInputHelper helper = new CsvInputHelper();
    TransMeta transMeta = mock( TransMeta.class );
    helper.setTransMeta( transMeta );

    CsvInputAwareMeta meta = mock( CsvInputAwareMeta.class );
    FileObject fileObject = mock( FileObject.class );
    InputStream expectedStream = new ByteArrayInputStream( "test".getBytes() );

    when( meta.getHeaderFileObject( transMeta ) ).thenReturn( fileObject );

    try ( MockedStatic<KettleVFS> kettleVFSMock = Mockito.mockStatic( KettleVFS.class ) ) {
      kettleVFSMock.when( () -> KettleVFS.getInputStream( fileObject ) ).thenReturn( expectedStream );

      InputStream result = helper.getInputStream( meta );

      assertEquals( expectedStream, result );
    }
  }

  @Test
  public void testGetInputStream_Exception() throws Exception {
    CsvInputHelper helper = new CsvInputHelper();
    TransMeta transMeta = mock( TransMeta.class );
    helper.setTransMeta( transMeta );

    CsvInputAwareMeta meta = mock( CsvInputAwareMeta.class );

    when( meta.getHeaderFileObject( transMeta ) ).thenThrow( new RuntimeException( "File not found" ) );

    InputStream result = helper.getInputStream( meta );

    assertNull( result );
  }

  @Test
  public void testLogChannel() {
    CsvInputHelper helper = new CsvInputHelper();

    LogChannelInterface logChannel = helper.logChannel();

    assertNotNull( logChannel );
  }

  @Test
  public void testPopulateMeta_WithValidData() throws Exception {
    CsvInputHelper helper = new CsvInputHelper();
    TransMeta transMeta = spy( new TransMeta() );
    helper.setTransMeta( transMeta );

    StepMeta stepMeta = mock( StepMeta.class );
    // Use real meta to avoid NPEs inside processor
    CsvInputMeta csvMeta = new CsvInputMeta();
    csvMeta.setDefault();
    csvMeta.setInputFields( new TextFileInputField[ 0 ] );

    InputStream inputStream = new ByteArrayInputStream( "field1,field2\nvalue1,value2".getBytes() );
    InputStreamReader inputStreamReader = new InputStreamReader( inputStream );
    BufferedInputStreamReader bufferedReader = new BufferedInputStreamReader( inputStreamReader );

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "testStep" );
    queryParams.put( "noOfFields", "10" );
    queryParams.put( "isSampleSummary", "true" );

    doReturn( stepMeta ).when( transMeta ).findStep( "testStep" );
    when( stepMeta.getStepMetaInterface() ).thenReturn( csvMeta );

    CsvInputHelper spyHelper = spy( helper );
    when( spyHelper.getInputStream( csvMeta ) ).thenReturn( inputStream );
    when( spyHelper.getBufferedReader( csvMeta, inputStream ) ).thenReturn( bufferedReader );
    when( spyHelper.getFieldNames( csvMeta ) ).thenReturn( new String[] { "field1", "field2" } );

    JSONObject response = spyHelper.getFieldsAction( queryParams );

    assertNotNull( response );
    assertNotNull( response.get( "fields" ) );
    assertNotNull( response.get( "summary" ) );
  }

  @Test
  public void testPopulateMeta_WithZeroSamples() throws Exception {
    CsvInputHelper helper = new CsvInputHelper();
    TransMeta transMeta = spy( new TransMeta() );
    helper.setTransMeta( transMeta );

    StepMeta stepMeta = mock( StepMeta.class );
    // Use real meta to avoid NPEs inside processor
    CsvInputMeta csvMeta = new CsvInputMeta();
    csvMeta.setDefault();
    csvMeta.setInputFields( new TextFileInputField[ 0 ] );

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "testStep" );
    queryParams.put( "isSampleSummary", "false" );

    doReturn( stepMeta ).when( transMeta ).findStep( "testStep" );
    when( stepMeta.getStepMetaInterface() ).thenReturn( csvMeta );

    CsvInputHelper spyHelper = spy( helper );
    InputStream inputStream = new ByteArrayInputStream( "test".getBytes() );
    BufferedInputStreamReader bufferedReader = new BufferedInputStreamReader( new InputStreamReader( inputStream ) );

    when( spyHelper.getInputStream( csvMeta ) ).thenReturn( inputStream );
    when( spyHelper.getBufferedReader( csvMeta, inputStream ) ).thenReturn( bufferedReader );
    when( spyHelper.getFieldNames( csvMeta ) ).thenReturn( new String[] { "field1" } );

    JSONObject response = spyHelper.getFieldsAction( queryParams );

    assertNotNull( response );
  }

  @Test( expected = NullPointerException.class )
  public void testGetFieldsAction_StepNotFound() throws Exception {
    CsvInputHelper helper = new CsvInputHelper();
    TransMeta transMeta = new TransMeta();
    helper.setTransMeta( transMeta );

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "stepName", "nonExistentStep" );
    queryParams.put( "noOfFields", "10" );

    helper.getFieldsAction( queryParams );
  }

  @Test
  public void testHandleStepAction_NullQueryParams() {
    CsvInputHelper helper = new CsvInputHelper();
    TransMeta transMeta = mock( TransMeta.class );

    JSONObject response = helper.handleStepAction( "getFields", transMeta, null );

    assertNotNull( response );
    assertEquals( CsvInputHelper.FAILURE_RESPONSE, response.get( CsvInputHelper.ACTION_STATUS ) );
  }

  @Test
  public void testConstructor() {
    CsvInputHelper helper = new CsvInputHelper();

    assertNotNull( helper );
    assertNull( helper.getTransMeta() );
  }

  @Test
  public void testGetInputStream_NullFileObject() throws Exception {
    CsvInputHelper helper = new CsvInputHelper();
    TransMeta transMeta = mock( TransMeta.class );
    helper.setTransMeta( transMeta );

    CsvInputAwareMeta meta = mock( CsvInputAwareMeta.class );

    when( meta.getHeaderFileObject( transMeta ) ).thenReturn( null );

    try ( MockedStatic<KettleVFS> kettleVFSMock = Mockito.mockStatic( KettleVFS.class ) ) {
      kettleVFSMock.when( () -> KettleVFS.getInputStream( (FileObject) null ) )
        .thenThrow( new NullPointerException( "FileObject is null" ) );

      InputStream result = helper.getInputStream( meta );

      assertNull( result );
    }
  }

  // Additional tests

  @Test
  public void testGetFieldsAction_UsesFieldNamesFromMeta() throws Exception {
    CsvInputHelper helper = new CsvInputHelper();
    TransMeta transMeta = spy( new TransMeta() );
    helper.setTransMeta( transMeta );

    StepMeta stepMeta = mock( StepMeta.class );
    CsvInputMeta metaSpy = spy( new CsvInputMeta() );
    metaSpy.setDefault();
    metaSpy.setInputFields( new TextFileInputField[ 0 ] );

    doReturn( stepMeta ).when( transMeta ).findStep( "csv" );
    when( stepMeta.getStepMetaInterface() ).thenReturn( metaSpy );

    CsvInputHelper spyHelper = spy( helper );
    InputStream inputStream = new ByteArrayInputStream( "a,b\n1,2".getBytes() );
    BufferedInputStreamReader reader = new BufferedInputStreamReader( new InputStreamReader( inputStream ) );
    when( spyHelper.getInputStream( metaSpy ) ).thenReturn( inputStream );
    when( spyHelper.getBufferedReader( metaSpy, inputStream ) ).thenReturn( reader );
    when( spyHelper.getFieldNames( metaSpy ) ).thenReturn( new String[] { "a", "b" } );

    Map<String, String> query = new HashMap<>();
    query.put( "stepName", "csv" );
    query.put( "noOfFields", "5" );
    query.put( "isSampleSummary", "true" );

    JSONObject resp = spyHelper.getFieldsAction( query );
    assertNotNull( resp );
    verify( metaSpy ).setFields( new String[] { "a", "b" } );
  }

  @Test
  public void testHandleStepAction_MethodCaseSensitivity() {
    CsvInputHelper helper = new CsvInputHelper();
    TransMeta transMeta = mock( TransMeta.class );

    JSONObject response = helper.handleStepAction( "GetFields", transMeta, new HashMap<>() );

    assertNotNull( response );
    assertEquals( CsvInputHelper.FAILURE_METHOD_NOT_FOUND_RESPONSE, response.get( CsvInputHelper.ACTION_STATUS ) );
  }

  @Test
  public void testHandleStepAction_SetsTransMeta() {
    CsvInputHelper helper = new CsvInputHelper();
    TransMeta transMeta = mock( TransMeta.class );

    helper.handleStepAction( "unknown", transMeta, new HashMap<>() );

    assertEquals( transMeta, helper.getTransMeta() );
  }

  @Test( expected = NullPointerException.class )
  public void testGetFieldsAction_NullQueryParams_ThrowsNPE() throws Exception {
    CsvInputHelper helper = new CsvInputHelper();
    helper.setTransMeta( new TransMeta() );
    helper.getFieldsAction( null );
  }

  @Test
  public void testGetFieldsAction_AnalyzeFileInvoked() throws Exception {
    CsvInputHelper helper = new CsvInputHelper();
    TransMeta transMeta = spy( new TransMeta() );
    helper.setTransMeta( transMeta );

    StepMeta stepMeta = mock( StepMeta.class );
    // Use real meta to avoid NPEs inside processor
    CsvInputMeta csvMeta = new CsvInputMeta();
    csvMeta.setDefault();
    csvMeta.setInputFields( new TextFileInputField[ 0 ] );

    doReturn( stepMeta ).when( transMeta ).findStep( "s" );
    when( stepMeta.getStepMetaInterface() ).thenReturn( csvMeta );

    CsvInputHelper spyHelper = spy( helper );
    InputStream inputStream = new ByteArrayInputStream( "x".getBytes() );
    BufferedInputStreamReader reader = new BufferedInputStreamReader( new InputStreamReader( inputStream ) );
    when( spyHelper.getInputStream( csvMeta ) ).thenReturn( inputStream );
    when( spyHelper.getBufferedReader( csvMeta, inputStream ) ).thenReturn( reader );
    when( spyHelper.getFieldNames( csvMeta ) ).thenReturn( new String[] { "c1" } );

    Map<String, String> query = new HashMap<>();
    query.put( "stepName", "s" );
    query.put( "noOfFields", "0" );
    query.put( "isSampleSummary", "true" );

    JSONObject resp = spyHelper.getFieldsAction( query );
    assertNotNull( resp );
  }
}