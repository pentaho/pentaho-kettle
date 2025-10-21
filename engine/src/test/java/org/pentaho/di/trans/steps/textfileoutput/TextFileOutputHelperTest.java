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

package org.pentaho.di.trans.steps.textfileoutput;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TextFileOutputHelperTest {

  private TextFileOutputHelper helper;
  private TransMeta transMeta;
  private StepMeta stepMeta;
  private TextFileOutputMeta textFileOutputMeta;

  @Before
  public void setUp() {
    helper = new TextFileOutputHelper();
    transMeta = mock( TransMeta.class );
    stepMeta = mock( StepMeta.class );
    textFileOutputMeta = mock( TextFileOutputMeta.class );
  }

  @Test
  public void testHandleStepAction_setMinimalWidth() {
    String stepName = "step1";
    Map<String, String> params = new HashMap<>();
    params.put( "stepName", stepName );
    when( transMeta.findStep( stepName ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( textFileOutputMeta );
    TextFileField field = new TextFileField();
    field.setName( "field1" );
    field.setType( ValueMetaInterface.TYPE_STRING );
    field.setCurrencySymbol( "$" );
    field.setDecimalSymbol( "." );
    field.setGroupingSymbol( "," );
    field.setNullString( "NULL" );
    when( textFileOutputMeta.getOutputFields() ).thenReturn( new TextFileField[] { field } );
    JSONObject result = helper.handleStepAction( "setMinimalWidth", transMeta, params );
    assertTrue( result.containsKey( "updatedData" ) );
    JSONArray arr = (JSONArray) result.get( "updatedData" );
    assertEquals( 1, arr.size() );
  }

  @Test
  public void testHandleStepAction_showFiles() {
    String stepName = "step2";
    Map<String, String> params = new HashMap<>();
    params.put( "stepName", stepName );
    params.put( "filter", "test" );
    params.put( "isRegex", "false" );
    when( transMeta.findStep( stepName ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( textFileOutputMeta );
    when( textFileOutputMeta.getFiles( transMeta ) ).thenReturn( new String[] { "testfile.txt", "other.txt" } );
    JSONObject result = helper.handleStepAction( "showFiles", transMeta, params );
    assertTrue( result.containsKey( "files" ) );
    JSONArray files = (JSONArray) result.get( "files" );
    assertEquals( 1, files.size() );
    assertEquals( "testfile.txt", files.get( 0 ) );
  }

  @Test
  public void testHandleStepAction_getFormats() {
    JSONObject result = helper.handleStepAction( "getFormats", transMeta, Collections.emptyMap() );
    assertTrue( result.containsKey( "formats" ) );
    JSONArray formats = (JSONArray) result.get( "formats" );
    assertFalse( formats.isEmpty() );
  }

  @Test
  public void testSetMinimalWidthAction_nullStepName() throws Exception {
    Map<String, String> params = new HashMap<>();
    params.put( "stepName", null );
    JSONObject result = helper.setMinimalWidthAction( transMeta, params );
    JSONArray arr = (JSONArray) result.get( "updatedData" );
    assertTrue( arr.isEmpty() );
  }

  @Test
  public void testShowFilesAction_noStepName() {
    Map<String, String> params = new HashMap<>();
    params.put( "stepName", "" );
    JSONObject result = helper.showFilesAction( transMeta, params );
    JSONArray arr = (JSONArray) result.get( "files" );
    assertTrue( arr.isEmpty() );
  }

  @Test
  public void testShowFilesAction_invalidStepMeta() {
    Map<String, String> params = new HashMap<>();
    params.put( "stepName", "step3" );
    when( transMeta.findStep( "step3" ) ).thenReturn( null );
    JSONObject result = helper.showFilesAction( transMeta, params );
    JSONArray arr = (JSONArray) result.get( "files" );
    assertTrue( arr.isEmpty() );
  }

  @Test
  public void testShowFilesAction_regexFilter() {
    String stepName = "step4";
    Map<String, String> params = new HashMap<>();
    params.put( "stepName", stepName );
    params.put( "filter", ".*file\\.txt" );
    params.put( "isRegex", "true" );
    when( transMeta.findStep( stepName ) ).thenReturn( stepMeta );
    when( stepMeta.getStepMetaInterface() ).thenReturn( textFileOutputMeta );
    when( textFileOutputMeta.getFiles( transMeta ) ).thenReturn( new String[] { "testfile.txt", "other.txt" } );
    JSONObject result = helper.showFilesAction( transMeta, params );
    JSONArray arr = (JSONArray) result.get( "files" );
    assertEquals( 1, arr.size() );
    assertEquals( "testfile.txt", arr.get( 0 ) );
  }

  @Test
  public void testFileMatchesFilter_nonRegex() {
    // Use reflection to test private method
    try {
      java.lang.reflect.Method m =
        helper.getClass().getDeclaredMethod( "fileMatchesFilter", String.class, String.class, String.class );
      m.setAccessible( true );
      assertTrue( (Boolean) m.invoke( helper, "abc.txt", "abc", "false" ) );
      assertFalse( (Boolean) m.invoke( helper, "abc.txt", "xyz", "false" ) );
      assertTrue( (Boolean) m.invoke( helper, "abc.txt", "", "false" ) );
    } catch ( Exception e ) {
      fail( "Reflection failed: " + e.getMessage() );
    }
  }

  @Test
  public void testFileMatchesFilter_regex() {
    try {
      java.lang.reflect.Method m =
        helper.getClass().getDeclaredMethod( "fileMatchesFilter", String.class, String.class, String.class );
      m.setAccessible( true );
      assertTrue( (Boolean) m.invoke( helper, "abc.txt", ".*\\.txt", "true" ) );
      assertFalse( (Boolean) m.invoke( helper, "abc.txt", "xyz", "true" ) );
      assertTrue( (Boolean) m.invoke( helper, "abc.txt", "", "true" ) );
    } catch ( Exception e ) {
      fail( "Reflection failed: " + e.getMessage() );
    }
  }
}
