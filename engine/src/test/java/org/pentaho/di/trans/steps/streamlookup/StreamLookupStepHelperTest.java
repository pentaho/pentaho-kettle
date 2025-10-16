
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


package org.pentaho.di.trans.steps.streamlookup;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StreamLookupStepHelperTest {

  private StreamLookupMeta streamLookupMeta;
  private TransMeta transMeta;
  private StepIOMetaInterface stepIOMetaInterface;
  private StreamInterface stream;
  private StepMeta stepMeta;
  private RowMetaInterface rowMeta;
  private ValueMetaInterface valueMeta;
  private StreamLookupStepHelper helper;

  @Before
  public void setUp() {
    streamLookupMeta = mock( StreamLookupMeta.class );
    transMeta = mock( TransMeta.class );
    stepIOMetaInterface = mock( StepIOMetaInterface.class );
    stream = mock( StreamInterface.class );
    stepMeta = mock( StepMeta.class );
    rowMeta = mock( RowMetaInterface.class );
    valueMeta = mock( ValueMetaInterface.class );

    LogChannelInterfaceFactory logChannelInterfaceFactory = mock( LogChannelInterfaceFactory.class );
    LogChannelInterface logChannelInterface = mock( LogChannelInterface.class );
    KettleLogStore.setLogChannelInterfaceFactory( logChannelInterfaceFactory );
    when( logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      logChannelInterface );
    when( logChannelInterfaceFactory.create( any() ) ).thenReturn( logChannelInterface );

    helper = new StreamLookupStepHelper( streamLookupMeta );
  }

  @Test
  public void lookupFields_returnsFields_whenStepNameAndFieldsPresent() throws KettleException {
    when( streamLookupMeta.getStepIOMeta() ).thenReturn( stepIOMetaInterface );
    when( stepIOMetaInterface.getInfoStreams() ).thenReturn( Collections.singletonList( stream ) );
    when( stream.getStepname() ).thenReturn( "stepName" );
    when( transMeta.findStep( "stepName" ) ).thenReturn( stepMeta );
    when( transMeta.getStepFields( stepMeta ) ).thenReturn( rowMeta );
    when( rowMeta.getValueMetaList() ).thenReturn( Collections.singletonList( valueMeta ) );
    when( valueMeta.getName() ).thenReturn( "field1" );
    when( valueMeta.getTypeDesc() ).thenReturn( "String" );

    JSONObject result = helper.handleStepAction( "lookupFields", transMeta, new HashMap<>() );

    assertNotNull( result );
    assertTrue( result.containsKey( "lookupFields" ) );
    JSONArray fields = (JSONArray) result.get( "lookupFields" );
    assertEquals( 1, fields.size() );
    JSONObject field = (JSONObject) fields.get( 0 );
    assertEquals( "field1", field.get( "name" ) );
    assertEquals( "field1", field.get( "id" ) );
    assertEquals( "String", field.get( "type" ) );
  }

  @Test
  public void lookupFields_returnsError_whenStepNameIsEmpty() {
    when( streamLookupMeta.getStepIOMeta() ).thenReturn( stepIOMetaInterface );
    when( stepIOMetaInterface.getInfoStreams() ).thenReturn( Collections.singletonList( stream ) );
    when( stream.getStepname() ).thenReturn( "" );

    JSONObject result = helper.handleStepAction( "lookupFields", transMeta, new HashMap<>() );

    assertNotNull( result );
    assertEquals( StepInterface.FAILURE_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    assertTrue( result.containsKey( "errorMessage" ) );
  }

  @Test
  public void lookupFields_returnsError_whenExceptionThrown() throws KettleException {
    when( streamLookupMeta.getStepIOMeta() ).thenReturn( stepIOMetaInterface );
    when( stepIOMetaInterface.getInfoStreams() ).thenReturn( Collections.singletonList( stream ) );
    when( stream.getStepname() ).thenReturn( "stepName" );
    when( transMeta.findStep( "stepName" ) ).thenReturn( stepMeta );
    when( transMeta.getStepFields( stepMeta ) ).thenThrow( new KettleStepException( "error" ) );

    JSONObject result = helper.handleStepAction( "lookupFields", transMeta, new HashMap<>() );

    assertNotNull( result );
    assertEquals( StepInterface.FAILURE_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
  }

  @Test
  public void getFields_returnsFields_fromPrevStep() throws KettleException {
    StepMeta parentStepMeta = mock( StepMeta.class );
    when( streamLookupMeta.getParentStepMeta() ).thenReturn( parentStepMeta );
    when( parentStepMeta.getName() ).thenReturn( "parentStep" );
    when( transMeta.getPrevStepFields( "parentStep" ) ).thenReturn( rowMeta );
    when( rowMeta.isEmpty() ).thenReturn( false );
    when( rowMeta.getValueMetaList() ).thenReturn( Collections.singletonList( valueMeta ) );
    when( valueMeta.getName() ).thenReturn( "prevField" );

    JSONObject result = helper.handleStepAction( "getFields", transMeta, new HashMap<>() );

    assertNotNull( result );
    assertTrue( result.containsKey( "fields" ) );
    JSONArray fields = (JSONArray) result.get( "fields" );
    assertEquals( 1, fields.size() );
    JSONObject field = (JSONObject) fields.get( 0 );
    assertEquals( "prevField", field.get( "name" ) );
  }

  @Test
  public void getFields_returnsFields_fromStream_whenPrevStepIsEmpty() throws KettleException {
    StepMeta parentStepMeta = mock( StepMeta.class );
    when( streamLookupMeta.getParentStepMeta() ).thenReturn( parentStepMeta );
    when( parentStepMeta.getName() ).thenReturn( "parentStep" );
    when( transMeta.getPrevStepFields( "parentStep" ) ).thenReturn( null );

    when( streamLookupMeta.getStepIOMeta() ).thenReturn( stepIOMetaInterface );
    when( stepIOMetaInterface.getInfoStreams() ).thenReturn( Collections.singletonList( stream ) );
    when( stream.getStepname() ).thenReturn( "stepName" );
    when( transMeta.getStepFields( "stepName" ) ).thenReturn( rowMeta );
    when( rowMeta.isEmpty() ).thenReturn( false );
    when( rowMeta.getValueMetaList() ).thenReturn( Collections.singletonList( valueMeta ) );
    when( valueMeta.getName() ).thenReturn( "streamField" );

    JSONObject result = helper.handleStepAction( "getFields", transMeta, new HashMap<>() );

    assertNotNull( result );
    assertTrue( result.containsKey( "fields" ) );
    JSONArray fields = (JSONArray) result.get( "fields" );
    assertEquals( 1, fields.size() );
    JSONObject field = (JSONObject) fields.get( 0 );
    assertEquals( "streamField", field.get( "name" ) );
  }

  @Test
  public void getFields_returnsError_whenStreamStepNameIsEmpty() throws KettleStepException {
    StepMeta parentStepMeta = mock( StepMeta.class );
    when( streamLookupMeta.getParentStepMeta() ).thenReturn( parentStepMeta );
    when( parentStepMeta.getName() ).thenReturn( "parentStep" );
    when( transMeta.getPrevStepFields( "parentStep" ) ).thenReturn( null );

    when( streamLookupMeta.getStepIOMeta() ).thenReturn( stepIOMetaInterface );
    when( stepIOMetaInterface.getInfoStreams() ).thenReturn( Collections.singletonList( stream ) );
    when( stream.getStepname() ).thenReturn( "" );

    JSONObject result = helper.handleStepAction( "getFields", transMeta, new HashMap<>() );

    assertNotNull( result );
    assertEquals( StepInterface.FAILURE_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    assertTrue( result.containsKey( "errorMessage" ) );
  }

  @Test
  public void getFields_returnsError_whenStreamFieldsAreNullOrEmpty() throws KettleStepException {
    StepMeta parentStepMeta = mock( StepMeta.class );
    when( streamLookupMeta.getParentStepMeta() ).thenReturn( parentStepMeta );
    when( parentStepMeta.getName() ).thenReturn( "parentStep" );
    when( transMeta.getPrevStepFields( "parentStep" ) ).thenReturn( null );

    when( streamLookupMeta.getStepIOMeta() ).thenReturn( stepIOMetaInterface );
    when( stepIOMetaInterface.getInfoStreams() ).thenReturn( Collections.singletonList( stream ) );
    when( stream.getStepname() ).thenReturn( "stepName" );
    when( transMeta.getStepFields( "stepName" ) ).thenReturn( null );

    JSONObject result = helper.handleStepAction( "getFields", transMeta, new HashMap<>() );

    assertNotNull( result );
    assertEquals( StepInterface.FAILURE_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    assertTrue( result.containsKey( "errorMessage" ) );
  }

  @Test
  public void getFields_returnsError_whenExceptionThrown() throws KettleException {
    StepMeta parentStepMeta = mock( StepMeta.class );
    when( streamLookupMeta.getParentStepMeta() ).thenReturn( parentStepMeta );
    when( parentStepMeta.getName() ).thenReturn( "parentStep" );
    when( transMeta.getPrevStepFields( "parentStep" ) ).thenReturn( null );

    when( streamLookupMeta.getStepIOMeta() ).thenReturn( stepIOMetaInterface );
    when( stepIOMetaInterface.getInfoStreams() ).thenReturn( Collections.singletonList( stream ) );
    when( stream.getStepname() ).thenReturn( "stepName" );
    when( transMeta.getStepFields( "stepName" ) ).thenThrow( new KettleStepException( "error" ) );

    JSONObject result = helper.handleStepAction( "getFields", transMeta, new HashMap<>() );

    assertNotNull( result );
    assertEquals( StepInterface.FAILURE_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
    assertTrue( result.containsKey( "errorMessage" ) );
  }

  @Test
  public void handleStepAction_returnsError_whenUnknownMethod() {
    JSONObject result = helper.handleStepAction( "unknownMethod", transMeta, new HashMap<>() );
    assertNotNull( result );
    assertEquals( StreamLookupStepHelper.FAILURE_METHOD_NOT_FOUND_RESPONSE, result.get( StepInterface.ACTION_STATUS ) );
  }
}
