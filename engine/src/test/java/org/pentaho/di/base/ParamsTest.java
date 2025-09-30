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

package org.pentaho.di.base;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.UnknownParamException;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class ParamsTest {

  @Mock
  private NamedParams mockNamedParams;

  @Mock
  private NamedParams mockCustomNamedParams;

  @Mock
  private NamedParams mockPluginNamedParams;

  @Test
  public void builderCreatesParamsWithGeneratedUuid() {
    Params params = new Params.Builder().build();

    assertNotNull( params.getUuid() );
    assertFalse( params.getUuid().isEmpty() );
  }

  @Test
  public void builderCreatesParamsWithSpecifiedUuid() {
    String expectedUuid = "test-uuid-123";

    Params params = new Params.Builder( expectedUuid ).build();

    assertEquals( expectedUuid, params.getUuid() );
  }

  @Test
  public void builderSetsAllStringProperties() {
    Params params = new Params.Builder()
      .repoName( "testRepo" )
      .blockRepoConns( "true" )
      .repoUsername( "testUser" )
      .trustRepoUser( "true" )
      .repoPassword( "testPass" )
      .inputDir( "/test/dir" )
      .inputFile( "test.ktr" )
      .listRepoFiles( "true" )
      .listRepoDirs( "true" )
      .exportRepo( "true" )
      .localFile( "local.ktr" )
      .localJarFile( "test.jar" )
      .localInitialDir( "/initial/dir" )
      .listRepos( "true" )
      .safeMode( "true" )
      .metrics( "true" )
      .listFileParams( "true" )
      .logLevel( "DEBUG" )
      .maxLogLines( "1000" )
      .maxLogTimeout( "30" )
      .logFile( "test.log" )
      .oldLogFile( "old.log" )
      .version( "1.0" )
      .resultSetStepName( "testStep" )
      .resultSetCopyNumber( "1" )
      .base64Zip( "dGVzdA==" )
      .runConfiguration( "testConfig" )
      .build();

    String[][] expected = {
      {"getRepoName", "testRepo"},
      {"getBlockRepoConns", "true"},
      {"getRepoUsername", "testUser"},
      {"getTrustRepoUser", "true"},
      {"getRepoPassword", "testPass"},
      {"getInputDir", "/test/dir"},
      {"getInputFile", "test.ktr"},
      {"getListRepoFiles", "true"},
      {"getListRepoDirs", "true"},
      {"getExportRepo", "true"},
      {"getLocalFile", "local.ktr"},
      {"getLocalJarFile", "test.jar"},
      {"getLocalInitialDir", "/initial/dir"},
      {"getListRepos", "true"},
      {"getSafeMode", "true"},
      {"getMetrics", "true"},
      {"getListFileParams", "true"},
      {"getLogLevel", "DEBUG"},
      {"getMaxLogLines", "1000"},
      {"getMaxLogTimeout", "30"},
      {"getLogFile", "test.log"},
      {"getOldLogFile", "old.log"},
      {"getVersion", "1.0"},
      {"getResultSetStepName", "testStep"},
      {"getResultSetCopyNumber", "1"},
      {"getBase64Zip", "dGVzdA=="},
      {"getRunConfiguration", "testConfig"}
    };

    for ( String[] pair : expected ) {
      try {
        assertEquals( pair[1], Params.class.getMethod( pair[0] ).invoke( params ) );
      } catch ( Exception e ) {
        throw new AssertionError( "Failed for method: " + pair[0], e );
      }
    }
  }

  @Test
  public void builderSetsNamedParamsObjects() {
    Params params = new Params.Builder()
      .namedParams( mockNamedParams )
      .customNamedParams( mockCustomNamedParams )
      .pluginNamedParams( mockPluginNamedParams )
      .build();

    assertEquals( mockNamedParams, params.getNamedParams() );
    assertEquals( mockCustomNamedParams, params.getCustomNamedParams() );
    assertEquals( mockPluginNamedParams, params.getPluginNamedParams() );
  }

  @Test
  public void resultSetCopyNumberDefaultsToZeroForNonNumericValue() {
    Params params = new Params.Builder()
      .resultSetCopyNumber( "invalid" )
      .build();

    assertEquals( "0", params.getResultSetCopyNumber() );
  }

  @Test
  public void resultSetCopyNumberPreservesNumericValue() {
    Params params = new Params.Builder()
      .resultSetCopyNumber( "5" )
      .build();

    assertEquals( "5", params.getResultSetCopyNumber() );
  }

  @Test
  public void resultSetCopyNumberDefaultsToZeroForNullValue() {
    Params params = new Params.Builder()
      .resultSetCopyNumber( null )
      .build();

    assertEquals( "0", params.getResultSetCopyNumber() );
  }

  @Test
  public void getParamsReturnsEmptyMapWhenNamedParamsIsNull() {
    Params params = new Params.Builder()
      .namedParams( null )
      .build();

    Map<String, String> result = params.getParams();

    assertTrue( result.isEmpty() );
  }

  @Test
  public void getParamsReturnsParameterValues() throws UnknownParamException {
    when( mockNamedParams.listParameters() ).thenReturn( new String[] {"param1", "param2"} );
    when( mockNamedParams.getParameterValue( "param1" ) ).thenReturn( "value1" );
    when( mockNamedParams.getParameterValue( "param2" ) ).thenReturn( "value2" );

    Params params = new Params.Builder()
      .namedParams( mockNamedParams )
      .build();

    Map<String, String> result = params.getParams();

    assertEquals( 2, result.size() );
    assertEquals( "value1", result.get( "param1" ) );
    assertEquals( "value2", result.get( "param2" ) );
  }

  @Test
  public void getParamsSkipsParametersWithUnknownException() throws UnknownParamException {
    when( mockNamedParams.listParameters() ).thenReturn( new String[] {"param1", "param2"} );
    when( mockNamedParams.getParameterValue( "param1" ) ).thenReturn( "value1" );
    when( mockNamedParams.getParameterValue( "param2" ) ).thenThrow( new UnknownParamException( "Unknown param" ) );

    Params params = new Params.Builder()
      .namedParams( mockNamedParams )
      .build();

    Map<String, String> result = params.getParams();

    assertEquals( 1, result.size() );
    assertEquals( "value1", result.get( "param1" ) );
    assertFalse( result.containsKey( "param2" ) );
  }

  @Test
  public void getCustomParamsReturnsEmptyMapWhenCustomNamedParamsIsNull() {
    Params params = new Params.Builder()
      .customNamedParams( null )
      .build();

    Map<String, String> result = params.getCustomParams();

    assertTrue( result.isEmpty() );
  }

  @Test
  public void getCustomParamsReturnsParameterValues() throws UnknownParamException {
    when( mockCustomNamedParams.listParameters() ).thenReturn( new String[] {"custom1", "custom2"} );
    when( mockCustomNamedParams.getParameterValue( "custom1" ) ).thenReturn( "customValue1" );
    when( mockCustomNamedParams.getParameterValue( "custom2" ) ).thenReturn( "customValue2" );

    Params params = new Params.Builder()
      .customNamedParams( mockCustomNamedParams )
      .build();

    Map<String, String> result = params.getCustomParams();

    assertEquals( 2, result.size() );
    assertEquals( "customValue1", result.get( "custom1" ) );
    assertEquals( "customValue2", result.get( "custom2" ) );
  }

  @Test
  public void getCustomParamsSkipsParametersWithUnknownException() throws UnknownParamException {
    when( mockCustomNamedParams.listParameters() ).thenReturn( new String[] {"custom1", "custom2"} );
    when( mockCustomNamedParams.getParameterValue( "custom1" ) ).thenReturn( "customValue1" );
    when( mockCustomNamedParams.getParameterValue( "custom2" ) ).thenThrow( new UnknownParamException( "Unknown custom param" ) );

    Params params = new Params.Builder()
      .customNamedParams( mockCustomNamedParams )
      .build();

    Map<String, String> result = params.getCustomParams();

    assertEquals( 1, result.size() );
    assertEquals( "customValue1", result.get( "custom1" ) );
    assertFalse( result.containsKey( "custom2" ) );
  }

  @Test
  public void getPluginParamsReturnsEmptyMapWhenPluginNamedParamsIsNull() {
    Params params = new Params.Builder()
      .pluginNamedParams( null )
      .build();

    Map<String, String> result = params.getPluginParams();

    assertTrue( result.isEmpty() );
  }

  @Test
  public void getPluginParamsReturnsParameterValues() throws UnknownParamException {
    when( mockPluginNamedParams.listParameters() ).thenReturn( new String[] {"plugin1", "plugin2"} );
    when( mockPluginNamedParams.getParameterValue( "plugin1" ) ).thenReturn( "pluginValue1" );
    when( mockPluginNamedParams.getParameterValue( "plugin2" ) ).thenReturn( "pluginValue2" );

    Params params = new Params.Builder()
      .pluginNamedParams( mockPluginNamedParams )
      .build();

    Map<String, String> result = params.getPluginParams();

    assertEquals( 2, result.size() );
    assertEquals( "pluginValue1", result.get( "plugin1" ) );
    assertEquals( "pluginValue2", result.get( "plugin2" ) );
  }

  @Test
  public void getPluginParamsSkipsParametersWithUnknownException() throws UnknownParamException {
    when( mockPluginNamedParams.listParameters() ).thenReturn( new String[] {"plugin1", "plugin2"} );
    when( mockPluginNamedParams.getParameterValue( "plugin1" ) ).thenReturn( "pluginValue1" );
    when( mockPluginNamedParams.getParameterValue( "plugin2" ) ).thenThrow( new UnknownParamException( "Unknown plugin param" ) );

    Params params = new Params.Builder()
      .pluginNamedParams( mockPluginNamedParams )
      .build();

    Map<String, String> result = params.getPluginParams();

    assertEquals( 1, result.size() );
    assertEquals( "pluginValue1", result.get( "plugin1" ) );
    assertFalse( result.containsKey( "plugin2" ) );
  }

  @Test
  public void settersUpdatePropertiesCorrectly() {
    Params params = new Params.Builder().build();

    String[][] propertyValues = {
      {"setRepoName", "getRepoName", "newRepo"},
      {"setBlockRepoConns", "getBlockRepoConns", "false"},
      {"setRepoUsername", "getRepoUsername", "newUser"},
      {"setTrustRepoUser", "getTrustRepoUser", "false"},
      {"setRepoPassword", "getRepoPassword", "newPass"},
      {"setInputDir", "getInputDir", "/new/dir"},
      {"setInputFile", "getInputFile", "new.ktr"},
      {"setListRepoFiles", "getListRepoFiles", "false"},
      {"setListRepoDirs", "getListRepoDirs", "false"},
      {"setExportRepo", "getExportRepo", "false"},
      {"setLocalFile", "getLocalFile", "newLocal.ktr"},
      {"setLocalJarFile", "getLocalJarFile", "new.jar"},
      {"setLocalInitialDir", "getLocalInitialDir", "/new/initial"},
      {"setListRepos", "getListRepos", "false"},
      {"setSafeMode", "getSafeMode", "false"},
      {"setMetrics", "getMetrics", "false"},
      {"setListFileParams", "getListFileParams", "false"},
      {"setLogLevel", "getLogLevel", "INFO"},
      {"setMaxLogLines", "getMaxLogLines", "500"},
      {"setMaxLogTimeout", "getMaxLogTimeout", "60"},
      {"setLogFile", "getLogFile", "new.log"},
      {"setOldLogFile", "getOldLogFile", "newOld.log"},
      {"setVersion", "getVersion", "2.0"},
      {"setResultSetStepName", "getResultSetStepName", "newStep"},
      {"setResultSetCopyNumber", "getResultSetCopyNumber", "3"},
      {"setBase64Zip", "getBase64Zip", "bmV3VGVzdA=="},
      {"setRunConfiguration", "getRunConfiguration", "newConfig"}
    };

    for ( String[] prop : propertyValues ) {
      try {
        Params.class.getMethod( prop[0], String.class ).invoke( params, prop[2] );
        assertEquals( prop[2], Params.class.getMethod( prop[1] ).invoke( params ) );
      } catch ( Exception e ) {
        throw new AssertionError( "Failed for property: " + prop[0], e );
      }
    }

    params.setNamedParams( mockNamedParams );
    params.setCustomNamedParams( mockCustomNamedParams );
    params.setPluginNamedParams( mockPluginNamedParams );

    assertEquals( mockNamedParams, params.getNamedParams() );
    assertEquals( mockCustomNamedParams, params.getCustomNamedParams() );
    assertEquals( mockPluginNamedParams, params.getPluginNamedParams() );
  }

  @Test
  public void builderMethodsChainingWorks() {
    Params params = new Params.Builder()
      .repoName( "testRepo" )
      .repoUsername( "testUser" )
      .inputFile( "test.ktr" )
      .logLevel( "DEBUG" )
      .build();

    assertEquals( "testRepo", params.getRepoName() );
    assertEquals( "testUser", params.getRepoUsername() );
    assertEquals( "test.ktr", params.getInputFile() );
    assertEquals( "DEBUG", params.getLogLevel() );
  }

  @Test
  public void builderWithNullValuesDoesNotThrowException() {
    Params params = new Params.Builder()
      .repoName( null )
      .repoUsername( null )
      .namedParams( null )
      .customNamedParams( null )
      .pluginNamedParams( null )
      .build();

    assertNull( params.getRepoName() );
    assertNull( params.getRepoUsername() );
    assertNull( params.getNamedParams() );
    assertNull( params.getCustomNamedParams() );
    assertNull( params.getPluginNamedParams() );
  }
}
