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

package org.pentaho.di.trans.steps.mergerows;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pentaho.di.trans.step.StepHelperInterface.ACTION_STATUS;
import static org.pentaho.di.trans.step.StepHelperInterface.FAILURE_METHOD_NOT_FOUND_RESPONSE;
import static org.pentaho.di.trans.step.StepHelperInterface.FAILURE_RESPONSE;

public class MergeRowsHelperTest {

  private MergeRowsHelper helper;
  private TransMeta transMeta;
  private StepMeta stepMeta;

  @Before
  public void setUp() {
    MergeRowsMeta mergeRowsMeta = mock( MergeRowsMeta.class );
    helper = new MergeRowsHelper( mergeRowsMeta );
    transMeta = mock( TransMeta.class );
    stepMeta = mock( StepMeta.class );

    StreamInterface stream = mock( StreamInterface.class );
    LogChannelInterfaceFactory logChannelInterfaceFactory = mock( LogChannelInterfaceFactory.class );
    LogChannelInterface logChannelInterface = mock( LogChannelInterface.class );
    KettleLogStore.setLogChannelInterfaceFactory( logChannelInterfaceFactory );
    when( logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      logChannelInterface );
    when( logChannelInterfaceFactory.create( any() ) ).thenReturn( logChannelInterface );

    when( stream.getStepname() ).thenReturn( "inputStep" );
    when( mergeRowsMeta.getStepIOMeta() ).thenReturn( mock( org.pentaho.di.trans.step.StepIOMetaInterface.class ) );
    when( mergeRowsMeta.getStepIOMeta().getInfoStreams() ).thenReturn( Collections.singletonList( stream ) );
    when( transMeta.findStep( "inputStep" ) ).thenReturn( stepMeta );
  }

  @Test
  public void testGetReferenceStepsFields_whenValidStepMeta_thenReturnsFields() throws Exception {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "field1" ) );
    rowMeta.addValueMeta( new ValueMetaString( "field2" ) );
    when( transMeta.getStepFields( stepMeta ) ).thenReturn( rowMeta );

    JSONObject result = helper.getReferenceStepsFields( transMeta );
    assertNotNull( result );
    assertTrue( result.containsKey( "stepFieldsNames" ) );
    JSONArray fields = (JSONArray) result.get( "stepFieldsNames" );
    assertEquals( 2, fields.size() );
    assertEquals( "field1", fields.get( 0 ) );
    assertEquals( "field2", fields.get( 1 ) );
  }

  @Test
  public void testHandleStepAction_whenReferenceStepFields_thenReturnsStepFieldsNames() throws Exception {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "field1" ) );
    when( transMeta.getStepFields( stepMeta ) ).thenReturn( rowMeta );

    JSONObject result = helper.stepAction( "referenceStepFields", transMeta, null );
    assertNotNull( result );
    assertTrue( result.containsKey( "stepFieldsNames" ) );
  }

  @Test
  public void testHandleStepAction_whenExceptionThrown_thenReturnsFailureResponse() throws Exception {
    when( transMeta.getStepFields( any( StepMeta.class ) ) ).thenThrow( new KettleStepException( "Error" ) );
    JSONObject result = helper.stepAction( "referenceStepFields", transMeta, null );
    assertNotNull( result );
    assertEquals( FAILURE_RESPONSE, result.get( ACTION_STATUS ) );
  }

  @Test
  public void testHandleStepAction_whenInvalidAction_thenReturnsFailureMethodNotFound() {
    JSONObject result = helper.stepAction( "invalidAction", transMeta, null );
    assertNotNull( result );
    assertEquals( FAILURE_METHOD_NOT_FOUND_RESPONSE, result.get( ACTION_STATUS ) );
  }
}
