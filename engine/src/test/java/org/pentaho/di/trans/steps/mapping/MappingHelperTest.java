/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2025 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.mapping;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.StepWithMappingMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.pentaho.di.trans.step.BaseStepHelper.IS_VALID_REFERENCE;
import static org.pentaho.di.trans.step.StepHelperInterface.ACTION_STATUS;
import static org.pentaho.di.trans.step.StepHelperInterface.FAILURE_METHOD_NOT_FOUND_RESPONSE;
import static org.pentaho.di.trans.step.StepHelperInterface.FAILURE_RESPONSE;
import static org.pentaho.di.trans.step.StepHelperInterface.SUCCESS_RESPONSE;
import static org.pentaho.di.trans.steps.mapping.MappingHelper.ERROR_MESSAGE;
import static org.pentaho.di.trans.steps.mapping.MappingHelper.GET_MAPPING_STEPS;
import static org.pentaho.di.trans.steps.mapping.MappingHelper.MAPPING_REFERENCE_PATH;
import static org.pentaho.di.trans.steps.mapping.MappingHelper.MAPPING_STEPS;

public class MappingHelperTest {

  MappingMeta mappingMeta;
  MappingHelper mappingHelper;
  TransMeta transMeta;

  @BeforeClass
  public static void setupBeforeClass() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() {
    transMeta = mock( TransMeta.class );
    mappingMeta = mock( MappingMeta.class );
    mappingHelper = new MappingHelper( mappingMeta );

    when( transMeta.environmentSubstitute( anyString() ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
    when( mappingMeta.getDirectoryPath() ).thenReturn( "/path" );
    when( mappingMeta.getTransName() ).thenReturn( "transFile.ktr" );
    when( mappingMeta.getFileName() ).thenReturn( "/path/transFile.ktr" );
  }

  @Test
  public void testReferencePath() {
    try ( MockedStatic<StepWithMappingMeta> mappingMetaMockedStatic = mockStatic( StepWithMappingMeta.class ) ) {
      mappingMetaMockedStatic.when( () -> StepWithMappingMeta.loadMappingMeta( any(), any(), any(), any(), any(), anyBoolean() ) )
          .thenReturn( mock( TransMeta.class ) );
      when( mappingMeta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
      JSONObject response = mappingHelper.stepAction( MAPPING_REFERENCE_PATH, transMeta, null );

      assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
      assertNotNull( response );
      assertNotNull( response.get( MAPPING_REFERENCE_PATH ) );
      assertEquals( "/path/transFile.ktr", response.get( MAPPING_REFERENCE_PATH ) );
      assertEquals( true, response.get( IS_VALID_REFERENCE ) );
    }
  }

  @Test
  public void testReferencePath_withFileNameSpecification() {
    try ( MockedStatic<StepWithMappingMeta> mappingMetaMockedStatic = mockStatic( StepWithMappingMeta.class ) ) {
      mappingMetaMockedStatic.when( () -> StepWithMappingMeta.loadMappingMeta( any(), any(), any(), any(), any(), anyBoolean() ) )
          .thenReturn( mock( TransMeta.class ) );
      when( mappingMeta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.FILENAME );
      JSONObject response = mappingHelper.stepAction( MAPPING_REFERENCE_PATH, transMeta, null );

      assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
      assertNotNull( response );
      assertNotNull( response.get( MAPPING_REFERENCE_PATH ) );
      assertEquals( "/path/transFile.ktr", response.get( MAPPING_REFERENCE_PATH ) );
      assertEquals( true, response.get( IS_VALID_REFERENCE ) );
    }
  }

  @Test
  public void testReferencePath_throwsException() {
    try ( MockedStatic<StepWithMappingMeta> mappingMetaMockedStatic = mockStatic( StepWithMappingMeta.class ) ) {
      mappingMetaMockedStatic.when( () -> StepWithMappingMeta.loadMappingMeta( any(), any(), any(), any(), any(), anyBoolean() )  )
          .thenThrow( new KettleException( "invalid Trans" ) );
      when( mappingMeta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
      JSONObject response = mappingHelper.stepAction( MAPPING_REFERENCE_PATH, transMeta, null );

      assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
      assertNotNull( response );
      assertNotNull( response.get( IS_VALID_REFERENCE ) );
      assertEquals( false, response.get( IS_VALID_REFERENCE ) );
    }
  }

  @Test
  public void testMappingSteps_forInput() throws KettleException {
    Repository repository = mock( Repository.class );
    RepositoryDirectoryInterface repositoryDirectoryInterface = mock( RepositoryDirectoryInterface.class );
    when( repository.findDirectory( anyString() ) ).thenReturn( repositoryDirectoryInterface );
    when( transMeta.getRepository() ).thenReturn( repository );

    TransMeta mappedTransMeta = mock( TransMeta.class );
    StepMeta stepMeta = new StepMeta();
    stepMeta.setName( "MappingInput" );
    stepMeta.setStepID( "MappingInput" );
    when( mappedTransMeta.getSteps() ).thenReturn( Collections.singletonList( stepMeta ) );
    when( repository.loadTransformation( anyString(), any(), any(), anyBoolean(), any() ) ).thenReturn( mappedTransMeta );

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "isMappingInput", "true" );

    JSONObject response = mappingHelper.stepAction( GET_MAPPING_STEPS, transMeta, queryParams );

    assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response );
    List<String> mappingSteps = (List<String>) response.get( MAPPING_STEPS );
    assertEquals( 1, mappingSteps.size() );
  }

  @Test
  public void testMappingSteps_forOutput() throws KettleException {
    Repository repository = mock( Repository.class );
    RepositoryDirectoryInterface repositoryDirectoryInterface = mock( RepositoryDirectoryInterface.class );
    when( repository.findDirectory( anyString() ) ).thenReturn( repositoryDirectoryInterface );
    when( transMeta.getRepository() ).thenReturn( repository );

    TransMeta mappedTransMeta = mock( TransMeta.class );
    StepMeta stepMeta = new StepMeta();
    stepMeta.setName( "MappingOutput" );
    stepMeta.setStepID( "MappingOutput" );
    when( mappedTransMeta.getSteps() ).thenReturn( Collections.singletonList( stepMeta ) );
    when( repository.loadTransformation( anyString(), any(), any(), anyBoolean(), any() ) ).thenReturn( mappedTransMeta );

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "isMappingInput", "false" );

    JSONObject response = mappingHelper.stepAction( GET_MAPPING_STEPS, transMeta, queryParams );

    assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response );
    List<String> mappingSteps = (List<String>) response.get( MAPPING_STEPS );
    assertEquals( 1, mappingSteps.size() );
  }

  @Test
  public void testMappingSteps_forEmptyFileName() {
    when( mappingMeta.getFileName() ).thenReturn( "" );

    JSONObject response = mappingHelper.stepAction( GET_MAPPING_STEPS, transMeta, null );

    assertEquals( FAILURE_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response );
    assertNotNull( response.get( ERROR_MESSAGE ) );
  }

  @Test
  public void testMappingSteps_forEmptyRepositoryDirectory() throws KettleException {
    Repository repository = mock( Repository.class );
    when( repository.findDirectory( anyString() ) ).thenReturn( null );
    when( transMeta.getRepository() ).thenReturn( repository );

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "isMappingInput", "true" );

    JSONObject response = mappingHelper.stepAction( GET_MAPPING_STEPS, transMeta, queryParams );

    assertEquals( FAILURE_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response );
    assertNotNull( response.get( ERROR_MESSAGE ) );
  }

  @Test
  public void testMappingSteps_whenLoadTransformationThrowsException() throws KettleException {
    Repository repository = mock( Repository.class );
    RepositoryDirectoryInterface repositoryDirectoryInterface = mock( RepositoryDirectoryInterface.class );
    when( repository.findDirectory( anyString() ) ).thenReturn( repositoryDirectoryInterface );
    when( transMeta.getRepository() ).thenReturn( repository );

    when( repository.loadTransformation( anyString(), any(), any(), anyBoolean(), any() ) ).thenThrow( new KettleException( "exception occurred" ) );

    JSONObject response = mappingHelper.stepAction( GET_MAPPING_STEPS, transMeta, null );

    assertEquals( FAILURE_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response );
    assertNotNull( response.get( ERROR_MESSAGE ) );
  }

  @Test
  public void testHandleStepAction_whenMethodNameIsInvalid() {
    JSONObject response = mappingHelper.stepAction( "invalidMethod", transMeta, null );

    assertNotNull( response );
    assertEquals( FAILURE_METHOD_NOT_FOUND_RESPONSE, response.get( ACTION_STATUS ) );
  }
}
