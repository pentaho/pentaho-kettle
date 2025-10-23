/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024-2025 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.trans.steps.excelwriter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ExcelWriterStepHelperTest {

  @Test
  public void testGetFormatsReturnsNonReservedSortedFormats() {
    ExcelWriterStepHelper helper = new ExcelWriterStepHelper();
    String[] formats = helper.getFormats();
    assertNotNull( formats );
    for ( String format : formats ) {
      assertFalse( format.startsWith( "reserved" ) );
    }
    // Should be sorted
    if ( formats.length > 1 ) {
      assertTrue( formats[ 0 ].compareTo( formats[ formats.length - 1 ] ) <= 0 );
    }
  }

  @Test
  public void testFormatTypeReturnsExpectedFormat() {
    ExcelWriterStepHelper helper = new ExcelWriterStepHelper();
    assertEquals( "", helper.formatType( ValueMetaInterface.TYPE_STRING ) );
    assertEquals( "0", helper.formatType( ValueMetaInterface.TYPE_INTEGER ) );
    assertEquals( "0.#####", helper.formatType( ValueMetaInterface.TYPE_NUMBER ) );
    assertNull( helper.formatType( -1 ) );
  }

  @Test
  public void testGetFormatsActionReturnsFormatsArray() {
    ExcelWriterStepHelper helper = new ExcelWriterStepHelper();
    JSONObject result = helper.getFormatsAction();
    assertNotNull( result );
    assertTrue( result.containsKey( "formats" ) );
    JSONArray formats = (JSONArray) result.get( "formats" );
    assertNotNull( formats );
    assertFalse( formats.isEmpty() );
  }

  @Test
  public void testGetFilesActionReturnsNotImplementedMessage() {
    ExcelWriterStepHelper helper = new ExcelWriterStepHelper();
    JSONObject result = helper.getFilesAction();
    assertEquals( "getFilesAction not implemented", result.get( "message" ) );
    assertEquals( "success", result.get( "action_status" ) );
  }

  @Test
  public void testSetMinimalWidthActionReturnsUpdatedData() throws Exception {
    ExcelWriterStepHelper helper = new ExcelWriterStepHelper();
    ExcelWriterStepMeta meta = Mockito.mock( ExcelWriterStepMeta.class );
    ExcelWriterStepField field = Mockito.mock( ExcelWriterStepField.class );

    Mockito.when( field.getType() ).thenReturn( ValueMetaInterface.TYPE_INTEGER );
    Mockito.when( field.getTypeDesc() ).thenReturn( "Integer" );
    Mockito.when( field.getCommentField() ).thenReturn( "comment" );
    Mockito.when( field.getCommentAuthorField() ).thenReturn( "author" );
    Mockito.when( field.getStyleCell() ).thenReturn( "style" );
    Mockito.when( field.getTitleStyleCell() ).thenReturn( "titleStyle" );
    Mockito.when( field.isFormula() ).thenReturn( true );
    Mockito.when( field.getName() ).thenReturn( "fieldName" );
    Mockito.when( field.getTitle() ).thenReturn( "title" );
    Mockito.when( field.getHyperlinkField() ).thenReturn( "hyperlink" );

    // Correct: Mock getOutputFields to return an array
    Mockito.when( meta.getOutputFields() ).thenReturn( new ExcelWriterStepField[] { field } );

    JSONObject result = helper.setMinimalWidthAction( meta );
    assertNotNull( result );
    assertTrue( result.containsKey( "updatedData" ) );
    JSONArray updatedData = (JSONArray) result.get( "updatedData" );
    assertEquals( 1, updatedData.size() );
  }

  @Test
  public void testHandleStepActionGetFormats() {
    ExcelWriterStepHelper helper = new ExcelWriterStepHelper();
    JSONObject result = helper.handleStepAction( "getFormats", null, Collections.emptyMap() );
    assertNotNull( result );
    assertTrue( result.containsKey( "formats" ) );
  }

  @Test
  public void testHandleStepActionGetFiles() {
    ExcelWriterStepHelper helper = new ExcelWriterStepHelper();
    JSONObject result = helper.handleStepAction( "getFiles", Mockito.mock( TransMeta.class ), Collections.emptyMap() );
    assertNotNull( result );
    assertEquals( "getFilesAction not implemented", result.get( "message" ) );
  }

  @Test
  public void testHandleStepActionSetMinimalWidth() {
    ExcelWriterStepHelper helper = new ExcelWriterStepHelper();
    TransMeta transMeta = Mockito.mock( TransMeta.class );
    StepMeta stepMeta = Mockito.mock( StepMeta.class );
    ExcelWriterStepMeta meta = Mockito.mock( ExcelWriterStepMeta.class );
    Mockito.when( transMeta.getStep( 0 ) ).thenReturn( stepMeta );
    Mockito.when( stepMeta.getStepMetaInterface() ).thenReturn( meta );
    Mockito.when( meta.getOutputFields() )
      .thenReturn( new ExcelWriterStepField[] { Mockito.mock( ExcelWriterStepField.class ) } );
    JSONObject result = helper.handleStepAction( "setMinimalWidth", transMeta, Collections.emptyMap() );
    assertNotNull( result );
    assertTrue( result.containsKey( "updatedData" ) );
  }
}
