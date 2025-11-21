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


package org.pentaho.di.trans.steps.denormaliser;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class DenormaliserStepHelperTest {

  private DenormaliserMeta mockMeta;
  private TransMeta mockTransMeta;
  private DenormaliserStepHelper helper;

  @Before
  public void setUp() {
    mockMeta = mock( DenormaliserMeta.class );
    mockTransMeta = mock( TransMeta.class );

    // Mock info stream
    StepIOMetaInterface mockStepIOMeta = mock( StepIOMetaInterface.class );
    StreamInterface mockStreamInterface = mock( StreamInterface.class );
    StepMeta mockStepMeta = mock( StepMeta.class );

    when( mockMeta.getStepIOMeta() ).thenReturn( mockStepIOMeta );
    when( mockStepIOMeta.getInfoStreams() ).thenReturn( Collections.singletonList( mockStreamInterface ) );
    when( mockStreamInterface.getStepname() ).thenReturn( "MockStep" );
    when( mockMeta.getParentStepMeta() ).thenReturn( mockStepMeta );
    when( mockStepMeta.getName() ).thenReturn( "MockStep" );

    helper = new DenormaliserStepHelper( mockMeta );
  }

  @Test
  public void testGetAggregationTypes() {
    JSONObject result = helper.handleStepAction( "getAggregationTypes", mockTransMeta, new HashMap<>() );
    assertNotNull( result );
    assertTrue( result.containsKey( "aggregationTypes" ) );
    JSONArray aggregationTypes = (JSONArray) result.get( "aggregationTypes" );
    assertEquals( DenormaliserTargetField.typeAggrDesc.length, aggregationTypes.size() );
    for ( int i = 0; i < aggregationTypes.size(); i++ ) {
      JSONObject aggregationType = (JSONObject) aggregationTypes.get( i );
      assertEquals( DenormaliserTargetField.typeAggrDesc[ i ], aggregationType.get( "id" ) );
      assertEquals( DenormaliserTargetField.typeAggrLongDesc[ i ], aggregationType.get( "name" ) );
    }
  }

  @Test
  public void testGetLookupFields_Success() throws KettleStepException {
    when( mockMeta.getGroupField() ).thenReturn( new String[] { "groupField1" } );
    when( mockMeta.getKeyField() ).thenReturn( "keyField" );

    RowMetaInterface rowMeta = new RowMeta();
    ValueMetaString includedField = new ValueMetaString( "includedField" );
    includedField.setLength( 50 );
    rowMeta.addValueMeta( includedField );

    // groupField and keyField should be excluded
    ValueMetaString groupField = new ValueMetaString( "groupField1" );
    rowMeta.addValueMeta( groupField );
    ValueMetaString keyField = new ValueMetaString( "keyField" );
    rowMeta.addValueMeta( keyField );

    when( mockTransMeta.getPrevStepFields( "MockStep" ) ).thenReturn( rowMeta );

    JSONObject result = helper.handleStepAction( "getLookupFields", mockTransMeta, new HashMap<>() );
    assertNotNull( result );
    assertTrue( result.containsKey( "denormaliserFields" ) );
    JSONArray fields = (JSONArray) result.get( "denormaliserFields" );
    assertEquals( 1, fields.size() );
    JSONObject field = (JSONObject) fields.get( 0 );
    assertEquals( "includedField", field.get( "fieldName" ) );
    assertEquals( "includedField", field.get( "targetName" ) );
    assertEquals( 50, field.get( "targetLength" ) );
    assertEquals( DenormaliserTargetField.getAggregationTypeDesc( DenormaliserTargetField.TYPE_AGGR_NONE ),
      field.get( "aggregationType" ) );
  }

  @Test
  public void testGetLookupFields_NoInfoStream() {
    when( mockMeta.getParentStepMeta() ).thenReturn( null );

    JSONObject result = helper.handleStepAction( "getLookupFields", mockTransMeta, new HashMap<>() );
    assertNotNull( result );
    assertEquals( "No info stream step found.", result.get( "message" ) );
    assertEquals( DenormaliserStepHelper.FAILURE_RESPONSE, result.get( DenormaliserStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testGetLookupFields_NoPreviousFields() throws KettleStepException {
    when( mockMeta.getGroupField() ).thenReturn( new String[ 0 ] );
    when( mockMeta.getKeyField() ).thenReturn( "" );
    when( mockTransMeta.getPrevStepFields( "MockStep" ) ).thenReturn( null );

    JSONObject result = helper.handleStepAction( "getLookupFields", mockTransMeta, new HashMap<>() );
    assertNotNull( result );
    assertTrue( result.get( "message" ).toString().contains( "No previous fields found" ) );
    assertEquals( DenormaliserStepHelper.FAILURE_RESPONSE, result.get( DenormaliserStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testGetLookupFields_EmptyPreviousFields() throws KettleStepException {
    when( mockMeta.getGroupField() ).thenReturn( new String[ 0 ] );
    when( mockMeta.getKeyField() ).thenReturn( "" );
    RowMetaInterface emptyRowMeta = new RowMeta();
    when( mockTransMeta.getPrevStepFields( "MockStep" ) ).thenReturn( emptyRowMeta );

    JSONObject result = helper.handleStepAction( "getLookupFields", mockTransMeta, new HashMap<>() );
    assertNotNull( result );
    assertTrue( result.get( "message" ).toString().contains( "No previous fields found" ) );
    assertEquals( DenormaliserStepHelper.FAILURE_RESPONSE, result.get( DenormaliserStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testHandleStepAction_UnknownMethod() {
    JSONObject result = helper.handleStepAction( "unknownMethod", mockTransMeta, new HashMap<>() );
    assertNotNull( result );
    assertEquals( DenormaliserStepHelper.FAILURE_METHOD_NOT_FOUND_RESPONSE,
      result.get( DenormaliserStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testGetLookupFields_ExceptionHandling() throws KettleStepException {
    when( mockMeta.getGroupField() ).thenReturn( new String[ 0 ] );
    when( mockMeta.getKeyField() ).thenReturn( "" );
    when( mockTransMeta.getPrevStepFields( "MockStep" ) ).thenThrow( new RuntimeException( "Test exception" ) );

    JSONObject result = helper.handleStepAction( "getLookupFields", mockTransMeta, new HashMap<>() );
    assertNotNull( result );
    assertTrue( result.get( "message" ).toString().contains( "Failed to retrieve lookup fields" ) );
    assertEquals( DenormaliserStepHelper.FAILURE_RESPONSE, result.get( DenormaliserStepHelper.ACTION_STATUS ) );
  }
}
