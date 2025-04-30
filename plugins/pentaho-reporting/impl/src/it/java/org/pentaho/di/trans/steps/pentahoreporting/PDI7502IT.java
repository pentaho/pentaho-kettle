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


package org.pentaho.di.trans.steps.pentahoreporting;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransTestFactory;
import org.pentaho.di.trans.steps.pentahoreporting.PentahoReportingOutputMeta.ProcessorType;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PDI7502IT {

  private static final String INPUT_FIELD_NAME = "PRPT_Report_File";
  private static final String OUTPUT_FIELD_NAME = "Generated Report File";
  private static final String REPORTING_STEP_NAME = "PRPT Output Step";
  private static File reportFile;
  private File outputFile;
  private List<RowMetaAndData> inputRows;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    ClassicEngineBoot.getInstance().start();
    KettleEnvironment.init( false );
    reportFile = new File( "src/it/resources/org/pentaho/di/trans/steps/pentahoreporting/pdi-7502.prpt" );
    assertTrue( reportFile.exists() );
  }

  @Before
  public void setUp() throws Exception {
    outputFile = File.createTempFile( this.getClass().getName(), ".out" );
    outputFile.deleteOnExit();
    outputFile.delete();
    inputRows = generateInputData( reportFile, outputFile );
  }

  @Test
  public void testFailPDF() {
    TransMeta tm = generateSampleTrans( ProcessorType.PDF );
    try {
      TransTestFactory.executeTestTransformation( tm, REPORTING_STEP_NAME, inputRows );
      fail();
    } catch ( KettleException e ) {
      // Success, the transformation failed to render the report
    }
    assertFalse( outputFile.exists() );
  }

  @Test
  public void testFailCSV() {
    TransMeta tm = generateSampleTrans( ProcessorType.CSV );
    try {
      TransTestFactory.executeTestTransformation( tm, REPORTING_STEP_NAME, inputRows );
      fail();
    } catch ( KettleException e ) {
      // Success, the transformation failed to render the report
    }
    assertFalse( outputFile.exists() );
  }

  @Test
  public void testFailExcel() {
    TransMeta tm = generateSampleTrans( ProcessorType.Excel );
    try {
      TransTestFactory.executeTestTransformation( tm, REPORTING_STEP_NAME, inputRows );
      fail();
    } catch ( KettleException e ) {
      // Success, the transformation failed to render the report
    }
    assertFalse( outputFile.exists() );
  }

  @Test
  public void testFailExcel2007() {
    TransMeta tm = generateSampleTrans( ProcessorType.Excel_2007 );
    try {
      TransTestFactory.executeTestTransformation( tm, REPORTING_STEP_NAME, inputRows );
      fail();
    } catch ( KettleException e ) {
      // Success, the transformation failed to render the report
    }
    assertFalse( outputFile.exists() );
  }

  @Test
  public void testFailStreamingHtml() {
    TransMeta tm = generateSampleTrans( ProcessorType.StreamingHTML );
    try {
      TransTestFactory.executeTestTransformation( tm, REPORTING_STEP_NAME, inputRows );
      fail();
    } catch ( KettleException e ) {
      // Success, the transformation failed to render the report
    }
    assertFalse( outputFile.exists() );
  }

  @Test
  public void testFailPagedHtml() {
    TransMeta tm = generateSampleTrans( ProcessorType.PagedHTML );
    try {
      TransTestFactory.executeTestTransformation( tm, REPORTING_STEP_NAME, inputRows );
      fail();
    } catch ( KettleException e ) {
      // Success, the transformation failed to render the report
    }
    assertFalse( outputFile.exists() );
  }

  @Test
  public void testFailRTF() {
    TransMeta tm = generateSampleTrans( ProcessorType.RTF );
    try {
      TransTestFactory.executeTestTransformation( tm, REPORTING_STEP_NAME, inputRows );
      fail();
    } catch ( KettleException e ) {
      // Success, the transformation failed to render the report
    }
    assertFalse( outputFile.exists() );
  }

  private TransMeta generateSampleTrans( ProcessorType type ) {
    PentahoReportingOutputMeta proMeta = new PentahoReportingOutputMeta();
    proMeta.setInputFileField( INPUT_FIELD_NAME );
    proMeta.setOutputFileField( OUTPUT_FIELD_NAME );
    proMeta.setOutputProcessorType( type );
    proMeta.setCreateParentfolder( false );
    proMeta.setUseValuesFromFields( true );
    return TransTestFactory.generateTestTransformation( new Variables(), proMeta, REPORTING_STEP_NAME );
  }

  private List<RowMetaAndData> generateInputData( File reportFile, File outputFile ) {
    RowMetaInterface rm = new RowMeta();
    rm.addValueMeta( new ValueMetaString( INPUT_FIELD_NAME ) );
    rm.addValueMeta( new ValueMetaString( OUTPUT_FIELD_NAME ) );
    RowMetaAndData rmd = new RowMetaAndData( rm, reportFile.getPath(), outputFile.getPath() );
    List<RowMetaAndData> result = new ArrayList<>();
    result.add( rmd );
    return result;
  }

}
