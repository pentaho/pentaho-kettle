/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.trans.steps.rest;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.MemoryRepository;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;
import org.pentaho.metastore.api.IMetaStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RestMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void beforeClass() throws KettleException {
    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init();
    String passwordEncoderPluginID =
      Const.NVL( EnvUtil.getSystemProperty( Const.KETTLE_PASSWORD_ENCODER_PLUGIN ), "Kettle" );
    Encr.init( passwordEncoderPluginID );
  }

  @Test
  public void testLoadSaveRoundTrip() throws KettleException {
    List<String> attributes =
      Arrays.asList( "applicationType", "method", "url", "urlInField", "dynamicMethod", "methodFieldName",
        "urlField", "bodyField", "httpLogin", "httpPassword", "proxyHost", "proxyPort", "preemptive",
        "trustStoreFile", "trustStorePassword", "ignoreSsl", "headerField", "headerName", "parameterField",
        "parameterName", "matrixParameterField", "matrixParameterName", "fieldName", "resultCodeFieldName",
        "responseTimeFieldName", "responseHeaderFieldName" );

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap = new HashMap<>();

    // Arrays need to be consistent length
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
      new ArrayLoadSaveValidator<>( new StringLoadSaveValidator(), 25 );
    fieldLoadSaveValidatorAttributeMap.put( "headerField", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "headerName", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "parameterField", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "parameterName", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "matrixParameterField", stringArrayLoadSaveValidator );
    fieldLoadSaveValidatorAttributeMap.put( "matrixParameterName", stringArrayLoadSaveValidator );

    LoadSaveTester<RestMeta> loadSaveTester =
      new LoadSaveTester<>( RestMeta.class, attributes, new HashMap<>(),
        new HashMap<>(), fieldLoadSaveValidatorAttributeMap,
        new HashMap<>() );

    loadSaveTester.testSerialization();
  }

  @Test
  public void testStepChecks() {
    RestMeta meta = new RestMeta();
    List<CheckResultInterface> remarks = new ArrayList<>();
    TransMeta transMeta = new TransMeta();
    StepMeta step = new StepMeta();
    RowMetaInterface prev = new RowMeta();
    RowMetaInterface info = new RowMeta();
    String[] input = new String[0];
    String[] output = new String[0];
    VariableSpace variables = new Variables();
    Repository repo = null;
    IMetaStore metaStore = null;

    // In a default configuration, it's expected that some errors will occur.
    // For this, we'll grab a baseline count of the number of errors
    // as the error count should decrease as we change configuration settings to proper values.
    remarks.clear();
    meta.check( remarks, transMeta, step, prev, input, output, info, variables, repo, metaStore );
    final int errorsDefault = getCheckResultErrorCount( remarks );
    assertTrue( errorsDefault > 0 );

    // Setting the step to read the URL from a field should fix one of the check() errors
    meta.setUrlInField( true );
    meta.setUrlField( "urlField" );
    prev.addValueMeta( new ValueMetaString( "urlField" ) );
    remarks.clear();
    meta.check( remarks, transMeta, step, prev, input, output, info, variables, repo, metaStore );
    int errorsCurrent = getCheckResultErrorCount( remarks );
    assertTrue( errorsDefault > errorsCurrent );
  }

  private static int getCheckResultErrorCount( List<CheckResultInterface> remarks ) {
    return remarks.stream()
      .filter( p -> p.getType() == CheckResultInterface.TYPE_RESULT_ERROR )
      .collect( Collectors.toList() ).size();
  }

  @Test
  public void testSaveRepReadRepRoundTripBooleanFields() throws Exception {
    RestMeta meta = createRoundTripMeta();
    MemoryRepository repo = Mockito.spy( new MemoryRepository() );
    ObjectId transId = new StringObjectId( "trans" );
    ObjectId stepId = new StringObjectId( "step" );
    meta.saveRep( repo, null, transId, stepId );

    // The repository persists booleans as "Y"/"N". If readRep falls back to the string accessor for ignoreSsl,
    // a spy that returns "true" here will force the old logic to read back false and fail the round trip.
    Mockito.doReturn( "true" ).when( repo ).getStepAttributeString( stepId, "ignoreSsl" );

    assertTrue( repo.getStepAttributeBoolean( stepId, "urlInField" ) );
    assertTrue( repo.getStepAttributeBoolean( stepId, "dynamicMethod" ) );
    assertTrue( repo.getStepAttributeBoolean( stepId, "preemptive" ) );
    assertTrue( repo.getStepAttributeBoolean( stepId, "ignoreSsl" ) );

    RestMeta loaded = new RestMeta();
    loaded.readRep( repo, null, stepId, new ArrayList<DatabaseMeta>() );

    assertEquals( meta.isUrlInField(), loaded.isUrlInField() );
    assertEquals( meta.isDynamicMethod(), loaded.isDynamicMethod() );
    assertEquals( meta.isPreemptive(), loaded.isPreemptive() );
    assertEquals( meta.isIgnoreSsl(), loaded.isIgnoreSsl() );
  }

  @Test
  public void testSaveRepReadRepRoundTripStringFields() throws Exception {
    RestMeta meta = createRoundTripMeta();
    MemoryRepository repo = new MemoryRepository();
    ObjectId transId = new StringObjectId( "trans" );
    ObjectId stepId = new StringObjectId( "step" );
    meta.saveRep( repo, null, transId, stepId );

    RestMeta loaded = new RestMeta();
    loaded.readRep( repo, null, stepId, new ArrayList<DatabaseMeta>() );

    assertEquals( meta.getApplicationType(), loaded.getApplicationType() );
    assertEquals( meta.getMethod(), loaded.getMethod() );
    assertEquals( meta.getUrl(), loaded.getUrl() );
    assertEquals( meta.getMethodFieldName(), loaded.getMethodFieldName() );
    assertEquals( meta.getUrlField(), loaded.getUrlField() );
    assertEquals( meta.getBodyField(), loaded.getBodyField() );
    assertEquals( meta.getHttpLogin(), loaded.getHttpLogin() );
    assertEquals( meta.getHttpPassword(), loaded.getHttpPassword() );
    assertEquals( meta.getProxyHost(), loaded.getProxyHost() );
    assertEquals( meta.getProxyPort(), loaded.getProxyPort() );
    assertEquals( meta.getTrustStoreFile(), loaded.getTrustStoreFile() );
    assertEquals( meta.getTrustStorePassword(), loaded.getTrustStorePassword() );
  }

  @Test
  public void testSaveRepReadRepRoundTripArrayFields() throws Exception {
    RestMeta meta = createRoundTripMeta();
    MemoryRepository repo = new MemoryRepository();
    ObjectId transId = new StringObjectId( "trans" );
    ObjectId stepId = new StringObjectId( "step" );
    meta.saveRep( repo, null, transId, stepId );

    RestMeta loaded = new RestMeta();
    loaded.readRep( repo, null, stepId, new ArrayList<DatabaseMeta>() );

    assertArrayEquals( meta.getHeaderField(), loaded.getHeaderField() );
    assertArrayEquals( meta.getHeaderName(), loaded.getHeaderName() );
    assertArrayEquals( meta.getParameterField(), loaded.getParameterField() );
    assertArrayEquals( meta.getParameterName(), loaded.getParameterName() );
    assertArrayEquals( meta.getMatrixParameterField(), loaded.getMatrixParameterField() );
    assertArrayEquals( meta.getMatrixParameterName(), loaded.getMatrixParameterName() );
  }

  @Test
  public void testSaveRepReadRepRoundTripResultFields() throws Exception {
    RestMeta meta = createRoundTripMeta();
    MemoryRepository repo = new MemoryRepository();
    ObjectId transId = new StringObjectId( "trans" );
    ObjectId stepId = new StringObjectId( "step" );
    meta.saveRep( repo, null, transId, stepId );

    RestMeta loaded = new RestMeta();
    loaded.readRep( repo, null, stepId, new ArrayList<DatabaseMeta>() );

    assertEquals( meta.getFieldName(), loaded.getFieldName() );
    assertEquals( meta.getResultCodeFieldName(), loaded.getResultCodeFieldName() );
    assertEquals( meta.getResponseTimeFieldName(), loaded.getResponseTimeFieldName() );
    assertEquals( meta.getResponseHeaderFieldName(), loaded.getResponseHeaderFieldName() );
  }

  private RestMeta createRoundTripMeta() {
    RestMeta meta = new RestMeta();
    meta.allocate( 2, 2, 2 );
    meta.setApplicationType( RestMeta.APPLICATION_TYPE_JSON );
    meta.setMethod( RestMeta.HTTP_METHOD_POST );
    meta.setUrl( "https://example.com/api" );
    meta.setUrlInField( true );
    meta.setDynamicMethod( true );
    meta.setMethodFieldName( "httpMethod" );
    meta.setUrlField( "urlField" );
    meta.setBodyField( "body" );
    meta.setHttpLogin( "user" );
    meta.setHttpPassword( "secret" );
    meta.setProxyHost( "proxy.example.com" );
    meta.setProxyPort( "8080" );
    meta.setPreemptive( true );
    meta.setTrustStoreFile( "/tmp/truststore.jks" );
    meta.setTrustStorePassword( "trust-secret" );
    meta.setIgnoreSsl( true );
    meta.setHeaderField( new String[] { "headerField0", "headerField1" } );
    meta.setHeaderName( new String[] { "headerName0", "headerName1" } );
    meta.setParameterField( new String[] { "parameterField0", "parameterField1" } );
    meta.setParameterName( new String[] { "parameterName0", "parameterName1" } );
    meta.setMatrixParameterField( new String[] { "matrixField0", "matrixField1" } );
    meta.setMatrixParameterName( new String[] { "matrixName0", "matrixName1" } );
    meta.setFieldName( "result" );
    meta.setResultCodeFieldName( "statusCode" );
    meta.setResponseTimeFieldName( "responseTime" );
    meta.setResponseHeaderFieldName( "responseHeader" );
    return meta;
  }

  @Test
  public void testEntityEnclosingMethods() {
    assertTrue( RestMeta.isActiveBody( RestMeta.HTTP_METHOD_POST ) );
    assertTrue( RestMeta.isActiveBody( RestMeta.HTTP_METHOD_PUT ) );
    assertTrue( RestMeta.isActiveBody( RestMeta.HTTP_METHOD_PATCH ) );

    assertFalse( RestMeta.isActiveBody( RestMeta.HTTP_METHOD_GET ) );
    assertFalse( RestMeta.isActiveBody( RestMeta.HTTP_METHOD_DELETE ) );
    assertFalse( RestMeta.isActiveBody( RestMeta.HTTP_METHOD_HEAD ) );
    assertFalse( RestMeta.isActiveBody( RestMeta.HTTP_METHOD_OPTIONS ) );
  }
}
