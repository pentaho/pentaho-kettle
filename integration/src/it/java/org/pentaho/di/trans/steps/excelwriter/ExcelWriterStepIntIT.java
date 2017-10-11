/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.excelwriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestFactory;
import org.pentaho.di.trans.steps.excelinput.ExcelInputField;
import org.pentaho.di.trans.steps.excelinput.ExcelInputMeta;
import org.pentaho.di.trans.steps.excelinput.SpreadSheetType;

public class ExcelWriterStepIntIT {

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  private List<RowMetaAndData> getPDI11374RowMetaAndData() {
    List<RowMetaAndData> rmd = new ArrayList<RowMetaAndData>();
    RowMeta rm = new RowMeta();
    rm.addValueMeta( new ValueMetaString( "col1" ) );
    rm.addValueMeta( new ValueMetaString( "col2" ) );
    rm.addValueMeta( new ValueMetaString( "col3" ) );
    rm.addValueMeta( new ValueMetaString( "col4" ) );
    rm.addValueMeta( new ValueMetaString( "col5" ) );
    rmd.add( new RowMetaAndData( rm, new Object[] { "data1", "data2", "data3", "data4", "data5" } ) );
    return rmd;
  }

  @Test
  public void testPDI11374() throws KettleException, IOException {
    String stepName = "Excel Writer";
    ExcelWriterStepMeta meta = new ExcelWriterStepMeta();
    meta.setDefault();

    File tempOutputFile = File.createTempFile( "testPDI11374", ".xlsx" );
    tempOutputFile.deleteOnExit();
    meta.setFileName( tempOutputFile.getAbsolutePath().replace( ".xlsx", "" ) );
    meta.setExtension( "xlsx" );
    meta.setSheetname( "Sheet10" );
    meta.setOutputFields( new ExcelWriterStepField[] {} );
    meta.setHeaderEnabled( true );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, meta, stepName );
    List<RowMetaAndData> inputList = getPDI11374RowMetaAndData();
    TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, stepName,
      TransTestFactory.DUMMY_STEPNAME, inputList );

    try {
      Thread.sleep( 1000 );
    } catch ( InterruptedException ignore ) {
      // Wait a second to ensure that the output file is properly closed
    }

    // Now, check the result
    String checkStepName = "Excel Input";
    ExcelInputMeta excelInput = new ExcelInputMeta();
    excelInput.setFileName( new String[] { tempOutputFile.getAbsolutePath() } );
    excelInput.setFileMask( new String[] { "" } );
    excelInput.setExcludeFileMask( new String[] { "" } );
    excelInput.setFileRequired( new String[] { "N" } );
    excelInput.setIncludeSubFolders( new String[]{ "N" } );
    excelInput.setSpreadSheetType( SpreadSheetType.POI );
    excelInput.setSheetName( new String[] { "Sheet10" } );
    excelInput.setStartColumn( new int[] { 0 } );
    excelInput.setStartRow( new int[] { 0 } );
    excelInput.setStartsWithHeader( false ); // Ensures that we can check the header names

    ExcelInputField[] fields = new ExcelInputField[5];
    for ( int i = 0; i < 5; i++ ) {
      fields[i] = new ExcelInputField();
      fields[i].setName( "field" + ( i + 1 ) );
    }
    excelInput.setField( fields );

    transMeta = TransTestFactory.generateTestTransformation( null, excelInput, checkStepName );

    //Remove the Injector hop, as it's not needed for this transformation
    TransHopMeta injectHop = transMeta.findTransHop( transMeta.findStep( TransTestFactory.INJECTOR_STEPNAME ),
      transMeta.findStep( stepName ) );
    transMeta.removeTransHop( transMeta.indexOfTransHop( injectHop ) );

    inputList = new ArrayList<RowMetaAndData>();
    List<RowMetaAndData> result =
      TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, stepName,
        TransTestFactory.DUMMY_STEPNAME, inputList );

    assertNotNull( result );
    assertEquals( 2, result.size() );
    assertEquals( 5, result.get( 0 ).getRowMeta().size() );
    assertEquals( ValueMetaInterface.TYPE_STRING, result.get( 0 ).getValueMeta( 0 ).getType() );
    assertEquals( ValueMetaInterface.TYPE_STRING, result.get( 0 ).getValueMeta( 1 ).getType() );
    assertEquals( ValueMetaInterface.TYPE_STRING, result.get( 0 ).getValueMeta( 2 ).getType() );
    assertEquals( ValueMetaInterface.TYPE_STRING, result.get( 0 ).getValueMeta( 3 ).getType() );
    assertEquals( ValueMetaInterface.TYPE_STRING, result.get( 0 ).getValueMeta( 4 ).getType() );

    assertEquals( "col1", result.get( 0 ).getString( 0, "default-value" ) );
    assertEquals( "col2", result.get( 0 ).getString( 1, "default-value" ) );
    assertEquals( "col3", result.get( 0 ).getString( 2, "default-value" ) );
    assertEquals( "col4", result.get( 0 ).getString( 3, "default-value" ) );
    assertEquals( "col5", result.get( 0 ).getString( 4, "default-value" ) );

    assertEquals( "data1", result.get( 1 ).getString( 0, "default-value" ) );
    assertEquals( "data2", result.get( 1 ).getString( 1, "default-value" ) );
    assertEquals( "data3", result.get( 1 ).getString( 2, "default-value" ) );
    assertEquals( "data4", result.get( 1 ).getString( 3, "default-value" ) );
    assertEquals( "data5", result.get( 1 ).getString( 4, "default-value" ) );
  }

  @Test
  public void testPDI14854() throws KettleException, IOException {
    try {
      testEmptyFileInit( true ); // An empty file should be created
    } catch ( KettleException e ) {
      fail();
    }
    try {
      testEmptyFileInit( false ); // No file should be created, but the transformation should not fail
    } catch ( KettleException e ) {
      fail();
    }
  }

  public void testEmptyFileInit( boolean createEmptyFile ) throws KettleException, IOException {
    String stepName = "Excel Writer";
    ExcelWriterStepMeta meta = new ExcelWriterStepMeta();
    meta.setDefault();

    File tempOutputFile = File.createTempFile( "testPDI14854", ".xlsx" );
    tempOutputFile.delete();
    meta.setFileName( tempOutputFile.getAbsolutePath().replace( ".xlsx", "" ) );
    meta.setExtension( "xlsx" );
    meta.setSheetname( "Sheet10" );
    meta.setOutputFields( new ExcelWriterStepField[] {} );
    meta.setHeaderEnabled( true );
    meta.setDoNotOpenNewFileInit( !createEmptyFile );

    TransMeta transMeta = TransTestFactory.generateTestTransformation( null, meta, stepName );

    List<RowMetaAndData> inputList = new ArrayList<RowMetaAndData>();
    TransTestFactory.executeTestTransformation( transMeta, TransTestFactory.INJECTOR_STEPNAME, stepName,
      TransTestFactory.DUMMY_STEPNAME, inputList );

    try {
      Thread.sleep( 1000 );
    } catch ( InterruptedException ignore ) {
      // Wait a second to ensure that the output file is properly closed
    }

    assertEquals( createEmptyFile, tempOutputFile.exists() );
    if ( tempOutputFile.exists() ) {
      tempOutputFile.delete();
    }
  }
}
