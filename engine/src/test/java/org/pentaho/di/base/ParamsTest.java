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
  public void builderSetsRepositoryAndExecutionProperties() {
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

    assertRepositoryAndExecutionProperties( params );
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
  public void builderSetsInteractiveAuthProperties() {
    Params params = new Params.Builder()
      .browserAuth( "Y" )
      .deviceCode( "Y" )
      .preferredIdp( "keycloak" )
      .serviceAccount( "Y" )
      .build();

    assertEquals( "Y", params.getBrowserAuth() );
    assertEquals( "Y", params.getDeviceCode() );
    assertEquals( "keycloak", params.getPreferredIdp() );
    assertEquals( "Y", params.getServiceAccount() );
  }

  @Test
  public void settersUpdateInteractiveAuthProperties() {
    Params params = new Params.Builder().build();

    params.setBrowserAuth( "Y" );
    params.setDeviceCode( "Y" );
    params.setPreferredIdp( "azure" );
    params.setServiceAccount( "N" );

    assertEquals( "Y", params.getBrowserAuth() );
    assertEquals( "Y", params.getDeviceCode() );
    assertEquals( "azure", params.getPreferredIdp() );
    assertEquals( "N", params.getServiceAccount() );
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

    params.setUuid( "new-uuid" );
    params.setRepoName( "newRepo" );
    params.setBlockRepoConns( "false" );
    params.setRepoUsername( "newUser" );
    params.setTrustRepoUser( "false" );
    params.setRepoPassword( "newPass" );
    params.setInputDir( "/new/dir" );
    params.setInputFile( "new.ktr" );
    params.setListRepoFiles( "false" );
    params.setListRepoDirs( "false" );
    params.setExportRepo( "false" );
    params.setLocalFile( "newLocal.ktr" );
    params.setLocalJarFile( "new.jar" );
    params.setLocalInitialDir( "/new/initial" );
    params.setListRepos( "false" );
    params.setSafeMode( "false" );
    params.setMetrics( "false" );
    params.setListFileParams( "false" );
    params.setLogLevel( "INFO" );
    params.setMaxLogLines( "500" );
    params.setMaxLogTimeout( "60" );
    params.setLogFile( "new.log" );
    params.setOldLogFile( "newOld.log" );
    params.setVersion( "2.0" );
    params.setResultSetStepName( "newStep" );
    params.setResultSetCopyNumber( "3" );
    params.setBase64Zip( "bmV3VGVzdA==" );
    params.setRunConfiguration( "newConfig" );

    assertUpdatedMutableProperties( params );

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

  private void assertRepositoryAndExecutionProperties( Params params ) {
    assertEquals( "testRepo", params.getRepoName() );
    assertEquals( "true", params.getBlockRepoConns() );
    assertEquals( "testUser", params.getRepoUsername() );
    assertEquals( "true", params.getTrustRepoUser() );
    assertEquals( "testPass", params.getRepoPassword() );
    assertEquals( "/test/dir", params.getInputDir() );
    assertEquals( "test.ktr", params.getInputFile() );
    assertEquals( "true", params.getListRepoFiles() );
    assertEquals( "true", params.getListRepoDirs() );
    assertEquals( "true", params.getExportRepo() );
    assertEquals( "local.ktr", params.getLocalFile() );
    assertEquals( "test.jar", params.getLocalJarFile() );
    assertEquals( "/initial/dir", params.getLocalInitialDir() );
    assertEquals( "true", params.getListRepos() );
    assertEquals( "true", params.getSafeMode() );
    assertEquals( "true", params.getMetrics() );
    assertEquals( "true", params.getListFileParams() );
    assertEquals( "DEBUG", params.getLogLevel() );
    assertEquals( "1000", params.getMaxLogLines() );
    assertEquals( "30", params.getMaxLogTimeout() );
    assertEquals( "test.log", params.getLogFile() );
    assertEquals( "old.log", params.getOldLogFile() );
    assertEquals( "1.0", params.getVersion() );
    assertEquals( "testStep", params.getResultSetStepName() );
    assertEquals( "1", params.getResultSetCopyNumber() );
    assertEquals( "dGVzdA==", params.getBase64Zip() );
    assertEquals( "testConfig", params.getRunConfiguration() );
  }

  private void assertUpdatedMutableProperties( Params params ) {
    assertEquals( "new-uuid", params.getUuid() );
    assertEquals( "newRepo", params.getRepoName() );
    assertEquals( "false", params.getBlockRepoConns() );
    assertEquals( "newUser", params.getRepoUsername() );
    assertEquals( "false", params.getTrustRepoUser() );
    assertEquals( "newPass", params.getRepoPassword() );
    assertEquals( "/new/dir", params.getInputDir() );
    assertEquals( "new.ktr", params.getInputFile() );
    assertEquals( "false", params.getListRepoFiles() );
    assertEquals( "false", params.getListRepoDirs() );
    assertEquals( "false", params.getExportRepo() );
    assertEquals( "newLocal.ktr", params.getLocalFile() );
    assertEquals( "new.jar", params.getLocalJarFile() );
    assertEquals( "/new/initial", params.getLocalInitialDir() );
    assertEquals( "false", params.getListRepos() );
    assertEquals( "false", params.getSafeMode() );
    assertEquals( "false", params.getMetrics() );
    assertEquals( "false", params.getListFileParams() );
    assertEquals( "INFO", params.getLogLevel() );
    assertEquals( "500", params.getMaxLogLines() );
    assertEquals( "60", params.getMaxLogTimeout() );
    assertEquals( "new.log", params.getLogFile() );
    assertEquals( "newOld.log", params.getOldLogFile() );
    assertEquals( "2.0", params.getVersion() );
    assertEquals( "newStep", params.getResultSetStepName() );
    assertEquals( "3", params.getResultSetCopyNumber() );
    assertEquals( "bmV3VGVzdA==", params.getBase64Zip() );
    assertEquals( "newConfig", params.getRunConfiguration() );
  }
}
