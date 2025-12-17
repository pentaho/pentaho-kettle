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

package org.pentaho.di.trans.steps.metainject;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.TransMeta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.pentaho.di.trans.step.BaseStepHelper.IS_VALID_REFERENCE;
import static org.pentaho.di.trans.step.StepHelperInterface.ACTION_STATUS;
import static org.pentaho.di.trans.step.StepHelperInterface.FAILURE_METHOD_NOT_FOUND_RESPONSE;
import static org.pentaho.di.trans.step.StepHelperInterface.SUCCESS_RESPONSE;
import static org.pentaho.di.trans.steps.metainject.MetaInjectHelper.META_INJECT_REFERENCE_PATH;

public class MetaInjectHelperTest {

  MetaInjectMeta metaInjectMeta;
  MetaInjectHelper metaInjectHelper;
  TransMeta transMeta;

  @Before
  public void setUp() {
    transMeta = mock( TransMeta.class );
    metaInjectMeta = mock( MetaInjectMeta.class );
    metaInjectHelper = new MetaInjectHelper( metaInjectMeta );

    when( transMeta.environmentSubstitute( anyString() ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
    when( metaInjectMeta.getDirectoryPath() ).thenReturn( "/path" );
    when( metaInjectMeta.getTransName() ).thenReturn( "transFile.ktr" );
    when( metaInjectMeta.getFileName() ).thenReturn( "/path/transFile.ktr" );
  }

  @Test
  public void testReferencePath() {
    try ( MockedStatic<MetaInjectMeta> mappingMetaMockedStatic = mockStatic( MetaInjectMeta.class ) ) {
      mappingMetaMockedStatic.when( () -> MetaInjectMeta.loadTransformationMeta( any(), any(), any(), any(), any() ) )
          .thenReturn( mock( TransMeta.class ) );
      when( metaInjectMeta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
      JSONObject response = metaInjectHelper.stepAction( META_INJECT_REFERENCE_PATH, transMeta, null );

      assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
      assertNotNull( response );
      assertNotNull( response.get( META_INJECT_REFERENCE_PATH ) );
      assertEquals( "/path/transFile.ktr", response.get( META_INJECT_REFERENCE_PATH ) );
      assertEquals( true, response.get( IS_VALID_REFERENCE ) );
    }
  }

  @Test
  public void testReferencePath_withFileNameSpecification() {
    try ( MockedStatic<MetaInjectMeta> mappingMetaMockedStatic = mockStatic( MetaInjectMeta.class ) ) {
      mappingMetaMockedStatic.when( () -> MetaInjectMeta.loadTransformationMeta( any(), any(), any(), any(), any() ) )
          .thenReturn( mock( TransMeta.class ) );
      when( metaInjectMeta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.FILENAME );
      JSONObject response = metaInjectHelper.stepAction( META_INJECT_REFERENCE_PATH, transMeta, null );

      assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
      assertNotNull( response );
      assertNotNull( response.get( META_INJECT_REFERENCE_PATH ) );
      assertEquals( "/path/transFile.ktr", response.get( META_INJECT_REFERENCE_PATH ) );
      assertEquals( true, response.get( IS_VALID_REFERENCE ) );
    }
  }

  @Test
  public void testReferencePath_throwsException() {
    try ( MockedStatic<MetaInjectMeta> mappingMetaMockedStatic = mockStatic( MetaInjectMeta.class ) ) {
      mappingMetaMockedStatic.when( () -> MetaInjectMeta.loadTransformationMeta( any(), any(), any(), any(), any() )  )
          .thenThrow( new KettleException( "invalid Trans" ) );
      when( metaInjectMeta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
      JSONObject response = metaInjectHelper.stepAction( META_INJECT_REFERENCE_PATH, transMeta, null );

      assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
      assertNotNull( response );
      assertNotNull( response.get( IS_VALID_REFERENCE ) );
      assertEquals( false, response.get( IS_VALID_REFERENCE ) );
    }
  }

  @Test
  public void testHandleStepAction_whenMethodNameIsInvalid() {
    JSONObject response = metaInjectHelper.stepAction( "invalidMethod", transMeta, null );

    assertNotNull( response );
    assertEquals( FAILURE_METHOD_NOT_FOUND_RESPONSE, response.get( ACTION_STATUS ) );
  }
}
