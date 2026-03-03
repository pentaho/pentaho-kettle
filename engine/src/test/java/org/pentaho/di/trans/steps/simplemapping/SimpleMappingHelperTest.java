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

package org.pentaho.di.trans.steps.simplemapping;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.trans.StepWithMappingMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.pentaho.di.trans.step.BaseStepHelper.IS_VALID_REFERENCE;
import static org.pentaho.di.trans.step.StepHelperInterface.ACTION_STATUS;
import static org.pentaho.di.trans.step.StepHelperInterface.FAILURE_METHOD_NOT_FOUND_RESPONSE;
import static org.pentaho.di.trans.step.StepHelperInterface.FAILURE_RESPONSE;
import static org.pentaho.di.trans.step.StepHelperInterface.SUCCESS_RESPONSE;
import static org.pentaho.di.trans.steps.simplemapping.SimpleMappingHelper.ERROR_MESSAGE;
import static org.pentaho.di.trans.steps.simplemapping.SimpleMappingHelper.GET_MAPPING_STEPS;
import static org.pentaho.di.trans.steps.simplemapping.SimpleMappingHelper.GET_SIMPLE_MAPPING_FIELDS;
import static org.pentaho.di.trans.steps.simplemapping.SimpleMappingHelper.MAPPING_STEPS;
import static org.pentaho.di.trans.steps.simplemapping.SimpleMappingHelper.SIMPLE_MAPPING_REFERENCE_PATH;
import static org.pentaho.di.trans.steps.simplemapping.SimpleMappingHelper.SOURCE_FIELDS;
import static org.pentaho.di.trans.steps.simplemapping.SimpleMappingHelper.TARGET_FIELDS;

public class SimpleMappingHelperTest {

  SimpleMappingMeta simpleMappingMeta;
  SimpleMappingHelper simpleMappingHelper;
  TransMeta transMeta;

  @BeforeClass
  public static void setupBeforeClass() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() {
    transMeta = mock( TransMeta.class );
    simpleMappingMeta = mock( SimpleMappingMeta.class );
    simpleMappingHelper = spy( new SimpleMappingHelper( simpleMappingMeta ) );

    when( transMeta.environmentSubstitute( anyString() ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
    when( simpleMappingMeta.getDirectoryPath() ).thenReturn( "/path" );
    when( simpleMappingMeta.getTransName() ).thenReturn( "transFile.ktr" );
    when( simpleMappingMeta.getFileName() ).thenReturn( "/path/transFile.ktr" );
  }

  @Test
  public void testReferencePath() {
    try ( MockedStatic<StepWithMappingMeta> mappingMetaMockedStatic = mockStatic( StepWithMappingMeta.class ) ) {
      mappingMetaMockedStatic.when( () -> StepWithMappingMeta.loadMappingMeta( any(), any(), any(), any(), any(), anyBoolean() ) )
          .thenReturn( mock( TransMeta.class ) );
      when( simpleMappingMeta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
      JSONObject response = simpleMappingHelper.stepAction( SIMPLE_MAPPING_REFERENCE_PATH, transMeta, null );

      assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
      assertNotNull( response );
      assertNotNull( response.get( SIMPLE_MAPPING_REFERENCE_PATH ) );
      assertEquals( "/path/transFile.ktr", response.get( SIMPLE_MAPPING_REFERENCE_PATH ) );
      assertEquals( true, response.get( IS_VALID_REFERENCE ) );
    }
  }

  @Test
  public void testReferencePath_withFileNameSpecification() {
    try ( MockedStatic<StepWithMappingMeta> mappingMetaMockedStatic = mockStatic( StepWithMappingMeta.class ) ) {
      mappingMetaMockedStatic.when( () -> StepWithMappingMeta.loadMappingMeta( any(), any(), any(), any(), any(), anyBoolean() ) )
          .thenReturn( mock( TransMeta.class ) );
      when( simpleMappingMeta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.FILENAME );
      JSONObject response = simpleMappingHelper.stepAction( SIMPLE_MAPPING_REFERENCE_PATH, transMeta, null );

      assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
      assertNotNull( response );
      assertNotNull( response.get( SIMPLE_MAPPING_REFERENCE_PATH ) );
      assertEquals( "/path/transFile.ktr", response.get( SIMPLE_MAPPING_REFERENCE_PATH ) );
      assertEquals( true, response.get( IS_VALID_REFERENCE ) );
    }
  }

  @Test
  public void testReferencePath_throwsException() {
    try ( MockedStatic<StepWithMappingMeta> mappingMetaMockedStatic = mockStatic( StepWithMappingMeta.class ) ) {
      mappingMetaMockedStatic.when( () -> StepWithMappingMeta.loadMappingMeta( any(), any(), any(), any(), any(), anyBoolean() )  )
          .thenThrow( new KettleException( "invalid Trans" ) );
      when( simpleMappingMeta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
      JSONObject response = simpleMappingHelper.stepAction( SIMPLE_MAPPING_REFERENCE_PATH, transMeta, null );

      assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
      assertNotNull( response );
      assertNotNull( response.get( IS_VALID_REFERENCE ) );
      assertEquals( false, response.get( IS_VALID_REFERENCE ) );
    }
  }

  @Test
  public void testMappingSteps() throws KettleException {
    Repository repository = mock( Repository.class );
    RepositoryDirectoryInterface repositoryDirectoryInterface = mock( RepositoryDirectoryInterface.class );
    when( repository.findDirectory( anyString() ) ).thenReturn( repositoryDirectoryInterface );
    when( transMeta.getRepository() ).thenReturn( repository );

    TransMeta mappedTransMeta = mock( TransMeta.class );
    RowMetaInterface rowMetaInterface = mock( RowMetaInterface.class );
    StepMeta stepMeta = mock( StepMeta.class );
    when( mappedTransMeta.findMappingOutputStep( null ) ).thenReturn( stepMeta );
    when( rowMetaInterface.getFieldNames() ).thenReturn( new String[] {"step"} );
    when( mappedTransMeta.getStepFields( stepMeta ) ).thenReturn( rowMetaInterface );
    doReturn( mappedTransMeta ).when( simpleMappingHelper ).loadSimpleMappingMeta( transMeta, simpleMappingMeta );

    JSONObject response = simpleMappingHelper.stepAction( GET_MAPPING_STEPS, transMeta, null );

    assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response );
    List<String> mappingSteps = (List<String>) response.get( MAPPING_STEPS );
    assertEquals( 1, mappingSteps.size() );
  }

  @Test
  public void testMappingSteps_forEmptyFileName() {
    when( simpleMappingMeta.getFileName() ).thenReturn( "" );

    JSONObject response = simpleMappingHelper.stepAction( GET_MAPPING_STEPS, transMeta, null );

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

    JSONObject response = simpleMappingHelper.stepAction( GET_MAPPING_STEPS, transMeta, queryParams );

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

    JSONObject response = simpleMappingHelper.stepAction( GET_MAPPING_STEPS, transMeta, null );

    assertEquals( FAILURE_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response );
    assertNotNull( response.get( ERROR_MESSAGE ) );
  }

  @Test
  public void testHandleStepAction_whenMethodNameIsInvalid() {
    JSONObject response = simpleMappingHelper.stepAction( "invalidMethod", transMeta, null );

    assertNotNull( response );
    assertEquals( FAILURE_METHOD_NOT_FOUND_RESPONSE, response.get( ACTION_STATUS ) );
  }

  @Test
  public void testMappingFields() throws KettleException {
    Repository repository = mock( Repository.class );
    RepositoryDirectoryInterface repositoryDirectoryInterface = mock( RepositoryDirectoryInterface.class );
    when( repository.findDirectory( anyString() ) ).thenReturn( repositoryDirectoryInterface );
    when( transMeta.getRepository() ).thenReturn( repository );
    StepMeta stepMeta = new StepMeta();
    stepMeta.setName( "Mapping Input" );
    stepMeta.setStepID( "MappingInput" );

    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "Name" ) );
    when( transMeta.getPrevStepFields( any( StepMeta.class ) ) ).thenReturn( rowMeta );
    when( transMeta.findStep( anyString() ) ).thenReturn( stepMeta );

    TransMeta mappedTransMeta = mock( TransMeta.class );

    RowMeta targetRowMeta = new RowMeta();
    targetRowMeta.addValueMeta( new ValueMetaString( "Name2" ) );
    when( mappedTransMeta.findMappingInputStep( null ) ).thenReturn( stepMeta );
    when( mappedTransMeta.getStepFields( stepMeta ) ).thenReturn( targetRowMeta );
    when( simpleMappingMeta.getParentStepMeta() ).thenReturn( stepMeta );
    doReturn( mappedTransMeta ).when( simpleMappingHelper ).loadSimpleMappingMeta( transMeta, simpleMappingMeta );

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "isMappingInput", "true" );

    JSONObject response = simpleMappingHelper.stepAction( GET_SIMPLE_MAPPING_FIELDS, transMeta, queryParams );

    assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response );
    List<String> sourceFields = (List<String>) response.get( SOURCE_FIELDS );
    assertEquals( 1, sourceFields.size() );
    List<String> targetFields = (List<String>) response.get( TARGET_FIELDS );
    assertEquals( 1, targetFields.size() );
  }

  @Test
  public void testMappingFields_forOutput() throws KettleException {
    Repository repository = mock( Repository.class );
    RepositoryDirectoryInterface repositoryDirectoryInterface = mock( RepositoryDirectoryInterface.class );
    when( repository.findDirectory( anyString() ) ).thenReturn( repositoryDirectoryInterface );
    when( transMeta.getRepository() ).thenReturn( repository );
    StepMeta stepMeta = new StepMeta();
    stepMeta.setName( "Mapping Input" );
    stepMeta.setStepID( "MappingInput" );

    RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "Name" ) );
    when( transMeta.getPrevStepFields( any( StepMeta.class ) ) ).thenReturn( rowMeta );
    when( transMeta.findStep( anyString() ) ).thenReturn( stepMeta );

    TransMeta mappedTransMeta = mock( TransMeta.class );

    RowMeta targetRowMeta = new RowMeta();
    targetRowMeta.addValueMeta( new ValueMetaString( "Name2" ) );
    when( mappedTransMeta.findMappingOutputStep( null ) ).thenReturn( stepMeta );
    when( mappedTransMeta.getStepFields( stepMeta ) ).thenReturn( targetRowMeta );
    when( simpleMappingMeta.getParentStepMeta() ).thenReturn( stepMeta );
    doReturn( mappedTransMeta ).when( simpleMappingHelper ).loadSimpleMappingMeta( transMeta, simpleMappingMeta );

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put( "isMappingInput", "false" );

    JSONObject response = simpleMappingHelper.stepAction( GET_SIMPLE_MAPPING_FIELDS, transMeta, queryParams );

    assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response );
    List<String> sourceFields = (List<String>) response.get( SOURCE_FIELDS );
    assertEquals( 1, sourceFields.size() );
    List<String> targetFields = (List<String>) response.get( TARGET_FIELDS );
    assertEquals( 1, targetFields.size() );
  }

  @Test
  public void testMappingFields_forEmptyFileName() {
    when( simpleMappingMeta.getFileName() ).thenReturn( "" );

    JSONObject response = simpleMappingHelper.stepAction( GET_SIMPLE_MAPPING_FIELDS, transMeta, null );

    assertEquals( FAILURE_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response );
    assertNotNull( response.get( SimpleMappingHelper.ERROR_MESSAGE ) );
  }

  @Test
  public void testMappingFields_whenLoadTransformationThrowsException() throws KettleException {
    Repository repository = mock( Repository.class );
    RepositoryDirectoryInterface repositoryDirectoryInterface = mock( RepositoryDirectoryInterface.class );
    when( repository.findDirectory( anyString() ) ).thenReturn( repositoryDirectoryInterface );
    when( transMeta.getRepository() ).thenReturn( repository );

    when( repository.loadTransformation( anyString(), any(), any(), anyBoolean(), any() ) ).thenThrow( new KettleException( "exception occurred" ) );

    JSONObject response = simpleMappingHelper.stepAction( GET_SIMPLE_MAPPING_FIELDS, transMeta, null );

    assertEquals( FAILURE_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response );
    assertNotNull( response.get( SimpleMappingHelper.ERROR_MESSAGE ) );
  }
}
