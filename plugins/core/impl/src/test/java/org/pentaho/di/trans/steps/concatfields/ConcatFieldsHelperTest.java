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

package org.pentaho.di.trans.steps.concatfields;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepHelper;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.textfileoutput.TextFileField;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConcatFieldsHelperTest {

  @Mock
  private TransMeta transMeta;
  @Mock
  private StepMeta stepMeta;
  @Mock
  private ConcatFieldsMeta concatFieldsMeta;
  @Mock
  private TextFileField textFileField1;
  @Mock
  private StepMetaInterface nonConcatFieldsMeta;

  private ConcatFieldsHelper concatFieldsHelper;
  private Map<String, String> queryParams;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks( this );
    concatFieldsHelper = new ConcatFieldsHelper();
    queryParams = new HashMap<>();
  }

  @Test
  public void testHandleStepActionAllCases() {
    setupValidField( textFileField1, "field1", ValueMetaInterface.TYPE_STRING );
    queryParams.put( "stepName", "testStep" );
    when( transMeta.findStep( "testStep" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( concatFieldsMeta );
    when( concatFieldsMeta.getOutputFields() ).thenReturn( new TextFileField[] { textFileField1 } );
    JSONObject result1 = concatFieldsHelper.handleStepAction( "setMinimalWidth", transMeta, queryParams );
    assertFieldsResult( result1, 1 );

    JSONObject result2 = concatFieldsHelper.handleStepAction( "unknownMethod", transMeta, queryParams );
    assertEquals( BaseStepHelper.FAILURE_METHOD_NOT_FOUND_RESPONSE, result2.get( BaseStepHelper.ACTION_STATUS ) );

    when( transMeta.findStep( anyString() ) ).thenThrow( new RuntimeException( "Test exception" ) );
    JSONObject result3 = concatFieldsHelper.handleStepAction( "setMinimalWidth", transMeta, queryParams );
    assertEquals( BaseStepHelper.FAILURE_RESPONSE, result3.get( BaseStepHelper.ACTION_STATUS ) );
  }

  @Test
  public void testSetMinimalWidthActionAllScenarios() throws JsonProcessingException {
    // Test edge cases that return 0 fields
    assertFieldsResult( concatFieldsHelper.setMinimalWidthAction( transMeta, createParams( null ) ), 0 );
    assertFieldsResult( concatFieldsHelper.setMinimalWidthAction( transMeta, createParams( "" ) ), 0 );
    assertFieldsResult( concatFieldsHelper.setMinimalWidthAction( transMeta, new HashMap<>() ), 0 );

    when( transMeta.findStep( "nonExistent" ) ).thenReturn( null );
    assertFieldsResult( concatFieldsHelper.setMinimalWidthAction( transMeta, createParams( "nonExistent" ) ), 0 );

    when( transMeta.findStep( "nonConcat" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( nonConcatFieldsMeta );
    assertFieldsResult( concatFieldsHelper.setMinimalWidthAction( transMeta, createParams( "nonConcat" ) ), 0 );

    when( stepMeta.getStepMetaInterface() ).thenReturn( concatFieldsMeta );
    when( concatFieldsMeta.getOutputFields() ).thenReturn( new TextFileField[ 0 ] );
    assertFieldsResult( concatFieldsHelper.setMinimalWidthAction( transMeta, createParams( "empty" ) ), 0 );

  }

  @Test
  public void testFormatTypeAllCases() {
    assertEquals( "", concatFieldsHelper.formatType( ValueMetaInterface.TYPE_STRING ) );
    assertEquals( "0", concatFieldsHelper.formatType( ValueMetaInterface.TYPE_INTEGER ) );
    assertEquals( "0.#####", concatFieldsHelper.formatType( ValueMetaInterface.TYPE_NUMBER ) );
    assertNull( concatFieldsHelper.formatType( ValueMetaInterface.TYPE_DATE ) );
    assertNull( concatFieldsHelper.formatType( ValueMetaInterface.TYPE_BOOLEAN ) );
    assertNull( concatFieldsHelper.formatType( 999 ) ); // Unknown type
  }

  @Test
  public void testAllFieldTypesFormatting() throws JsonProcessingException {
    when( transMeta.findStep( "allTypes" ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( concatFieldsMeta );

    TextFileField[] fields = {
      createField( "str", ValueMetaInterface.TYPE_STRING ),
      createField( "int", ValueMetaInterface.TYPE_INTEGER ),
      createField( "num", ValueMetaInterface.TYPE_NUMBER ),
      createField( "date", ValueMetaInterface.TYPE_DATE )
    };
    when( concatFieldsMeta.getOutputFields() ).thenReturn( fields );

    assertFieldsResult( concatFieldsHelper.setMinimalWidthAction( transMeta, createParams( "allTypes" ) ), 4 );
  }

  private void assertFieldsResult( JSONObject result, int expectedFieldCount ) {
    assertNotNull( result );
    assertTrue( result.containsKey( "updatedData" ) );
    assertEquals( expectedFieldCount, ( (JSONArray) result.get( "updatedData" ) ).size() );
  }

  private Map<String, String> createParams( String stepName ) {
    Map<String, String> params = new HashMap<>();
    if ( stepName != null ) {
      params.put( "stepName", stepName );
    }
    return params;
  }

  private void setupValidField( TextFileField field, String name, int type ) {
    when( field.getName() ).thenReturn( name );
    when( field.getType() ).thenReturn( type );
    when( field.getCurrencySymbol() ).thenReturn( "$" );
    when( field.getDecimalSymbol() ).thenReturn( "." );
    when( field.getGroupingSymbol() ).thenReturn( "," );
    when( field.getNullString() ).thenReturn( "" );
  }

  private TextFileField createField( String name, int type ) {
    TextFileField field = mock( TextFileField.class );
    setupValidField( field, name, type );
    return field;
  }
}
